package at.htl.android_frontend.camera

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * This entity holds the image and checkpoint data necessary for creating the photobook.
 */
@Parcelize
data class ImageReference(
    val image: String?,
    val uid: Int = 0,
    val cp_id: Long = -1) : Parcelable
{
}