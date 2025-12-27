package org.retrofun.project

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform