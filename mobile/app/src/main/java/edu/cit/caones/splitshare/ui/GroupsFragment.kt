package edu.cit.caones.splitshare.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import edu.cit.caones.splitshare.R
import edu.cit.caones.splitshare.SessionManager
import edu.cit.caones.splitshare.network.RetrofitClient
import edu.cit.caones.splitshare.network.dto.CreateGroupRequest
import edu.cit.caones.splitshare.network.dto.GroupSummaryDto
import edu.cit.caones.splitshare.network.dto.UserConnectionDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.abs

class GroupsFragment : Fragment() {

    private lateinit var btnNewGroup: MaterialButton
    private lateinit var etPeopleSearch: EditText
    private lateinit var btnSearchPeople: MaterialButton
    private lateinit var tvPeopleLoading: TextView
    private lateinit var tvPeopleEmpty: TextView
    private lateinit var tvPeopleError: TextView
    private lateinit var llPeopleResults: LinearLayout
    private lateinit var tvError: TextView
    private lateinit var tvLoading: TextView
    private lateinit var tvEmpty: TextView
    private lateinit var llGroups: LinearLayout

    private var mutualUsers: List<UserConnectionDto> = emptyList()
    private var searchJob: Job? = null

    

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_groups, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnNewGroup = view.findViewById(R.id.btnNewGroup)
        etPeopleSearch = view.findViewById(R.id.etPeopleSearch)
        btnSearchPeople = view.findViewById(R.id.btnSearchPeople)
        tvPeopleLoading = view.findViewById(R.id.tvPeopleLoading)
        tvPeopleEmpty = view.findViewById(R.id.tvPeopleEmpty)
        tvPeopleError = view.findViewById(R.id.tvPeopleError)
        llPeopleResults = view.findViewById(R.id.llPeopleResults)
        tvError = view.findViewById(R.id.tvGroupsError)
        tvLoading = view.findViewById(R.id.tvGroupsLoading)
        tvEmpty = view.findViewById(R.id.tvGroupsEmpty)
        llGroups = view.findViewById(R.id.llGroups)

