package com.ease.timezones

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.ease.timezones.models.DisplayedUser
import com.ease.timezones.models.User
import java.text.SimpleDateFormat
import java.util.*

object Utils {
    const val HAS_LOGGED_IN: String = "has logged in"
    const val RC_SIGN_IN: Int = 100
    const val PROPER_ENDING: String = ".com"
    const val PARCEL: String = "parcel"
    const val USER: String = "user"
    const val ADMIN: String = "admin"
    const val MANAGER: String = "manager"
    const val ROLE: String = "role"

    fun hasLoggedIn(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences(context.packageName, MODE_PRIVATE)
        return sharedPreferences.getBoolean(HAS_LOGGED_IN, false)
    }

    fun getRole(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences(context.packageName, MODE_PRIVATE)
        return sharedPreferences.getString(HAS_LOGGED_IN, null)
    }

    fun getHourMinuteString(time: Long): String {
        val format = SimpleDateFormat("H:m")
        format.setTimeZone(TimeZone.getTimeZone("GMT"))
        val sign = if (time > -1) "+"
        else {
            "-"
        }
        return sign + format.format(Date(time))
    }

    fun endsProperly(email: String): Boolean {
        if (email.length < 5) return false
//        Log.d(TAG, "isValidDomain: verifying email has correct domain: $email")
        val domain = email.substring(email.length - 4).toLowerCase()
//        Log.d(TAG, "isValidDomain: users domain: $domain")
        return domain == PROPER_ENDING
    }

    fun showToast(message: String, context: Context) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun isThereConnection(): Boolean {
        return true;
    }

    fun isEmptyOrNull(livedata: MutableLiveData<String>): Boolean {
        return livedata.value == null || livedata.value == ""
    }
     fun Map<String, User>.asDisplayedUsers(): MutableList<DisplayedUser> {
        val displayedUsers= mutableListOf<DisplayedUser>()
         forEach { (key, value) ->
             val displayedUser= DisplayedUser(
                 value.displayName,
                 key,
                 value.email,
                 value.password
             )
             displayedUsers.add(displayedUser)
          }
//        for ((key, value) in this) {
//            val displayedUser= DisplayedUser(
//                value.displayName,
//                key,
//                value.email,
//                value.password
//            )
//            displayedUsers.add(displayedUser)
//        }
        return displayedUsers

    }
}