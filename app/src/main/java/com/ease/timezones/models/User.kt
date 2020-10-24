package com.ease.timezones.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(
    var displayName: String?,
    var email: String?,
    var password: String? = null
) : Parcelable {
    constructor() : this(null, null, null)

    fun asDisplayedUser(id: String): DisplayedUser {
        return DisplayedUser(
            displayName = displayName,
            authId = id,
            email = email,
            password = password
        )

    }
}
