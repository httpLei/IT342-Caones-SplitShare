package edu.cit.caones.splitshare.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import edu.cit.caones.splitshare.R
import edu.cit.caones.splitshare.SessionManager
import edu.cit.caones.splitshare.model.ActivityItem
import edu.cit.caones.splitshare.model.Group
import java.text.NumberFormat
import java.util.Locale

class HomeFragment : Fragment() {

    private lateinit var tvGreeting: TextView
    private lateinit var tvAvatar: TextView
    private lateinit var tvNetBalance: TextView
    private lateinit var tvOwedToYou: TextView
    private lateinit var tvYouOwe: TextView
    private lateinit var llGroups: LinearLayout
    private lateinit var llActivity: LinearLayout

    private val php = NumberFormat.getCurrencyInstance(Locale("en", "PH")).apply {
        currency = java.util.Currency.getInstance("PHP")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_home, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        populateHeader()
        populateGroups()
        populateActivity()
    }

    private fun bindViews(view: View) {
        tvGreeting = view.findViewById(R.id.tvGreeting)
        tvAvatar = view.findViewById(R.id.tvAvatar)
        tvNetBalance = view.findViewById(R.id.tvNetBalance)
        tvOwedToYou = view.findViewById(R.id.tvOwedToYou)
        tvYouOwe = view.findViewById(R.id.tvYouOwe)
        llGroups = view.findViewById(R.id.llGroups)
        llActivity = view.findViewById(R.id.llActivity)
    }

    private fun populateHeader() {
        val user = SessionManager.getCurrentUser()
        tvGreeting.text = "Hey, ${user?.firstName ?: "there"} 👋"
        tvAvatar.text = user?.initials ?: "?"
    }

    private fun populateGroups() {
        // Mock data — replace with API call GET /groups
        val groups = listOf(
            Group("g1", "Roommates", "🏠", 4, 12400.0, 340.0),
            Group("g2", "Japan Trip", "✈️", 6, 58200.0, -200.0),
            Group("g3", "Barkada Lunches", "🍕", 3, 4100.0, 0.0)
        )

        var totalOwed = 0.0
        var totalOwe = 0.0
        groups.forEach { g ->
            if (g.myBalance > 0) totalOwed += g.myBalance
            else if (g.myBalance < 0) totalOwe += (-g.myBalance)
        }

        val net = totalOwed - totalOwe
        tvNetBalance.text = if (net >= 0) "+${php.format(net)}" else php.format(net)
        tvOwedToYou.text = php.format(totalOwed)
        tvYouOwe.text = php.format(totalOwe)

        llGroups.removeAllViews()
        groups.forEach { group -> addGroupCard(group) }
    }

    private fun addGroupCard(group: Group) {
        val card = layoutInflater.inflate(R.layout.item_group_card, llGroups, false)
        card.findViewById<TextView>(R.id.tvGroupEmoji).text = group.emoji
        card.findViewById<TextView>(R.id.tvGroupName).text = group.name
        card.findViewById<TextView>(R.id.tvGroupMembers).text = "${group.memberCount} members"
        card.findViewById<TextView>(R.id.tvGroupTotal).text = "Total: ${php.format(group.totalAmount)}"

        val badge = card.findViewById<TextView>(R.id.tvGroupBadge)
        when {
            group.myBalance > 0 -> {
                badge.text = "Owed ${php.format(group.myBalance)}"
                badge.background = resources.getDrawable(R.drawable.bg_badge_owed, null)
                badge.setTextColor(resources.getColor(R.color.green_owed, null))
            }
            group.myBalance < 0 -> {
                badge.text = "Owe ${php.format(-group.myBalance)}"
                badge.background = resources.getDrawable(R.drawable.bg_badge_owe, null)
                badge.setTextColor(resources.getColor(R.color.red_owe, null))
            }
            else -> {
                badge.text = "Settled ✓"
                badge.background = resources.getDrawable(R.drawable.bg_badge_settled, null)
                badge.setTextColor(resources.getColor(R.color.text_secondary, null))
            }
        }

        card.setOnClickListener {
            val intent = Intent(requireContext(), GroupDetailActivity::class.java).apply {
                putExtra(GroupDetailActivity.EXTRA_GROUP_ID, group.id)
                putExtra(GroupDetailActivity.EXTRA_GROUP_NAME, group.name)
                putExtra(GroupDetailActivity.EXTRA_GROUP_EMOJI, group.emoji)
            }
            startActivity(intent)
        }

        llGroups.addView(card)
    }

    private fun populateActivity() {
        // Mock data — replace with GET /users/me/history
        val items = listOf(
            ActivityItem("Groceries — SM", "Roommates · Paid by you · today", 175.0, "🛒", true),
            ActivityItem("Shinkansen tickets", "Japan Trip · Paid by Carlo · yesterday", -200.0, "✈️", false),
            ActivityItem("Settle up — Mia paid you", "Roommates · 2 days ago", 500.0, "💰", true),
            ActivityItem("Dinner — Yakimix", "Barkada Lunches · Paid by you · 3 days ago", 165.0, "🍜", true)
        )

        llActivity.removeAllViews()
        items.forEachIndexed { index, item ->
            val row = layoutInflater.inflate(R.layout.item_activity, llActivity, false)
            row.findViewById<TextView>(R.id.tvActivityIcon).text = item.emoji
            row.findViewById<TextView>(R.id.tvActivityTitle).text = item.title
            row.findViewById<TextView>(R.id.tvActivitySubtitle).text = item.subtitle

            val amountTv = row.findViewById<TextView>(R.id.tvActivityAmount)
            val absAmount = Math.abs(item.amount)
            amountTv.text = if (item.isPositive) "+${php.format(absAmount)}" else "-${php.format(absAmount)}"
            amountTv.setTextColor(
                if (item.isPositive) resources.getColor(R.color.green_owed, null)
                else resources.getColor(R.color.red_owe, null)
            )

            // Add a divider between items (not after last)
            if (index < items.size - 1) {
                val divider = View(requireContext())
                divider.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 1
                )
                divider.setBackgroundColor(resources.getColor(R.color.divider, null))
                llActivity.addView(row)
                llActivity.addView(divider)
            } else {
                llActivity.addView(row)
            }
        }
    }
}
