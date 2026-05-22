package edu.cit.caones.splitshare.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import edu.cit.caones.splitshare.R
import edu.cit.caones.splitshare.SessionManager
import edu.cit.caones.splitshare.network.RetrofitClient
import edu.cit.caones.splitshare.network.dto.ChangePasswordRequest
import edu.cit.caones.splitshare.network.dto.UpdateProfileRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsFragment : Fragment() {

    private val currencies = listOf("PHP", "USD", "EUR")
    private val themes = listOf("Light", "Dark", "System")
    private val PREDEFINED_CATEGORIES = arrayOf("Food", "Transport", "Entertainment", "Utilities", "Shopping", "Healthcare", "Education", "Other")

    private lateinit var tvGroupsCount: TextView
    private lateinit var tvFollowersCount: TextView
    private lateinit var tvFollowingCount: TextView

    private lateinit var tvSettingsInitials: TextView
    private lateinit var tvSettingsName: TextView
    private lateinit var tvSettingsEmail: TextView

    private lateinit var llGroups: View
    private lateinit var llFollowers: View
    private lateinit var llFollowing: View

    private lateinit var etFirstName: TextInputEditText
    private lateinit var etLastName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var spinnerCurrency: Spinner
    private lateinit var spinnerTheme: Spinner
    private lateinit var btnSaveProfile: MaterialButton
    private lateinit var btnSavePreferences: MaterialButton
    private lateinit var btnExpenseCategories: MaterialButton

    private lateinit var etCurrentPassword: TextInputEditText
    private lateinit var etNewPassword: TextInputEditText
    private lateinit var etConfirmPassword: TextInputEditText
    private lateinit var btnUpdatePassword: MaterialButton
    private lateinit var btnLogout: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        initViews(view)
        setupListeners()
        loadData()
        return view
    }

    private fun initViews(view: View) {
        tvGroupsCount = view.findViewById(R.id.tvGroupsCount)
        tvFollowersCount = view.findViewById(R.id.tvFollowersCount)
        tvFollowingCount = view.findViewById(R.id.tvFollowingCount)
        
        tvSettingsInitials = view.findViewById(R.id.tvSettingsInitials)
        tvSettingsName = view.findViewById(R.id.tvSettingsName)
        tvSettingsEmail = view.findViewById(R.id.tvSettingsEmail)

        llGroups = view.findViewById(R.id.llGroups)
        llFollowers = view.findViewById(R.id.llFollowers)
        llFollowing = view.findViewById(R.id.llFollowing)

        etFirstName = view.findViewById(R.id.etFirstName)
        etLastName = view.findViewById(R.id.etLastName)
        etEmail = view.findViewById(R.id.etEmail)
        spinnerCurrency = view.findViewById(R.id.spinnerCurrency)
        spinnerTheme = view.findViewById(R.id.spinnerTheme)

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, currencies)
        spinnerCurrency.adapter = adapter

        val themeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, themes)
        spinnerTheme.adapter = themeAdapter

        btnSaveProfile = view.findViewById(R.id.btnSaveProfile)
        btnSavePreferences = view.findViewById(R.id.btnSavePreferences)
        btnExpenseCategories = view.findViewById(R.id.btnExpenseCategories)

        etCurrentPassword = view.findViewById(R.id.etCurrentPassword)
        etNewPassword = view.findViewById(R.id.etNewPassword)
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword)
        btnUpdatePassword = view.findViewById(R.id.btnUpdatePassword)
        btnLogout = view.findViewById(R.id.btnLogout)
        
        // Populate profile info
        val user = SessionManager.getCurrentUser()
        if (user != null) {
            etFirstName.setText(user.firstName)
            etLastName.setText(user.lastName)
            etEmail.setText(user.email)
            
            tvSettingsName.text = "${user.firstName} ${user.lastName}"
            tvSettingsEmail.text = user.email
            val fnameInit = user.firstName.firstOrNull()?.uppercaseChar() ?: ""
            val lnameInit = user.lastName.firstOrNull()?.uppercaseChar() ?: ""
            tvSettingsInitials.text = "$fnameInit$lnameInit"
            
            val currencyIdx = currencies.indexOf(user.currency)
            if (currencyIdx >= 0) {
                spinnerCurrency.setSelection(currencyIdx)
            }
        }
        
        val currentTheme = SessionManager.getTheme()
        val themeIdx = themes.indexOfFirst { it.equals(currentTheme, ignoreCase = true) }
        if (themeIdx >= 0) {
            spinnerTheme.setSelection(themeIdx)
        }
    }

    private fun setupListeners() {
        llGroups.setOnClickListener {
            requireActivity().findViewById<BottomNavigationView>(R.id.bottomNav)?.selectedItemId = R.id.nav_groups
        }
        llFollowers.setOnClickListener {
            val intent = Intent(requireContext(), ConnectionsActivity::class.java).apply {
                putExtra("CONNECTION_TYPE", "followers")
            }
            startActivity(intent)
        }
        llFollowing.setOnClickListener {
            val intent = Intent(requireContext(), ConnectionsActivity::class.java).apply {
                putExtra("CONNECTION_TYPE", "following")
            }
            startActivity(intent)
        }

        btnExpenseCategories.setOnClickListener {
            showCategoriesDialog()
        }

        btnSaveProfile.setOnClickListener {
            updateProfile()
        }
        btnSavePreferences.setOnClickListener {
            updatePreferences()
        }
        btnUpdatePassword.setOnClickListener {
            updatePassword()
        }
        btnLogout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Sign out?")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("Yes") { _, _ ->
                    SessionManager.logout()
                    startActivity(Intent(requireContext(), LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                    requireActivity().finish()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun showCategoriesDialog() {
        val selectedCategories = SessionManager.getSelectedCategories().toMutableSet()
        val checkedItems = BooleanArray(PREDEFINED_CATEGORIES.size) { i ->
            selectedCategories.contains(PREDEFINED_CATEGORIES[i])
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Expense Categories")
            .setMultiChoiceItems(PREDEFINED_CATEGORIES, checkedItems) { _, which, isChecked ->
                val category = PREDEFINED_CATEGORIES[which]
                if (isChecked) {
                    selectedCategories.add(category)
                } else {
                    selectedCategories.remove(category)
                }
            }
            .setPositiveButton("Save") { _, _ ->
                SessionManager.saveSelectedCategories(selectedCategories)
                Toast.makeText(requireContext(), "Categories saved", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun loadData() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val statsRes = RetrofitClient.api.getMyStats()
                if (statsRes.isSuccessful && statsRes.body()?.success == true) {
                    val stats = statsRes.body()?.data
                    withContext(Dispatchers.Main) {
                        tvGroupsCount.text = stats?.groupsCount?.toString() ?: "0"
                        tvFollowersCount.text = stats?.followersCount?.toString() ?: "0"
                        tvFollowingCount.text = stats?.followingCount?.toString() ?: "0"
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updateProfile() {
        val fname = etFirstName.text.toString().trim()
        val lname = etLastName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val currentUser = SessionManager.getCurrentUser()
        val currency = currentUser?.currency ?: "PHP"

        if (fname.isEmpty() || lname.isEmpty() || email.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val emailChanged = !currentUser?.email.equals(email, ignoreCase = true)
        if (emailChanged) {
            AlertDialog.Builder(requireContext())
                .setTitle("Change email?")
                .setMessage("You will be logged out after changing your email. Please sign in again using your new email address.")
                .setPositiveButton("Continue") { _, _ ->
                    performProfileUpdate(fname, lname, email, currency, emailChanged = true)
                }
                .setNegativeButton("Cancel", null)
                .show()
            return
        }

        performProfileUpdate(fname, lname, email, currency, emailChanged = false)
    }

    private fun performProfileUpdate(
        fname: String,
        lname: String,
        email: String,
        currency: String,
        emailChanged: Boolean
    ) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val req = UpdateProfileRequest(fname, lname, email, currency)
                val res = RetrofitClient.api.updateProfile(req)
                withContext(Dispatchers.Main) {
                    if (res.isSuccessful && res.body()?.success == true) {
                        SessionManager.updateUserProfile(fname, lname, email, currency)
                        if (emailChanged) {
                            Toast.makeText(requireContext(), "Email updated. Please sign in again.", Toast.LENGTH_SHORT).show()
                            logoutAfterEmailChange()
                        } else {
                            refreshProfileHeader(fname, lname, email)
                            Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        if (emailChanged) {
                            val message = res.body()?.error?.message ?: "Unable to update email."
                            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                        } else {
                            SessionManager.updateUserProfile(fname, lname, email, currency)
                            Toast.makeText(requireContext(), "Saved locally (Server update failed)", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (emailChanged) {
                        Toast.makeText(requireContext(), "Cannot update email while offline.", Toast.LENGTH_SHORT).show()
                    } else {
                        SessionManager.updateUserProfile(fname, lname, email, currency)
                        Toast.makeText(requireContext(), "Saved locally (Offline)", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun refreshProfileHeader(fname: String, lname: String, email: String) {
        tvSettingsName.text = "$fname $lname"
        tvSettingsEmail.text = email
        val fnameInit = fname.firstOrNull()?.uppercaseChar() ?: ""
        val lnameInit = lname.firstOrNull()?.uppercaseChar() ?: ""
        tvSettingsInitials.text = "$fnameInit$lnameInit"
    }

    private fun logoutAfterEmailChange() {
        SessionManager.logout()
        startActivity(Intent(requireContext(), LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        requireActivity().finish()
    }

    private fun updatePreferences() {
        val currency = spinnerCurrency.selectedItem.toString()
        val theme = spinnerTheme.selectedItem.toString()
        val user = SessionManager.getCurrentUser()
        
        SessionManager.saveTheme(theme)
        when (theme.lowercase()) {
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
        
        if (user != null) {
            val fname = user.firstName
            val lname = user.lastName
            val email = user.email

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val req = UpdateProfileRequest(fname, lname, email, currency)
                    val res = RetrofitClient.api.updateProfile(req)
                    withContext(Dispatchers.Main) {
                        if (res.isSuccessful && res.body()?.success == true) {
                            SessionManager.updateUserProfile(fname, lname, email, currency)
                            Toast.makeText(requireContext(), "Preferences updated", Toast.LENGTH_SHORT).show()
                        } else {
                            SessionManager.updateUserProfile(fname, lname, email, currency)
                            Toast.makeText(requireContext(), "Saved locally (Server update failed)", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        SessionManager.updateUserProfile(fname, lname, email, currency)
                        Toast.makeText(requireContext(), "Saved locally (Offline)", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            Toast.makeText(requireContext(), "Theme saved", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updatePassword() {
        val currentPwd = etCurrentPassword.text.toString()
        val newPwd = etNewPassword.text.toString()
        val confirmPwd = etConfirmPassword.text.toString()

        if (currentPwd.isEmpty() || newPwd.isEmpty() || confirmPwd.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all password fields", Toast.LENGTH_SHORT).show()
            return
        }
        if (newPwd != confirmPwd) {
            Toast.makeText(requireContext(), "New passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val req = ChangePasswordRequest(currentPassword = currentPwd, newPassword = newPwd)
                val res = RetrofitClient.api.changePassword(req)
                withContext(Dispatchers.Main) {
                    if (res.isSuccessful && res.body()?.success == true) {
                        Toast.makeText(requireContext(), "Password updated successfully", Toast.LENGTH_SHORT).show()
                        etCurrentPassword.setText("")
                        etNewPassword.setText("")
                        etConfirmPassword.setText("")
                    } else {
                        val msg = res.body()?.error?.message ?: "Failed to update password"
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
