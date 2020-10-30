package com.ease.timezones.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(
    var displayName: String?,
    var email: String?,
    var password: String? = null
) : Parcelable {
    //empty constructor required for firebase
    constructor() : this(null, null, null)


}
