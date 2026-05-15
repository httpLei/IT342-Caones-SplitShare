package edu.cit.caones.splitshare.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import edu.cit.caones.splitshare.MainActivity
import edu.cit.caones.splitshare.R
import edu.cit.caones.splitshare.SessionManager
import edu.cit.caones.splitshare.model.User

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

        // Skip login if already authenticated
        if (SessionManager.isLoggedIn()) {
            goToDashboard()
            return
        }

        setContentView(R.layout.activity_login)
        bindViews()
        setupListeners()
    }

    private fun bindViews() {
        tilEmail = findViewById(R.id.tilEmail)
        tilPassword = findViewById(R.id.tilPassword)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnSignIn = findViewById(R.id.btnSignIn)
        btnGoToRegister = findViewById(R.id.btnGoToRegister)
        tvError = findViewById(R.id.tvError)
    }

    private fun setupListeners() {
        btnSignIn.setOnClickListener { attemptLogin() }
        btnGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun attemptLogin() {
        val email = etEmail.text?.toString()?.trim() ?: ""
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

        btnSignIn.isEnabled = false
        btnSignIn.text = "Signing in..."

        // TODO: Replace this stub with a real Retrofit API call to POST /auth/login
        // For now, simulate a successful login after a short delay
        android.os.Handler(mainLooper).postDelayed({
            val mockUser = User(
                id = "user-001",
                firstName = "Lerah",
                lastName = "Caones",
                email = email
            )
            SessionManager.saveSession("mock-jwt-token", mockUser)
            goToDashboard()
        }, 800)
    }

    private fun clearErrors() {
        tilEmail.error = null
        tilPassword.error = null
        tvError.visibility = View.GONE
    }

    private fun goToDashboard() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
