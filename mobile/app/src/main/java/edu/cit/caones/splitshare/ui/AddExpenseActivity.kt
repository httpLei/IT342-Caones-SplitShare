package edu.cit.caones.splitshare.ui

import android.app.DatePickerDialog
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import edu.cit.caones.splitshare.R
import edu.cit.caones.splitshare.SessionManager
import edu.cit.caones.splitshare.network.RetrofitClient
import edu.cit.caones.splitshare.network.dto.CreateExpenseRequest
import edu.cit.caones.splitshare.network.dto.GroupSummaryDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddExpenseActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_GROUP_ID = "extra_group_id"
        const val EXTRA_GROUP_NAME = "extra_group_name"
    }

    private lateinit var btnClose: View
    private lateinit var etAmount: TextInputEditText
    private lateinit var tilDescription: TextInputLayout
    private lateinit var etDescription: TextInputEditText
    private lateinit var actvCategory: MaterialAutoCompleteTextView
    private lateinit var actvGroup: MaterialAutoCompleteTextView
    private lateinit var actvPaidBy: MaterialAutoCompleteTextView
    private lateinit var etDate: TextInputEditText
    private lateinit var btnAttachReceipt: LinearLayout
    private lateinit var tvReceiptStatus: TextView
    private lateinit var tvReceiptHint: TextView
    private lateinit var tvError: TextView
    private lateinit var btnCancelExpense: MaterialButton
    private lateinit var btnSaveExpense: MaterialButton

    private var receiptUri: Uri? = null
    private var selectedGroupId: Long? = null
    private var loadedGroups: List<GroupSummaryDto> = emptyList()

    private val json = Gson()

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            receiptUri = it
            tvReceiptStatus.text = "Receipt attached"
            tvReceiptStatus.setTextColor(Color.WHITE)
            tvReceiptHint.text = "Ready to upload"
            tvReceiptHint.setTextColor(Color.parseColor("#9CA3AF"))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)

        val preselectedGroup = intent.getStringExtra(EXTRA_GROUP_NAME)

        bindViews()
        setupGroupDropdown(preselectedGroup)
        setupPaidByDropdown()
        setupDatePicker()
        loadGroups(preselectedGroup)

        btnClose.setOnClickListener { finish() }
        btnCancelExpense.setOnClickListener { finish() }
        btnAttachReceipt.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
        btnSaveExpense.setOnClickListener { attemptSave() }
    }

    private fun bindViews() {
        btnClose = findViewById(R.id.btnClose)
        etAmount = findViewById(R.id.etAmount)
        tilDescription = findViewById(R.id.tilDescription)
        etDescription = findViewById(R.id.etDescription)
        actvCategory = findViewById(R.id.actvCategory)
        actvGroup = findViewById(R.id.actvGroup)
        actvPaidBy = findViewById(R.id.actvPaidBy)
        etDate = findViewById(R.id.etDate)
        btnAttachReceipt = findViewById(R.id.btnAttachReceipt)
        tvReceiptStatus = findViewById(R.id.tvReceiptStatus)
        tvReceiptHint = findViewById(R.id.tvReceiptHint)
        tvError = findViewById(R.id.tvError)
        btnCancelExpense = findViewById(R.id.btnCancelExpense)
        btnSaveExpense = findViewById(R.id.btnSaveExpense)
        
        setupCategoryDropdown()
    }

    private fun setupCategoryDropdown() {
        val categories = SessionManager.getSelectedCategories()
        val defaultCategories = if (categories.isEmpty()) listOf("Food", "Transport") else categories.toList()
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, defaultCategories)
        actvCategory.setAdapter(adapter)
        if (defaultCategories.isNotEmpty()) {
            actvCategory.setText(defaultCategories.first(), false)
        }
    }

    private fun setupGroupDropdown(preselected: String?) {
        val groups = mutableListOf<String>()
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, groups)
        actvGroup.setAdapter(adapter)
        if (!preselected.isNullOrEmpty()) {
            actvGroup.setText(preselected, false)
        }
    }

    private fun setupPaidByDropdown() {
        val currentUser = SessionManager.getCurrentUser()
        val members = listOf(currentUser?.fullName?.trim().takeIf { !it.isNullOrBlank() } ?: "You")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, members)
        actvPaidBy.setAdapter(adapter)
        actvPaidBy.setText(members.first(), false)
    }

    private fun loadGroups(preselectedGroup: String?) {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) { RetrofitClient.api.getGroups() }
                if (response.isSuccessful && response.body()?.success == true) {
                    loadedGroups = response.body()?.data ?: emptyList()
                    val names = loadedGroups.map { it.name }
                    (actvGroup.adapter as? ArrayAdapter<String>)?.clear()
                    (actvGroup.adapter as? ArrayAdapter<String>)?.addAll(names)
                    (actvGroup.adapter as? ArrayAdapter<String>)?.notifyDataSetChanged()

                    actvGroup.setOnItemClickListener { _, _, position, _ ->
                        selectedGroupId = loadedGroups.getOrNull(position)?.id
                    }

                    val selected = loadedGroups.firstOrNull {
                        it.name.equals(preselectedGroup, ignoreCase = true)
                    } ?: loadedGroups.firstOrNull { it.id == intent.getStringExtra(EXTRA_GROUP_ID)?.toLongOrNull() }

                    selected?.let {
                        selectedGroupId = it.id
                        actvGroup.setText(it.name, false)
                    }
                }
            } catch (_: Exception) {
                // Keep the prefilled value from the intent and let validation handle it.
            }
        }
    }

    private fun setupDatePicker() {
        val today = Calendar.getInstance()
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        etDate.setText(sdf.format(today.time))

        etDate.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    val cal = Calendar.getInstance()
                    cal.set(year, month, day)
                    etDate.setText(sdf.format(cal.time))
                },
                today.get(Calendar.YEAR),
                today.get(Calendar.MONTH),
                today.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun attemptSave() {
        val amountStr = etAmount.text?.toString()?.trim() ?: ""
        val description = etDescription.text?.toString()?.trim() ?: ""
        val groupName = actvGroup.text?.toString()?.trim().orEmpty()
        val selectedCategory = selectedCategory()
        val amount = amountStr.toDoubleOrNull()

        tvError.visibility = View.GONE
        var valid = true

        if (amount == null || amount <= 0) {
            tvError.text = "Please enter a valid amount"
            tvError.visibility = View.VISIBLE
            valid = false
        }
        if (description.isEmpty()) {
            tilDescription.error = "Description is required"
            valid = false
        } else {
            tilDescription.error = null
        }
        if (groupName.isEmpty()) {
            tvError.text = "Please select a group"
            tvError.visibility = View.VISIBLE
            valid = false
        }
        if (selectedGroupId == null) {
            selectedGroupId = loadedGroups.firstOrNull { it.name.equals(groupName, ignoreCase = true) }?.id
        }
        if (selectedGroupId == null) {
            tvError.text = "Please choose a valid group"
            tvError.visibility = View.VISIBLE
            valid = false
        }
        if (selectedCategory.isBlank()) {
            tvError.text = "Please select a category"
            tvError.visibility = View.VISIBLE
            valid = false
        }
        if (!valid) return

        btnSaveExpense.isEnabled = false
        btnSaveExpense.text = "Saving..."

        lifecycleScope.launch {
            try {
                val request = CreateExpenseRequest(
                    description = description,
                    category = selectedCategory,
                    amount = amount!!
                )

                val dataPart = json.toJson(request)
                    .toRequestBody("application/json".toMediaTypeOrNull())

                val receiptPart = receiptUri?.let { uri ->
                    val bytes = contentResolver.openInputStream(uri)?.use { input ->
                        ByteArrayOutputStream().use { output ->
                            input.copyTo(output)
                            output.toByteArray()
                        }
                    }
                    bytes?.let {
                        MultipartBody.Part.createFormData(
                            "receipt",
                            "receipt.jpg",
                            it.toRequestBody("image/jpeg".toMediaTypeOrNull())
                        )
                    }
                }

                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.api.addExpense(selectedGroupId!!, dataPart, receiptPart)
                }

                if (response.isSuccessful && response.body()?.success == true) {
                    setResult(RESULT_OK)
                    finish()
                } else {
                    tvError.text = response.body()?.error?.message ?: "Unable to save expense."
                    tvError.visibility = View.VISIBLE
                    btnSaveExpense.isEnabled = true
                    btnSaveExpense.text = "Save expense"
                }
            } catch (_: Exception) {
                tvError.text = "Cannot reach server. Is the backend running?"
                tvError.visibility = View.VISIBLE
                btnSaveExpense.isEnabled = true
                btnSaveExpense.text = "Save expense"
            }
        }
    }

    private fun selectedCategory(): String {
        return actvCategory.text?.toString()?.trim().orEmpty()
    }
}
