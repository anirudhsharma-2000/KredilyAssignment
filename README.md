# Location Tracking App

This application tracks user location, stores it locally when internet is unavailable, and uploads the data to Firebase Firestore when connectivity is restored.

---

## Architecture Explanation

The app follows the **MVVM (Model–View–ViewModel)** architecture with an **offline-first approach**.

- **UI (Jetpack Compose)**  
  Displays location data and handles user interactions.

- **ViewModel**  
  Collects location updates, exposes UI state using `StateFlow`, and forwards data to the repository.

- **Repository**  
  Acts as a single source of truth. It saves all location data to Room and triggers background sync.

- **Room Database**  
  Stores location data locally and works as the primary data source.

- **WorkManager**  
  Handles background synchronization of pending location data to Firestore.

- **Firebase Firestore**  
  Stores synced location data remotely.

This separation ensures clean code, testability, and resilience to network changes.

---

## How Offline Storage Works

- Every location update is **always saved to Room first**, regardless of internet availability.
- Each stored record contains a `timestamp` and a `synced` flag.
- When the device is offline:
  - Location data continues to be stored locally.
  - No network calls are made.
- Data remains available even if the app is closed or the device restarts.

Room acts as the **local source of truth**, ensuring no data loss.

---

## How Sync Mechanism Works

- Whenever a new location is stored in Room, a **WorkManager task is enqueued**.
- The WorkManager task runs only when the device has internet connectivity.
- The worker:
  1. Fetches all unsynced records from Room.
  2. Orders them by `timestamp` (FIFO).
  3. Uploads each record to Firestore one by one.
  4. Marks a record as synced only after a successful upload.
- If an upload fails, WorkManager retries automatically.

This guarantees reliable, ordered, and eventual synchronization.

---

## API Contract Used for Sending Locations

### Firestore Collection


### Data Format

```json
{
  "employeeId": "EMP0001",
  "latitude": 28.6139,
  "longitude": 77.2090,
  "accuracy": 12.5,
  "speed": 1.8,
  "timestamp": 1710851200000
}
```

- Firestore document IDs are auto-generated.
- FIFO ordering is maintained using the timestamp field.

## Assumptions and Limitations

### Assumptions

- User grants location permission.
- Location services (GPS) are enabled on the device.
- Internet availability is determined using Android system connectivity APIs.
- Firebase Firestore is correctly configured and accessible.

### Limitations

- Location data is uploaded **one record at a time** (no batch uploads).
- Synchronization timing depends on **WorkManager scheduling** and is not immediate.
- Background location permission is **not handled**.
- No conflict resolution is implemented for multiple devices using the same employee ID.

