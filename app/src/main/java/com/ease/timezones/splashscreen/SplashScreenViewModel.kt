package com.ease.timezones.splashscreen

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.ease.timezones.Utils
import com.ease.timezones.Utils.isEmptyOrNull
import com.ease.timezones.firebaselivedatas.FirebaseUserLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.*

class SplashScreenViewModel(app: Application) : AndroidViewModel(app) {
    private lateinit var managersDR: DatabaseReference
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

    val _emailText = MutableLiveData<String>()
    val _passwordText = MutableLiveData<String>()
    val registerEmailText = MutableLiveData<String>()
    val registerPasswordText = MutableLiveData<String>()
    val registerPassWordConfirmationText = MutableLiveData<String>()

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

    private val _registerBackToLogin = MutableLiveData<Boolean>()
    val registerBackToLogin: LiveData<Boolean>
        get() = _registerBackToLogin

    val authenticationState: LiveData<AuthenticationState> = FirebaseUserLiveData(mFirebaseAuth).map { user ->
        Log.i("ooooooo","1")
        if (user != null) {
            if (user.isEmailVerified) {
                Log.i("ooooooo","1")
                val adminsDR = mFirebaseDatabase.getReference().child("admins").child(user.uid)
                managersDR = mFirebaseDatabase.getReference().child("managers").child(user.getUid())
                adminsDR.addListenerForSingleValueEvent(valueEventListener(user))
                AuthenticationState.EMAILVERIFIED
            } else {
                AuthenticationState.NOTEMAILVERIFIED
            }
        } else {
            Log.i("ooooooo","1")

            _toastMessage.value = "signed out"
            AuthenticationState.UNAUTHENTICATED

        }
    }


    private fun valueEventListener(user: FirebaseUser, checkingForAdmin: Boolean = true): ValueEventListener {
        return object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (checkingForAdmin) {
                    if (snapshot.exists()) {
                        _role.value = Role.Admin()
                        _toastMessage.value = "Authenticated with: " + user.email
                    } else {
                        managersDR.addListenerForSingleValueEvent(valueEventListener(user, false))
                    }
                } else {
                    if (snapshot.exists()) {
                        _role.value = Role.Manager()
                        _toastMessage.value = "Authenticated with: " + user.email
                    } else {
                        _role.value = Role.NormalUser(user.uid)
                        _toastMessage.value = "Authenticated with: " + user.email
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                _toastMessage.value = "Error authenticating" + user.email + "due to" + databaseError.message
            }
        }
    }

    fun signIn() {
        if (!isEmptyOrNull(_emailText.value) && !isEmptyOrNull(_passwordText.value)) {
            _showLoginProgressBar.value = true
            mFirebaseAuth.signInWithEmailAndPassword(_emailText.value!!, _passwordText.value!!).addOnCompleteListener { task ->
                _showLoginProgressBar.value = false
                if (!task.isSuccessful) {
                    if (task.exception != null) {
                        _toastMessage.value = """Authentication Unsuccessful${task.exception!!.message}""".trimIndent()
                    } else {
                        _toastMessage.value = "Authentication Unsuccessful"
                    }
                    _showResetPassword.value = true
                }
            }.addOnFailureListener { e ->
                _toastMessage.value = """Authentication Failed${e.message}""".trimIndent()
                _showLoginProgressBar.value = false
                _showResetPassword.value = true
            }
        } else {
            _toastMessage.value = "You didn't fill in all the fields."
        }
    }
    fun resetPassword() {
        if (isEmptyOrNull(_emailText.value)) {
            _toastMessage.value = "Fill in your email"
            return;
        }
        mFirebaseAuth.sendPasswordResetEmail(_emailText.value!!).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                _toastMessage.value = "Password Reset Link Sent to Email"
            } else {
                _toastMessage.value = "No User is Associated with that Email"
            }
        }
    }

    fun firebaseAuthWithGoogle(idToken: String?) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mFirebaseAuth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                _toastMessage.value = "Authentication Failed"
            }
        }.addOnCanceledListener {
            _toastMessage.value = "Authentication Cancelled"
        }
    }

    fun sendEmailVerification() {
        mFirebaseAuth.currentUser?.sendEmailVerification()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                _toastMessage.value = "Sent Verification Email"
            } else {
                _toastMessage.value = "couldn't send email"
            }
        }
        mFirebaseAuth.signOut()
    }

    fun registerNewUser() {
        if (!isEmptyOrNull(registerEmailText.value) && !isEmptyOrNull(registerPasswordText.value)
                && !isEmptyOrNull(registerPassWordConfirmationText.value)) {
            //check if email ends with '.com'
            if (Utils.endsProperly(registerEmailText.value!!)) {
                //check if passwords match
                if (registerPasswordText.value.equals(registerPassWordConfirmationText.value)) {
                    //Initiate registration task
                    registerNewEmail(registerEmailText.value!!, registerPasswordText.value!!)
                } else {
                    _toastMessage.value = "Passwords do not Match"
                }
            } else {
                _toastMessage.value = "Please Register with valid Email"
            }
        } else {
            _toastMessage.value = "You must fill out all the fields"
        }
    }

    private fun registerNewEmail(email: String, password: String) {
        _showLoginProgressBar.value = true
        mFirebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                //send email verificaiton
                sendEmailVerification()
                mFirebaseAuth.signOut()

                //redirect the user to the login screen
                _registerBackToLogin.value = true
            }
            if (!task.isSuccessful) {
                _toastMessage.value = "Unable to Register" + task.exception
            }
            _showLoginProgressBar.value = false
        }
    }

    fun onRegisterToLoginCompleted() {
        _registerBackToLogin.value = false
    }

    fun onToastDisplayed() {
        _toastMessage.value = ""
    }
}