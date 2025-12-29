package org.retrofun.project.emulation.nes

internal data class StepInfo(
    var address: Int,
    var PC: Int,
    var mode: Int
)