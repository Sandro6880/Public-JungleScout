package at.htl.junglescout.network

data class Checkpoint(
    val id: Long,
    val cpLocation: String,
    val name: String,
    val gpsLng: String,
    val gpsLat: String,
    val details: String
)