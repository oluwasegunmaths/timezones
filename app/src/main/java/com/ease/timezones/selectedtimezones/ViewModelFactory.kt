package com.ease.timezones.selectedtimezones

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ViewModelFactory(
    private val application: Application,
    private val uid: String,
    ) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TimeZoneViewModel::class.java)) {
            return TimeZoneViewModel( application,uid) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
//        return null
    }
}