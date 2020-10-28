package com.ease.timezones.users

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.ease.timezones.firebaselivedatas.FirebaseQueryLiveData
import com.ease.timezones.models.DisplayedUser

import com.ease.timezones.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception


class UserListViewModel(app: Application) : AndroidViewModel(app) {
    fun signOut() {
        mFirebaseAuth.signOut()
    }

    var isAdmin: Boolean = false
    val mFirebaseDatabase = FirebaseDatabase.getInstance()
    val mFirebaseAuth = FirebaseAuth.getInstance()
    val userDR = mFirebaseDatabase.reference.child("users")
//    private val _displayedUsers = MutableLiveData<MutableList<DisplayedUser>>()
//    val displayedUsers: LiveData<MutableList<DisplayedUser>>
//        get() = _displayedUsers
    private val databaseReference =mFirebaseDatabase.getReference("/users")

    private val liveData = FirebaseQueryLiveData(databaseReference)
     val users: MediatorLiveData<MutableList<DisplayedUser>> = MediatorLiveData<MutableList<DisplayedUser>>()
    sealed class Status() {
        class Loading : Status()
        class Loaded : Status()
        class Error(val message: String) : Status()
    }

    private val _loadingStatus = MutableLiveData<Status>()
    val loadingStatus: LiveData<Status> = _loadingStatus
    init {
        Log.i("ooooooo","a")
        _loadingStatus.value= Status.Loading()
        users.addSource(liveData) {
            if (it != null) {
                viewModelScope.launch {
                    var list = mutableListOf<DisplayedUser>()
                    withContext(Dispatchers.IO) {
                        try {
                            Log.i("ooooooo", "b")
                            for (d in it) {
                                val user = d.getValue(User::class.java)

                                val key = d.key
                                if (user != null) {
                                    Log.i("ooooooo", "x")
                                    list.add(DisplayedUser(
                                            user.displayName,
                                            key ?: "",
                                            user.email,
                                            user.password
                                    ))
                                }
                            }
//                                 list=it.map {
//                                    val user=it.getValue(User::class.java)
//                                    val key=it.key
//                                    if(user==null){
//                                        Log.i("ooooooo","x")
//                                        return@map
//                                    }
//                                      DisplayedUser(
//                                            user.displayName,
//                                            key?:"",
//                                            user.email,
//                                            user.password
//                                        ) ^map
//                                    }
//                                }
                            Log.i("ooooooo", "d")


                        } catch (e: Exception) {
                            _loadingStatus.postValue( Status.Error("Database error"))

//                            Log.i("ooooooo", e.message!!)

                        }
                    }
                    Log.i("ooooooo", "z")
                    _loadingStatus.value= Status.Loaded()

                    users.value = list
                    Log.i("ooooooo", "y")

                }
//                Thread { hotStockLiveData.postValue(dataSnapshot.getValue(HotStock::class.java)) }.start()
            } else {
                _loadingStatus.value= Status.Error("Error Loading Users")

                users.setValue(null)
            }
        }
//        users.value=null
//        val t=users.value
    }


//    fun getDataSnapshotLiveData(): LiveData<DataSnapshot?> {
//        return liveData
//    }
//
//
//
//    fun attachChatDatabaseReadListener() {
//        val users = mutableListOf<DisplayedUser>()
//        val mChatChildEventListener: ChildEventListener = object : ChildEventListener {
//            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
//
//                val user = dataSnapshot.getValue(User::class.java)
//                val key = dataSnapshot.key
//                if (key == null) return
//                user?.let {
//                    users.add(it.asDisplayedUser(key))
//                    _displayedUsers.value = users
//                }
//            }
//
//            override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {}
//            override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
//            override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
//            override fun onCancelled(databaseError: DatabaseError) {
//                Log.e("Firebase database", databaseError.message)
//            }
//        }
//        userDR.addChildEventListener(mChatChildEventListener)
////        patientsDR!!.addListenerForSingleValueEvent(object : ValueEventListener {
////            override fun onDataChange(snapshot: DataSnapshot) {
////                if (!snapshot.exists()) {
////                    binding.emptyTextView.visibility = View.VISIBLE
////                    binding.emptyTextView.text =
////                        getString(R.string.get_started_by_adding_a_new_jogging_time)
////                }
////            }
////
////            override fun onCancelled(databaseError: DatabaseError) {
////                Log.e("Firebase database", databaseError.message)
////            }
////        })
//    }
}

