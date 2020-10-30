package com.ease.timezones.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DisplayedTime(
        var name: String?,
        var location: String?,
        var currentTime: String,
        var browserOffset: String?,
        var fireBaseKey: String
) : Parcelable

