package org.retrofun.project.domain.model

enum class ConsoleType {
    NES,
    SNES
}

enum class RomSourceType {
    INTERNAL,
    USER_FILE
}

data class Game(
    val id: String,
    val name: String,
    val console: ConsoleType,
    val romSourceType: RomSourceType,
    val romPath: String // ex: "roms/demo_nes_game.nes"
)
