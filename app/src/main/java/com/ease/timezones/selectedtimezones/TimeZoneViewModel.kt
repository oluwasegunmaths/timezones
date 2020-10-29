package com.ease.timezones.selectedtimezones

import android.app.Application
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import com.ease.timezones.Utils.getHourMinuteString
import com.ease.timezones.models.DisplayedTime
import com.ease.timezones.models.SelectedTime
import com.ease.timezones.firebaselivedatas.FirebaseQueryLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception

class TimeZoneViewModel(app: Application, private val uid:String) : AndroidViewModel(app) {
     var isSearching: Boolean = false
    val mFirebaseDatabase = FirebaseDatabase.getInstance()
//    val mFirebaseAuth = FirebaseAuth.getInstance()
    var displayedTimeZonesCache: MutableList<DisplayedTime>? = null
    private val _searchText = MutableLiveData<String>()
    val searchText: LiveData<String> = _searchText
//        get() = _searchText
//enum class Status {
//    LOADING, LOADED, ERROR,NOTIMEZONES
//}
    sealed class Status() {
        class Loading : Status()
        class Loaded : Status()
        class Error(val message: String) : Status()
        class NoTimeZones:Status()
    }

    private val _loadingStatus = MutableLiveData<Status>()
    val loadingStatus: LiveData<Status> = _loadingStatus
    private val databaseReference = mFirebaseDatabase.getReference("/timezones/$uid")

    private val liveData = FirebaseQueryLiveData(databaseReference)
    val timeZones: MediatorLiveData<MutableList<DisplayedTime>> =
        MediatorLiveData<MutableList<DisplayedTime>>()

    init {
        _loadingStatus.value = Status.Loading()
        Log.i("jjjjjjjjjj", "timetime")

        FirebaseFunctions.getInstance().getHttpsCallable("getTime")
                .call().addOnCompleteListener {
                    if (it.isSuccessful && it.result != null) {
                        val timestamp = it.result!!.data as Long
//                val format = SimpleDateFormat("M??d??H?m??s??")
//                format.setTimeZone(TimeZone.getTimeZone("GMT"))
                        Log.i("jjjjjjjjjj", "jjjj")

                        addSourceToTimezones(timestamp)
                    }else{
                        if(it.exception==null){
                            _loadingStatus.value = Status.Error("Error connecting to database")
                        }else {
                            _loadingStatus.value = Status.Error(it.exception!!.message
                                    ?: "Error connecting to database")
                        }

                    }
                }.addOnCanceledListener {
                    _loadingStatus.value = Status.Error("Connecting to database was cancelled")

                }

//        { httpsCallableResult ->
//
//                val timestamp = httpsCallableResult.data as Long
////                val format = SimpleDateFormat("M??d??H?m??s??")
////                format.setTimeZone(TimeZone.getTimeZone("GMT"))
//                Log.i("ooooooo","a")
//                timeZones.addSource(liveData, Observer {
//                    if (it != null) {
//                        viewModelScope.launch {
//                            val list= mutableListOf<DisplayedTime>()
//                            withContext(Dispatchers.IO){
//                                try {
//                                    Log.i("ooooooo","b")
//                                    for (d in it) {
//                                        Log.i("kkkkkkkk","a")
//
//                                        val time=d.getValue(SelectedTime::class.java)
//                                        val key=d.key
//                                        if(key==null)return@withContext
//                                        if(time!=null){
//                                            Log.i("kkkkkkkk","b")
//
//                                            Log.i("ooooooo","x")
//
//                                            val timeString=getHourMinuteString(timestamp+(time.gmtoffset?:0))
//                                            val offset= getHourMinuteString(time.gmtoffset?:0)
//                                            Log.i("kkkkkkkk","c")
//
//                                            list.add(
//                                                DisplayedTime(
//                                                    time.name,
//                                                    time.location,
//                                                    timeString,
//                                                    offset,
//                                                    key
//                                                )
//                                            )
//                                            Log.i("kkkkkkkk","e")
//
//                                        }
//                                    }
//                                    Log.i("kkkkkkkk","d")
//
//
//                                }
//                                catch (e: Exception){
////                                    Log.i("ooooooo",e.message!!)
//
//                                }
//                            }
//                            Log.i("ooooooo","z")
//                            displayedTimeZonesCache=list
//
//                            if (!isSearching) {
//                                timeZones.value=list
//
//                            }
//                            Log.i("ooooooo","y")
//
//                        }
//                    } else {
//                        timeZones.setValue(null)
//                    }
//                })
//            }

    }

    private fun addSourceToTimezones(timestamp: Long) {
                        timeZones.addSource(liveData) {
                            if (it != null&&it.isNotEmpty()) {
                                viewModelScope.launch {
                                    val list = mutableListOf<DisplayedTime>()
                                    withContext(Dispatchers.IO) {
                                        try {
                                            Log.i("ooooooo", "b")
                                            for (d in it) {
                                                Log.i("kkkkkkkk", "a")

                                                val time = d.getValue(SelectedTime::class.java)
                                                val key = d.key
                                                if (key == null) {
                                                    _loadingStatus.value=Status.Error("Database error")
                                                    return@withContext
                                                }

                                                if (time != null) {
                                                    Log.i("kkkkkkkk", "b")

                                                    Log.i("ooooooo", "x")

                                                    val timeString = getHourMinuteString(timestamp + (time.gmtoffset
                                                            ?: 0))
                                                    val offset = getHourMinuteString(time.gmtoffset
                                                            ?: 0)
                                                    Log.i("kkkkkkkk", "c")

                                                    list.add(
                                                            DisplayedTime(
                                                                    time.name,
                                                                    time.location,
                                                                    timeString,
                                                                    offset,
                                                                    key
                                                            )
                                                    )
                                                    Log.i("kkkkkkkk", "e")

                                                }
                                            }
                                            Log.i("kkkkkkkk", "d")


                                        } catch (e: Exception) {
//                                    Log.i("ooooooo",e.message!!)
                                            _loadingStatus.postValue(Status.Error("Database error"))
                                            return@withContext
                                        }
                                    }
                                    Log.i("ooooooo", "z")
                                    displayedTimeZonesCache = list

                                    if (!isSearching) {
                                        _loadingStatus.value=Status.Loaded()
                                        timeZones.value = list

                                    }
                                    Log.i("jjjjjjjjjj", list.size.toString())

                                }
                            } else {
                                _loadingStatus.value=Status.NoTimeZones()
                                timeZones.setValue(null)
                            }
                        }
    }



    fun getTextWatcher(): TextWatcher {

        return object   : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(s==null||s.isEmpty()){
                    timeZones.value=displayedTimeZonesCache
                    return
                }
                _searchText.value=s.toString()
                val times=displayedTimeZonesCache
                val filteredTimes= mutableListOf<DisplayedTime>()
               times?.run {
                   for (t in this) {
                       if(t.name!=null&&t.location!=null){
                           if(t.name!!.contains(s,true)||t.location!!.contains(s,true)){
                               filteredTimes.add(t)
                           }

                       }
                   }
               }
                timeZones.value=filteredTimes
            }

            override fun afterTextChanged(s: Editable?) {
            }
        }
    }

    fun changeSearching(searching: Boolean) {

        isSearching=searching
        if(!searching){
            timeZones.value=displayedTimeZonesCache
        }
    }

}

