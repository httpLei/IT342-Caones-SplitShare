package edu.cit.caones.splitshare

import android.content.Context
import android.content.SharedPreferences
import edu.cit.caones.splitshare.model.User
import edu.cit.caones.splitshare.network.dto.AuthData

object SessionManager {

    private const val PREF_NAME = "splitshare_session"
    private const val KEY_ACCESS_TOKEN  = "access_token"
    private const val KEY_REFRESH_TOKEN = "refresh_token"
    private const val KEY_USER_ID       = "user_id"
    private const val KEY_FIRST_NAME    = "first_name"
    private const val KEY_LAST_NAME     = "last_name"
    private const val KEY_EMAIL         = "email"
    private const val KEY_ROLE          = "role"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    /** Save a session from a real API AuthData response */
    fun saveSession(authData: AuthData) {
        prefs.edit().apply {
            putString(KEY_ACCESS_TOKEN,  authData.accessToken)
            putString(KEY_REFRESH_TOKEN, authData.refreshToken)
            putString(KEY_FIRST_NAME,    authData.user.firstname)
            putString(KEY_LAST_NAME,     authData.user.lastname)
            putString(KEY_EMAIL,         authData.user.email)
            putString(KEY_ROLE,          authData.user.role ?: "ROLE_USER")
            apply()
        }
    }

    /** Legacy overload kept for backward compat */
    fun saveSession(token: String, user: User) {
        prefs.edit().apply {
            putString(KEY_ACCESS_TOKEN, token)
            putString(KEY_FIRST_NAME,   user.firstName)
            putString(KEY_LAST_NAME,    user.lastName)
            putString(KEY_EMAIL,        user.email)
            putString(KEY_ROLE,         user.role)
            apply()
        }
    }

    fun getToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)

    fun getCurrentUser(): User? {
        val firstName = prefs.getString(KEY_FIRST_NAME, null) ?: return null
        return User(
            id        = prefs.getString(KEY_USER_ID, "") ?: "",
            firstName = firstName,
            lastName  = prefs.getString(KEY_LAST_NAME, "") ?: "",
            email     = prefs.getString(KEY_EMAIL, "") ?: "",
            role      = prefs.getString(KEY_ROLE, "ROLE_USER") ?: "ROLE_USER"
        )
    }

    fun isLoggedIn(): Boolean = getToken() != null

    fun logout() {
        prefs.edit().clear().apply()
    }
}
