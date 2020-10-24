package com.ease.timezones.crudusers

import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.ease.timezones.R
import com.ease.timezones.databinding.FragmentAddEditOrDeleteUserBinding
import com.ease.timezones.models.DisplayedUser
import com.ease.timezones.models.User
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AddEditOrDeleteUserFragment : Fragment() {
    private lateinit var binding: FragmentAddEditOrDeleteUserBinding
    private lateinit var mFirebaseDatabase: FirebaseDatabase
    private lateinit var userDR: DatabaseReference
    private lateinit var user: DisplayedUser

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
            if (::user.isInitialized) {
                val key = user.authId
                key?.let {
                    userDR.child(it).setValue(
                        User(
                            binding.edittextUsername.text.toString(),
                            binding.edittextUserEmail.text.toString(),
                            binding.edittextUserPassword.text.toString()
                        )
                    )
                }

            } else {
                Log.i("aaaaaa", "1")
                userDR.push().setValue(
                    User(
                        binding.edittextUsername.text.toString(),
                        binding.edittextUserEmail.text.toString(),
                        binding.edittextUserPassword.text.toString()
                    )
                ).addOnCompleteListener {
                    if (it.isSuccessful) {
                        findNavController().popBackStack()
                    } else {

                    }
                }.addOnFailureListener {
                    Log.i("aaaaaa", it.toString())
                }

            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (::user.isInitialized) inflater.inflate(R.menu.menu_time_zone_detail, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_delete) {
//            this::mFirebaseDatabase.isInitialized
            val key = user.authId
            userDR.child(key).removeValue()
//            key?.let {
//
//            }
//            userDR?.child(key)?.removeValue()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tempUser = AddEditOrDeleteUserFragmentArgs.fromBundle(requireArguments()).user
        tempUser?.let {
            user = it
        }
    }
}