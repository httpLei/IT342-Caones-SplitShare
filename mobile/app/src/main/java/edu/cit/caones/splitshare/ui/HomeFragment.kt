package edu.cit.caones.splitshare.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import edu.cit.caones.splitshare.R
import edu.cit.caones.splitshare.SessionManager
import edu.cit.caones.splitshare.network.RetrofitClient
import edu.cit.caones.splitshare.network.dto.GroupSummaryDto
import edu.cit.caones.splitshare.network.dto.UserActivityDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

class HomeFragment : Fragment() {

    private lateinit var tvGreeting: TextView
    private lateinit var tvNetBalance: TextView
    private lateinit var tvOwedToYou: TextView
    private lateinit var tvYouOwe: TextView
    private lateinit var llGroups: LinearLayout
    private lateinit var llActivity: LinearLayout
    private lateinit var tvGroupsEmpty: TextView
    private lateinit var tvActivityEmpty: TextView
    private lateinit var tvGroupsLoading: TextView
    private lateinit var tvActivityLoading: TextView
    private lateinit var tvGlobalError: TextView

    

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_home, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        populateHeader()
        loadDashboard()
    }

    private fun bindViews(view: View) {
        tvGreeting       = view.findViewById(R.id.tvGreeting)
        tvNetBalance     = view.findViewById(R.id.tvNetBalance)
        tvOwedToYou      = view.findViewById(R.id.tvOwedToYou)
        tvYouOwe         = view.findViewById(R.id.tvYouOwe)
        llGroups         = view.findViewById(R.id.llGroups)
        llActivity       = view.findViewById(R.id.llActivity)
        tvGroupsEmpty    = view.findViewById(R.id.tvGroupsEmpty)
        tvActivityEmpty  = view.findViewById(R.id.tvActivityEmpty)
        tvGroupsLoading  = view.findViewById(R.id.tvGroupsLoading)
        tvActivityLoading = view.findViewById(R.id.tvActivityLoading)
        tvGlobalError    = view.findViewById(R.id.tvGlobalError)
    }

    private fun setupLogoutButton(view: View) {
        view.findViewById<MaterialButton>(R.id.btnLogout)?.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun populateHeader() {
        val user = SessionManager.getCurrentUser()
        tvGreeting.text = "Hey, ${user?.firstName ?: "there"} 👋"
    }

    private fun loadDashboard() {
        showGroupsLoading(true)
        showActivityLoading(true)
        tvGlobalError.visibility = View.GONE

        viewLifecycleOwner.lifecycleScope.launch {
            // Load groups
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.api.getGroups()
                }
                if (response.isSuccessful && response.body()?.success == true) {
                    val groups = response.body()?.data ?: emptyList()
                    renderGroups(groups)
                } else {
                    val msg = response.body()?.error?.message ?: "Failed to load groups."
                    showGlobalError(msg)
                }
            } catch (e: Exception) {
                showGlobalError("Cannot reach server. Is the backend running?")
            } finally {
                showGroupsLoading(false)
            }

            // Load recent activity
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.api.getMyHistory()
                }
                if (response.isSuccessful && response.body()?.success == true) {
                    val items = response.body()?.data ?: emptyList()
                    renderActivity(items)
                }
                // Activity loading failure is non-blocking — just show empty
            } catch (e: Exception) {
                // non-fatal
            } finally {
                showActivityLoading(false)
            }
        }
    }

    // ── Groups rendering ──────────────────────────────────────────────────────

    private fun renderGroups(groups: List<GroupSummaryDto>) {
        // Calculate totals for the balance hero card
        val totalOwed = groups.sumOf { it.owed ?: maxOf(it.balance, 0.0) }
        val totalOwe  = groups.sumOf { it.owe  ?: abs(minOf(it.balance, 0.0)) }
        val net       = totalOwed - totalOwe

        tvNetBalance.text = if (net >= 0) "+${SessionManager.formatCurrency(net)}" else SessionManager.formatCurrency(net)
        tvOwedToYou.text  = SessionManager.formatCurrency(totalOwed)
        tvYouOwe.text     = SessionManager.formatCurrency(totalOwe)

        llGroups.removeAllViews()

        if (groups.isEmpty()) {
            tvGroupsEmpty.visibility = View.VISIBLE
            return
        }
        tvGroupsEmpty.visibility = View.GONE

        groups.forEach { dto ->
            val card = layoutInflater.inflate(R.layout.item_group_card, llGroups, false)

            card.findViewById<TextView>(R.id.tvGroupEmoji).text =
                emojiForGroup(dto.name)
            card.findViewById<TextView>(R.id.tvGroupName).text    = dto.name
            card.findViewById<TextView>(R.id.tvGroupMembers).text =
                "${dto.members.size} member${if (dto.members.size != 1) "s" else ""}"
            card.findViewById<TextView>(R.id.tvGroupTotal).text   =
                "Total: ${SessionManager.formatCurrency(dto.total)}"

            val badge   = card.findViewById<TextView>(R.id.tvGroupBadge)
            val balance = dto.balance
            when {
                balance > 0 -> {
                    badge.text = "Owed ${SessionManager.formatCurrency(balance)}"
                    badge.background = resources.getDrawable(R.drawable.bg_badge_owed, null)
                    badge.setTextColor(resources.getColor(R.color.green_owed, null))
                }
                balance < 0 -> {
                    badge.text = "Owe ${SessionManager.formatCurrency(abs(balance))}"
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
                    putExtra(GroupDetailActivity.EXTRA_GROUP_ID,    dto.id.toString())
                    putExtra(GroupDetailActivity.EXTRA_GROUP_NAME,  dto.name)
                    putExtra(GroupDetailActivity.EXTRA_GROUP_EMOJI, emojiForGroup(dto.name))
                }
                startActivity(intent)
            }
            card.setOnLongClickListener {
                showDeleteGroupDialog(dto)
                true
            }

            llGroups.addView(card)
        }
    }

    // ── Activity rendering ────────────────────────────────────────────────────

    private fun showDeleteGroupDialog(group: GroupSummaryDto) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete group?")
            .setMessage("Delete ${group.name}? This will remove its expenses and balances.")
            .setPositiveButton("Delete") { _, _ -> deleteGroup(group.id) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteGroup(groupId: Long) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.api.deleteGroup(groupId)
                }
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(requireContext(), "Group deleted", Toast.LENGTH_SHORT).show()
                    loadDashboard()
                } else {
                    showGlobalError(response.body()?.error?.message ?: "Unable to delete group.")
                }
            } catch (_: Exception) {
                showGlobalError("Cannot reach server. Is the backend running?")
            }
        }
    }

    private fun renderActivity(items: List<UserActivityDto>) {
        llActivity.removeAllViews()

        if (items.isEmpty()) {
            tvActivityEmpty.visibility = View.VISIBLE
            return
        }
        tvActivityEmpty.visibility = View.GONE

        // Show at most 5 most recent items on the dashboard
        items.take(5).forEachIndexed { index, item ->
            val row = layoutInflater.inflate(R.layout.item_activity, llActivity, false)

            row.findViewById<TextView>(R.id.tvActivityIcon).text     = emojiForCategory(item.desc)
            row.findViewById<TextView>(R.id.tvActivityTitle).text    = item.desc
            row.findViewById<TextView>(R.id.tvActivitySubtitle).text = item.sub

            val amountTv = row.findViewById<TextView>(R.id.tvActivityAmount)
            val shareAbs = abs(item.share)
            amountTv.text = if (item.positive) "+${SessionManager.formatCurrency(shareAbs)}"
                            else "-${SessionManager.formatCurrency(shareAbs)}"
            amountTv.setTextColor(
                if (item.positive) resources.getColor(R.color.green_owed, null)
                else resources.getColor(R.color.red_owe, null)
            )

            row.setOnClickListener {
                startActivity(Intent(requireContext(), ActivityDetailActivity::class.java).apply {
                    putExtra(ActivityDetailActivity.EXTRA_EXPENSE_ID, item.id.toString())
                })
            }

            // Divider between items
            if (index < minOf(items.size, 5) - 1) {
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

    // ── Logout ────────────────────────────────────────────────────────────────

    private fun showLogoutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Sign out?")
            .setMessage("Are you sure you want to sign out of SplitShare?")
            .setPositiveButton("Sign out") { _, _ ->
                SessionManager.logout()
                startActivity(
                    Intent(requireContext(), LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                )
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun showGroupsLoading(loading: Boolean) {
        tvGroupsLoading.visibility  = if (loading) View.VISIBLE else View.GONE
        llGroups.visibility         = if (loading) View.GONE    else View.VISIBLE
    }

    private fun showActivityLoading(loading: Boolean) {
        tvActivityLoading.visibility = if (loading) View.VISIBLE else View.GONE
        llActivity.visibility        = if (loading) View.GONE    else View.VISIBLE
    }

    private fun showGlobalError(message: String) {
        tvGlobalError.text       = message
        tvGlobalError.visibility = View.VISIBLE
    }

    /** Simple emoji picker based on group name keywords */
    private fun emojiForGroup(name: String): String {
        val lower = name.lowercase()
        return when {
            lower.contains("room") || lower.contains("house") || lower.contains("flat") -> "🏠"
            lower.contains("trip") || lower.contains("travel") || lower.contains("japan")
                || lower.contains("vacation") -> "✈️"
            lower.contains("food") || lower.contains("lunch") || lower.contains("dinner")
                || lower.contains("barkada") -> "🍕"
            lower.contains("gym") || lower.contains("sport") -> "🏋️"
            else -> "👥"
        }
    }

    /** Simple emoji picker based on the expense description */
    private fun emojiForCategory(desc: String): String {
        val lower = desc.lowercase()
        return when {
            lower.contains("groceries") || lower.contains("grocery") -> "🛒"
            lower.contains("dinner") || lower.contains("food") || lower.contains("restaurant") -> "🍜"
            lower.contains("electric") || lower.contains("water") || lower.contains("util")
                || lower.contains("internet") || lower.contains("pldt") -> "⚡"
            lower.contains("transport") || lower.contains("grab") || lower.contains("taxi") -> "🚌"
            lower.contains("settle") -> "💰"
            lower.contains("trip") || lower.contains("plane") || lower.contains("ticket") -> "✈️"
            else -> "💳"
        }
    }
}
