package com.ease.timezones.crudusers

import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.ease.timezones.R
import com.ease.timezones.Utils
import com.ease.timezones.Utils.endsProperly
import com.ease.timezones.Utils.isEmptyOrNull
import com.ease.timezones.Utils.showToast
import com.ease.timezones.databinding.FragmentAddEditOrDeleteUserBinding
import com.ease.timezones.models.DisplayedUser
import com.ease.timezones.models.User
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddEditOrDeleteUserFragment : Fragment() {
    private lateinit var binding: FragmentAddEditOrDeleteUserBinding
    private lateinit var mFirebaseDatabase: FirebaseDatabase
    private lateinit var userDR: DatabaseReference
    private lateinit var user: DisplayedUser
    private var tryingToCommunicate: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_add_edit_or_delete_user, container, false
        )
        mFirebaseDatabase = FirebaseDatabase.getInstance()
        userDR = mFirebaseDatabase.getReference().child("admincreatedusers")
        setUpSaveButton()
        if (::user.isInitialized) {
            populateViews(user)
            userDR = mFirebaseDatabase.getReference().child("users")
        }
        setHasOptionsMenu(true)
        return binding.root
    }

    private fun populateViews(user: DisplayedUser) {
        binding.textViewCrudUsersTitle.text = getString(R.string.edit_this_user)
        binding.edittextUserEmail.setText(user.email)
        binding.edittextUserEmail.inputType = InputType.TYPE_NULL
        binding.edittextUserPassword.setText(user.password)
        binding.edittextUsername.setText(user.displayName)
        val isAdmin = AddEditOrDeleteUserFragmentArgs.fromBundle(requireArguments()).isAdmin
        if (isAdmin) {
            setUpEmailClickListener()
        }
        binding.floatingActionButtonDeleteUser.show()
        binding.floatingActionButtonDeleteUser.setOnClickListener {
            showWarningDialog()
        }
    }

    private fun showWarningDialog() {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setMessage(getString(R.string.delete_user_warning))
                .setPositiveButton("Ok") { dialog, id ->
                    deleteUser()
                    dialog.dismiss()
                }.setNegativeButton(getString(R.string.cancal)) { dialog, id ->
                    dialog.dismiss()
                }
        val alert = dialogBuilder.create()
        alert.setTitle(getString(R.string.warning))
        alert.show()
    }

    private fun deleteUser() {
        CoroutineScope(Dispatchers.Main).launch {
            val isConnected = Utils.isInternetAvailable()
            if (isConnected) {
                val key = user.authId
                userDR.child(key).removeValue().addOnCompleteListener {
                    if (it.isSuccessful) {
                        context?.let {
                            showToast("Successfully deleted", it)
                        }
                        findNavController().popBackStack()
                    } else {
                        showToast("Unable to delete user due to ${it.exception?.message ?: "an unknown problem"}", context)
                    }
                }.addOnCanceledListener {
                    context?.let {
                        showToast("Delete was cancelled", it)
                    }

                }
            } else {
                showToast("No or poor network. Action cant be completed", requireContext())
            }
        }
    }

    private fun setUpEmailClickListener() {
        binding.edittextUserEmail.setOnClickListener {
            findNavController().navigate(
                    AddEditOrDeleteUserFragmentDirections.actionAddEditOrDeleteUserFragmentToTimeZoneFragment(
                            user.authId
                    )
            )
        }
    }

    private fun setUpSaveButton() {
        binding.buttonSaveUser.setOnClickListener {
            if (!tryingToCommunicate) {
                if (::user.isInitialized) {
                    verifyInputsThenUpdateUser()
                } else {
                    verifyInputsThenCreateUser()
                }
            }
        }
    }

    private fun verifyInputsThenCreateUser() {
        if (!isEmptyOrNull(binding.edittextUsername.text.toString()) && !isEmptyOrNull(binding.edittextUserPassword.text.toString())
                && !isEmptyOrNull(binding.edittextUserEmail.text.toString())) {
            if (endsProperly(binding.edittextUserEmail.text.toString())) {
                if (binding.edittextUserPassword.text.toString().length > 5) {
                    createUser()
                } else {
                    context?.let {
                        showToast("Password should be at least 6 characters long", it)
                    }
                }
            } else {
                context?.let {
                    showToast("The email is invalid", it)
                }
            }
        } else {
            context?.let {
                showToast("You didnt fill all the fields", it)
            }
        }
    }

    private fun createUser() {
        CoroutineScope(Dispatchers.Main).launch {
            val isConnected = Utils.isInternetAvailable()
            if (isConnected) {
                tryingToCommunicate = true
                userDR.push().setValue(
                        User(
                                binding.edittextUsername.text.toString(),
                                binding.edittextUserEmail.text.toString(),
                                binding.edittextUserPassword.text.toString()
                        )
                ).addOnCompleteListener {
                    if (it.isSuccessful) {
                        context?.let {
                            showToast("User creation successful, could take some seconds to reflect", it)
                        }
                        findNavController().popBackStack()

                    } else {
                        context?.let {
                            showToast("Action was not successful", it)
                        }
                        tryingToCommunicate = false
                    }
                }.addOnCanceledListener {
                    context?.let {
                        showToast("Action could not be completed", it)
                    }
                    tryingToCommunicate = false
                }
            } else {
                showToast("No or poor network. Action cant be completed", requireContext())
            }
        }
    }

    private fun verifyInputsThenUpdateUser() {
        if (!isEmptyOrNull(binding.edittextUsername.text.toString()) && !isEmptyOrNull(binding.edittextUserPassword.text.toString())) {
            if (binding.edittextUserPassword.text.toString().length > 5) {
                val key = user.authId
                updateUser(key)
            } else {
                context?.let {
                    showToast("Password should be at least 6 characters long", it)
                }
            }
        } else {
            context?.let {
                showToast("You didnt fill all the fields", it)
            }
        }
    }

    private fun updateUser(it: String) {
        CoroutineScope(Dispatchers.Main).launch {
            val isConnected = Utils.isInternetAvailable()
            if (isConnected) {
                tryingToCommunicate = true

                userDR.child(it).setValue(
                        User(
                                binding.edittextUsername.text.toString(),
                                binding.edittextUserEmail.text.toString(),
                                binding.edittextUserPassword.text.toString()
                        )
                ).addOnCompleteListener {
                    if (it.isSuccessful) {
                        findNavController().popBackStack()
                        context?.let {
                            showToast("Update was successful", it)
                        }
                    } else {
                        context?.let {
                            showToast("Action was not successful", it)
                        }
                        tryingToCommunicate = false
                    }
                }.addOnCanceledListener {
                    context?.let {
                        showToast("Action could not be completed", it)
                    }
                    tryingToCommunicate = false
                }
            } else {
                context?.let {
                    showToast("No or poor network. Action cant be completed", requireContext())
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tempUser = AddEditOrDeleteUserFragmentArgs.fromBundle(requireArguments()).user
        tempUser?.let {
            user = it
        }
    }
}