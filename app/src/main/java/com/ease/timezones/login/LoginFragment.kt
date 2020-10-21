package com.ease.timezones.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.ease.timezones.R
import com.ease.timezones.Utils.ADMIN
import com.ease.timezones.Utils.HAS_LOGGED_IN
import com.ease.timezones.Utils.MANAGER
import com.ease.timezones.Utils.RC_SIGN_IN
import com.ease.timezones.Utils.ROLE
import com.ease.timezones.Utils.USER
import com.ease.timezones.Utils.getRole
import com.ease.timezones.databinding.FragmentLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.*

class LoginFragment : Fragment() {
    private var shouldOpen: Boolean = false
    private lateinit var binding: FragmentLoginBinding
    private lateinit var con: Context
    private lateinit var navController: NavController
    private lateinit var mFirebaseDatabase: FirebaseDatabase

    private var mAuthListener: FirebaseAuth.AuthStateListener? = null
    private var auth: FirebaseAuth? = null
    private lateinit var adminsDR: DatabaseReference
     private lateinit var managersDR: DatabaseReference


    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding= DataBindingUtil.inflate(
                inflater, R.layout.fragment_login, container, false
        )

        return  binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController=findNavController()
//        if(hasLoggedIn(requireContext())){
        val role=getRole(requireContext())
        role?.let {
            when {
                it.equals(ADMIN)-> navController.navigate(LoginFragmentDirections.actionLoginFragmentToUsersFragment(true))
                it.equals(MANAGER)->navController.navigate(LoginFragmentDirections.actionLoginFragmentToUsersFragment(false))
                it.equals(USER)->navController.navigate(LoginFragmentDirections.actionLoginFragmentToTimeZoneFragment(null))
            }
            return@onViewCreated
        }
//            if () {
//                navController.navigate(R.id.action_loginFragment_to_timeZoneFragment)
//            } else {
//            }
//        }else{
            con=requireContext()
            setupFirebaseAuth()
            setupLogInButtonListener()
            setupRegisterButtonListener()
            setUpGoogleSignInButton()
//        }
    }

    private fun setUpGoogleSignInButton() {
        binding.GoogleLoginButton.setSize(SignInButton.SIZE_STANDARD)
        binding.GoogleLoginButton.setOnClickListener { v ->
            val gso =
                    GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(getString(R.string.default_web_client_id))
                            .requestEmail()
                            .build()
            val mGoogleSignInClient = GoogleSignIn.getClient(this.requireContext(), gso)
            val signInIntent = mGoogleSignInClient.signInIntent
            this.startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    firebaseAuthWithGoogle(account.idToken)
                }
                // Signed in successfully, show authenticated UI.
//                updateUI(account);
            } catch (e: ApiException) {
                // The ApiException status code indicates the detailed failure reason.
                // Please refer to the GoogleSignInStatusCodes class reference for more information.
//                Log.w(TAG, "signInResult:failed code=" + e.message)
                //                updateUI(null);
                Toast.makeText(
                        con,
                        "signInResult:failed code=" + e.message,
                        Toast.LENGTH_SHORT
                ).show()
            }

