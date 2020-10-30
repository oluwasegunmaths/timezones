package com.ease.timezones.crudtimezone

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ease.timezones.Utils.convertToViewerFriendlyTimeZone
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class AddEditOrDeleteTimeZoneViewModel(app: Application) : AndroidViewModel(app) {
    fun makeSpinnerVisible() {
        _isSpinnerVisible.value=true
    }

    var indexOfSelectedTime: Int = -1
    var timeZones: MutableList<String> = mutableListOf()
    private val _populateAdapter = MutableLiveData<Boolean>()
    val populateAdapter: LiveData<Boolean>
        get() = _populateAdapter
    private val _isSpinnerVisible = MutableLiveData<Boolean>()
    val isSpinnerVisible: LiveData<Boolean>
        get() = _isSpinnerVisible
    init {
        viewModelScope.launch {
            val times = TimeZone.getAvailableIDs()
            withContext(Dispatchers.IO){
                for (t in times){
                    if(t.contains('/')&& t.indexOf("/")==t.lastIndexOf("/")){
                        timeZones.add(convertToViewerFriendlyTimeZone(t))
                    }
                }
            }
            _populateAdapter.value=true
        }

    }
}
