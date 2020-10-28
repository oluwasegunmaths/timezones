package com.ease.timezones.users

import com.ease.timezones.users.UserAdapter
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ease.timezones.R
import com.ease.timezones.databinding.FragmentUsersBinding
import com.ease.timezones.models.DisplayedUser
import com.ease.timezones.selectedtimezones.TimeZoneViewModel


class UsersFragment : Fragment() {
    private lateinit var binding: FragmentUsersBinding
    private lateinit var adapter: UserAdapter
    val viewModel: UserListViewModel by lazy {
        ViewModelProvider(this).get(UserListViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_users, container, false
        )
        viewModel.isAdmin = UsersFragmentArgs.fromBundle(requireArguments()).isAdmin
        setUpRecycler()
        setUpFAB()
        setHasOptionsMenu(true)
//        viewModel.attachChatDatabaseReadListener()
        Log.i("ooooooo","e")

        viewModel.users.observe(viewLifecycleOwner,{
            Log.i("ooooooo","f")
              if(it==null||it.isEmpty()){
                  Log.i("ooooooo","hhhhhhh")

              }
            adapter.setPatients(it)
            Log.i("ooooooo","g")

        })
//        viewModel.displayedUsers.observe(viewLifecycleOwner, Observer {
//            adapter.setPatients(it)
//            adapter.notifyItemInserted(it.size)
//
//        })
        viewModel.loadingStatus.observe(viewLifecycleOwner, {
            when(it){
                is UserListViewModel.Status.Loading -> showProgresssBarAndLoadingText()
                is UserListViewModel.Status.Loaded -> hideProgressBarAndLoadingText()
                is UserListViewModel.Status.Error -> showErrorLoading(it.message)



            }

//            adapter.setSearchText(it)

        })
        val toolbar = binding.toolbar
        (activity as AppCompatActivity?)?.setSupportActionBar(toolbar)
        return binding.root
    }

    private fun hideProgressBarAndLoadingText() {
        binding.progressBarUserList.visibility= View.GONE
        binding.textViewUserListMessage.visibility= View.GONE

    }

    private fun showErrorLoading(message: String) {
        binding.progressBarUserList.visibility= View.GONE
        binding.textViewUserListMessage.visibility= View.VISIBLE
        binding.textViewUserListMessage.text= message

    }

    private fun showProgresssBarAndLoadingText() {
        binding.progressBarUserList.visibility= View.VISIBLE
        binding.textViewUserListMessage.visibility= View.VISIBLE
        binding.textViewUserListMessage.text= "Loading"


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
        adapter = UserAdapter( object : UserAdapter.ItemClickListener {
            override fun onItemClick(user: DisplayedUser?) {
                user?.run {
                    findNavController().navigate(
                            UsersFragmentDirections.actionUsersFragmentToAddEditOrDeleteUserFragment(
                                    user,
                                    viewModel.isAdmin
                            )
                    )
                }
            }

        })
        binding.recycler.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_admins, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_signout) {
            viewModel.signOut()
            findNavController().navigate(UsersFragmentDirections.actionUsersFragmentToLoginFragment())
            return true
        }
        return super.onOptionsItemSelected(item)
    }


}