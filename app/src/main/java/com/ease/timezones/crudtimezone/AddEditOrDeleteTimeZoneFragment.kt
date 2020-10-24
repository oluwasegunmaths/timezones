package com.ease.timezones.crudtimezone

import android.os.Bundle
import android.preference.PreferenceManager
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.ease.timezones.R
import com.ease.timezones.Utils
import com.ease.timezones.Utils.getHourMinuteString
import com.ease.timezones.databinding.FragmentAddEditOrDeleteTimeZoneBinding
import com.ease.timezones.models.DisplayedTime
import com.ease.timezones.models.SelectedTime
import com.ease.timezones.users.UsersFragmentDirections

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class AddEditOrDeleteTimeZoneFragment : Fragment() {
    private lateinit var binding: FragmentAddEditOrDeleteTimeZoneBinding
    private lateinit var mFirebaseDatabase: FirebaseDatabase
    private lateinit var mFirebaseAuth: FirebaseAuth
    private var userDR: DatabaseReference? = null
    private var offset: Long? = null
    private var displayedTime: DisplayedTime? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_add_edit_or_delete_time_zone, container, false
        )
        val timezones = TimeZone.getAvailableIDs()
        val minutes = TimeZone.getDefault().rawOffset / 60000
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
        binding.locationSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val zone = parent?.getItemAtPosition(position) as String
                    offset = TimeZone.getTimeZone(zone).rawOffset.toLong()
                    offset?.let {
                        binding.textView.text = getHourMinuteString(it)

                    }

                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

            }
        mFirebaseDatabase = FirebaseDatabase.getInstance()
        mFirebaseAuth = FirebaseAuth.getInstance()
        userDR = mFirebaseDatabase.getReference().child("timezones")
            .child(AddEditOrDeleteTimeZoneFragmentArgs.fromBundle(requireArguments()).authId)
        setUpBUttonClickListener()
        displayedTime?.let {
            populateViews(it)
        }
        return binding.root
    }

    private fun populateViews(displayedTime: DisplayedTime) {
        binding.editTextTextPersonName.setText(displayedTime.name)
        binding.textView.setText(displayedTime.browserOffset)

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        displayedTime =
            AddEditOrDeleteTimeZoneFragmentArgs.fromBundle(requireArguments()).displayedTime
    }

    private fun setUpBUttonClickListener() {
        binding.button2.setOnClickListener {
            var ref = userDR
            if (displayedTime == null) {
                ref = userDR?.push()
            } else {
                val key = displayedTime!!.fireBaseKey
//                key?.let {
                ref = userDR?.child(key)

//                }


            }
            ref?.setValue(
                SelectedTime(
                    binding.editTextTextPersonName.text.toString(),
                    binding.locationSpinner.selectedItem.toString(),
                    offset
                )
            )?.addOnCompleteListener {
                it.addOnSuccessListener {
                    Toast.makeText(requireContext(), "uploaded", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        displayedTime?.let { inflater.inflate(R.menu.menu_time_zone_detail, menu) }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_delete) {
//            this::mFirebaseDatabase.isInitialized
            val key = displayedTime!!.fireBaseKey
            userDR?.child(key)?.removeValue()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

}