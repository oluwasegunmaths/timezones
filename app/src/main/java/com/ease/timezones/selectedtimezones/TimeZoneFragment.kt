package com.ease.timezones.selectedtimezones

import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ease.timezones.DisplayedTimezones.TimeZonesAdapter
import com.ease.timezones.R
import com.ease.timezones.Utils.ROLE
import com.ease.timezones.databinding.FragmentTimeZoneBinding
import com.ease.timezones.models.DisplayedTime
import com.ease.timezones.models.SelectedTime
import com.ease.timezones.users.UserListViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.functions.FirebaseFunctions
import java.text.SimpleDateFormat
import java.util.*


class TimeZoneFragment : Fragment() {
    private lateinit var viewModel:TimeZoneViewModel
    private lateinit var binding: FragmentTimeZoneBinding
    private lateinit var adapter: TimeZonesAdapter
    private lateinit var mFirebaseDatabase: FirebaseDatabase
    private lateinit var mFirebaseAuth: FirebaseAuth
    private var uid: String? = null

    private var userDR: DatabaseReference? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_time_zone, container, false
        )
        mFirebaseDatabase = FirebaseDatabase.getInstance()
        mFirebaseAuth = FirebaseAuth.getInstance()


            userDR = mFirebaseDatabase.getReference().child("timezones").child(uid!!)


        setUpRecycler()

        setUpFAB()
        setHasOptionsMenu(true)
        viewModel.timeZones.observe(viewLifecycleOwner,{
            Log.i("ooooooo","f")
            if(it==null||it.isEmpty()){
                Log.i("ooooooo","hhhhhhh")

            }
            adapter.setDisplayedTimes(it)
            Log.i("ooooooo","g")

        })
        viewModel.searchText.observe(viewLifecycleOwner,{
            adapter.setSearchText(it)

        })
//        FirebaseFunctions.getInstance().getHttpsCallable("getTime")
//            .call().addOnSuccessListener { httpsCallableResult ->
//                val timestamp = httpsCallableResult.data as Long
//                val format = SimpleDateFormat("M??d??H?m??s??")
//                format.setTimeZone(TimeZone.getTimeZone("GMT"))
//
//                binding.emptyTextView.text = format.format(Date(timestamp))
//                binding.emptyTextView.visibility = VISIBLE
//                attachCDatabaseReadListener(timestamp)
//
//
//            }
//        Remove title bar
//        requestWindowFeature(Window.FEATURE_NO_TITLE)

//Remove notification bar
        //Remove notification bar
//        requireActivity().window.setFlags(
//            WindowManager.LayoutParams.FLAG_FULLSCREEN,
//            WindowManager.LayoutParams.FLAG_FULLSCREEN
//        )
//        val toolbar = binding.toolbarLayman
//
//        (activity as AppCompatActivity?)?.setSupportActionBar(toolbar)
        return binding.root
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        uid = TimeZoneFragmentArgs.fromBundle(requireArguments()).authId
        if(uid==null){
            return
        }
        viewModel =ViewModelProvider(this,ViewModelFactory(requireActivity().application,uid!!)).get(TimeZoneViewModel::class.java)

    }

    private fun setUpFAB() {
        binding.floatingActionButton.setOnClickListener { v ->
            findNavController().navigate(
                TimeZoneFragmentDirections.actionTimeZoneFragmentToAddEditOrDeleteTimeZoneFragment(
                    uid!!,
                    null
                )
            )
        }
    }

    private fun setUpRecycler() {
        binding.recycler.layoutManager = LinearLayoutManager(this.requireContext())
        adapter =
            TimeZonesAdapter(this.requireContext(), object : TimeZonesAdapter.ItemClickListener {
                override fun onItemClick(displayedTime: DisplayedTime?) {
                    displayedTime?.let {
                        findNavController().navigate(
                            TimeZoneFragmentDirections.actionTimeZoneFragmentToAddEditOrDeleteTimeZoneFragment(
                                uid!!,
                                it
                            )
                        )
                    }
                }

            })
        binding.recycler.adapter = adapter
    }

    private fun attachCDatabaseReadListener(browsersTime: Long) {
        val displayedTimes = mutableListOf<DisplayedTime>()
        val mChatChildEventListener: ChildEventListener = object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {

                val key = dataSnapshot.key
                if (key == null) return
                val selectedTime = dataSnapshot.getValue(SelectedTime::class.java)
                selectedTime?.let {
                    displayedTimes.add(it.asDisplayedTime(browsersTime, key))
                    adapter.setDisplayedTimes(displayedTimes)
                    adapter.notifyItemInserted(displayedTimes.size)
                }
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {}
            override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
            override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("Firebase database", databaseError.message)
            }
        }
        userDR!!.addChildEventListener(mChatChildEventListener)
//        patientsDR!!.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                if (!snapshot.exists()) {
//                    binding.emptyTextView.visibility = View.VISIBLE
//                    binding.emptyTextView.text =
//                        getString(R.string.get_started_by_adding_a_new_jogging_time)
//                }
//            }
//
//            override fun onCancelled(databaseError: DatabaseError) {
//                Log.e("Firebase database", databaseError.message)
//            }
//        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_time_zone_list, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_signout) {
            FirebaseAuth.getInstance().signOut()
            findNavController().navigate(TimeZoneFragmentDirections.actionTimeZoneFragmentToLoginFragment())
            return true
        } else if (item.itemId == R.id.action_search) {
            if (binding.toolbarEdittext.visibility == VISIBLE) {
                viewModel.changeSearching(false)
                binding.toolbarEdittext.removeTextChangedListener(viewModel.getTextWatcher())
                binding.toolbarEdittext.visibility = GONE
                binding.toolbarEdittext.setText("")
                item.setIcon(R.drawable.ic_search)
                binding.emptyTextView.visibility = GONE


            } else {
                viewModel.changeSearching(true)
                item.setIcon(R.drawable.ic_up)
                binding.toolbarEdittext.setVisibility(VISIBLE)

                binding.toolbarEdittext.addTextChangedListener(viewModel.getTextWatcher())
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }


}