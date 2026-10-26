package at.htl.junglescout.network

data class Path(
    val id: Long,
    val name: String,
    val pathLength: String,
    val pathDuration: String,
    val checkpoints: List<Checkpoint>
)