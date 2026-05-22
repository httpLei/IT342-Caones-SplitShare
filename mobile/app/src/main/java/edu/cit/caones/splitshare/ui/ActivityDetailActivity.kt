package edu.cit.caones.splitshare.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import edu.cit.caones.splitshare.R
import edu.cit.caones.splitshare.SessionManager
import edu.cit.caones.splitshare.network.RetrofitClient
import edu.cit.caones.splitshare.network.dto.ExpenseDto
import edu.cit.caones.splitshare.network.dto.UpdateExpenseRequest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.abs

class ActivityDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_EXPENSE_ID = "expense_id"
        private const val API_BASE_URL = "http://10.0.2.2:8080/"
    }

    private lateinit var tvLoading: TextView
    private lateinit var tvError: TextView
    private lateinit var tvTitle: TextView
    private lateinit var tvSubtitle: TextView
    private lateinit var tvDescription: TextView
    private lateinit var tvGroupDate: TextView
    private lateinit var tvPaidBy: TextView
    private lateinit var tvAmount: TextView
    private lateinit var tvShare: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvCategory: TextView
    private lateinit var tvReceipt: TextView
    private lateinit var btnReceipt: Button
    private lateinit var btnEdit: MaterialButton
    private lateinit var btnDelete: MaterialButton

    private var receiptUrl: String? = null
    private var currentExpense: ExpenseDto? = null

    

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_activity_detail)

        val expenseId = intent.getStringExtra(EXTRA_EXPENSE_ID)?.toLongOrNull()
        if (expenseId == null) {
            finish()
            return
        }

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        tvLoading = findViewById(R.id.tvActivityLoading)
        tvError = findViewById(R.id.tvActivityError)
        tvTitle = findViewById(R.id.tvExpenseTitle)
        tvSubtitle = findViewById(R.id.tvExpenseSubtitle)
        tvDescription = findViewById(R.id.tvExpenseDescription)
        tvGroupDate = findViewById(R.id.tvExpenseGroupDate)
        tvPaidBy = findViewById(R.id.tvExpensePaidBy)
        tvAmount = findViewById(R.id.tvExpenseAmount)
        tvShare = findViewById(R.id.tvExpenseShare)
        tvStatus = findViewById(R.id.tvExpenseStatus)
        tvCategory = findViewById(R.id.tvExpenseCategory)
        tvReceipt = findViewById(R.id.tvExpenseReceipt)
        btnReceipt = findViewById(R.id.btnViewReceipt)
        btnEdit = findViewById(R.id.btnEditExpense)
        btnDelete = findViewById(R.id.btnDeleteExpense)

        btnReceipt.setOnClickListener {
            showReceiptPreview()
        }

        btnEdit.setOnClickListener { showEditDialog() }
        btnDelete.setOnClickListener { confirmDelete() }

        loadExpense(expenseId)
    }

    private fun loadExpense(expenseId: Long) {
        showLoading(true)
        tvError.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.api.getExpense(expenseId)
                }

                if (response.isSuccessful && response.body()?.success == true) {
                    renderExpense(response.body()?.data)
                } else {
                    showError(response.body()?.error?.message ?: "Unable to load expense details.")
                }
            } catch (_: Exception) {
                showError("Cannot reach server. Is the backend running?")
            } finally {
                showLoading(false)
            }
        }
    }

    private fun renderExpense(expense: ExpenseDto?) {
        if (expense == null) {
            showError("Expense not found.")
            return
        }

        currentExpense = expense
        receiptUrl = expense.receiptUrl

        tvTitle.text = expense.desc
        tvDescription.text = expense.description
        tvGroupDate.text = expense.sub
        tvPaidBy.text = expense.paidByName
        tvSubtitle.visibility = View.GONE
        tvSubtitle.text = ""
        tvDescription.text = expense.description
        tvPaidBy.text = expense.paidByName
        tvAmount.text = SessionManager.formatCurrency(expense.amount)

        val shareAbs = abs(expense.share)
        tvShare.text = if (expense.positive) "+${SessionManager.formatCurrency(shareAbs)}" else "-${SessionManager.formatCurrency(shareAbs)}"
        tvShare.setTextColor(
            if (expense.positive) resources.getColor(R.color.green_owed, null)
            else resources.getColor(R.color.red_owe, null)
        )

        tvStatus.text = if (expense.positive) "Credit" else "Debit"
        tvStatus.setTextColor(
            if (expense.positive) resources.getColor(R.color.green_owed, null)
            else resources.getColor(R.color.red_owe, null)
        )

        tvCategory.text = expense.category.ifBlank { "N/A" }

        if (expense.receiptUrl.isNullOrBlank()) {
            tvReceipt.text = "No receipt attached."
            btnReceipt.visibility = View.GONE
        } else {
            tvReceipt.text = "Tap to preview the receipt."
            btnReceipt.visibility = View.VISIBLE
        }

        val canEdit = expense.paidByEmail.equals(SessionManager.getCurrentUser()?.email, ignoreCase = true)
        btnEdit.visibility = if (canEdit) View.VISIBLE else View.GONE
        btnDelete.visibility = if (canEdit) View.VISIBLE else View.GONE
    }

    private fun showLoading(loading: Boolean) {
        tvLoading.visibility = if (loading) View.VISIBLE else View.GONE
    }

    private fun showError(message: String) {
        tvError.text = message
        tvError.visibility = View.VISIBLE
    }

    private fun showReceiptPreview() {
        val url = receiptUrl?.let { absoluteUrl(it) } ?: return
        val dialogView = layoutInflater.inflate(R.layout.dialog_receipt_preview, null)
        val image = dialogView.findViewById<ImageView>(R.id.ivReceiptPreview)
        val dialog = AlertDialog.Builder(this).setView(dialogView).create()

        val headers = LazyHeaders.Builder()
            .addHeader("Authorization", "Bearer ${SessionManager.getToken().orEmpty()}")
            .build()

        Glide.with(this)
            .load(GlideUrl(url, headers))
            .into(image)

        dialogView.findViewById<MaterialButton>(R.id.btnCloseReceipt).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showEditDialog() {
        val expense = currentExpense ?: return
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_expense, null)
        val etDescription = dialogView.findViewById<android.widget.EditText>(R.id.etEditExpenseDescription)
        val actvCategory = dialogView.findViewById<MaterialAutoCompleteTextView>(R.id.actvEditExpenseCategory)
        val etAmount = dialogView.findViewById<android.widget.EditText>(R.id.etEditExpenseAmount)
        val tvEditError = dialogView.findViewById<TextView>(R.id.tvEditExpenseError)
        val dialog = AlertDialog.Builder(this).setView(dialogView).create()
        val categories = SessionManager.getSelectedCategories().let {
            if (it.isEmpty()) listOf("Food", "Transport") else it.toList()
        }
        val categoryOptions = if (expense.category.isBlank() || categories.any { it.equals(expense.category, ignoreCase = true) }) {
            categories
        } else {
            listOf(expense.category) + categories
        }

        etDescription.setText(expense.description)
        actvCategory.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categoryOptions))
        actvCategory.setText(expense.category.ifBlank { categoryOptions.firstOrNull().orEmpty() }, false)
        etAmount.setText(expense.amount.toString())

        dialogView.findViewById<MaterialButton>(R.id.btnCancelEditExpense).setOnClickListener { dialog.dismiss() }
        dialogView.findViewById<MaterialButton>(R.id.btnSaveEditExpense).setOnClickListener {
            val description = etDescription.text?.toString()?.trim().orEmpty()
            val category = actvCategory.text?.toString()?.trim().orEmpty()
            val amount = etAmount.text?.toString()?.toDoubleOrNull()

            if (description.isBlank() || category.isBlank() || amount == null || amount <= 0.0) {
                tvEditError.text = "Enter a valid description, category, and amount."
                tvEditError.visibility = View.VISIBLE
                return@setOnClickListener
            }

            dialog.dismiss()
            updateExpense(description, category, amount)
        }

        dialog.show()
    }

    private fun updateExpense(description: String, category: String, amount: Double) {
        val expense = currentExpense ?: return
        showLoading(true)

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    val payload = UpdateExpenseRequest(description, category, amount)
                    val json = "{\"description\":\"${escapeJson(payload.description)}\",\"category\":\"${escapeJson(payload.category)}\",\"amount\":${payload.amount}}"
                    RetrofitClient.api.updateExpense(expense.id, json.toRequestBody("application/json".toMediaType()))
                }

                if (response.isSuccessful && response.body()?.success == true) {
                    renderExpense(response.body()?.data)
                } else {
                    showError(response.body()?.error?.message ?: "Unable to update expense.")
                }
            } catch (_: Exception) {
                showError("Cannot reach server. Is the backend running?")
            } finally {
                showLoading(false)
            }
        }
    }

    private fun confirmDelete() {
        val expense = currentExpense ?: return
        AlertDialog.Builder(this)
            .setTitle("Delete expense?")
            .setMessage("Delete ${expense.description}?")
            .setPositiveButton("Delete") { _, _ -> deleteExpense(expense.id) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteExpense(expenseId: Long) {
        showLoading(true)

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.api.deleteExpense(expenseId)
                }

                if (response.isSuccessful && response.body()?.success == true) {
                    finish()
                } else {
                    showError(response.body()?.error?.message ?: "Unable to delete expense.")
                }
            } catch (_: Exception) {
                showError("Cannot reach server. Is the backend running?")
            } finally {
                showLoading(false)
            }
        }
    }

    private fun absoluteUrl(url: String): String {
        return if (url.startsWith("http://") || url.startsWith("https://")) url else "$API_BASE_URL${url.trimStart('/')}"
    }

    private fun escapeJson(value: String): String {
        return value.replace("\\", "\\\\").replace("\"", "\\\"")
    }
}