        btnNewGroup.setOnClickListener { openCreateGroupDialog() }
        btnSearchPeople.setOnClickListener { searchPeople() }
        etPeopleSearch.addTextChangedListener { editable ->
            searchJob?.cancel()
            searchJob = viewLifecycleOwner.lifecycleScope.launch {
                delay(250)
                searchPeople(editable?.toString().orEmpty())
            }
        }
        loadGroups()
        loadMutuals()
    }

    override fun onResume() {
        super.onResume()
        loadGroups()
        loadMutuals()
    }

    private fun loadGroups() {
        tvError.visibility = View.GONE
        tvLoading.visibility = View.VISIBLE
        tvEmpty.visibility = View.GONE
        llGroups.visibility = View.GONE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.api.getGroups()
                }

                if (response.isSuccessful && response.body()?.success == true) {
                    renderGroups(response.body()?.data ?: emptyList())
                } else {
                    showError(response.body()?.error?.message ?: "Failed to load groups.")
                }
            } catch (_: Exception) {
                showError("Cannot reach server. Is the backend running?")
            } finally {
                tvLoading.visibility = View.GONE
            }
        }
    }

    private fun loadMutuals() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.api.getMutuals()
                }

                if (response.isSuccessful && response.body()?.success == true) {
                    mutualUsers = response.body()?.data ?: emptyList()
                }
            } catch (_: Exception) {
                // Non-blocking. Create group dialog will just show no available users.
            }
        }
    }

    private fun searchPeople(raw: String = etPeopleSearch.text?.toString().orEmpty()) {
        val keyword = raw.trim()
        tvPeopleError.visibility = View.GONE
        tvPeopleEmpty.visibility = View.GONE

        if (keyword.isBlank()) {
            llPeopleResults.removeAllViews()
            tvPeopleEmpty.text = "Type a name or email to search people."
            tvPeopleEmpty.visibility = View.VISIBLE
            return
        }

        tvPeopleLoading.visibility = View.VISIBLE
        llPeopleResults.visibility = View.GONE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.api.searchUsers(keyword)
                }

                if (response.isSuccessful && response.body()?.success == true) {
                    renderPeople(response.body()?.data ?: emptyList(), keyword)
                } else {
                    showPeopleError(response.body()?.error?.message ?: "Unable to search people.")
                }
            } catch (_: Exception) {
                showPeopleError("Cannot reach server. Is the backend running?")
            } finally {
                tvPeopleLoading.visibility = View.GONE
            }
        }
    }

    private fun renderPeople(users: List<UserConnectionDto>, keyword: String) {
        llPeopleResults.removeAllViews()

        if (users.isEmpty()) {
            tvPeopleEmpty.text = "No people found."
            tvPeopleEmpty.visibility = View.VISIBLE
            llPeopleResults.visibility = View.GONE
            return
        }

        tvPeopleEmpty.visibility = View.GONE
        llPeopleResults.visibility = View.VISIBLE

        users.forEach { user ->
            val row = layoutInflater.inflate(R.layout.item_people_result, llPeopleResults, false)
            row.findViewById<TextView>(R.id.tvPersonInitials).text = initialsFor(user.firstname, user.lastname)
            row.findViewById<TextView>(R.id.tvPersonName).text = "${user.firstname} ${user.lastname}"
            row.findViewById<TextView>(R.id.tvPersonEmail).text = user.email

            val badge = row.findViewById<TextView>(R.id.tvPersonBadge)
            badge.text = when {
                user.mutual -> "Mutual"
                user.following -> "Following"
                user.followedBy -> "Follows you"
                else -> "Not followed"
            }

            val followButton = row.findViewById<MaterialButton>(R.id.btnFollowToggle)
            followButton.text = if (user.following) "Unfollow" else "Follow"
            followButton.setOnClickListener {
                toggleFollow(user, keyword)
            }

            row.setOnClickListener {
                etPeopleSearch.setText(user.email)
            }

            llPeopleResults.addView(row)
        }
    }

    private fun openCreateGroupDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_create_group, null)
        val etGroupName = dialogView.findViewById<EditText>(R.id.etGroupName)
        val llMutuals = dialogView.findViewById<LinearLayout>(R.id.llMutuals)
        val tvDialogError = dialogView.findViewById<TextView>(R.id.tvDialogError)

        llMutuals.removeAllViews()
        if (mutualUsers.isEmpty()) {
            llMutuals.addView(TextView(requireContext()).apply {
                text = "No mutual users yet. Search and follow people on the web or wait until mutuals are available."
                setTextColor(resources.getColor(R.color.text_secondary, null))
                textSize = 13f
            })
        } else {
            mutualUsers.forEach { user ->
                val checkBox = CheckBox(requireContext()).apply {
                    text = "${user.firstname} ${user.lastname} · ${user.email}"
                    tag = user.email
                }
                llMutuals.addView(checkBox)
            }
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialogView.findViewById<MaterialButton>(R.id.btnCancelGroup).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<MaterialButton>(R.id.btnCreateGroup).setOnClickListener {
            val groupName = etGroupName.text?.toString()?.trim().orEmpty()
            val selectedEmails = buildList {
                for (index in 0 until llMutuals.childCount) {
                    val child = llMutuals.getChildAt(index)
                    if (child is CheckBox && child.isChecked) {
                        add(child.tag.toString())
                    }
                }
            }

            if (groupName.isBlank()) {
                tvDialogError.text = "Group name is required."
                tvDialogError.visibility = View.VISIBLE
                return@setOnClickListener
            }

            createGroup(groupName, selectedEmails, dialog, tvDialogError)
        }

        dialog.show()
    }

    private fun createGroup(name: String, memberEmails: List<String>, dialog: android.app.AlertDialog, tvDialogError: TextView) {
        tvDialogError.visibility = View.GONE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.api.createGroup(CreateGroupRequest(name, memberEmails))
                }

                if (response.isSuccessful && response.body()?.success == true) {
                    dialog.dismiss()
                    etPeopleSearch.setText("")
                    searchPeople()
                    loadGroups()
                } else {
                    tvDialogError.text = response.body()?.error?.message ?: "Unable to create group."
                    tvDialogError.visibility = View.VISIBLE
                }
            } catch (_: Exception) {
                tvDialogError.text = "Cannot reach server. Is the backend running?"
                tvDialogError.visibility = View.VISIBLE
            }
        }
    }

    private fun renderGroups(groups: List<GroupSummaryDto>) {
        llGroups.removeAllViews()

        if (groups.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            llGroups.visibility = View.GONE
            return
        }

        tvEmpty.visibility = View.GONE
        llGroups.visibility = View.VISIBLE

        groups.forEach { dto ->
            val card = layoutInflater.inflate(R.layout.item_group_card, llGroups, false)

            card.findViewById<TextView>(R.id.tvGroupEmoji).text = emojiForGroup(dto.name)
            card.findViewById<TextView>(R.id.tvGroupName).text = dto.name
            card.findViewById<TextView>(R.id.tvGroupMembers).text =
                "${dto.members.size} member${if (dto.members.size != 1) "s" else ""}"
            card.findViewById<TextView>(R.id.tvGroupTotal).text = "Total: ${SessionManager.formatCurrency(dto.total)}"

            val badge = card.findViewById<TextView>(R.id.tvGroupBadge)
            when {
                dto.balance > 0 -> {
                    badge.text = "Owed ${SessionManager.formatCurrency(dto.balance)}"
                    badge.background = resources.getDrawable(R.drawable.bg_badge_owed, null)
                    badge.setTextColor(resources.getColor(R.color.green_owed, null))
                }
                dto.balance < 0 -> {
                    badge.text = "Owe ${SessionManager.formatCurrency(abs(dto.balance))}"
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
                startActivity(Intent(requireContext(), GroupDetailActivity::class.java).apply {
                    putExtra(GroupDetailActivity.EXTRA_GROUP_ID, dto.id.toString())
                    putExtra(GroupDetailActivity.EXTRA_GROUP_NAME, dto.name)
                    putExtra(GroupDetailActivity.EXTRA_GROUP_EMOJI, emojiForGroup(dto.name))
                })
            }
            card.setOnLongClickListener {
                showDeleteGroupDialog(dto)
                true
            }

            llGroups.addView(card)
        }
    }

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
                    loadGroups()
                } else {
                    showError(response.body()?.error?.message ?: "Unable to delete group.")
                }
            } catch (_: Exception) {
                showError("Cannot reach server. Is the backend running?")
            }
        }
    }

    private fun showError(message: String) {
        tvError.text = message
        tvError.visibility = View.VISIBLE
        tvEmpty.visibility = View.GONE
        llGroups.visibility = View.GONE
    }

    private fun showPeopleError(message: String) {
        tvPeopleError.text = message
        tvPeopleError.visibility = View.VISIBLE
        tvPeopleEmpty.visibility = View.GONE
        llPeopleResults.visibility = View.GONE
    }

    private fun toggleFollow(user: UserConnectionDto, keyword: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    if (user.following) {
                        RetrofitClient.api.unfollowUser(user.id)
                    } else {
                        RetrofitClient.api.followUser(user.id)
                    }
                }

                if (response.isSuccessful && response.body()?.success == true) {
                    loadMutuals()
                    searchPeople(keyword)
                } else {
                    showPeopleError(response.body()?.error?.message ?: "Unable to update follow status.")
                }
            } catch (_: Exception) {
                showPeopleError("Cannot reach server. Is the backend running?")
            }
        }
    }

    private fun initialsFor(firstname: String, lastname: String): String {
        val first = firstname.firstOrNull()?.uppercaseChar()?.toString().orEmpty()
        val last = lastname.firstOrNull()?.uppercaseChar()?.toString().orEmpty()
        return (first + last).ifBlank { "?" }
    }

    private fun emojiForGroup(name: String): String {
        return when (name.lowercase(Locale.getDefault())) {
            "roommates", "home", "house" -> "🏠"
            "trip", "travel", "vacation" -> "✈️"
            "food", "lunch", "dinner" -> "🍔"
            "bills", "utilities" -> "⚡"
            else -> "👥"
        }
    }
}
