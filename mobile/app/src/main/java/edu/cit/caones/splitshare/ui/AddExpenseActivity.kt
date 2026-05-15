package edu.cit.caones.splitshare.ui

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import edu.cit.caones.splitshare.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddExpenseActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_GROUP_ID = "extra_group_id"
        const val EXTRA_GROUP_NAME = "extra_group_name"
    }

    private lateinit var btnClose: ImageButton
    private lateinit var etAmount: TextInputEditText
    private lateinit var tilDescription: TextInputLayout
    private lateinit var etDescription: TextInputEditText
    private lateinit var chipGroupCategory: ChipGroup
    private lateinit var actvGroup: MaterialAutoCompleteTextView
    private lateinit var actvPaidBy: MaterialAutoCompleteTextView
    private lateinit var etDate: TextInputEditText
    private lateinit var btnAttachReceipt: LinearLayout
    private lateinit var tvReceiptStatus: TextView
    private lateinit var tvError: TextView
    private lateinit var btnSaveExpense: MaterialButton

    private var receiptUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            receiptUri = it
            tvReceiptStatus.text = "Receipt attached ✓"
            tvReceiptStatus.setTextColor(resources.getColor(R.color.green_owed, null))
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

        btnClose.setOnClickListener { finish() }
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
        chipGroupCategory = findViewById(R.id.chipGroupCategory)
        actvGroup = findViewById(R.id.actvGroup)
        actvPaidBy = findViewById(R.id.actvPaidBy)
        etDate = findViewById(R.id.etDate)
        btnAttachReceipt = findViewById(R.id.btnAttachReceipt)
        tvReceiptStatus = findViewById(R.id.tvReceiptStatus)
        tvError = findViewById(R.id.tvError)
        btnSaveExpense = findViewById(R.id.btnSaveExpense)
    }

    private fun setupGroupDropdown(preselected: String?) {
        val groups = listOf("Roommates", "Japan Trip", "Barkada Lunches")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, groups)
        actvGroup.setAdapter(adapter)
        if (!preselected.isNullOrEmpty()) {
            actvGroup.setText(preselected, false)
        }
    }

    private fun setupPaidByDropdown() {
        val members = listOf("You", "Mia Cruz", "Jake Reyes", "Sara Lim")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, members)
        actvPaidBy.setAdapter(adapter)
        actvPaidBy.setText("You", false)
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
        val group = actvGroup.text?.toString()?.trim() ?: ""

        tvError.visibility = View.GONE
        var valid = true

        if (amountStr.isEmpty() || amountStr.toDoubleOrNull() == null || amountStr.toDouble() <= 0) {
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
        if (group.isEmpty()) {
            tvError.text = "Please select a group"
            tvError.visibility = View.VISIBLE
            valid = false
        }
        if (!valid) return

        btnSaveExpense.isEnabled = false
        btnSaveExpense.text = "Saving..."

        // TODO: POST to /expenses and if receiptUri != null, POST to /expenses/{id}/receipt
        android.os.Handler(mainLooper).postDelayed({
            setResult(RESULT_OK)
            finish()
        }, 600)
    }
}
