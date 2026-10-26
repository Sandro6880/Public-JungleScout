package at.htl.android_frontend.camera

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import retrofit2.http.Body

/**
 * This entity holds the data necessary for creating the photobook in the backend.
 * It has the targets email location and the images that the user took.
 */
@Parcelize
data class Photobook(
    @Body var photos: List<ImageReference>? = null,
    var email: String? = null
): Parcelable {
}