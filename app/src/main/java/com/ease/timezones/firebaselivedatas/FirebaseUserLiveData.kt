/*
 * Copyright 2019, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ease.timezones.firebaselivedatas

import android.util.Log
import androidx.lifecycle.LiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

/**
 * This class observes the current FirebaseUser. If there is no logged in user, FirebaseUser will
 * be null.
 *
 */
class FirebaseUserLiveData(val f: FirebaseAuth) : LiveData<FirebaseUser?>() {
    //    private val firebaseAuth = FirebaseAuth.getInstance()
    // boolean flag to ensure the method in the listener is not triggered when the listener
//    is just being registered . This is to prevent bugs. link: https://firebase.google.com/docs/reference/android/com/google/firebase/auth/FirebaseAuth.AuthStateListener
    private var authHasBeenRegistered = false

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        // Use the FirebaseAuth instance instantiated at the beginning of the class to get an entry
        // point into the Firebase Authentication SDK the app is using.
        // With an instance of the FirebaseAuth class, you can now query for the current user.
        Log.i("nnnnnnnnnn", "howhow")
//        if (authHasBeenRegistered) {
////            Log.i("nnnnnnnnnn", "howhow1")

            value = firebaseAuth.currentUser

//        } else {
//            authHasBeenRegistered = true
//        }
    }

    // When this object has an active observer, start observing the FirebaseAuth state to see if
    // there is currently a logged in user.
    override fun onActive() {
        Log.i("qwwerty","gg")
        f.addAuthStateListener(authStateListener)
    }

    // When this object no longer has an active observer, stop observing the FirebaseAuth state to
    // prevent memory leaks.
    override fun onInactive() {
//        authHasBeenRegistered=false
        f.removeAuthStateListener(authStateListener)
    }
}