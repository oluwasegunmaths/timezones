package com.ease.timezones.splashscreen

import android.app.Application
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.navigation.fragment.findNavController
import com.ease.timezones.Utils
import com.ease.timezones.login.LoginFragmentDirections
import com.ease.timezones.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class SplashScreenViewModel(
    app: Application
) : AndroidViewModel(app) {
    //    private val _userLiveData = MutableLiveData<FirebaseUser>()
    private lateinit var managersDR: DatabaseReference

    //    val userLiveData: LiveData<FirebaseUser>
//        get() = _userLiveData
    val mFirebaseDatabase = FirebaseDatabase.getInstance()
    val mFirebaseAuth = FirebaseAuth.getInstance()

    enum class AuthenticationState {
        EMAILVERIFIED, NOTEMAILVERIFIED, UNAUTHENTICATED
    }

    sealed class Role(id: String? = null) {
        class Admin : Role()
        class Manager : Role()
        class NormalUser(val uid: String) : Role()
    }

    private val _emailText = MutableLiveData<String>()
    val emailText: LiveData<String>
        get() = _emailText
    private val _passwordText = MutableLiveData<String>()
    val passwordText: LiveData<String>
        get() = _passwordText
    private val _role = MutableLiveData<Role>()
    val role: LiveData<Role>
        get() = _role
    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String>
        get() = _toastMessage
    private val _showResetPassword = MutableLiveData<Boolean>()
    val showResetPassword: LiveData<Boolean>
        get() = _showResetPassword
    private val _showLoginProgressBar = MutableLiveData<Boolean>()
    val showLoginProgressBar: LiveData<Boolean>
        get() = _showLoginProgressBar


    val authenticationState = FirebaseUserLiveData(mFirebaseAuth).map { user ->
        if (user != null) {
            if (user.isEmailVerified) {
                AuthenticationState.EMAILVERIFIED
//                _userLiveData.value=user
                val adminsDR = mFirebaseDatabase.getReference().child("admins").child(user.uid)
                managersDR = mFirebaseDatabase.getReference().child("managers").child(user.getUid())

                adminsDR.addListenerForSingleValueEvent(valueEventListener(user))
            } else {
                AuthenticationState.NOTEMAILVERIFIED
            }
        } else {
            AuthenticationState.UNAUTHENTICATED

        }
    }


    private fun valueEventListener(
        user: FirebaseUser,
        checkingForAdmin: Boolean = true
    ): ValueEventListener {
        Log.i("nnnnnnnnnn", "before")

        return object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.i("nnnnnnnnnn", "after")

                if (checkingForAdmin) {
                    if (snapshot.exists()) {

                        Log.i("nnnnnnnnnn", "3")
                        _role.value = Role.Admin()
//                        Toast.makeText(con,
//                            "Authenticated with: " + user?.email, Toast.LENGTH_SHORT)
//                            .show()
//
//                        navController.navigate(LoginFragmentDirections.actionLoginFragmentToUsersFragment(true))

                    } else {
                        Log.i("nnnnnnnnnn", "4")

                        managersDR.addListenerForSingleValueEvent(valueEventListener(user, false))
                    }
                } else {
                    if (snapshot.exists()) {
                        Log.i("nnnnnnnnnn", "5")
                        _role.value = Role.Manager()


//                        Toast.makeText(con,
//                            "Authenticated with: " + user?.email, Toast.LENGTH_SHORT)
//                            .show()
//                        Log.i("dddddddd", findNavController().currentDestination?.label.toString())
//
//                        navController.navigate(LoginFragmentDirections.actionLoginFragmentToUsersFragment(false))
                    } else {
                        _role.value = Role.NormalUser(user.uid)
                        Log.i("nnnnnnnnnn", "6")

//                        Toast.makeText(con,
//                            "Authenticated with: " + user?.email, Toast.LENGTH_SHORT)
//                            .show()
//                        Log.i("dddddddd", findNavController().currentDestination?.label.toString())
//
//                        navController.navigate(LoginFragmentDirections.actionLoginFragmentToTimeZoneFragment(auth?.uid))
                    }

                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("Firebase database", databaseError.message)
            }
        }
    }

    fun signIn() {
        if (!(emailText.value == null || emailText.value == "" || passwordText.value == null || passwordText.value == "")) {
            _showLoginProgressBar.value = true

            mFirebaseAuth.signInWithEmailAndPassword(
                emailText.value!!,
                passwordText.value!!
            ).addOnCompleteListener { task ->
                _showLoginProgressBar.value = false
//                    binding.progressBar.setVisibility(View.GONE)
                if (!task.isSuccessful) {
                    if (task.exception != null) {
                        _toastMessage.value = """
                                    Authentication Unsuccessful
                                    ${task.exception!!.message}
                                    """.trimIndent()

                    } else {
                        _toastMessage.value = "Authentication Unsuccessful"
                    }
                    _showResetPassword.value = true
                }
            }.addOnFailureListener { e ->
                _toastMessage.value = """Authentication Failed 
                            ${e.message}
                            """.trimIndent()

                _showLoginProgressBar.value = false
                _showResetPassword.value = true
            }
        } else {
            _toastMessage.value = "You didn't fill in all the fields."

        }

    }

    fun resetPassword() {
        if (emailText.value == null || emailText.value == "") {
            _toastMessage.value = "Password Reset Link Sent to Email"


            return;
        }

        mFirebaseAuth.sendPasswordResetEmail(emailText.value!!)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _toastMessage.value = "Password Reset Link Sent to Email"
                } else {
                    _toastMessage.value = "No User is Associated with that Email"
                }
            }

    }
}