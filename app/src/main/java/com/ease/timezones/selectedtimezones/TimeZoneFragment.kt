package com.ease.timezones.selectedtimezones

import android.content.res.Configuration
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.ease.timezones.DisplayedTimezones.TimeZonesAdapter
import com.ease.timezones.R
import com.ease.timezones.databinding.FragmentTimeZoneBinding
import com.ease.timezones.models.DisplayedTime
import com.google.firebase.auth.FirebaseAuth

class TimeZoneFragment : Fragment() {
    private lateinit var viewModel:TimeZoneViewModel
    private lateinit var binding: FragmentTimeZoneBinding
    private lateinit var adapter: TimeZonesAdapter
    private lateinit var mFirebaseAuth: FirebaseAuth
    private lateinit var uid: String
    private val args: TimeZoneFragmentArgs by navArgs()
    private var shouldShowSearchIcon: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_time_zone, container, false
        )
        mFirebaseAuth = FirebaseAuth.getInstance()
        setUpRecycler()
        setUpFAB()
        setHasOptionsMenu(true)
        viewModel.timeZones.observe(viewLifecycleOwner, {
            if (it == null || it.isEmpty()) {
                promptUserToAddNewTimeZone()
                return@observe
            }
            if (!shouldShowSearchIcon) {
                shouldShowSearchIcon = true
                requireActivity().invalidateOptionsMenu()
            }
            if (binding.recycler.visibility != VISIBLE) {
                binding.recycler.visibility = VISIBLE
                binding.textViewTimezoneMessage.visibility = GONE
                binding.progressBarTimezones.visibility = GONE
            }
            adapter.submitList(it)
            //redraw the whole recycler to highlight searched text
            if (viewModel.isSearching) {
                adapter.notifyDataSetChanged()
            }
        })
        viewModel.searchText.observe(viewLifecycleOwner, {
            adapter.setSearchText(it)
        })
        viewModel.loadingStatus.observe(viewLifecycleOwner, {
            when (it) {
                is TimeZoneViewModel.Status.Loading -> showProgresssBarAndLoadingText()
                is TimeZoneViewModel.Status.Loaded -> hideProgressBarAndLoadingText()
                is TimeZoneViewModel.Status.Error -> showErrorLoading(it.message)
                is TimeZoneViewModel.Status.NoTimeZones -> promptUserToAddNewTimeZone()
            }
        })
        viewModel.isLoggedIn.observe(viewLifecycleOwner, {
            if (!it) {
                findNavController().navigate(TimeZoneFragmentDirections.actionTimeZoneFragmentToLoginFragment())
            }
        })
        val toolbar = binding.toolbar
        (activity as AppCompatActivity?)?.setSupportActionBar(toolbar)
        (activity as AppCompatActivity?)?.title = "Welcome ${mFirebaseAuth.currentUser?.email}"
        return binding.root
    }

    private fun promptUserToAddNewTimeZone() {
        binding.recycler.visibility= GONE
        binding.progressBarTimezones.visibility= GONE
        binding.textViewTimezoneMessage.visibility= VISIBLE
        if (viewModel.isSearching) {
            binding.textViewTimezoneMessage.text = getString(R.string.no_timezones_match)
        } else {
            binding.textViewTimezoneMessage.text = getString(R.string.no_timezones_yet)
        }
    }

    private fun hideProgressBarAndLoadingText() {
        binding.progressBarTimezones.visibility= GONE
        binding.textViewTimezoneMessage.visibility= GONE
        binding.recycler.visibility= VISIBLE
    }

    private fun showErrorLoading(message: String) {
        binding.progressBarTimezones.visibility= GONE
        binding.textViewTimezoneMessage.visibility= VISIBLE
        binding.textViewTimezoneMessage.text= message
    }

    private fun showProgresssBarAndLoadingText() {
        binding.progressBarTimezones.visibility= VISIBLE
        binding.textViewTimezoneMessage.visibility = VISIBLE
        binding.textViewTimezoneMessage.text = getString(R.string.loading)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        uid = args.authId
        viewModel = ViewModelProvider(this, ViewModelFactory(requireActivity().application, uid)).get(TimeZoneViewModel::class.java)
    }

    private fun setUpFAB() {
        binding.floatingActionButton.setOnClickListener {
            findNavController().navigate(
                    TimeZoneFragmentDirections.actionTimeZoneFragmentToAddEditOrDeleteTimeZoneFragment(uid!!, null)
            )
        }
    }

    private fun setUpRecycler() {
        binding.recycler.layoutManager = LinearLayoutManager(this.requireContext())
        adapter = TimeZonesAdapter(object : TimeZonesAdapter.ItemClickListener {
            override fun onItemClick(displayedTime: DisplayedTime?) {
                displayedTime?.let {
                    findNavController().navigate(
                            TimeZoneFragmentDirections.actionTimeZoneFragmentToAddEditOrDeleteTimeZoneFragment(uid!!, it)
                    )
                }
            }
        })
        binding.recycler.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_time_zone_list, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.action_search).isVisible = shouldShowSearchIcon
        if (shouldShowSearchIcon) {
            if (viewModel.isSearching) {
                changeToolbarToSearchingMode(menu.findItem(R.id.action_search))
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_signout) {
            viewModel.signOut()
            return true
        } else if (item.itemId == R.id.action_search) {
            if (binding.toolbarEdittext.visibility == VISIBLE) {
                viewModel.changeSearching(false)
                binding.toolbarEdittext.addTextChangedListener(null)
                if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    binding.toolbarEdittext.visibility = View.INVISIBLE
                } else {
                    binding.toolbarEdittext.visibility = GONE
                }
                binding.toolbarEdittext.setText("")
                item.setIcon(R.drawable.ic_search)
            } else {
                viewModel.changeSearching(true)
                changeToolbarToSearchingMode(item)
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun changeToolbarToSearchingMode(item: MenuItem) {
        item.setIcon(R.drawable.ic_up)
        binding.toolbarEdittext.visibility = VISIBLE
        binding.toolbarEdittext.addTextChangedListener(getTextWatcher())
    }

    private fun getTextWatcher(): TextWatcher {
        return Watcher()
    }

    inner class Watcher : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (s == null) {
                viewModel.resetTimeZones()
                return
            }
            viewModel.setSearchText(s.toString())
            if (s.isEmpty()) {
                viewModel.resetTimeZones()
                return
            }
            val times = viewModel.displayedTimeZonesCache
            val filteredTimes = mutableListOf<DisplayedTime>()
            times?.run {
                for (t in this) {
                    if (t.name != null && t.location != null) {
                        if (t.name!!.contains(s, true) || t.location!!.contains(s, true)) {
                            filteredTimes.add(t)
                        }
                    }
                }
            }
            viewModel.setTimeZones(filteredTimes)
        }

        override fun afterTextChanged(s: Editable?) {}
    }
}