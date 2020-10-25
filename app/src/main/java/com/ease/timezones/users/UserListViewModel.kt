package com.ease.timezones.users

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ease.timezones.models.DisplayedUser
import com.ease.timezones.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase

class UserListViewModel(app: Application) : AndroidViewModel(app) {
    fun signOut() {
        mFirebaseAuth.signOut()
    }

    var isAdmin: Boolean = false
    val mFirebaseDatabase = FirebaseDatabase.getInstance()
    val mFirebaseAuth = FirebaseAuth.getInstance()
    val userDR = mFirebaseDatabase.reference.child("users")
    private val _displayedUsers = MutableLiveData<MutableList<DisplayedUser>>()
    val displayedUsers: LiveData<MutableList<DisplayedUser>>
        get() = _displayedUsers

    fun attachChatDatabaseReadListener() {
        val users = mutableListOf<DisplayedUser>()
        val mChatChildEventListener: ChildEventListener = object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                val user = dataSnapshot.getValue(User::class.java)
                val key = dataSnapshot.key
                if (key == null) return
                user?.let {
                    users.add(it.asDisplayedUser(key))
                    _displayedUsers.value = users
                }
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {}
            override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
            override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("Firebase database", databaseError.message)
            }
        }
        userDR.addChildEventListener(mChatChildEventListener)
//        patientsDR!!.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                if (!snapshot.exists()) {
//                    binding.emptyTextView.visibility = View.VISIBLE
//                    binding.emptyTextView.text =
//                        getString(R.string.get_started_by_adding_a_new_jogging_time)
//                }
//            }
//
//            override fun onCancelled(databaseError: DatabaseError) {
//                Log.e("Firebase database", databaseError.message)
//            }
//        })
    }
}

