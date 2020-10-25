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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.ease.timezones.R
import com.ease.timezones.Utils.showToast
import com.ease.timezones.databinding.FragmentRegisterBinding
import com.ease.timezones.splashscreen.SplashScreenViewModel
import com.google.firebase.auth.FirebaseAuth


class RegisterFragment : Fragment() {
    private lateinit var binding: FragmentRegisterBinding
    private val viewModel: SplashScreenViewModel by lazy {
        ViewModelProvider(this.requireActivity()).get(SplashScreenViewModel::class.java)
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_register, container, false
        )
        binding.viewModel = viewModel

        binding.lifecycleOwner = this
        viewModel.registerBackToLogin.observe(viewLifecycleOwner, Observer {
            if (it) {
                findNavController().popBackStack()
                viewModel.onRegisterToLoginCompleted()
            }
        })
        viewModel.toastMessage.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {

                showToast(it, requireContext())
                viewModel.onToastDisplayed()
            }
        })
        viewModel.showLoginProgressBar.observe(viewLifecycleOwner, Observer {
            if (it) {
                binding.progressBar2.visibility = View.VISIBLE
            } else {
                binding.progressBar2.visibility = View.GONE
            }
        })
        return binding.root
    }
}