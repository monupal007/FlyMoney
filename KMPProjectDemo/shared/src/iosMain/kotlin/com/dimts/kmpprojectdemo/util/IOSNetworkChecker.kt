package com.dimts.kmpprojectdemo.util

class IOSNetworkChecker : NetworkChecker {
    override fun isConnected(): Boolean {
        // Simplified, always true; async reachability requires more setup
        return true
    }
}