//            handleSignInResult(task);
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String?) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth!!.signInWithCredential(credential)
                .addOnCompleteListener(this.requireActivity(),
                        OnCompleteListener<AuthResult?> { task ->
                            if (task.isSuccessful) {
                                // Sign in success, update UI with the signed-in user's information
//                                Log.d(TAG, "signInWithCredential:success")
                                val user = auth!!.currentUser
                                shouldOpen = true
//                         openApp(user)
                                //                            updateUI(user);
                            } else {
                                // If sign in fails, display a message to the user.
//                                Log.w(TAG, "signInWithCredential:failure", task.exception)
                                Snackbar.make(
                                        binding.root,
                                        "Authentication Failed.",
                                        Snackbar.LENGTH_SHORT
                                ).show()
                                //                            updateUI(null);
                            }

                            // ...
                        })
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.i("gggggggg", "onattachcontext")

    }

    private fun setupRegisterButtonListener() {
        binding.registerButton.setOnClickListener { v ->
            navController.navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    private fun setupLogInButtonListener() {
        binding.signinButton.setOnClickListener(View.OnClickListener { //check if the fields are filled out
            if (!binding.emailSigninEdittext.getText().toString().equals("")
                    && !binding.emailSigninPasswordEdittext.getText().toString().equals("")
            ) {
//                Log.d(TAG, "onClick: attempting to authenticate.")
                binding.progressBar.setVisibility(View.VISIBLE)
                auth!!.signInWithEmailAndPassword(
                        binding.emailSigninEdittext.getText().toString(),
                        binding.emailSigninPasswordEdittext.getText().toString()
                )
                        .addOnCompleteListener { task ->
                            binding.progressBar.setVisibility(View.GONE)
                            if (!task.isSuccessful) {
                                if (task.exception != null) {
                                    Toast.makeText(
                                            con,
                                            """
                                    Authentication Unsuccessful
                                    ${task.exception!!.message}
                                    """.trimIndent(),
                                            Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                            con,
                                            "Authentication Unsuccessful",
                                            Toast.LENGTH_SHORT
                                    ).show()
                                }
                                showResetPasswordText()
                            }
                        }.addOnFailureListener { e ->
                            Toast.makeText(
                                    con,
                                    """
                            Authentication Failed 
                            ${e.message}
                            """.trimIndent(),
                                    Toast.LENGTH_SHORT
                            ).show()
                            binding.progressBar.setVisibility(View.GONE)
                            showResetPasswordText()
                        }
            } else {
                Toast.makeText(
                        con,
                        "You didn't fill in all the fields.",
                        Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun showResetPasswordText() {
        binding.emailSigninEdittext.setError("")
        binding.emailSigninPasswordEdittext.setError("")
        binding.resetPassword.setText(getString(R.string.forgot_password))
        binding.resetPassword.setVisibility(View.VISIBLE)
        binding.resetPassword.setOnClickListener(View.OnClickListener {
            FirebaseAuth.getInstance()
                    .sendPasswordResetEmail(binding.emailSigninEdittext.getText().toString())
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
//                            Log.d(TAG, "onComplete: Password Reset Email sent.")
                            Toast.makeText(
                                    con, "Password Reset Link Sent to Email",
                                    Toast.LENGTH_SHORT
                            ).show()
                        } else {
//                            Log.d(TAG, "onComplete: No user associated with that email.")
                            Toast.makeText(
                                    con, "No User is Associated with that Email",
                                    Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
        })
    }

    override fun onStart() {
        super.onStart()
        FirebaseAuth.getInstance().addAuthStateListener(mAuthListener!!)
        Log.i("gggggggg", "onStart")

    }

    override fun onStop() {
        super.onStop()
        mAuthListener?.let {

            FirebaseAuth.getInstance().removeAuthStateListener(mAuthListener!!)
        }
        Log.i("gggggggg", "onStop")

    }


    private fun setupFirebaseAuth() {
//        Log.d(TAG, "setupFirebaseAuth: started.")
        auth = FirebaseAuth.getInstance()
        mFirebaseDatabase= FirebaseDatabase.getInstance()
        mAuthListener = FirebaseAuth.AuthStateListener { firebaseAuth: FirebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {

                //check if email is verified
                if (user.isEmailVerified || shouldOpen) {
                    Log.i("nnnnnnnnnn","gothere")
                    openApp(user)
                } else {
                    binding.resetPassword.setText(R.string.didnt_get_link)
                    binding.resetPassword.setVisibility(View.VISIBLE)
                    binding.resetPassword.setOnClickListener(View.OnClickListener {
                        user.sendEmailVerification()
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(
                                                con,
                                                "Sent Verification Email",
                                                Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        Toast.makeText(
                                                con,
                                                "couldn't send email",
                                                Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                    })
                    Toast.makeText(
                            con,
                            "Email is not Verified\nCheck your Inbox",
                            Toast.LENGTH_SHORT
                    ).show()
                    FirebaseAuth.getInstance().signOut()
                }
            } else {
                // User is signed out
                Toast.makeText(
                        con,
                        "onAuthStateChanged:signed_out",
                        Toast.LENGTH_SHORT
                ).show()
//                Log.d(TAG, "onAuthStateChanged:signed_out")
            }
        }
    }

    private fun openApp(user: FirebaseUser?) {

        //this gives every user a unique node on the fireBase tree according to the user id
        Log.i("nnnnnnnnnn","1")

        adminsDR = mFirebaseDatabase.getReference().child("admins").child(user!!.uid)
        managersDR = mFirebaseDatabase.getReference().child("managers").child(user!!.getUid())
        Log.i("nnnnnnnnnn","2")

        adminsDR.addListenerForSingleValueEvent(valueEventListener(user))


    }

    private fun valueEventListener(user:FirebaseUser?,checkingForAdmin:Boolean=true): ValueEventListener {
        Log.i("nnnnnnnnnn","before")

        return object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.i("nnnnnnnnnn","after")

                if (checkingForAdmin) {
                    if (snapshot.exists()) {
                        Log.i("nnnnnnnnnn","3")

                        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(con)
                        val editor = sharedPreferences.edit()
                        editor.putString(ROLE, ADMIN)
                        editor.apply()
//        Log.d(TAG, "onAuthStateChanged:signed_in:" + user!!.uid)
                        Toast.makeText(con,
                                "Authenticated with: " + user?.email, Toast.LENGTH_SHORT)
                                .show()
                        Log.i("dddddddd", findNavController().currentDestination?.label.toString())

                        navController.navigate(LoginFragmentDirections.actionLoginFragmentToUsersFragment(true))
    //                    emptyTextView.setVisibility(View.VISIBLE)
    //                    emptyTextView.setText(getString(R.string.seems_you_have_never_used_this_app))
    //                    notAPreviousUser = true
                    }else{
                        Log.i("nnnnnnnnnn","4")

                        managersDR.addListenerForSingleValueEvent(valueEventListener(user,false))
                    }
                } else {
                    if (snapshot.exists()) {
                        Log.i("nnnnnnnnnn","5")

                        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(con)
                        val editor = sharedPreferences.edit()
                        editor.putString(ROLE, MANAGER)
                        editor.apply()
//        Log.d(TAG, "onAuthStateChanged:signed_in:" + user!!.uid)
                        Toast.makeText(con,
                                "Authenticated with: " + user?.email, Toast.LENGTH_SHORT)
                                .show()
                        Log.i("dddddddd", findNavController().currentDestination?.label.toString())

                        navController.navigate(LoginFragmentDirections.actionLoginFragmentToUsersFragment(false))
                    } else {
                        Log.i("nnnnnnnnnn","6")

                        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(con)
                        val editor = sharedPreferences.edit()
                        editor.putString(ROLE, USER)
                        editor.apply()
//        Log.d(TAG, "onAuthStateChanged:signed_in:" + user!!.uid)
                        Toast.makeText(con,
                                "Authenticated with: " + user?.email, Toast.LENGTH_SHORT)
                                .show()
                        Log.i("dddddddd", findNavController().currentDestination?.label.toString())

                        navController.navigate(LoginFragmentDirections.actionLoginFragmentToTimeZoneFragment(null))
                    }

                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("Firebase database", databaseError.message)
            }
        }
    }
}