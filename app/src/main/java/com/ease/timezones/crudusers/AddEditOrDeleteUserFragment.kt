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
import com.ease.timezones.databinding.FragmentAddEditOrDeleteUserBinding
import com.ease.timezones.models.DisplayedUser
import com.ease.timezones.models.User
import com.google.android.gms.tasks.Task
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
            Log.i("aaaaaa", "2")


        }
        setHasOptionsMenu(true)
        return binding.root
    }

    private fun populateViews(user: DisplayedUser) {
        binding.edittextUserEmail.setText(user.email)
        binding.edittextUserEmail.inputType = InputType.TYPE_NULL
        binding.edittextUserPassword.setText(user.password)
        binding.edittextUsername.setText(user.displayName)
        val isAdmin = AddEditOrDeleteUserFragmentArgs.fromBundle(requireArguments()).isAdmin
        if (isAdmin) {
            binding.buttonShowUserTimezones.visibility = View.VISIBLE
            setUpUserTimeZoneButtonListener()
        }
        binding.floatingActionButtonDeleteUser.show()
        binding.floatingActionButtonDeleteUser.setOnClickListener {

            showWarningDialog()
        }

    }
    private fun showWarningDialog() {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setMessage("Are you sure you want to permanently delete this user?")
            .setPositiveButton("Ok") { dialog, id ->
                deleteUser()
                dialog.dismiss()
            } .setNegativeButton("Cancel") { dialog, id ->
                dialog.dismiss()
            }
        val alert = dialogBuilder.create()
        alert.setTitle("Warning")
        alert.show()
    }

    private fun deleteUser() {
        CoroutineScope(Dispatchers.Main).launch {
            val isConnected = Utils.isInternetAvailable()

            if (isConnected) {
                val key = user.authId
                userDR.child(key).removeValue().addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(requireContext(), "Successfully deleted", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    } else {
                        Toast.makeText(requireContext(), "Unable to delete user due to ${it.exception?.message ?: "an unknown problem"}", Toast.LENGTH_SHORT).show()
                    }
                }.addOnCanceledListener {
                    Toast.makeText(requireContext(), "Delete was cancelled", Toast.LENGTH_SHORT).show()

                }
            } else {
                Utils.showToast("No or poor network. Action cant be completed", requireContext())

            }
        }
    }
    private fun setUpUserTimeZoneButtonListener() {
        binding.buttonShowUserTimezones.setOnClickListener {
            findNavController().navigate(
                AddEditOrDeleteUserFragmentDirections.actionAddEditOrDeleteUserFragmentToTimeZoneFragment(
                    user.authId
                )
            )
        }

    }

    private fun setUpSaveButton() {
        Log.i("aaaaaa", "0")

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
                    Log.i("aaaaaa", "1")
                    createUser()
                } else {
                    Toast.makeText(requireContext(), "Password should be at least 6 characters long", Toast.LENGTH_SHORT).show()

                }
            } else {
                Toast.makeText(requireContext(), "The email is invalid", Toast.LENGTH_SHORT).show()

            }
        } else {
            Toast.makeText(requireContext(), "You didnt fill all the fields", Toast.LENGTH_SHORT).show()
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
                        Toast.makeText(requireContext(), "User creation successful, could take some seconds to reflect", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()

                    } else {
                        Toast.makeText(requireContext(), "Action was not successful", Toast.LENGTH_SHORT).show()
                        tryingToCommunicate = false

                    }
                }.addOnCanceledListener {
                    Toast.makeText(requireContext(), "Action could not be completed", Toast.LENGTH_SHORT).show()
                    tryingToCommunicate = false

                }
            } else {
                Utils.showToast("No or poor network. Action cant be completed", requireContext())
            }
        }
    }

    private fun verifyInputsThenUpdateUser() {
        if (!isEmptyOrNull(binding.edittextUsername.text.toString()) && !isEmptyOrNull(binding.edittextUserPassword.text.toString())) {
            if (binding.edittextUserPassword.text.toString().length > 5) {
                val key = user.authId
                updateUser(key)

            } else {
                Toast.makeText(requireContext(), "Password should be at least 6 characters long", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "You didnt fill all the fields", Toast.LENGTH_SHORT).show()

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
                        Toast.makeText(requireContext(), "Update was successful", Toast.LENGTH_SHORT).show()

                    } else {
                        Toast.makeText(requireContext(), "Action was not successful", Toast.LENGTH_SHORT).show()
                        tryingToCommunicate = false

                    }
                }.addOnCanceledListener {
                    Toast.makeText(requireContext(), "Action could not be completed", Toast.LENGTH_SHORT).show()
                    tryingToCommunicate = false

                }
            } else {
                Utils.showToast("No or poor network. Action cant be completed", requireContext())
            }
        }
    }

//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        if (::user.isInitialized) inflater.inflate(R.menu.menu_time_zone_detail, menu)
//        super.onCreateOptionsMenu(menu, inflater)
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        if (item.itemId == R.id.action_delete) {
////            this::mFirebaseDatabase.isInitialized
//            val key = user.authId
//            userDR.child(key).removeValue()
////            key?.let {
////
////            }
////            userDR?.child(key)?.removeValue()
//            return true
//        }
//        return super.onOptionsItemSelected(item)
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tempUser = AddEditOrDeleteUserFragmentArgs.fromBundle(requireArguments()).user
        tempUser?.let {
            user = it
        }
    }
}