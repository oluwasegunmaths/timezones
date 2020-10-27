package com.ease.timezones.crudtimezone

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.fragment.app.Fragment
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.ease.timezones.R
import com.ease.timezones.Utils.convertToRealTimeZone
import com.ease.timezones.Utils.convertToViewerFriendlyTimeZone
import com.ease.timezones.Utils.getHourMinuteString
import com.ease.timezones.databinding.FragmentAddEditOrDeleteTimeZoneBinding
import com.ease.timezones.models.DisplayedTime
import com.ease.timezones.models.SelectedTime

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
    val viewModel: AddEditOrDeleteTimeZoneViewModel by lazy {
        ViewModelProvider(this).get(AddEditOrDeleteTimeZoneViewModel::class.java)

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_add_edit_or_delete_time_zone, container, false
        )
        viewModel.populateAdapter.observe(viewLifecycleOwner,  {
            if(it){
                Log.i("uuuuuuu","1")
                setUpSpinner()
            }
        })
        viewModel.isSpinnerVisible.observe(viewLifecycleOwner,  {
            if(it){
                Log.i("uuuuuuuu","2")

                binding.textViewSpinnerOverlay.visibility=GONE
                binding.locationSpinner.isClickable=true
                binding.locationSpinner.visibility= VISIBLE
            }
        })
//        val timezones = TimeZone.getAvailableIDs()
//        val minutes = TimeZone.getDefault().rawOffset / 60000

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

    private fun setUpSpinner() {
        Log.i("uuuuuuuu","3")

        ArrayAdapter(requireContext(),android.R.layout.simple_spinner_item,viewModel.timeZones).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            binding.locationSpinner.adapter = adapter
        }
        binding.locationSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        Log.i("uuuuuuuu","4")

                        val zone = parent?.getItemAtPosition(position) as String
                        offset = TimeZone.getTimeZone(convertToRealTimeZone(zone)).rawOffset.toLong()
                        offset?.let {
                            binding.textView.text = getHourMinuteString(it)
                        }
                    }
                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }

    }

    private fun populateViews(displayedTime: DisplayedTime) {
        Log.i("uuuuuuuu","5")

        binding.floatingActionButtonDeleteTimezone.show()
        binding.floatingActionButtonDeleteTimezone.setOnClickListener {
                showWarningDialog()
        }
        binding.editTextTextPersonName.setText(displayedTime.name)
        binding.textView.setText(displayedTime.browserOffset)
        binding.textViewSpinnerOverlay.setText(convertToViewerFriendlyTimeZone(displayedTime.location))
        binding.textViewSpinnerOverlay.setOnClickListener {
            Log.i("uuuuuuuu","6")

            viewModel.makeSpinnerVisible()
        }
    }

    private fun showWarningDialog() {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setMessage("Are you sure you want to permanently delete this timezone?")
            .setPositiveButton("Ok") { dialog, id ->
                deleteTimeZone()
                dialog.dismiss()
            } .setNegativeButton("Cancel") { dialog, id ->
                dialog.dismiss()
            }
        val alert = dialogBuilder.create()
        alert.setTitle("Warning")
        alert.show()
    }

    private fun deleteTimeZone() {
        val key = displayedTime!!.fireBaseKey
        userDR?.child(key)?.removeValue()?.addOnCompleteListener {
            if(it.isSuccessful){
                Toast.makeText(requireContext(), "Successfully deleted", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }else{
                Toast.makeText(requireContext(), "Unable to delete timezone due to ${it.exception?.message?:"an unknown problem"}", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        displayedTime = AddEditOrDeleteTimeZoneFragmentArgs.fromBundle(requireArguments()).displayedTime
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

            }
            val realZone=convertToRealTimeZone(binding.locationSpinner.selectedItem.toString())
            ref?.setValue(
                SelectedTime(
                    binding.editTextTextPersonName.text.toString(),
                    realZone,
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

//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        displayedTime?.let { inflater.inflate(R.menu.menu_time_zone_detail, menu) }
//        super.onCreateOptionsMenu(menu, inflater)
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        if (item.itemId == R.id.action_delete) {
////            this::mFirebaseDatabase.isInitialized
//            val key = displayedTime!!.fireBaseKey
//            userDR?.child(key)?.removeValue()
//            return true
//        }
//        return super.onOptionsItemSelected(item)
//    }

}