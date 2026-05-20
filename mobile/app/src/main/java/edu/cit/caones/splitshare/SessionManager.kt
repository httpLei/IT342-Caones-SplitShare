package edu.cit.caones.splitshare

import android.content.Context
import android.content.SharedPreferences
import edu.cit.caones.splitshare.model.User
import edu.cit.caones.splitshare.network.dto.AuthData
import java.text.NumberFormat
import java.util.Currency

object SessionManager {

    // Base amounts from backend are treated as PHP and converted for display.
    private const val PHP_PER_USD = 61.37
    private const val PHP_PER_EUR = 69.00

    private const val PREF_NAME = "splitshare_session"
    private const val KEY_ACCESS_TOKEN  = "access_token"
    private const val KEY_REFRESH_TOKEN = "refresh_token"
    private const val KEY_USER_ID       = "user_id"
    private const val KEY_FIRST_NAME    = "first_name"
    private const val KEY_LAST_NAME     = "last_name"
    private const val KEY_EMAIL         = "email"
    private const val KEY_ROLE          = "role"
    private const val KEY_CURRENCY      = "currency"
    
    private const val KEY_THEME         = "theme"
    private const val KEY_CATEGORIES    = "categories"

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
            putString(KEY_CURRENCY,      authData.user.currency ?: "PHP")
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
            putString(KEY_CURRENCY,     user.currency)
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
            role      = prefs.getString(KEY_ROLE, "ROLE_USER") ?: "ROLE_USER",
            currency  = prefs.getString(KEY_CURRENCY, "PHP") ?: "PHP"
        )
    }

    fun updateUserProfile(firstName: String, lastName: String, email: String, currency: String) {
        prefs.edit().apply {
            putString(KEY_FIRST_NAME, firstName)
            putString(KEY_LAST_NAME,  lastName)
            putString(KEY_EMAIL,      email)
            putString(KEY_CURRENCY,   currency)
            apply()
        }
    }
    
    fun getTheme(): String {
        return prefs.getString(KEY_THEME, "System") ?: "System"
    }

    fun getCurrencyCode(): String {
        return prefs.getString(KEY_CURRENCY, "PHP") ?: "PHP"
    }

    fun formatCurrency(amount: Double): String {
        val code = getCurrencyCode()
        val converted = when (code.uppercase()) {
            "USD" -> amount / PHP_PER_USD
            "EUR" -> amount / PHP_PER_EUR
            else -> amount
        }

        val fmt = NumberFormat.getCurrencyInstance()
        try {
            fmt.currency = Currency.getInstance(code)
        } catch (_: Exception) {
        }
        return fmt.format(converted)
    }
    
    fun saveTheme(theme: String) {
        prefs.edit().putString(KEY_THEME, theme).apply()
    }
    
    fun getSelectedCategories(): Set<String> {
        val defCategories = setOf("Food", "Transport", "Entertainment", "Utilities", "Shopping", "Healthcare", "Education", "Other")
        return prefs.getStringSet(KEY_CATEGORIES, defCategories) ?: defCategories
    }
    
    fun saveSelectedCategories(categories: Set<String>) {
        prefs.edit().putStringSet(KEY_CATEGORIES, categories).apply()
    }

    fun isLoggedIn(): Boolean = getToken() != null

    fun logout() {
        prefs.edit().clear().apply()
    }
}