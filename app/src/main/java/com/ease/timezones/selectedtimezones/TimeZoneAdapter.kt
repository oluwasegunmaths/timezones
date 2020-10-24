package com.ease.timezones.DisplayedTimezones

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ease.timezones.models.DisplayedTime
import com.ease.timezones.databinding.TimezoneListItemBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

class TimeZonesAdapter internal constructor(
        private val context: Context,
        private val itemClickListener: ItemClickListener?
) :

        RecyclerView.Adapter<TimeZonesAdapter.ViewHolder>() {

    private var viewModelJob = Job()


    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var displayedTimeList: List<DisplayedTime>? = null
    private var searchText: String? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        LayoutInflater inflater = LayoutInflater.from(context);
//        View view = inflater.inflate(R.layout.patient_list_item, parent, false);
        val binding: TimezoneListItemBinding =
            TimezoneListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(displayedTimeList!![position])
    }

    override fun getItemCount(): Int {
        return if (null == displayedTimeList) 0 else displayedTimeList!!.size
    }

    fun setDisplayedTimes(displayedTimes: MutableList<DisplayedTime>) {
        if (viewModelJob.isActive) viewModelJob.cancel()

        displayedTimeList = displayedTimes
        notifyDataSetChanged()
    }

    fun setSearchText(searchString: String?) {
        searchText = searchString
    }

    interface ItemClickListener {
        fun onItemClick(displayedTime: DisplayedTime?)
    }

    inner class ViewHolder internal constructor(val binding: TimezoneListItemBinding) :
            RecyclerView.ViewHolder(binding.getRoot()), View.OnClickListener {
        //        var hospitalNumber: TextView? = null
//        var binding: JoggingEntryListItemBinding
        override fun onClick(view: View) {
            itemClickListener?.onItemClick(displayedTimeList!![adapterPosition])
        }

        fun bind(displayedTime: DisplayedTime) {
            binding.timeZoneNameTv.setText(displayedTime.name)
            binding.timeZoneLocTv.setText(displayedTime.location)
            binding.timeZoneTimeTv.setText(displayedTime.currentTime)
            binding.timeZoneOffsetTv.setText(displayedTime.browserOffset)


        }

        init {
            //            binding = JoggingEntryListItemBinding.bind(itemView);
            binding.getRoot().setOnClickListener(this)
//            this.binding = binding
        }
    }
}