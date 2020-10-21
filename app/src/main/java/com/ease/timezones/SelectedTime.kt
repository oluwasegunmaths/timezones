package com.ease.timezones

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SelectedTime(var name: String?,
                        var location: String?,
                        var GMTOffset: String?) : Parcelable {
    constructor():this(null,null,null)



//    @Ignore
//    constructor():this() {
//
//    }

//    constructor(date: String?, distance: String?, duration: String?, hasBeenUploaded: Boolean):this() {
//        this.date = date
//        this.duration = duration
//        this.distance = distance
//        isHasBeenUploaded = hasBeenUploaded
//    }


}

