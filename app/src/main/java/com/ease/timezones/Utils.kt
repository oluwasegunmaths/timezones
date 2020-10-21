package com.ease.timezones

import android.content.Context
import android.content.Context.MODE_PRIVATE

object Utils {
       const val HAS_LOGGED_IN :String="has logged in"
       const val RC_SIGN_IN :Int=100
       const val PROPER_ENDING :String=".com"
       const val PARCEL :String="parcel"
       const val USER :String="user"
       const val ADMIN :String="admin"
       const val MANAGER :String="manager"
       const val ROLE :String="role"

       fun hasLoggedIn(context: Context):Boolean{
              val sharedPreferences=context.getSharedPreferences(context.packageName,MODE_PRIVATE)
              return sharedPreferences.getBoolean(HAS_LOGGED_IN,false )
       }
       fun getRole(context: Context):String?{
              val sharedPreferences=context.getSharedPreferences(context.packageName,MODE_PRIVATE)
              return sharedPreferences.getString(HAS_LOGGED_IN,null)
       }
}