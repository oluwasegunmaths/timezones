package com.ease.timezones.firebaselivedatas

import android.util.Log
import androidx.lifecycle.LiveData
import com.google.firebase.database.*
import kotlinx.coroutines.*

//recommended way of using firebase with mvvm according to the official firebase blog
//https://firebase.googleblog.com/2017/12/using-android-architecture-components.html
class FirebaseQueryLiveData(ref: DatabaseReference) : LiveData<MutableList<DataSnapshot>>() {
    private var listenerRemovePending = false
    private val firebaseScope = CoroutineScope(Dispatchers.Main)


    private val query: Query = ref
    private val childEventListener: MyChildEventListener = MyChildEventListener()
    private val valueEventListener: ValueEventListener = MyValueEventListener()

    //    private  var queryHasChanged=false
    internal val dataSnapShotList = mutableListOf<DataSnapshot>()

    override fun onActive() {
        if (!listenerRemovePending) {
            query.addListenerForSingleValueEvent(valueEventListener)
        } else {
            if (firebaseScope.isActive) {
                firebaseScope.cancel()
            }
            listenerRemovePending = false

        }

    }

    override fun onInactive() {
        listenerRemovePending = true
        firebaseScope.launch {
            //this is to prevent unnecessary removal of event listener on device rotation
            //which prevents unnecessary requerying of the database on orientation change
            delay(2000)

            if (listenerRemovePending) {
                query.removeEventListener(valueEventListener)
                query.removeEventListener(childEventListener)
                dataSnapShotList.clear()
            }
            listenerRemovePending = false
        }
    }

    private inner class MyValueEventListener : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            if (!dataSnapshot.exists()) {
                value = null
            }
            query.addChildEventListener(childEventListener)
        }
        override fun onCancelled(databaseError: DatabaseError) {
        }
    }
    private  inner class MyChildEventListener:  ChildEventListener {
                    override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                        dataSnapShotList.add(dataSnapshot)
                        value = dataSnapShotList
                    }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
            var pos = 0

            for (d in dataSnapShotList) {
                if (d.key == dataSnapshot.key) {
                    dataSnapShotList.removeAt(pos)
                    dataSnapShotList.add(pos, dataSnapshot)
                    break
                }
                pos++
            }
            value = dataSnapShotList

        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {
            dataSnapShotList.remove(dataSnapshot)
            value = dataSnapShotList
        }

        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
        override fun onCancelled(databaseError: DatabaseError) {
            Log.e("Firebase database", databaseError.message)
        }
    }
}