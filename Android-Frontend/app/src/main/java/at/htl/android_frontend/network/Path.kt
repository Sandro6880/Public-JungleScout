package at.htl.android_frontend.network

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Path(
        val id: Long,
        val name: String,
        val pathLength: String,
        val pathDuration: String,
        val checkpoints: List<Checkpoint>
): Parcelable {

}