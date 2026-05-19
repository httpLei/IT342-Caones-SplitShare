package edu.cit.caones.splitshare.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import edu.cit.caones.splitshare.R
import edu.cit.caones.splitshare.SessionManager
import edu.cit.caones.splitshare.network.RetrofitClient
import edu.cit.caones.splitshare.network.dto.SettleBalanceRequest
import edu.cit.caones.splitshare.network.dto.UpdateGroupRequest
import edu.cit.caones.splitshare.network.dto.UserConnectionDto
import edu.cit.caones.splitshare.network.dto.ExpenseDto
import edu.cit.caones.splitshare.network.dto.GroupDetailsDto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

class GroupDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_GROUP_ID = "extra_group_id"
        const val EXTRA_GROUP_NAME = "extra_group_name"
        const val EXTRA_GROUP_EMOJI = "extra_group_emoji"
    }

    private lateinit var btnBack: ImageButton
    private lateinit var tvGroupEmoji: TextView
    private lateinit var tvGroupName: TextView
    private lateinit var tvGroupMeta: TextView
    private lateinit var btnAddExpense: MaterialButton
    private lateinit var btnEditGroup: MaterialButton
    private lateinit var tabLayout: TabLayout
    private lateinit var sectionBalances: LinearLayout
    private lateinit var sectionExpenses: LinearLayout
    private lateinit var llMembers: LinearLayout
    private lateinit var llExpenses: LinearLayout
    private lateinit var tvLoading: TextView
    private lateinit var tvError: TextView
    private var groupIdValue: Long = -1L
    private var currentGroup: GroupDetailsDto? = null
    private var mutualUsers: List<UserConnectionDto> = emptyList()

    private val activityScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val php = NumberFormat.getCurrencyInstance(Locale("en", "PH")).apply {
        currency = java.util.Currency.getInstance("PHP")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_detail)

        val groupId = intent.getStringExtra(EXTRA_GROUP_ID) ?: ""
        val groupName = intent.getStringExtra(EXTRA_GROUP_NAME) ?: "Group"
        val groupEmoji = intent.getStringExtra(EXTRA_GROUP_EMOJI) ?: "👥"
        groupIdValue = groupId.toLongOrNull() ?: -1L

        bindViews()
        setupHeader(groupName, groupEmoji)
        setupTabs()
        loadGroup(groupId)

        btnBack.setOnClickListener { finish() }
        btnAddExpense.setOnClickListener {
            startActivity(Intent(this, AddExpenseActivity::class.java).apply {
                putExtra(AddExpenseActivity.EXTRA_GROUP_ID, groupId)
                putExtra(AddExpenseActivity.EXTRA_GROUP_NAME, groupName)
            })
        }
        btnEditGroup.setOnClickListener {
            openEditGroupDialog()
        }
    }

    private fun bindViews() {
        btnBack = findViewById(R.id.btnBack)
        tvGroupEmoji = findViewById(R.id.tvGroupEmoji)
        tvGroupName = findViewById(R.id.tvGroupName)
        tvGroupMeta = findViewById(R.id.tvGroupMeta)
        btnAddExpense = findViewById(R.id.btnAddExpense)
        btnEditGroup = findViewById(R.id.btnEditGroup)
        tabLayout = findViewById(R.id.tabLayout)
        sectionBalances = findViewById(R.id.sectionBalances)
        sectionExpenses = findViewById(R.id.sectionExpenses)
        llMembers = findViewById(R.id.llMembers)
        llExpenses = findViewById(R.id.llExpenses)
        tvLoading = findViewById(R.id.tvGroupLoading)
        tvError = findViewById(R.id.tvGroupError)
    }

    private fun setupHeader(groupName: String, groupEmoji: String) {
        tvGroupEmoji.text = groupEmoji
        tvGroupName.text = groupName
        tvGroupMeta.text = "Loading group data…"
    }

    private fun setupTabs() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> { sectionBalances.visibility = View.VISIBLE; sectionExpenses.visibility = View.GONE }
                    1 -> { sectionBalances.visibility = View.GONE; sectionExpenses.visibility = View.VISIBLE }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun loadGroup(groupId: String) {
        val parsedId = groupId.toLongOrNull()
        if (parsedId == null) {
            showError("Invalid group identifier.")
            return
        }

        tvError.visibility = View.GONE
        tvLoading.visibility = View.VISIBLE

        activityScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.api.getGroup(parsedId)
                }

                if (response.isSuccessful && response.body()?.success == true) {
                    val details = response.body()?.data
                    if (details == null) {
                        showError("No group data returned.")
                    } else {
                        currentGroup = details
                        renderGroup(details)
                    }
                } else {
                    showError(response.body()?.error?.message ?: "Unable to load group.")
                }
            } catch (_: Exception) {
                showError("Cannot reach server. Is the backend running?")
            } finally {
                tvLoading.visibility = View.GONE
            }
        }
    }

    private fun renderGroup(group: GroupDetailsDto) {
        currentGroup = group
        tvGroupMeta.text = "${group.members.size} members · ${php.format(group.total)} total"

        val currentEmail = SessionManager.getCurrentUser()?.email?.lowercase(Locale.getDefault()).orEmpty()
        val displayBalances = if (currentEmail.isNotBlank()) {
            group.balances.filter { it.email.lowercase(Locale.getDefault()) != currentEmail }
        } else {
            group.balances
        }

        llMembers.removeAllViews()
        if (displayBalances.isEmpty()) {
            llMembers.addView(emptyStateView("No balance data yet."))
        } else {
            displayBalances.forEachIndexed { index, member ->
                val row = layoutInflater.inflate(R.layout.item_member_balance, llMembers, false)
                row.findViewById<TextView>(R.id.tvMemberInitials).text =
                    member.initial.ifBlank { member.name.take(2).uppercase() }
                row.findViewById<TextView>(R.id.tvMemberName).text = member.name

                val badge = row.findViewById<TextView>(R.id.tvMemberBadge)
                when {
                    member.positive -> {
                        badge.text = "+${php.format(member.amount)}"
                        badge.background = resources.getDrawable(R.drawable.bg_badge_owed, null)
                        badge.setTextColor(resources.getColor(R.color.green_owed, null))
                    }
                    member.amount != 0.0 -> {
                        badge.text = "-${php.format(abs(member.amount))}"
                        badge.background = resources.getDrawable(R.drawable.bg_badge_owe, null)
                        badge.setTextColor(resources.getColor(R.color.red_owe, null))
                    }
                    else -> {
                        badge.text = if (member.settledByCurrentUser) "Settled ✓" else "Balanced"
                        badge.background = resources.getDrawable(R.drawable.bg_badge_settled, null)
                        badge.setTextColor(resources.getColor(R.color.text_secondary, null))
                    }
                }

                val settleButton = row.findViewById<MaterialButton>(R.id.btnSettleMember)
                val canSettle = member.amount != 0.0
                settleButton.visibility = if (canSettle) View.VISIBLE else View.GONE
                if (canSettle) {
                    settleButton.text = when {
                        member.settlementPending && member.settledByCurrentUser -> "Waiting..."
                        member.settlementPending -> "Confirm Settle"
                        else -> "Settle"
                    }
                    settleButton.isEnabled = !(member.settlementPending && member.settledByCurrentUser)
                    settleButton.setOnClickListener { settleCounterpart(member.email) }
                }

                if (index < displayBalances.size - 1) {
                    val divider = View(this)
                    divider.layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1
                    ).apply { setMargins(56, 0, 0, 0) }
                    divider.setBackgroundColor(resources.getColor(R.color.divider, null))
                    llMembers.addView(row)
                    llMembers.addView(divider)
                } else {
                    llMembers.addView(row)
                }
            }
        }

        renderExpenses(group.expenses)
    }

    private fun renderExpenses(expenses: List<ExpenseDto>) {
        llExpenses.removeAllViews()

        if (expenses.isEmpty()) {
            llExpenses.addView(emptyStateView("No expenses yet."))
            return
        }

        expenses.forEach { expense ->
            val card = layoutInflater.inflate(R.layout.item_expense, llExpenses, false)
            card.findViewById<TextView>(R.id.tvExpenseIcon).text = emojiForCategory(expense.category)
            card.findViewById<TextView>(R.id.tvExpenseTitle).text = expense.description
            card.findViewById<TextView>(R.id.tvExpenseSubtitle).text =
                "Paid by ${expense.paidByName} · ${expense.createdAt}"
            card.findViewById<TextView>(R.id.tvExpenseTotal).text = php.format(expense.amount)
            card.findViewById<TextView>(R.id.tvExpenseShare).text =
                "Your share: ${php.format(expense.share)}"

            if (!expense.receiptUrl.isNullOrBlank()) {
                card.findViewById<View>(R.id.receiptDot).visibility = View.VISIBLE
                card.findViewById<TextView>(R.id.tvReceiptLabel).visibility = View.VISIBLE
            }

            card.setOnClickListener {
                startActivity(Intent(this, ActivityDetailActivity::class.java).apply {
                    putExtra(ActivityDetailActivity.EXTRA_EXPENSE_ID, expense.id.toString())
                })
            }

            llExpenses.addView(card)
        }
    }

    private fun emptyStateView(message: String): TextView {
        return TextView(this).apply {
            text = message
            setTextColor(resources.getColor(R.color.text_secondary, null))
            textSize = 13f
            setPadding(8, 8, 8, 8)
        }
    }

    private fun showError(message: String) {
        tvError.text = message
        tvError.visibility = View.VISIBLE
    }

    private fun settleCounterpart(counterpartEmail: String) {
        if (groupIdValue <= 0) return

        tvError.visibility = View.GONE
        tvLoading.visibility = View.VISIBLE
        activityScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.api.settleBalance(groupIdValue, SettleBalanceRequest(counterpartEmail))
                }

                if (response.isSuccessful && response.body()?.success == true) {
                    currentGroup = response.body()?.data
                    response.body()?.data?.let { renderGroup(it) }
                } else {
                    showError(response.body()?.error?.message ?: "Unable to settle this balance.")
                }
            } catch (_: Exception) {
                showError("Cannot reach server. Is the backend running?")
            } finally {
                tvLoading.visibility = View.GONE
            }
        }
    }

    private fun openSettleDialog() {
        val group = currentGroup ?: return
        val view = layoutInflater.inflate(R.layout.dialog_settle_group, null)
        val container = view.findViewById<LinearLayout>(R.id.llSettleBalances)
        val dialog = AlertDialog.Builder(this).setView(view).create()

        container.removeAllViews()
        val currentEmail = SessionManager.getCurrentUser()?.email?.lowercase(Locale.getDefault()).orEmpty()
        group.balances.filter { it.email.lowercase(Locale.getDefault()) != currentEmail }
            .forEach { balance ->
                val row = layoutInflater.inflate(R.layout.item_member_balance, container, false)
                row.findViewById<TextView>(R.id.tvMemberInitials).text = balance.initial.ifBlank { balance.name.take(2).uppercase() }
                row.findViewById<TextView>(R.id.tvMemberName).text = balance.name
                row.findViewById<TextView>(R.id.tvMemberBadge).text = if (balance.positive) "+${php.format(balance.amount)}" else php.format(abs(balance.amount))
                val settleButton = row.findViewById<MaterialButton>(R.id.btnSettleMember)
                settleButton.visibility = View.VISIBLE
                settleButton.text = if (balance.settlementPending && balance.settledByCurrentUser) "Waiting..." else if (balance.settlementPending) "Confirm Settle" else "Settle"
                settleButton.isEnabled = !(balance.settlementPending && balance.settledByCurrentUser)
                settleButton.setOnClickListener {
                    dialog.dismiss()
                    settleCounterpart(balance.email)
                }
                container.addView(row)
            }

        view.findViewById<MaterialButton>(R.id.btnCloseSettleDialog).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun openEditGroupDialog() {
        val group = currentGroup ?: return
        if (mutualUsers.isEmpty()) {
            loadMutualUsers()
        }

        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_group, null)
        val etName = dialogView.findViewById<EditText>(R.id.etEditGroupName)
        val llCurrentMembers = dialogView.findViewById<LinearLayout>(R.id.llCurrentMembers)
        val llMutualUsers = dialogView.findViewById<LinearLayout>(R.id.llMutualUsers)
        val tvDialogError = dialogView.findViewById<TextView>(R.id.tvEditError)
        etName.setText(group.name)

        llCurrentMembers.removeAllViews()
        group.members.forEach { member ->
            llCurrentMembers.addView(TextView(this).apply {
                text = member
                setTextColor(resources.getColor(R.color.text_primary, null))
                textSize = 13f
                setPadding(0, 8, 0, 8)
            })
        }

        llMutualUsers.removeAllViews()
        val currentEmails = group.memberEmails.map { it.lowercase(Locale.getDefault()) }.toSet()
        mutualUsers.forEach { user ->
            val checkBox = CheckBox(this).apply {
                text = "${user.firstname} ${user.lastname} · ${user.email}"
                isChecked = currentEmails.contains(user.email.lowercase(Locale.getDefault()))
                isEnabled = !currentEmails.contains(user.email.lowercase(Locale.getDefault()))
                tag = user.email
            }
            llMutualUsers.addView(checkBox)
        }

        val dialog = AlertDialog.Builder(this).setView(dialogView).create()
        dialogView.findViewById<MaterialButton>(R.id.btnCancelEditGroup).setOnClickListener { dialog.dismiss() }
        dialogView.findViewById<MaterialButton>(R.id.btnSaveEditGroup).setOnClickListener {
            val selectedEmails = buildList {
                for (index in 0 until llMutualUsers.childCount) {
                    val child = llMutualUsers.getChildAt(index)
                    if (child is CheckBox && child.isChecked && child.isEnabled) {
                        add(child.tag.toString())
                    }
                }
            }

            val newName = etName.text?.toString()?.trim().orEmpty()
            if (newName.isBlank()) {
                tvDialogError.text = "Group name is required."
                tvDialogError.visibility = View.VISIBLE
                return@setOnClickListener
            }

            tvDialogError.visibility = View.GONE
            btnEditGroup.isEnabled = false
            activityScope.launch {
                try {
                    val response = withContext(Dispatchers.IO) {
                        RetrofitClient.api.updateGroup(group.id, UpdateGroupRequest(newName, selectedEmails))
                    }

                    if (response.isSuccessful && response.body()?.success == true) {
                        dialog.dismiss()
                        response.body()?.data?.let { renderGroup(it) }
                    } else {
                        tvDialogError.text = response.body()?.error?.message ?: "Unable to update group."
                        tvDialogError.visibility = View.VISIBLE
                    }
                } catch (_: Exception) {
                    tvDialogError.text = "Cannot reach server. Is the backend running?"
                    tvDialogError.visibility = View.VISIBLE
                } finally {
                    btnEditGroup.isEnabled = true
                }
            }
        }
        dialog.show()
    }

    private fun loadMutualUsers() {
        activityScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.api.getMutuals()
                }
                if (response.isSuccessful && response.body()?.success == true) {
                    mutualUsers = response.body()?.data ?: emptyList()
                }
            } catch (_: Exception) {
                // Non-blocking for edit dialog.
            }
        }
    }

    private fun emojiForCategory(category: String): String {
        return when (category.lowercase(Locale.getDefault())) {
            "food", "groceries", "restaurant" -> "🍽️"
            "transport", "travel" -> "🧳"
            "utilities", "bills" -> "⚡"
            "rent", "housing" -> "🏠"
            else -> "🧾"
        }
    }

    override fun onDestroy() {
        activityScope.cancel()
        super.onDestroy()
    }
}
