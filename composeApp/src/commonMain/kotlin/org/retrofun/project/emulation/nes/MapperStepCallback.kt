package org.retrofun.project.emulation.nes

interface MapperStepCallback {
  fun onStep(
      register: Int,
      registers: IntArray,
      prgMode: Int,
      chrMode: Int,
      prgOffsets: IntArray,
      chrOffsets: IntArray,
      reload: Int,
      counter: Int,
      irqEnable: Boolean
  )
}