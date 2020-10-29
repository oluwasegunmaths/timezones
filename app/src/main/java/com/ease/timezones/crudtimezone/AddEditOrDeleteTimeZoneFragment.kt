package com.ease.timezones.crudtimezone

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.ease.timezones.R
import com.ease.timezones.Utils
import com.ease.timezones.Utils.convertToRealTimeZone
import com.ease.timezones.Utils.convertToViewerFriendlyTimeZone
import com.ease.timezones.Utils.getHourMinuteString
import com.ease.timezones.Utils.isEmptyOrNull
import com.ease.timezones.Utils.showToast
import com.ease.timezones.databinding.FragmentAddEditOrDeleteTimeZoneBinding
import com.ease.timezones.models.DisplayedTime
import com.ease.timezones.models.SelectedTime
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
    private var tryingToCommunicate: Boolean = false
//    private var spinnerTouched = false


    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_add_edit_or_delete_time_zone, container, false
        )
        viewModel.populateAdapter.observe(viewLifecycleOwner, {
            if (it) {
                Log.i("uuuuuuu", "1")
                setUpSpinner()
            }
        })
        viewModel.isSpinnerVisible.observe(viewLifecycleOwner, {
            if (it) {
                Log.i("uuuuuuuu", "2")

                binding.textViewSpinnerOverlay.visibility = GONE
                binding.locationSpinner.isClickable = true
                binding.locationSpinner.visibility = VISIBLE
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
        binding.textViewSpinnerOverlay.setOnClickListener {
            Log.i("uuuuuuuu", "6")
            binding.textViewOffset.visibility = View.VISIBLE
//            spinnerTouched=true
            viewModel.makeSpinnerVisible()
        }
        return binding.root
    }

    private fun setUpSpinner() {
        Log.i("uuuuuuuu", "3")

        ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, viewModel.timeZones).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            binding.locationSpinner.adapter = adapter
        }
        displayedTime?.apply {
            //prevents onitemselected from being triggered when th listener is first attached to the spinner
            binding.locationSpinner.setSelection(viewModel.timeZones.indexOf(convertToViewerFriendlyTimeZone(location)))
        }
//        binding.locationSpinner.setSelection(Adapter.NO_SELECTION, false);

//        if(displayedTime==null){
//            binding.locationSpinner.setSelection(1)
////            binding.textView.setText(getHourMinuteString(viewModel.timeZones[1]))
//        }
//        binding.locationSpinner.setOnTouchListener { v, event ->
//            spinnerTouched=true
//            false
//        }
        binding.locationSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
//                if (spinnerTouched) {
                    Log.i("uuuuuuuu", "4")

                    val zone = parent?.getItemAtPosition(position) as String
                    if(isEmptyOrNull(zone))return
                    offset = TimeZone.getTimeZone(convertToRealTimeZone(zone)).rawOffset.toLong()
                    offset?.let {
                        binding.textViewOffset.text = getHourMinuteString(it)
                    }
//                }
//                spinnerTouched=false
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }


    }

    private fun populateViews(displayedTime: DisplayedTime) {
        Log.i("uuuuuuuu", "5")
        binding.textViewCrudTimezoneTitle.text = "Edit This Timezone"
        binding.buttonCrudTimezone.text = "Update"

        binding.floatingActionButtonDeleteTimezone.show()
        binding.floatingActionButtonDeleteTimezone.setOnClickListener {
            showWarningDialog()
        }
        binding.editTextTimezoneName.setText(displayedTime.name)
        binding.textViewOffset.setText(displayedTime.browserOffset)
        binding.textViewOffset.visibility = View.VISIBLE

        binding.textViewSpinnerOverlay.setText(convertToViewerFriendlyTimeZone(displayedTime.location))

    }

    private fun showWarningDialog() {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setMessage("Are you sure you want to permanently delete this timezone?")
                .setPositiveButton("Ok") { dialog, id ->
                    deleteTimeZone()
                    dialog.dismiss()
                }.setNegativeButton("Cancel") { dialog, id ->
                    dialog.dismiss()
                }
        val alert = dialogBuilder.create()
        alert.setTitle("Warning")
        alert.show()
    }

    private fun deleteTimeZone() {
        CoroutineScope(Dispatchers.Main).launch {
            val isConnected = Utils.isInternetAvailable()

            if (isConnected) {
                val key = displayedTime!!.fireBaseKey
                userDR?.child(key)?.removeValue()?.addOnCompleteListener {
                    if (it.isSuccessful) {
                        context?.let {
                            showToast("Successfully deleted", it)
                        }
//                    Toast.makeText(requireContext(), "Successfully deleted", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    } else {
                        Toast.makeText(requireContext(), "Unable to delete timezone due to ${it.exception?.message ?: "an unknown problem"}", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Utils.showToast("No or poor network. Action cant be completed", requireContext())

            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        displayedTime = AddEditOrDeleteTimeZoneFragmentArgs.fromBundle(requireArguments()).displayedTime
    }

    private fun setUpBUttonClickListener() {
        binding.buttonCrudTimezone.setOnClickListener {
            if (!tryingToCommunicate) {
                if (!isEmptyOrNull(binding.editTextTimezoneName.text.toString())) {
                    var ref = userDR

                    if (displayedTime == null) {
                        if (!binding.textViewSpinnerOverlay.isVisible) {
                            ref = userDR?.push()
                        } else {
                            Toast.makeText(requireContext(), "You must select a location", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }
                    } else {
                        if (binding.editTextTimezoneName.text.toString().equals(displayedTime!!.name) && binding.textViewSpinnerOverlay.isVisible) {
                            Toast.makeText(requireContext(), "You have not made any changes", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        } else {
                            ref = userDR?.child(displayedTime!!.fireBaseKey)

                        }

                    }
                    createOrUpdateTimeZone(ref)
                } else {
                    Toast.makeText(requireContext(), "You must input a name and select a location", Toast.LENGTH_SHORT).show()

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

    private fun createOrUpdateTimeZone(ref: DatabaseReference?) {
        CoroutineScope(Dispatchers.Main).launch {
            val isConnected = Utils.isInternetAvailable()
            if (isConnected) {
                tryingToCommunicate = true

                val realZone = convertToRealTimeZone(binding.locationSpinner.selectedItem.toString())
                ref?.setValue(
                        SelectedTime(
                                binding.editTextTimezoneName.text.toString(),
                                realZone,
                                offset
                        )
                )?.addOnCompleteListener {
                    if (it.isSuccessful) {
                        it.addOnSuccessListener {
                            Toast.makeText(requireContext(), "uploaded", Toast.LENGTH_SHORT).show()
                            findNavController().popBackStack()
                        }
                    } else {
                        Toast.makeText(requireContext(), "Unable to complete", Toast.LENGTH_SHORT).show()
                        tryingToCommunicate = false

                    }
                }?.addOnCanceledListener {
                    Toast.makeText(requireContext(), "Action cancelled", Toast.LENGTH_SHORT).show()
                    tryingToCommunicate = false

                }
            } else {
                showToast("No or poor network. Action cant be completed", requireContext())
            }
        }
    }

}