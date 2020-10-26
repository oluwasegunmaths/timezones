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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.ease.timezones.R
import com.ease.timezones.Utils
import com.ease.timezones.Utils.ADMIN
import com.ease.timezones.Utils.HAS_LOGGED_IN
import com.ease.timezones.Utils.MANAGER
import com.ease.timezones.Utils.RC_SIGN_IN
import com.ease.timezones.Utils.ROLE
import com.ease.timezones.Utils.USER
import com.ease.timezones.Utils.getRole
import com.ease.timezones.Utils.showToast
import com.ease.timezones.databinding.FragmentLoginBinding
import com.ease.timezones.splashscreen.SplashScreenFragmentDirections
import com.ease.timezones.splashscreen.SplashScreenViewModel
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
    private lateinit var binding: FragmentLoginBinding
    private val viewModel: SplashScreenViewModel by lazy {
        ViewModelProvider(this).get(SplashScreenViewModel::class.java)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_login, container, false
        )
        Log.i("lllllllll", "2")

        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        viewModel.authenticationState.observe(viewLifecycleOwner, { authenticationState ->
            when (authenticationState) {
                SplashScreenViewModel.AuthenticationState.NOTEMAILVERIFIED -> {
                    binding.resetPassword.setText(R.string.didnt_get_link)
                    binding.resetPassword.visibility = View.VISIBLE
                    binding.resetPassword.setOnClickListener {
                        viewModel.sendEmailVerification()
                    }
                    showToast("Check your mail to verify your login", requireContext())
//                    viewModel.authenticationState.removeObservers(this)
                }
            }
        })
        viewModel.role.observe(viewLifecycleOwner, Observer {
            when (it) {
                is SplashScreenViewModel.Role.Admin -> openAppAsAdmin(true)
                is SplashScreenViewModel.Role.Manager -> openAppAsAdmin(false)
                is SplashScreenViewModel.Role.NormalUser -> openAppAsNormalUser(it.uid)
            }
        })
        viewModel.showResetPassword.observe(viewLifecycleOwner, Observer {
            if (it) {
                binding.emailSigninEdittext.error = "Check the spelling of your email"
                binding.emailSigninPasswordEdittext.error = getString(R.string.forgot_password)
                binding.resetPassword.text = getString(R.string.forgot_password)
                binding.resetPassword.visibility = View.VISIBLE
                binding.resetPassword.setOnClickListener {
                    viewModel.resetPassword()
                }
            }
        })
        viewModel.showLoginProgressBar.observe(viewLifecycleOwner, Observer {
            if (it) {
                binding.progressBar.visibility = View.VISIBLE
            } else {
                binding.progressBar.visibility = View.GONE
            }
        })
        viewModel.toastMessage.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {
                Utils.showToast(it, requireContext())
                viewModel.onToastDisplayed()
            }
        })
        setupRegisterButtonListener()
        setUpGoogleSignInButton()
        return binding.root
    }

    private fun openAppAsNormalUser(uid: String) {
        Log.i("lllllllll", "1")

        findNavController().navigate(
                LoginFragmentDirections.actionLoginFragmentToTimeZoneFragment(uid)
        )
    }

    private fun openAppAsAdmin(isAdmin: Boolean) {
        findNavController().navigate(
                LoginFragmentDirections.actionLoginFragmentToUsersFragment(isAdmin)
        )
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
                    viewModel.firebaseAuthWithGoogle(account.idToken)
                }
            } catch (e: ApiException) {
                showToast("signInResult:failed code=" + e.message, requireContext())
            }
        }
    }
    private fun setupRegisterButtonListener() {
        binding.registerButton.setOnClickListener { v ->
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }
}