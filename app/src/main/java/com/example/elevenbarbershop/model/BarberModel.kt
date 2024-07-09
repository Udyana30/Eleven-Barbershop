package com.example.elevenbarbershop.model

import android.os.Parcel
import android.os.Parcelable

data class BarberModel(
    val name: String = "",
    val id: Int = 0,
    val bPict: String = "",
    val loc: String = "",
    val des: String = "",
    val skill: String = "",
    val lg: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeInt(id)
        parcel.writeString(bPict)
        parcel.writeString(loc)
        parcel.writeString(des)
        parcel.writeString(skill)
        parcel.writeString(lg)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<BarberModel> {
        override fun createFromParcel(parcel: Parcel): BarberModel {
            return BarberModel(parcel)
        }

        override fun newArray(size: Int): Array<BarberModel?> {
            return arrayOfNulls(size)
        }
    }
}