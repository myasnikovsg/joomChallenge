package com.hedin.joomchallenge.model

import android.os.Parcel
import android.os.Parcelable

data class GifItem(
        val id: String,
        val title: String?,
        val username: String?,
        val name: String?,
        val twitter: String?,
        val profile: String?,
        val url: String?) : Parcelable {

    constructor(dataItem: DataItem) : this(
            dataItem.id,
            dataItem.title,
            dataItem.user?.username,
            dataItem.user?.display_name,
            dataItem.user?.twitter,
            dataItem.user?.profile_url,
            dataItem.getPreview()?.url
    )


    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(title)
        parcel.writeString(username)
        parcel.writeString(name)
        parcel.writeString(twitter)
        parcel.writeString(profile)
        parcel.writeString(url)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<GifItem> {
        override fun createFromParcel(parcel: Parcel): GifItem {
            return GifItem(parcel)
        }

        override fun newArray(size: Int): Array<GifItem?> {
            return arrayOfNulls(size)
        }
    }
}