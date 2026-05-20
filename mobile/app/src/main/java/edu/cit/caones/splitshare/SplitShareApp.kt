package edu.cit.caones.splitshare

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class SplitShareApp : Application() {
    override fun onCreate() {
        super.onCreate()
        SessionManager.init(this)
        
        val theme = SessionManager.getTheme()
        when (theme.lowercase()) {
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
}
