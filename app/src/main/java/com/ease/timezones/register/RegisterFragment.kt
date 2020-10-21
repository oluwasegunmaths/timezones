package com.ease.timezones.register

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.ease.timezones.R
import com.ease.timezones.Utils.PROPER_ENDING
import com.ease.timezones.databinding.FragmentRegisterBinding
import com.google.firebase.auth.FirebaseAuth


class RegisterFragment : Fragment() {
    private lateinit var binding: FragmentRegisterBinding
    private lateinit var con: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding= DataBindingUtil.inflate(
                inflater, R.layout.fragment_register, container, false
        )
        con=requireContext()
        binding.button.setOnClickListener {
            Log.d(
                    TAG,
                    "onClick: attempting to register."
            )

            //check for null valued EditText fields
            if (!binding.editTextEmailAddress.text.toString().equals("")
                    && !binding.editTextPassword.text.toString().equals("")
                    && !binding.editTextPasswordConfirmation.text.toString().equals("")
            ) {

                //check if user has a company email address
                if (endsProperly(binding.editTextEmailAddress.text.toString())) {

                    //check if passwords match
                    if (binding.editTextPassword.text.toString()
                                    .equals(binding.editTextPasswordConfirmation.text.toString())
                    ) {

                        //Initiate registration task
                        registerNewEmail(
                                binding.editTextEmailAddress.text.toString(),
                                binding.editTextPassword.text.toString()
                        )
                    } else {
                        Toast.makeText(
                                con,
                                "Passwords do not Match",
                                Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                            con,
                            "Please Register with Company Email",
                            Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                        con,
                        "You must fill out all the fields",
                        Toast.LENGTH_SHORT
                ).show()
            }
        }
        return  binding.root
    }
    fun registerNewEmail(email: String?, password: String?) {
        binding.progressBar2.setVisibility(View.VISIBLE)
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email!!, password!!)
                .addOnCompleteListener { task ->
                    Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful)
                    if (task.isSuccessful) {
                        Log.d(
                                TAG, "onComplete: AuthState: " + FirebaseAuth.getInstance().currentUser!!
                                .uid
                        )

                        //send email verificaiton
                        sendVerificationEmail()
                        FirebaseAuth.getInstance().signOut()

                        //redirect the user to the login screen
                        redirectLoginScreen()
                    }
                    if (!task.isSuccessful) {
                        Toast.makeText(
                                con,
                                "Unable to Register",
                                Toast.LENGTH_SHORT
                        ).show()
                    }
                    binding.progressBar2.setVisibility(View.GONE)

                    // ...
                }
    }

    private fun sendVerificationEmail() {
        val user = FirebaseAuth.getInstance().currentUser
        user?.sendEmailVerification()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(
                        con,
                        "Sent Verification Email", Toast.LENGTH_SHORT)
                        .show()
            } else {
                Toast.makeText(
                        con,
                        "Couldn't Verification Send Email",
                        Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun endsProperly(email: String): Boolean {
        if (email.length < 5) return false
        Log.d(TAG, "isValidDomain: verifying email has correct domain: $email")
        val domain = email.substring(email.length - 4).toLowerCase()
        Log.d(TAG, "isValidDomain: users domain: $domain")
        return domain == PROPER_ENDING
    }

    /**
     * Redirects the user to the login screen
     */
    private fun redirectLoginScreen() {
        Log.d(TAG, "redirectLoginScreen: redirecting to login screen.")
        findNavController().popBackStack()
    }

    private val TAG = "RegisterActivity"
}