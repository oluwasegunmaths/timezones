package com.ease.timezones.users

import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ease.timezones.R
import com.ease.timezones.models.User
import com.ease.timezones.Utils.ROLE
import com.ease.timezones.databinding.FragmentUsersBinding
import com.ease.timezones.models.DisplayedUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*


class UsersFragment : Fragment() {
    private lateinit var binding: FragmentUsersBinding
    private lateinit var adapter: SelectedTimesAdapter
    private lateinit var mFirebaseDatabase: FirebaseDatabase
    private lateinit var mFirebaseAuth: FirebaseAuth
    private var isAdmin: Boolean = false

    private var userDR: DatabaseReference? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_users, container, false
        )
        mFirebaseDatabase = FirebaseDatabase.getInstance()
        mFirebaseAuth = FirebaseAuth.getInstance()
        userDR = mFirebaseDatabase.getReference().child("users")
        setUpRecycler()
        isAdmin = UsersFragmentArgs.fromBundle(requireArguments()).isAdmin

        attachChatDatabaseReadListener()
        setUpFAB()
        setHasOptionsMenu(true)
        return binding.root
    }

    private fun setUpFAB() {
        binding.floatingActionButton.setOnClickListener { v ->
            findNavController().navigate(
                UsersFragmentDirections.actionUsersFragmentToAddEditOrDeleteUserFragment(
                    null,
                    false
                )
            )
        }
    }

    private fun setUpRecycler() {
        binding.recycler.layoutManager = LinearLayoutManager(this.requireContext())
        adapter = SelectedTimesAdapter(
            this.requireContext(),
            object : SelectedTimesAdapter.ItemClickListener {
                override fun onItemClick(user: DisplayedUser?) {
                    user?.run {
                        findNavController().navigate(
                            UsersFragmentDirections.actionUsersFragmentToAddEditOrDeleteUserFragment(
                                user,
                                isAdmin
                            )
                        )
                    }
                }

            })
        binding.recycler.adapter = adapter
    }

    private fun attachChatDatabaseReadListener() {
        val users = mutableListOf<DisplayedUser>()
//        mFirebaseAuth.
//        var page: ListUsersPage = FirebaseAuth.getInstance().del(null)
//        while (page != null) {
//            for (user in page.getValues()) {
//                System.out.println("User: " + user.getUid())
//            }
//            page = page.getNextPage()
//        }
        val mChatChildEventListener: ChildEventListener = object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                val user = dataSnapshot.getValue(User::class.java)
                val key = dataSnapshot.key
                if (key == null) return
                user?.let {
                    users.add(it.asDisplayedUser(key))
                    adapter.setPatients(users)
                    adapter.notifyItemInserted(users.size)
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
        inflater.inflate(R.menu.menu_admins, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_signout) {
            FirebaseAuth.getInstance().signOut()
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.requireContext())
            val editor = sharedPreferences.edit()
            editor.putString(ROLE, null)
            editor.apply()
            findNavController().navigate(UsersFragmentDirections.actionUsersFragmentToLoginFragment())
            return true
        }
        return super.onOptionsItemSelected(item)
    }


}