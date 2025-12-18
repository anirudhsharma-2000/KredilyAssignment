package com.anirudh.assignment.kredilyassignment.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anirudh.assignment.kredilyassignment.data.LocationEntity
import com.anirudh.assignment.kredilyassignment.domain.HomeDomain
import com.anirudh.assignment.kredilyassignment.domain.LocationRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: LocationRepository
) : ViewModel() {

    // 🌐 Internet status (unchanged)
    val isOnline: StateFlow<Boolean> =
        HomeDomain.observeInternetConnection()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                false
            )

    // 📍 Latest location for UI
    private val _location = MutableStateFlow<LocationEntity?>(null)
    val location = _location.asStateFlow()

    private var locationJob: Job? = null

    /* --------------------------------------------------- */
    /* Start location tracking */
    /* --------------------------------------------------- */
    fun startLocationUpdates(empId: String, interval: Long) {
        stopLocationUpdates()

        locationJob = viewModelScope.launch {
            HomeDomain.requestCurrentLocation(interval)
                .collect { androidLocation ->

                    val entity = LocationEntity(
                        employeeId = empId,
                        latitude = androidLocation.latitude,
                        longitude = androidLocation.longitude,
                        accuracy = androidLocation.accuracy,
                        speed = androidLocation.speed,
                        timeStamp = androidLocation.time
                    )

                    // 🔥 Update UI
                    _location.value = entity

                    // 🔥 ALWAYS save to Room
                    repository.saveLocation(entity)
                }
        }
    }

    /* --------------------------------------------------- */
    /* Stop tracking */
    /* --------------------------------------------------- */
    fun stopLocationUpdates() {
        locationJob?.cancel()
        locationJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopLocationUpdates()
    }
}
