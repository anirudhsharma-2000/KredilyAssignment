package com.anirudh.assignment.kredilyassignment

import android.app.Application
import android.content.Context
import android.util.Log

class AssignmentApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: ")
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        appContext = base!!
    }
}