package com.ease.timezones.models

import android.os.Parcelable
import com.ease.timezones.Utils.getHourMinuteString
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SelectedTime(
    var name: String?,
    var location: String?,
    var gmtoffset: Long?
) : Parcelable {
    //empty constructor required for firebase
    constructor() : this(null, null, null)

}



