package com.bountybugger

import android.app.Application
import android.content.Context

/**
 * BountyBugger Application Class
 * Main entry point for the vulnerability testing toolbox
 */
class BountyBuggerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        initializeApp()
    }

    private fun initializeApp() {
        // Initialize any app-wide configurations here
        // Set up crash handling, analytics, etc.
    }

    companion object {
        private lateinit var instance: BountyBuggerApp

        fun getInstance(): BountyBuggerApp = instance

        fun getAppContext(): Context = instance.applicationContext
    }
}
