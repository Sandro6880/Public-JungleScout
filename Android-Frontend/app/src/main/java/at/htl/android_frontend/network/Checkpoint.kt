package at.htl.android_frontend.network

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Checkpoint(
        val id: Long,
        val cpLocation: String,
        val name: String,
        val gpsLng: String,
        val gpsLat: String,
        val details: String
) : Parcelable {

}