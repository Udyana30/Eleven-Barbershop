package com.example.elevenbarbershop.model

import android.os.Parcel
import android.os.Parcelable

data class GalleryModel(
    val name: String = "",
    val type: String = "",
    val des: String = "",
    val face: String = "",
    val gUrl: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(type)
        parcel.writeString(des)
        parcel.writeString(face)
        parcel.writeString(gUrl)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<GalleryModel> {
        override fun createFromParcel(parcel: Parcel): GalleryModel {
            return GalleryModel(parcel)
        }

        override fun newArray(size: Int): Array<GalleryModel?> {
            return arrayOfNulls(size)
        }
    }
}
