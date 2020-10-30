package com.ease.timezones.users

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.ease.timezones.firebaselivedatas.FirebaseQueryLiveData
import com.ease.timezones.firebaselivedatas.FirebaseUserLiveData
import com.ease.timezones.models.DisplayedUser

import com.ease.timezones.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.*
import java.lang.Exception


class UserListViewModel(app: Application) : AndroidViewModel(app) {
    fun signOut() {
        mFirebaseAuth.signOut()
    }

    var isAdmin: Boolean = false
    val mFirebaseDatabase = FirebaseDatabase.getInstance()
    val mFirebaseAuth = FirebaseAuth.getInstance()
    private val databaseReference = mFirebaseDatabase.getReference("/users")
    val isLoggedIn: LiveData<Boolean> = FirebaseUserLiveData(mFirebaseAuth).map {
        it != null
    }
    private var viewModelJob: Job = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    private val firebaseQueryLiveData = FirebaseQueryLiveData(databaseReference)
    val users: MediatorLiveData<MutableList<DisplayedUser>> = MediatorLiveData<MutableList<DisplayedUser>>()

    sealed class Status {
        class Loading : Status()
        class Loaded : Status()
        class Error(val message: String) : Status()
    }

    private val _loadingStatus = MutableLiveData<Status>()
    val loadingStatus: LiveData<Status> = _loadingStatus

    init {
        _loadingStatus.value = Status.Loading()
        users.addSource(firebaseQueryLiveData) {
            if (it != null) {
                convertToDisplayedUsers(it)
            } else {
                _loadingStatus.value = Status.Error("Error Loading Users")
                users.setValue(null)
            }
        }
    }

    private fun convertToDisplayedUsers(it: MutableList<DataSnapshot>) {
        viewModelJob.cancelChildren()
        viewModelJob = uiScope.launch {
            val list = mutableListOf<DisplayedUser>()
            withContext(Dispatchers.IO) {
                for (dataSnapshot in it) {
                    val user = dataSnapshot.getValue(User::class.java)
                    val key = dataSnapshot.key
                    //key == null should never ever happen
                    if (key == null) {
                        continue
                    }
                    if (user != null) {
                        Log.i("ooooooo", "x")
                        list.add(DisplayedUser(user.displayName, key, user.email, user.password))
                    }
                }
            }
            _loadingStatus.value = Status.Loaded()
            users.value = list
        }
    }
}

