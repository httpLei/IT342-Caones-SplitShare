package edu.cit.caones.splitshare

import android.app.Application

class SplitShareApp : Application() {
    override fun onCreate() {
        super.onCreate()
        SessionManager.init(this)
    }
}
