package com.ease.timezones

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.ease.timezones.models.DisplayedUser
import com.ease.timezones.models.User
import java.lang.Exception
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

    fun gettimeString(time: Long): String {
        val format = SimpleDateFormat("HH:mm")
        format.setTimeZone(TimeZone.getTimeZone("GMT"))
        val sign = if (time > -1) "+"
        else {
            "-"
        }
        return "GMT $sign" + format.format(Date(time))
    }
    fun getHourMinuteString(time: Long): String {
        val sign = if (time > -1) "+" else { "-" }
        val abstime=Math.abs(time)
        val hours= abstime/3600000
//        hours=Math.abs(hours)
        val minutes=(abstime%3600000)/60000
        var hourString:String=hours.toString()
        if(hours<10)hourString="0$hourString"
        var minuteString:String=minutes.toString()
        if(minutes<10)minuteString="0$minuteString"
        return "GMT $sign$hourString:$minuteString"
    }
    fun endsProperly(email: String): Boolean {
        if (email.length < 5) return false
//        Log.d(TAG, "isValidDomain: verifying email has correct domain: $email")
        val domain = email.substring(email.length - 4).toLowerCase(Locale.ROOT)
//        Log.d(TAG, "isValidDomain: users domain: $domain")
        return domain.equals( PROPER_ENDING)
    }

    fun showToast(message: String, context: Context) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun isThereConnection(): Boolean {
        return true;
    }

     fun <T> isEmptyOrNull (livedata:  T?): Boolean {
         if(livedata==null&&livedata is String){
             return true
         }else if(livedata==null){
             throw Exception("Utils method isemptyornull should only have mutablelivedata of string as parameter")
         }
        if(livedata is MutableLiveData<*>) {
            if (livedata.value is String){
            return livedata.value == null || livedata.value == ""}
            else{
                throw Exception("Utils method isemptyornull should only have mutablelivedata of string as parameter")
            }
        }else if(livedata is String){
            return  livedata==null||livedata.equals("")
        }else{
            throw Exception("Utils method isemptyornull should only have mutablelivedata or string parameters")
        }

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
    fun convertToRealTimeZone(zone: String): String {
        val index= zone.indexOf(',')
        return zone.substring(index+2)+"/"+zone.substring(0,index)
    }
     fun convertToViewerFriendlyTimeZone(zone: String?): String {
        if(zone==null)return ""
        return zone.run {
            val index= indexOf('/')
            substring(index+1)+", "+substring(0,index)}
    }
}