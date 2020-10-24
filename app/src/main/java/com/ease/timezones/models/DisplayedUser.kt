package com.ease.timezones.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DisplayedUser(
    var displayName: String?,
    var authId: String,
    var email: String?,
    var password: String? = null
) : Parcelable
