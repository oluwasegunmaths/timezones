package com.ease.timezones.selectedtimezones

import android.app.Application
import androidx.lifecycle.*
import com.ease.timezones.Utils.getHourMinuteString
import com.ease.timezones.models.DisplayedTime
import com.ease.timezones.models.SelectedTime
import com.ease.timezones.firebaselivedatas.FirebaseQueryLiveData
import com.ease.timezones.firebaselivedatas.FirebaseUserLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.*

class TimeZoneViewModel(app: Application, private val uid:String) : AndroidViewModel(app) {
    var isSearching: Boolean = false
    val mFirebaseDatabase = FirebaseDatabase.getInstance()
    val mFirebaseAuth = FirebaseAuth.getInstance()
    var displayedTimeZonesCache: MutableList<DisplayedTime>? = null
    private val _searchText = MutableLiveData<String>()
    val searchText: LiveData<String> = _searchText
    val isLoggedIn: LiveData<Boolean> = FirebaseUserLiveData(mFirebaseAuth).map {
        it != null
    }
    private var viewModelJob: Job = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    sealed class Status {
        class Loading : Status()
        class Loaded : Status()
        class Error(val message: String) : Status()
        class NoTimeZones : Status()
    }

    private val _loadingStatus = MutableLiveData<Status>()
    val loadingStatus: LiveData<Status> = _loadingStatus
    private val databaseReference = mFirebaseDatabase.getReference("/timezones/$uid")

    private val firebaseQueryLiveData = FirebaseQueryLiveData(databaseReference)
    val timeZones: MediatorLiveData<MutableList<DisplayedTime>> = MediatorLiveData<MutableList<DisplayedTime>>()

    init {
        _loadingStatus.value = Status.Loading()
        FirebaseFunctions.getInstance().getHttpsCallable("getTime")
                .call().addOnCompleteListener {
                    if (it.isSuccessful && it.result != null) {
                        val timestamp = it.result!!.data as Long
                        addSourceToTimezones(timestamp)
                    } else {
                        if (it.exception == null) {
                            _loadingStatus.value = Status.Error("Error connecting to database")
                        }else {
                            _loadingStatus.value = Status.Error(it.exception!!.message
                                    ?: "Error connecting to database")
                        }
                    }
                }.addOnCanceledListener {
                    _loadingStatus.value = Status.Error("Connecting to database was cancelled")
                }
    }

    private fun addSourceToTimezones(timestamp: Long) {
        timeZones.addSource(firebaseQueryLiveData) {
            if (it != null && it.isNotEmpty()) {
                convertSnapshotToDisplayedTimes(it, timestamp)
            } else {
                _loadingStatus.value = Status.NoTimeZones()
                timeZones.setValue(null)
            }
        }
    }

    private fun convertSnapshotToDisplayedTimes(it: MutableList<DataSnapshot>, timestamp: Long) {
        viewModelJob.cancelChildren()
        viewModelJob = uiScope.launch {
            val list = mutableListOf<DisplayedTime>()
            concertToDisplayedTimesOnABackgroundThread(it, timestamp, list)
            displayedTimeZonesCache = list
            if (!isSearching) {
                _loadingStatus.value = Status.Loaded()
                timeZones.value = list
            }
        }
    }

    private suspend fun concertToDisplayedTimesOnABackgroundThread(it: MutableList<DataSnapshot>, timestamp: Long, list: MutableList<DisplayedTime>) {
        withContext(Dispatchers.IO) {
            for (dataSnapshot in it) {
                val time = dataSnapshot.getValue(SelectedTime::class.java)
                val key = dataSnapshot.key
                //this should never happen
                if (key == null) {
                    continue
                }
                if (time != null) {
                    val timeString = getHourMinuteString(timestamp + (time.gmtoffset ?: 0))
                    val offset = getHourMinuteString(time.gmtoffset ?: 0)
                    list.add(
                            DisplayedTime(
                                    time.name,
                                    time.location,
                                    timeString,
                                    offset,
                                    key
                            )
                    )
                }
            }
        }
    }


    fun changeSearching(searching: Boolean) {
        if (!searching) {
            //this removes the yellow highlight from previously searched text
            _searchText.value = ""
            resetTimeZones()
        }
        isSearching = searching
    }

    fun setTimeZones(filteredTimes: MutableList<DisplayedTime>) {
        timeZones.value = filteredTimes
    }

    //restores the data to the full list
    fun resetTimeZones() {
        timeZones.value = displayedTimeZonesCache
    }

    fun setSearchText(s: String) {
        _searchText.value = s
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()

    }

    fun signOut() {
        mFirebaseAuth.signOut()
    }
}

