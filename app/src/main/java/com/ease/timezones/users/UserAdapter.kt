package com.ease.timezones.users

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ease.timezones.databinding.UserListItemBinding
import com.ease.timezones.models.DisplayedUser

class UserAdapter internal constructor(
        private val itemClickListener: ItemClickListener? = null
) : ListAdapter<DisplayedUser, UserAdapter.ViewHolder>(DisplayedUserDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: UserListItemBinding =
                UserListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    interface ItemClickListener {
        fun onItemClick(user: DisplayedUser?)
    }

    inner class ViewHolder internal constructor(val binding: UserListItemBinding) :
        RecyclerView.ViewHolder(binding.getRoot()), View.OnClickListener {
        var user: DisplayedUser? = null
        override fun onClick(view: View) {
            itemClickListener?.onItemClick(user)
        }

        fun bind(displayedUser: DisplayedUser) {
            user = displayedUser
            binding.textviewListItemUsername.setText(displayedUser.displayName)
            binding.textviewListItemEmail.setText(displayedUser.email)
        }

        init {
            binding.getRoot().setOnClickListener(this)
        }
    }
}

class DisplayedUserDiffCallback : DiffUtil.ItemCallback<DisplayedUser>() {
    override fun areItemsTheSame(oldItem: DisplayedUser, newItem: DisplayedUser): Boolean {
        return oldItem.authId == newItem.authId
    }

    override fun areContentsTheSame(oldItem: DisplayedUser, newItem: DisplayedUser): Boolean {
        return oldItem.equals(newItem)
    }
}