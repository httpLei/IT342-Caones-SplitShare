package edu.cit.caones.splitshare.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import edu.cit.caones.splitshare.MainActivity
import edu.cit.caones.splitshare.R
import edu.cit.caones.splitshare.SessionManager
import edu.cit.caones.splitshare.model.User

class RegisterActivity : AppCompatActivity() {

    private lateinit var tilFirstName: TextInputLayout
    private lateinit var tilLastName: TextInputLayout
    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var tilConfirmPassword: TextInputLayout
    private lateinit var etFirstName: TextInputEditText
    private lateinit var etLastName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etConfirmPassword: TextInputEditText
    private lateinit var btnRegister: MaterialButton
    private lateinit var btnGoToLogin: MaterialButton
    private lateinit var btnBack: ImageButton
    private lateinit var tvError: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        bindViews()
        setupListeners()
    }

    private fun bindViews() {
        tilFirstName = findViewById(R.id.tilFirstName)
        tilLastName = findViewById(R.id.tilLastName)
        tilEmail = findViewById(R.id.tilEmail)
        tilPassword = findViewById(R.id.tilPassword)
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword)
        etFirstName = findViewById(R.id.etFirstName)
        etLastName = findViewById(R.id.etLastName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnRegister = findViewById(R.id.btnRegister)
        btnGoToLogin = findViewById(R.id.btnGoToLogin)
        btnBack = findViewById(R.id.btnBack)
        tvError = findViewById(R.id.tvError)
    }

    private fun setupListeners() {
        btnBack.setOnClickListener { finish() }
        btnGoToLogin.setOnClickListener { finish() }
        btnRegister.setOnClickListener { attemptRegister() }
    }

    private fun attemptRegister() {
        val firstName = etFirstName.text?.toString()?.trim() ?: ""
        val lastName = etLastName.text?.toString()?.trim() ?: ""
        val email = etEmail.text?.toString()?.trim() ?: ""
        val password = etPassword.text?.toString() ?: ""
        val confirmPassword = etConfirmPassword.text?.toString() ?: ""

        clearErrors()

        var valid = true
        if (firstName.isEmpty()) { tilFirstName.error = "Required"; valid = false }
        if (lastName.isEmpty()) { tilLastName.error = "Required"; valid = false }
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.error = "Enter a valid email"; valid = false
        }
        if (password.length < 8) {
            tilPassword.error = "At least 8 characters"; valid = false
        }
        if (password != confirmPassword) {
            tilConfirmPassword.error = "Passwords do not match"; valid = false
        }
        if (!valid) return

        btnRegister.isEnabled = false
        btnRegister.text = "Creating account..."

        // TODO: Replace with real Retrofit call to POST /auth/register
        android.os.Handler(mainLooper).postDelayed({
            val mockUser = User(
                id = "user-002",
                firstName = firstName,
                lastName = lastName,
                email = email
            )
            SessionManager.saveSession("mock-jwt-token", mockUser)
            startActivity(Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
        }, 800)
    }

    private fun clearErrors() {
        tilFirstName.error = null
        tilLastName.error = null
        tilEmail.error = null
        tilPassword.error = null
        tilConfirmPassword.error = null
        tvError.visibility = View.GONE
    }
}
