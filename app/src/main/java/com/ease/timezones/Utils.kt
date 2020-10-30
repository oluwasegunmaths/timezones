package com.ease.timezones

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.ease.timezones.models.DisplayedUser
import com.ease.timezones.models.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetAddress
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

    fun getHourMinuteString(time: Long): String {
        val sign = if (time > -1) "+" else { "-" }
        val abstime=Math.abs(time)
        val hours= abstime/3600000
        val minutes=(abstime%3600000)/60000
        var hourString:String=hours.toString()
        if(hours<10)hourString="0$hourString"
        var minuteString:String=minutes.toString()
        if (minutes < 10) minuteString = "0$minuteString"
        return "GMT $sign$hourString:$minuteString"
    }

    fun endsProperly(email: String): Boolean {
        if (email.length < 5) return false
        val domain = email.substring(email.length - 4).toLowerCase(Locale.ROOT)
        return domain.equals(PROPER_ENDING)
    }

    fun showToast(message: String, context: Context?) {
        context?.let {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    fun isEmptyOrNull(string: String?): Boolean {
        if (string == null || string.equals("")) return true
        return false;
    }

    fun convertToRealTimeZone(zone: String): String {
        val index = zone.indexOf(',')
        return zone.substring(index + 2) + "/" + zone.substring(0, index)
    }

    fun convertToViewerFriendlyTimeZone(zone: String?): String {
        if (zone == null) return ""
        return zone.run {
            val index = indexOf('/')
            substring(index + 1) + ", " + substring(0, index)
        }
    }

    suspend fun isInternetAvailable(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val ipAddr: InetAddress = InetAddress.getByName("google.com")
                !ipAddr.equals("")
            } catch (e: Exception) {
                false
            }
        }
    }
}