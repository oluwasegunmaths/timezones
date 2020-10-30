package com.ease.timezones.DisplayedTimezones

import android.content.Context
import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ease.timezones.Utils
import com.ease.timezones.databinding.TimezoneListItemBinding
import com.ease.timezones.models.DisplayedTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import java.util.*

class TimeZonesAdapter internal constructor(
        private val itemClickListener: ItemClickListener?
) : ListAdapter<DisplayedTime, TimeZonesAdapter.ViewHolder>(DisplayedTimeDiffCallback()) {

    private var searchText: String? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: TimezoneListItemBinding =
                TimezoneListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun setSearchText(searchString: String?) {
        searchText = searchString
    }

    interface ItemClickListener {
        fun onItemClick(displayedTime: DisplayedTime?)
    }

    inner class ViewHolder internal constructor(val binding: TimezoneListItemBinding) :
            RecyclerView.ViewHolder(binding.getRoot()), View.OnClickListener {

        var time: DisplayedTime? = null

        override fun onClick(view: View) {
            itemClickListener?.onItemClick(time)
        }

        fun bind(displayedTime: DisplayedTime) {
            time = displayedTime
            binding.timeZoneNameTv.setText(displayedTime.name)
            binding.timeZoneLocTv.setText(Utils.convertToViewerFriendlyTimeZone(displayedTime.location))
            binding.timeZoneTimeTv.setTimeZone(displayedTime.location)
            binding.timeZoneTimeTv.format12Hour="hh:mm:ss a"
            binding.timeZoneTimeTv.format24Hour=null
            binding.timeZoneOffsetTv.setText(displayedTime.browserOffset)
            if (!searchText.isNullOrEmpty()) {
                highlightString(binding.timeZoneNameTv)
                highlightString(binding.timeZoneLocTv)
            }
        }

        init {
            binding.getRoot().setOnClickListener(this)
        }
    }

    //helper method to colour the search item yellow as appropriate
    private fun highlightString(textView: TextView) {
//Get the text from text view and create a spannable string
        val spannableString = SpannableString(textView.text)
        //Get the previous spans and remove them
        val backgroundSpans = spannableString.getSpans(
                0, spannableString.length,
                BackgroundColorSpan::class.java
        )
        for (span in backgroundSpans) {
            spannableString.removeSpan(span)
        }

//Search for the first occurrence of the keyword in the string
        val ss = spannableString.toString()
        val indexOfKeyword = ss.toLowerCase(Locale.ROOT).indexOf(searchText!!.toLowerCase(Locale.ROOT))
        if (indexOfKeyword >= 0) {
            //Create a background color span on the keyword
            spannableString.setSpan(
                    BackgroundColorSpan(Color.YELLOW),
                    indexOfKeyword,
                    indexOfKeyword + searchText!!.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

//Set the final text on TextView
        textView.text = spannableString
    }
}

class DisplayedTimeDiffCallback : DiffUtil.ItemCallback<DisplayedTime>() {
    override fun areItemsTheSame(oldItem: DisplayedTime, newItem: DisplayedTime): Boolean {
        return oldItem.fireBaseKey == newItem.fireBaseKey
    }

    override fun areContentsTheSame(oldItem: DisplayedTime, newItem: DisplayedTime): Boolean {
        return oldItem.equals(newItem)
    }
}