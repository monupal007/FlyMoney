package com.dimts.kmpprojectdemo

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform