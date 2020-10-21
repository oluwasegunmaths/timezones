package com.ease.timezones.crudtimezone

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.ease.timezones.R
import com.ease.timezones.SelectedTime
import com.ease.timezones.databinding.FragmentAddOrEditTimeZoneBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class AddOrEditTimeZoneFragment : Fragment() {
    private lateinit var binding: FragmentAddOrEditTimeZoneBinding
    private lateinit var mFirebaseDatabase: FirebaseDatabase
    private lateinit var mFirebaseAuth: FirebaseAuth
    private  var userDR: DatabaseReference?=null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        binding= DataBindingUtil.inflate(
            inflater, R.layout.fragment_add_or_edit_time_zone, container, false
        )
        val timezones=TimeZone.getAvailableIDs()
        val minutes=TimeZone.getDefault().rawOffset/60000
        ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            timezones
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            binding.locationSpinner.adapter = adapter
        }
        binding.locationSpinner.onItemSelectedListener=object :AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val zone=parent?.getItemAtPosition(position) as String
                binding.textView.text=(TimeZone.getTimeZone(zone).rawOffset/60000).toString()

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }
        mFirebaseDatabase = FirebaseDatabase.getInstance()
        mFirebaseAuth = FirebaseAuth.getInstance()
        userDR = mFirebaseDatabase.getReference().child("timezones").child( AddOrEditTimeZoneFragmentArgs.fromBundle(requireArguments()).authId)
        setUpBUttonClickListener()
        return binding.root
    }

    private fun setUpBUttonClickListener() {
        binding.button2.setOnClickListener {
            userDR?.push()?.setValue(SelectedTime(binding.editTextTextPersonName.text.toString(),
                                                binding.locationSpinner.selectedItem.toString(),
                                                binding.textView.text.toString()))?.
            addOnCompleteListener{
                   it.addOnSuccessListener {
                       Toast.makeText(requireContext(),"uploaded",Toast.LENGTH_SHORT).show()
                       findNavController().popBackStack()
                   }
            }
        }
    }
}