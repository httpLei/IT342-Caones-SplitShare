package edu.cit.caones.splitshare.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import edu.cit.caones.splitshare.R
import edu.cit.caones.splitshare.model.Expense
import edu.cit.caones.splitshare.model.Member
import java.text.NumberFormat
import java.util.Locale

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
    private lateinit var btnSettleUp: MaterialButton
    private lateinit var tabLayout: TabLayout
    private lateinit var sectionBalances: LinearLayout
    private lateinit var sectionExpenses: LinearLayout
    private lateinit var llMembers: LinearLayout
    private lateinit var llExpenses: LinearLayout

    private val php = NumberFormat.getCurrencyInstance(Locale("en", "PH")).apply {
        currency = java.util.Currency.getInstance("PHP")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_detail)

        val groupId = intent.getStringExtra(EXTRA_GROUP_ID) ?: ""
        val groupName = intent.getStringExtra(EXTRA_GROUP_NAME) ?: "Group"
        val groupEmoji = intent.getStringExtra(EXTRA_GROUP_EMOJI) ?: "👥"

        bindViews()
        setupHeader(groupName, groupEmoji)
        setupTabs()
        populateMembers()
        populateExpenses()

        btnBack.setOnClickListener { finish() }
        btnAddExpense.setOnClickListener {
            startActivity(Intent(this, AddExpenseActivity::class.java).apply {
                putExtra(AddExpenseActivity.EXTRA_GROUP_ID, groupId)
                putExtra(AddExpenseActivity.EXTRA_GROUP_NAME, groupName)
            })
        }
        btnSettleUp.setOnClickListener {
            // TODO: Open settle up bottom sheet
        }
    }

    private fun bindViews() {
        btnBack = findViewById(R.id.btnBack)
        tvGroupEmoji = findViewById(R.id.tvGroupEmoji)
        tvGroupName = findViewById(R.id.tvGroupName)
        tvGroupMeta = findViewById(R.id.tvGroupMeta)
        btnAddExpense = findViewById(R.id.btnAddExpense)
        btnSettleUp = findViewById(R.id.btnSettleUp)
        tabLayout = findViewById(R.id.tabLayout)
        sectionBalances = findViewById(R.id.sectionBalances)
        sectionExpenses = findViewById(R.id.sectionExpenses)
        llMembers = findViewById(R.id.llMembers)
        llExpenses = findViewById(R.id.llExpenses)
    }

    private fun setupHeader(groupName: String, groupEmoji: String) {
        tvGroupEmoji.text = groupEmoji
        tvGroupName.text = groupName
        tvGroupMeta.text = "4 members · ₱12,400 total" // TODO: Load from API
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

    private fun populateMembers() {
        // Mock data — replace with GET /groups/{groupId}/summary
        val members = listOf(
            Member("u1", "Lerah (You)", "LA", 340.0),
            Member("u2", "Mia Cruz", "MC", 0.0),
            Member("u3", "Jake Reyes", "JR", -200.0),
            Member("u4", "Sara Lim", "SL", -140.0)
        )

        llMembers.removeAllViews()
        members.forEachIndexed { index, member ->
            val row = layoutInflater.inflate(R.layout.item_member_balance, llMembers, false)
            row.findViewById<TextView>(R.id.tvMemberInitials).text = member.initials
            row.findViewById<TextView>(R.id.tvMemberName).text = member.name

            val badge = row.findViewById<TextView>(R.id.tvMemberBadge)
            when {
                member.balance > 0 -> {
                    badge.text = "+${php.format(member.balance)}"
                    badge.background = resources.getDrawable(R.drawable.bg_badge_owed, null)
                    badge.setTextColor(resources.getColor(R.color.green_owed, null))
                }
                member.balance < 0 -> {
                    badge.text = php.format(member.balance)
                    badge.background = resources.getDrawable(R.drawable.bg_badge_owe, null)
                    badge.setTextColor(resources.getColor(R.color.red_owe, null))
                }
                else -> {
                    badge.text = "Settled ✓"
                    badge.background = resources.getDrawable(R.drawable.bg_badge_settled, null)
                    badge.setTextColor(resources.getColor(R.color.text_secondary, null))
                }
            }

            if (index < members.size - 1) {
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

    private fun populateExpenses() {
        // Mock data — replace with GET /expenses?groupId=...
        val expenses = listOf(
            Expense("e1", "", "Roommates", "Groceries — SM Cebu", 700.0, 175.0, "You", true, "Food", "May 9, 2026", true, "🛒"),
            Expense("e2", "", "Roommates", "Electricity bill — May", 3200.0, 800.0, "Jake", false, "Utilities", "May 5, 2026", true, "⚡"),
            Expense("e3", "", "Roommates", "Internet — PLDT", 1899.0, 475.0, "Mia", false, "Utilities", "May 1, 2026", true, "📡"),
            Expense("e4", "", "Roommates", "Dinner — Yellowcab", 1240.0, 310.0, "You", true, "Food", "Apr 28, 2026", false, "🍕")
        )

        llExpenses.removeAllViews()
        expenses.forEach { expense ->
            val card = layoutInflater.inflate(R.layout.item_expense, llExpenses, false)
            card.findViewById<TextView>(R.id.tvExpenseIcon).text = expense.emoji
            card.findViewById<TextView>(R.id.tvExpenseTitle).text = expense.description
            card.findViewById<TextView>(R.id.tvExpenseSubtitle).text =
                "Paid by ${expense.paidByName} · ${expense.date}"
            card.findViewById<TextView>(R.id.tvExpenseTotal).text = php.format(expense.amount)
            card.findViewById<TextView>(R.id.tvExpenseShare).text =
                "Your share: ${php.format(expense.myShare)}"

            if (expense.hasReceipt) {
                card.findViewById<View>(R.id.receiptDot).visibility = View.VISIBLE
                card.findViewById<TextView>(R.id.tvReceiptLabel).visibility = View.VISIBLE
            }

            llExpenses.addView(card)
        }
    }
}
