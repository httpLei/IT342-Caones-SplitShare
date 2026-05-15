package edu.cit.caones.splitshare

import android.content.Context
import android.content.SharedPreferences
import edu.cit.caones.splitshare.model.User

object SessionManager {

    private const val PREF_NAME = "splitshare_session"
    private const val KEY_TOKEN = "access_token"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_FIRST_NAME = "first_name"
    private const val KEY_LAST_NAME = "last_name"
    private const val KEY_EMAIL = "email"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveSession(token: String, user: User) {
        prefs.edit().apply {
            putString(KEY_TOKEN, token)
            putString(KEY_USER_ID, user.id)
            putString(KEY_FIRST_NAME, user.firstName)
            putString(KEY_LAST_NAME, user.lastName)
            putString(KEY_EMAIL, user.email)
            apply()
        }
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun getCurrentUser(): User? {
        val firstName = prefs.getString(KEY_FIRST_NAME, null) ?: return null
        return User(
            id = prefs.getString(KEY_USER_ID, "") ?: "",
            firstName = firstName,
            lastName = prefs.getString(KEY_LAST_NAME, "") ?: "",
            email = prefs.getString(KEY_EMAIL, "") ?: ""
        )
    }

    fun isLoggedIn(): Boolean = getToken() != null

    fun logout() {
        prefs.edit().clear().apply()
    }
}
