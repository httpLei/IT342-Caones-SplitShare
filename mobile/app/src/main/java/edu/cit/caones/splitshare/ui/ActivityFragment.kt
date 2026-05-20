package edu.cit.caones.splitshare.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import edu.cit.caones.splitshare.R
import edu.cit.caones.splitshare.SessionManager
import edu.cit.caones.splitshare.network.RetrofitClient
import edu.cit.caones.splitshare.network.dto.UserActivityDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.abs

class ActivityFragment : Fragment() {

    private lateinit var etActivitySearch: EditText
    private lateinit var tvActivityLoading: TextView
    private lateinit var tvActivityEmpty: TextView
    private lateinit var tvActivityError: TextView
    private lateinit var llActivity: LinearLayout

    private var allActivity: List<UserActivityDto> = emptyList()
    private var searchJob: Job? = null

    

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_activity, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etActivitySearch = view.findViewById(R.id.etActivitySearch)
        tvActivityLoading = view.findViewById(R.id.tvActivityLoading)
        tvActivityEmpty = view.findViewById(R.id.tvActivityEmpty)
        tvActivityError = view.findViewById(R.id.tvActivityError)
        llActivity = view.findViewById(R.id.llActivity)

        etActivitySearch.addTextChangedListener { editable ->
            searchJob?.cancel()
            searchJob = viewLifecycleOwner.lifecycleScope.launch {
                delay(200)
                renderActivity(filterActivity(editable?.toString().orEmpty()))
            }
        }

        loadActivity()
    }

    override fun onResume() {
        super.onResume()
        loadActivity()
    }

    private fun loadActivity() {
        tvActivityError.visibility = View.GONE
        tvActivityLoading.visibility = View.VISIBLE
        tvActivityEmpty.visibility = View.GONE
        llActivity.visibility = View.GONE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.api.getMyHistory()
                }

                if (response.isSuccessful && response.body()?.success == true) {
                    allActivity = response.body()?.data ?: emptyList()
                    renderActivity(filterActivity(etActivitySearch.text?.toString().orEmpty()))
                } else {
                    showError(response.body()?.error?.message ?: "Unable to load activity history.")
                }
            } catch (_: Exception) {
                showError("Cannot reach server. Is the backend running?")
            } finally {
                tvActivityLoading.visibility = View.GONE
            }
        }
    }

    private fun filterActivity(keywordRaw: String): List<UserActivityDto> {
        val keyword = keywordRaw.trim().lowercase()
        if (keyword.isBlank()) return allActivity

        return allActivity.filter { item ->
            item.desc.lowercase().contains(keyword) ||
                item.sub.lowercase().contains(keyword) ||
                item.amount.toString().contains(keyword) ||
                abs(item.share).toString().contains(keyword)
        }
    }

    private fun renderActivity(items: List<UserActivityDto>) {
        llActivity.removeAllViews()

        if (items.isEmpty()) {
            tvActivityEmpty.visibility = View.VISIBLE
            llActivity.visibility = View.GONE
            return
        }

        tvActivityEmpty.visibility = View.GONE
        llActivity.visibility = View.VISIBLE

        items.forEachIndexed { index, item ->
            val row = layoutInflater.inflate(R.layout.item_activity, llActivity, false)
            row.findViewById<TextView>(R.id.tvActivityIcon).text = emojiForCategory(item.desc)
            row.findViewById<TextView>(R.id.tvActivityTitle).text = item.desc
            row.findViewById<TextView>(R.id.tvActivitySubtitle).text = item.sub

            val amountTv = row.findViewById<TextView>(R.id.tvActivityAmount)
            val shareAbs = abs(item.share)
            amountTv.text = if (item.positive) "+${SessionManager.formatCurrency(shareAbs)}" else "-${SessionManager.formatCurrency(shareAbs)}"
            amountTv.setTextColor(
                if (item.positive) resources.getColor(R.color.green_owed, null)
                else resources.getColor(R.color.red_owe, null)
            )

            row.setOnClickListener {
                startActivity(Intent(requireContext(), ActivityDetailActivity::class.java).apply {
                    putExtra(ActivityDetailActivity.EXTRA_EXPENSE_ID, item.id.toString())
                })
            }

            llActivity.addView(row)

            if (index < items.lastIndex) {
                val divider = View(requireContext())
                divider.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1
                )
                divider.setBackgroundColor(resources.getColor(R.color.divider, null))
                llActivity.addView(divider)
            }
        }
    }

    private fun showError(message: String) {
        tvActivityError.text = message
        tvActivityError.visibility = View.VISIBLE
        tvActivityEmpty.visibility = View.GONE
        llActivity.visibility = View.GONE
    }

    private fun emojiForCategory(desc: String): String {
        val lower = desc.lowercase()
        return when {
            lower.contains("groceries") || lower.contains("grocery") -> "🛒"
            lower.contains("dinner") || lower.contains("food") || lower.contains("restaurant") -> "🍜"
            lower.contains("electric") || lower.contains("water") || lower.contains("util") || lower.contains("internet") -> "⚡"
            lower.contains("transport") || lower.contains("grab") || lower.contains("taxi") -> "🚌"
            lower.contains("settle") -> "💰"
            lower.contains("trip") || lower.contains("plane") || lower.contains("ticket") -> "✈️"
            else -> "💳"
        }
    }
}