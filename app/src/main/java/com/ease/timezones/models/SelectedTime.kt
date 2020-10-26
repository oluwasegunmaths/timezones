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
    constructor() : this(null, null, null)


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

    fun asDisplayedTime(browserTime: Long, key: String): DisplayedTime {
        val offset = gmtoffset ?: 0
        val currentTime = getHourMinuteString(browserTime + offset)
        val hourMinute = getHourMinuteString(offset)
        return DisplayedTime(
            name = name,
            location = location,
            currentTime = currentTime,
            browserOffset = hourMinute,
            fireBaseKey = key
        )

    }
}

//@Entity
//data class DatabaseVideo constructor(
//        @PrimaryKey
//        val url: String,
//        val updated: String,
//        val title: String,
//        val description: String,
//        val thumbnail: String)
//fun List<DatabaseVideo>.asDomainModel(): List<Video> {
//    return map {
//        Video (
//                url = it.url,
//                title = it.title,
//                description = it.description,
//                updated = it.updated,
//                thumbnail = it.thumbnail)
//    }
//}


