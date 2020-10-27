package com.ease.timezones.firebaselivedatas

import android.util.Log
import androidx.lifecycle.LiveData
import com.google.firebase.database.*

//recommended way of using firebase with mvvm according to the official firebase blog
//https://firebase.googleblog.com/2017/12/using-android-architecture-components.html
class FirebaseQueryLiveData : LiveData<MutableList<DataSnapshot>> {
    private val query: Query
    private val listener: MyChildEventListener = MyChildEventListener()
    private  val dataSnapShotList= mutableListOf<DataSnapshot>()
    constructor(query: Query) {
        this.query = query
    }

    constructor(ref: DatabaseReference) {
        query = ref
    }

    override fun onActive() {
        Log.d(LOG_TAG, "onActive")
        query.addChildEventListener(listener)
    }

    override fun onInactive() {
        Log.d(LOG_TAG, "onInactive")
        query.removeEventListener(listener)
        dataSnapShotList.clear()
    }

//    private inner class MyValueEventListener : ValueEventListener {
//        override fun onDataChange(dataSnapshot: DataSnapshot) {
//            value = dataSnapshot
//        }
//
//        override fun onCancelled(databaseError: DatabaseError) {
//            Log.e(
//                LOG_TAG,
//                "Can't listen to query $query", databaseError.toException()
//            )
//        }
//    }
    private inner class MyChildEventListener:  ChildEventListener {
                    override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                        dataSnapShotList.add(dataSnapshot)
                        value = dataSnapShotList

//                val user = dataSnapshot.getValue(User::class.java)
//                val key = dataSnapshot.key
//                if (key == null) return
//                user?.let {
//                    users.add(it.asDisplayedUser(key))
//                    _displayedUsers.value = users
//                }
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {}
            override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
            override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("Firebase database", databaseError.message)
            }
        }
    companion object {
        private const val LOG_TAG = "FirebaseQueryLiveData"
    }
}