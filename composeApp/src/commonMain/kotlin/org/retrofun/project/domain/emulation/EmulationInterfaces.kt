package org.retrofun.project.domain.emulation

data class VideoFrame(
    val width: Int,
    val height: Int,
    val pixels: IntArray // ARGB
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as VideoFrame
        if (width != other.width) return false
        if (height != other.height) return false
        if (!pixels.contentEquals(other.pixels)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = width
        result = 31 * result + height
        result = 31 * result + pixels.contentHashCode()
        return result
    }
}

data class ControllerState(
    val up: Boolean = false,
    val down: Boolean = false,
    val left: Boolean = false,
    val right: Boolean = false,
    val a: Boolean = false,
    val b: Boolean = false,
    val start: Boolean = false,
    val select: Boolean = false
)

interface EmulatorEngine {
    fun init(romBytes: ByteArray)
    fun reset()
    fun setControllerState(state: ControllerState)
    fun runFrame(): VideoFrame
    fun release()
}
