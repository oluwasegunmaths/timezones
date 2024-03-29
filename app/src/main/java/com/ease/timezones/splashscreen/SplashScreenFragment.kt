package com.ease.timezones.splashscreen

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.ease.timezones.R
import com.ease.timezones.Utils
import com.ease.timezones.Utils.isInternetAvailable
import com.ease.timezones.databinding.FragmentSplashScreenBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class SplashScreenFragment : Fragment() {
    val viewModel: SplashScreenViewModel by lazy {
        ViewModelProvider(this).get(SplashScreenViewModel::class.java)

    }

    private lateinit var binding: FragmentSplashScreenBinding

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_splash_screen, container, false
        )
        binding.buttonCheckForInternet.setOnClickListener {
            checkForInternetConnection()
        }
        checkForInternetConnection()
        return binding.root
    }

    private fun checkForInternetConnection() {
        //check if user is connected
        CoroutineScope(Dispatchers.Main).launch {
            val isConnected = isInternetAvailable()

            if (isConnected) {
                binding.progressBarSplashScreen.visibility = View.VISIBLE
                observeViewModel()
                binding.buttonCheckForInternet.visibility = View.GONE

            } else {
                binding.progressBarSplashScreen.visibility = View.GONE
                binding.textViewSplashScreen.text = "Network required to use app"
                Toast.makeText(
                        requireContext(),
                        "Not connected or poor connection",
                        Toast.LENGTH_LONG
                ).show()
                binding.buttonCheckForInternet.visibility = View.VISIBLE
//            makeTryAgainButtonVisible()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.authenticationState.observe(viewLifecycleOwner, Observer { authenticationState ->
          Log.i("pppppppp","1")
            when (authenticationState) {
                SplashScreenViewModel.AuthenticationState.UNAUTHENTICATED -> {
                    Log.i("pppppppp","2")

                    findNavController().navigate(SplashScreenFragmentDirections.actionSplashScreenFragmentToLoginFragment())
//                    viewModel.authenticationState.removeObservers(this)

                }
                SplashScreenViewModel.AuthenticationState.NOTEMAILVERIFIED -> {
                    Toast.makeText(
                            requireContext(),
                            "Check your mail to verify your login",
                            Toast.LENGTH_SHORT
                    ).show()
                    findNavController().navigate(SplashScreenFragmentDirections.actionSplashScreenFragmentToLoginFragment())

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
    }

    private fun openAppAsNormalUser(uid: String) {
        Log.i("pppppppp","3")

        findNavController().navigate(
                SplashScreenFragmentDirections.actionSplashScreenFragmentToTimeZoneFragment(
                        uid
                )
        )
    }

    private fun openAppAsAdmin(isAdmin: Boolean) {
        Log.i("pppppppp","4")

        findNavController().navigate(
                SplashScreenFragmentDirections.actionSplashScreenFragmentToUsersFragment(
                        isAdmin
                )
        )
    }
}