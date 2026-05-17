package edu.cit.caones.splitshare.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import edu.cit.caones.splitshare.MainActivity
import edu.cit.caones.splitshare.R
import edu.cit.caones.splitshare.SessionManager
import edu.cit.caones.splitshare.network.RetrofitClient
import edu.cit.caones.splitshare.network.dto.ApiResponse
import edu.cit.caones.splitshare.network.dto.RegisterRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
        tilFirstName        = findViewById(R.id.tilFirstName)
        tilLastName         = findViewById(R.id.tilLastName)
        tilEmail            = findViewById(R.id.tilEmail)
        tilPassword         = findViewById(R.id.tilPassword)
        tilConfirmPassword  = findViewById(R.id.tilConfirmPassword)
        etFirstName         = findViewById(R.id.etFirstName)
        etLastName          = findViewById(R.id.etLastName)
        etEmail             = findViewById(R.id.etEmail)
        etPassword          = findViewById(R.id.etPassword)
        etConfirmPassword   = findViewById(R.id.etConfirmPassword)
        btnRegister         = findViewById(R.id.btnRegister)
        btnGoToLogin        = findViewById(R.id.btnGoToLogin)
        btnBack             = findViewById(R.id.btnBack)
        tvError             = findViewById(R.id.tvError)
    }

    private fun setupListeners() {
        btnBack.setOnClickListener { finish() }
        btnGoToLogin.setOnClickListener { finish() }
        btnRegister.setOnClickListener { attemptRegister() }
    }

    private fun attemptRegister() {
        val firstName       = etFirstName.text?.toString()?.trim() ?: ""
        val lastName        = etLastName.text?.toString()?.trim() ?: ""
        val email           = etEmail.text?.toString()?.trim() ?: ""
        val password        = etPassword.text?.toString() ?: ""
        val confirmPassword = etConfirmPassword.text?.toString() ?: ""

        clearErrors()

        // Client-side validation first
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

        setLoading(true)

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.api.register(
                        RegisterRequest(
                            firstname = firstName,
                            lastname  = lastName,
                            email     = email,
                            password  = password
                        )
                    )
                }

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        SessionManager.saveSession(body.data)
                        goToDashboard()
                    } else {
                        showError(body?.error?.message ?: "Registration failed.")
                    }
                } else {
                    handleErrorBody(response.errorBody()?.string())
                }

            } catch (e: Exception) {
                showError("Cannot reach server. Make sure the backend is running.")
            } finally {
                setLoading(false)
            }
        }
    }

    /**
     * Backend returns VALID-001 with a details map of field -> message.
     * Map each field error back to the right TextInputLayout.
     */
    private fun handleErrorBody(raw: String?) {
        if (raw.isNullOrBlank()) { showError("Registration failed."); return }
        try {
            val type    = object : TypeToken<ApiResponse<Unit>>() {}.type
            val parsed  = Gson().fromJson<ApiResponse<Unit>>(raw, type)
            val err     = parsed.error

            if (err?.code == "VALID-001") {
                // details is a LinkedTreeMap<String,String> from Gson
                @Suppress("UNCHECKED_CAST")
                val details = err.details as? Map<String, String>
                if (details != null) {
                    details["firstname"]?.let { tilFirstName.error = it }
                    details["lastname"]?.let  { tilLastName.error  = it }
                    details["email"]?.let     { tilEmail.error     = it }
                    details["password"]?.let  { tilPassword.error  = it }
                    return
                }
            }

            // AUTH-002 = duplicate email
            if (err?.code == "AUTH-002") {
                tilEmail.error = err.message ?: "Email already in use"
                return
            }

            showError(err?.message ?: "Registration failed.")
        } catch (e: Exception) {
            showError("Registration failed.")
        }
    }

    private fun clearErrors() {
        tilFirstName.error       = null
        tilLastName.error        = null
        tilEmail.error           = null
        tilPassword.error        = null
        tilConfirmPassword.error = null
        tvError.visibility       = View.GONE
    }

    private fun showError(message: String) {
        tvError.text = message
        tvError.visibility = View.VISIBLE
    }

    private fun setLoading(loading: Boolean) {
        btnRegister.isEnabled = !loading
        btnRegister.text = if (loading) "Creating account..." else getString(R.string.sign_up)
    }

    private fun goToDashboard() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}
