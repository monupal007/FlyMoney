package com.dimts.kmpprojectdemo.util

import org.koin.core.annotation.Single

interface NetworkChecker {
    fun isConnected(): Boolean
}