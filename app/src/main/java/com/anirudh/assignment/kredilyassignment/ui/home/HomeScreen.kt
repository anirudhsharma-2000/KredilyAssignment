package com.anirudh.assignment.kredilyassignment.ui.home

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.WorkManager
import com.anirudh.assignment.kredilyassignment.TAG
import com.anirudh.assignment.kredilyassignment.data.AppDatabase
import com.anirudh.assignment.kredilyassignment.domain.LocationRepository
import com.anirudh.assignment.kredilyassignment.domain.LocationSettingsManager
import com.anirudh.assignment.kredilyassignment.ui.openAppSettings

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    val database = remember { AppDatabase.getInstance() }
    val repository = remember {
        LocationRepository(
            dao = database.locationDao(), workManager = WorkManager.getInstance()
        )
    }
    val homeViewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory(repository))

    val context = LocalContext.current
    val activity = context as Activity

    var isTracking by rememberSaveable { mutableStateOf(false) }
    var askedPermissionFromButton by rememberSaveable { mutableStateOf(false) }
    var logCounter by rememberSaveable { mutableStateOf(0) }

    val isOnline = homeViewModel.isOnline.collectAsStateWithLifecycle().value
    val location = homeViewModel.location.collectAsStateWithLifecycle().value

    /* --------------------------------------------------- */
    /* Start / Stop location tracking */
    /* --------------------------------------------------- */
    LaunchedEffect(isTracking) {
        if (isTracking) {
            homeViewModel.startLocationUpdates(
                empId = "EMP0001",
                interval = 1_000L
            )
        } else {
            Log.d(TAG, "Stopping location updates")
            homeViewModel.stopLocationUpdates()
        }
    }

    /* --------------------------------------------------- */
    /* Permission launcher (Start Tracking button) */
    /* --------------------------------------------------- */
    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                // Permission granted → check GPS
                if (!LocationSettingsManager.isLocationEnabled()) {
                    LocationSettingsManager.requestEnableLocation(activity) {
                        isTracking = true
                    }
                } else {
                    isTracking = true
                }
            } else {
                // Permission denied
                val permanentlyDenied =
                    !ActivityCompat.shouldShowRequestPermissionRationale(
                        activity,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )

                if (permanentlyDenied && askedPermissionFromButton) {
                    activity.openAppSettings()
                }
            }
        }

    /* --------------------------------------------------- */
    /* UI */
    /* --------------------------------------------------- */
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        /* -------- ONLINE / OFFLINE -------- */
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isOnline) "🟢" else "🔴",
                modifier = Modifier.padding(16.dp)
            )
            Text(
                text = if (isOnline) "ONLINE" else "OFFLINE",
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(100.dp))

        /* -------- LOCATION -------- */
        Text(
            text = "Latitude: ${location?.latitude}\nLongitude: ${location?.longitude}",
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(20.dp))

        /* -------- START / STOP BUTTON -------- */
        Button(
            onClick = {
                if (isTracking) {
                    // Stop tracking
                    isTracking = false
                    return@Button
                }

                // Step 1: Permission
                val hasPermission =
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED

                if (!hasPermission) {
                    askedPermissionFromButton = true
                    permissionLauncher.launch(
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                    return@Button
                }

                // Step 2: GPS
                if (!LocationSettingsManager.isLocationEnabled()) {
                    LocationSettingsManager.requestEnableLocation(activity) {
                        isTracking = true
                    }
                } else {
                    isTracking = true
                }
            }
        ) {
            Text(if (isTracking) "Stop Tracking" else "Start Tracking")
        }

        Spacer(Modifier.height(20.dp))

        /* -------- COUNTER (OFFLINE ONLY) -------- */
        if (!isOnline) {
            Text(
                text = "Counter",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color.White
            )
            Text(
                text = logCounter.toString(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color.White
            )
        }
    }
}