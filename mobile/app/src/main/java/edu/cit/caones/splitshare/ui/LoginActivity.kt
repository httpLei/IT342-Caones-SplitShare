package edu.cit.caones.splitshare.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import edu.cit.caones.splitshare.MainActivity
import edu.cit.caones.splitshare.R
import edu.cit.caones.splitshare.SessionManager
import edu.cit.caones.splitshare.network.RetrofitClient
import edu.cit.caones.splitshare.network.dto.LoginRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnSignIn: MaterialButton
    private lateinit var btnGoToRegister: MaterialButton
    private lateinit var tvError: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (SessionManager.isLoggedIn()) {
            goToDashboard()
            return
        }

        setContentView(R.layout.activity_login)
        bindViews()
        setupListeners()
    }

    private fun bindViews() {
        tilEmail        = findViewById(R.id.tilEmail)
        tilPassword     = findViewById(R.id.tilPassword)
        etEmail         = findViewById(R.id.etEmail)
        etPassword      = findViewById(R.id.etPassword)
        btnSignIn       = findViewById(R.id.btnSignIn)
        btnGoToRegister = findViewById(R.id.btnGoToRegister)
        tvError         = findViewById(R.id.tvError)
    }

    private fun setupListeners() {
        btnSignIn.setOnClickListener { attemptLogin() }
        btnGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun attemptLogin() {
        val email    = etEmail.text?.toString()?.trim() ?: ""
        val password = etPassword.text?.toString() ?: ""

        clearErrors()

        var valid = true
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.error = "Please enter a valid email"
            valid = false
        }
        if (password.length < 8) {
            tilPassword.error = "Password must be at least 8 characters"
            valid = false
        }
        if (!valid) return

        setLoading(true)

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.api.login(LoginRequest(email, password))
                }

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        SessionManager.saveSession(body.data)
                        goToDashboard()
                    } else {
                        showError(body?.error?.message ?: "Login failed.")
                    }
                } else {
                    // Parse the error body for a meaningful message
                    val errorMsg = parseErrorBody(response.errorBody()?.string())
                    showError(errorMsg)
                }

            } catch (e: Exception) {
                showError("Cannot reach server. Make sure the backend is running.")
            } finally {
                setLoading(false)
            }
        }
    }

    private fun parseErrorBody(raw: String?): String {
        if (raw.isNullOrBlank()) return "Login failed."
        return try {
            val gson = com.google.gson.Gson()
            val type = object : com.google.gson.reflect.TypeToken<
                edu.cit.caones.splitshare.network.dto.ApiResponse<Unit>>() {}.type
            val parsed = gson.fromJson<edu.cit.caones.splitshare.network.dto.ApiResponse<Unit>>(raw, type)
            parsed.error?.message ?: "Login failed."
        } catch (e: Exception) {
            "Login failed."
        }
    }

    private fun clearErrors() {
        tilEmail.error    = null
        tilPassword.error = null
        tvError.visibility = View.GONE
    }

    private fun showError(message: String) {
        tvError.text = message
        tvError.visibility = View.VISIBLE
    }

    private fun setLoading(loading: Boolean) {
        btnSignIn.isEnabled = !loading
        btnSignIn.text = if (loading) "Signing in..." else getString(R.string.sign_in)
    }

    private fun goToDashboard() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}
