package org.retrofun.project.emulation.nes

class AudioBuffer {
  // Use a dynamic list to store samples to ensure we only return what was actually written
  private val samples = mutableListOf<Float>()

  fun write(value: Float) {
    samples.add(value)
  }

  fun drain(): FloatArray {
    val result = samples.toFloatArray()
    samples.clear()
    return result
  }
}