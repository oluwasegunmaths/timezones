package com.ease.timezones.selectedtimezones

import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ease.timezones.R
import com.ease.timezones.SelectedTime
import com.ease.timezones.User
import com.ease.timezones.Utils.ROLE
import com.ease.timezones.databinding.FragmentTimeZoneBinding
import com.ease.timezones.databinding.FragmentUsersBinding
import com.ease.timezones.users.SelectedTimesAdapter
import com.ease.timezones.users.UsersFragmentDirections
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class TimeZoneFragment : Fragment() {

    private lateinit var binding: FragmentTimeZoneBinding
    private lateinit var adapter: TimeZonesAdapter
    private lateinit var mFirebaseDatabase: FirebaseDatabase
    private lateinit var mFirebaseAuth: FirebaseAuth
    private  var userDR: DatabaseReference?=null
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding= DataBindingUtil.inflate(
                inflater, R.layout.fragment_time_zone, container, false
        )
        mFirebaseDatabase = FirebaseDatabase.getInstance()
        mFirebaseAuth = FirebaseAuth.getInstance()
        userDR = mFirebaseDatabase.getReference().child("timezones").child(mFirebaseAuth.uid!!)
        setUpRecycler()

        attachChatDatabaseReadListener()
        setUpFAB()
        setHasOptionsMenu(true)
        return binding.root
    }
    private fun setUpFAB() {
        binding.floatingActionButton.setOnClickListener { v ->
            findNavController().navigate(TimeZoneFragmentDirections.actionTimeZoneFragmentToAddOrEditTimeZoneFragment(mFirebaseAuth.uid!!))
        }
    }

    private fun setUpRecycler() {
        binding.recycler.layoutManager = LinearLayoutManager(this.requireContext())
        adapter = TimeZonesAdapter(this.requireContext(), object : TimeZonesAdapter.ItemClickListener{
            override fun onItemClick(selectedTime: SelectedTime?) {
                selectedTime?.run {
                    findNavController().navigate(TimeZoneFragmentDirections.actionTimeZoneFragmentToAddOrEditTimeZoneFragment(mFirebaseAuth.uid!!))
                }
            }

        })
        binding.recycler.adapter = adapter
    }

    private fun attachChatDatabaseReadListener() {
        val selectedTimes= mutableListOf<SelectedTime>()
        val mChatChildEventListener: ChildEventListener = object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                val selectedTime = dataSnapshot.getValue(SelectedTime::class.java)
                selectedTime?.let {
                    selectedTimes.add(it)
                    adapter.setPatients(selectedTimes)
                    adapter.notifyItemInserted(selectedTimes.size)
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
        inflater.inflate(R.menu.menu_users, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_signout) {
            FirebaseAuth.getInstance().signOut()
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.requireContext())
            val editor = sharedPreferences.edit()
                editor.putString(ROLE, null)
            editor.apply()
            findNavController().navigate(TimeZoneFragmentDirections.actionTimeZoneFragmentToLoginFragment())
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}