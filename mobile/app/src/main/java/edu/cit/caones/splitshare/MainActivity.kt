package edu.cit.caones.splitshare

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import edu.cit.caones.splitshare.network.RetrofitClient
import edu.cit.caones.splitshare.network.dto.GroupSummaryDto
import edu.cit.caones.splitshare.ui.AddExpenseActivity
import edu.cit.caones.splitshare.ui.ActivityFragment
import edu.cit.caones.splitshare.ui.GroupsFragment
import edu.cit.caones.splitshare.ui.HomeFragment
import edu.cit.caones.splitshare.ui.LoginActivity
import edu.cit.caones.splitshare.ui.SettingsFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!SessionManager.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.fragmentContainer)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        setupNavigation()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, HomeFragment())
                .commit()
            findViewById<BottomNavigationView>(R.id.bottomNav).selectedItemId = R.id.nav_dashboard
        }
    }

    private fun setupNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, HomeFragment())
                        .commit()
                    true
                }
                R.id.nav_groups -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, GroupsFragment())
                        .commit()
                    true
                }
                R.id.nav_activity -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, ActivityFragment())
                        .commit()
                    true
                }
                R.id.nav_settings -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, SettingsFragment())
                        .commit()
                    true
                }
                R.id.nav_add -> {
                    showAddExpenseGroupPicker()
                    false
                }
                else -> false
            }
        }
    }

    private fun showAddExpenseGroupPicker() {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) { RetrofitClient.api.getGroups() }
                if (response.isSuccessful && response.body()?.success == true) {
                    val groups: List<GroupSummaryDto> = response.body()?.data ?: emptyList()
                    if (groups.isEmpty()) {
                        startActivity(Intent(this@MainActivity, AddExpenseActivity::class.java))
                    } else {
                        val names = groups.map { it.name }.toTypedArray()
                        AlertDialog.Builder(this@MainActivity)
                            .setTitle("Add expense to group")
                            .setItems(names) { _, which: Int ->
                                val g = groups[which]
                                startActivity(Intent(this@MainActivity, AddExpenseActivity::class.java).apply {
                                    putExtra(AddExpenseActivity.EXTRA_GROUP_ID, g.id.toString())
                                    putExtra(AddExpenseActivity.EXTRA_GROUP_NAME, g.name)
                                })
                            }
                            .setNegativeButton("Cancel", null)
                            .show()
                    }
                } else {
                    startActivity(Intent(this@MainActivity, AddExpenseActivity::class.java))
                }
            } catch (_: Exception) {
                startActivity(Intent(this@MainActivity, AddExpenseActivity::class.java))
            }
        }
    }
}
