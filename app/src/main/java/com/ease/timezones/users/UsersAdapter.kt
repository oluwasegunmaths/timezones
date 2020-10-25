//package com.ease.timezones.users

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ease.timezones.databinding.UserListItemBinding
import com.ease.timezones.models.DisplayedUser

class UserAdapter internal constructor(
        private val context: Context,
        private val itemClickListener: ItemClickListener? = null
) :
        RecyclerView.Adapter<UserAdapter.ViewHolder>() {
    private var joggingEntryList: List<DisplayedUser>? = null
    private var searchText: String? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        LayoutInflater inflater = LayoutInflater.from(context);
//        View view = inflater.inflate(R.layout.patient_list_item, parent, false);
        val binding: UserListItemBinding =
                UserListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(joggingEntryList!![position])
    }

    override fun getItemCount(): Int {
        return if (null == joggingEntryList) 0 else joggingEntryList!!.size
    }

    fun setPatients(joggingEntries: List<DisplayedUser>?) {
        joggingEntryList = joggingEntries
        notifyDataSetChanged()
    }

    fun setSearchText(searchString: String?) {
        searchText = searchString
    }

    interface ItemClickListener {
        fun onItemClick(user: DisplayedUser?)
    }

    inner class ViewHolder internal constructor(val binding: UserListItemBinding) :
            RecyclerView.ViewHolder(binding.getRoot()), View.OnClickListener {
        //        var hospitalNumber: TextView? = null
//        var binding: JoggingEntryListItemBinding
        override fun onClick(view: View) {
            itemClickListener?.onItemClick(joggingEntryList!![adapterPosition])
        }

        fun bind(user: DisplayedUser) {
            binding.textviewListItemUsername.setText(user.displayName)
            binding.textviewListItemEmail.setText(user.email)
        }

        init {
            //            binding = JoggingEntryListItemBinding.bind(itemView);
            binding.getRoot().setOnClickListener(this)
//            this.binding = binding
        }
    }
}