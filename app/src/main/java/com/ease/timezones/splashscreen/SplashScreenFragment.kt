package com.ease.timezones.splashscreen

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.ease.timezones.R
import com.ease.timezones.databinding.FragmentSplashScreenBinding


class SplashScreenFragment : Fragment() {
    val viewModel: SplashScreenViewModel =
        ViewModelProvider(this).get(SplashScreenViewModel::class.java)

    private lateinit var binding: FragmentSplashScreenBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_splash_screen, container, false
        )

        OpenAppOrLogIn()
//        viewModel.userLiveData.observe(viewLifecycleOwner, Observer {
//
//        })
        viewModel.role.observe(viewLifecycleOwner, Observer {
            when (it) {
                SplashScreenViewModel.Role.Admin() -> openAppAsAdmin(true)
                SplashScreenViewModel.Role.Manager() -> openAppAsAdmin(false)
                else -> openAppAsNormalUser((it as SplashScreenViewModel.Role.NormalUser).uid)

            }
        })
        return binding.root
    }

    private fun openAppAsNormalUser(uid: String) {
        findNavController().navigate(
            SplashScreenFragmentDirections.actionSplashScreenFragmentToTimeZoneFragment(
                uid
            )
        )
    }

    private fun openAppAsAdmin(isAdmin: Boolean) {
        findNavController().navigate(
            SplashScreenFragmentDirections.actionSplashScreenFragmentToUsersFragment(
                isAdmin
            )
        )
    }

    private fun OpenAppOrLogIn() {
        //check if user is connected
        if (isThereConnection()) {
            observeViewModel()
        } else {
            Toast.makeText(
                requireContext(),
                "Network required to use app",
                Toast.LENGTH_LONG
            ).show()
//            makeSignInButtonVisible()

            //            finish()
        }
    }

    private fun isThereConnection(): Boolean {
        return true
    }


    private fun observeViewModel() {

        viewModel.authenticationState.observe(viewLifecycleOwner, Observer { authenticationState ->
            when (authenticationState) {
                SplashScreenViewModel.AuthenticationState.UNAUTHENTICATED -> {
                    findNavController().navigate(SplashScreenFragmentDirections.actionSplashScreenFragmentToLoginFragment())
                    viewModel.authenticationState.removeObservers(this)

                }
                SplashScreenViewModel.AuthenticationState.NOTEMAILVERIFIED -> {
                    Toast.makeText(
                        requireContext(),
                        "Check your mail to verify your login",
                        Toast.LENGTH_SHORT
                    ).show()
                    findNavController().navigate(SplashScreenFragmentDirections.actionSplashScreenFragmentToLoginFragment())

                    viewModel.authenticationState.removeObservers(this)
                }

            }

        })
    }
}