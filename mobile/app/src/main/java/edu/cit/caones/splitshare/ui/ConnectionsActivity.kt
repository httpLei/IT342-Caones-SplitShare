package edu.cit.caones.splitshare.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import edu.cit.caones.splitshare.R
import edu.cit.caones.splitshare.network.RetrofitClient
import edu.cit.caones.splitshare.network.dto.UserConnectionDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ConnectionsActivity : AppCompatActivity() {

    private lateinit var rvConnections: RecyclerView
    private lateinit var tvEmptyState: TextView
    private lateinit var toolbar: MaterialToolbar
    private val connectionsList = mutableListOf<UserConnectionDto>()
    private lateinit var adapter: ConnectionsAdapter
    private var currentType: String = "followers"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connections)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        rvConnections = findViewById(R.id.rvConnections)
        tvEmptyState = findViewById(R.id.tvEmptyState)

        currentType = intent.getStringExtra("CONNECTION_TYPE") ?: "followers"
        supportActionBar?.title = if (currentType == "followers") "Followers" else "Following"

        adapter = ConnectionsAdapter(connectionsList)
        rvConnections.layoutManager = LinearLayoutManager(this)
        rvConnections.adapter = adapter

        loadConnections()
    }

    private fun loadConnections() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val res = if (currentType == "followers") {
                    RetrofitClient.api.getFollowers()
                } else {
                    RetrofitClient.api.getFollowing()
                }

                if (res.isSuccessful && res.body()?.success == true) {
                    val data = res.body()?.data ?: emptyList()
                    withContext(Dispatchers.Main) {
                        connectionsList.clear()
                        connectionsList.addAll(data)
                        adapter.notifyDataSetChanged()
                        if (connectionsList.isEmpty()) {
                            tvEmptyState.visibility = View.VISIBLE
                            rvConnections.visibility = View.GONE
                        } else {
                            tvEmptyState.visibility = View.GONE
                            rvConnections.visibility = View.VISIBLE
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun handleUnfollow(userId: Long) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val res = RetrofitClient.api.unfollowUser(userId)
                withContext(Dispatchers.Main) {
                    if (res.isSuccessful && res.body()?.success == true) {
                        Toast.makeText(this@ConnectionsActivity, "Unfollowed user", Toast.LENGTH_SHORT).show()
                        loadConnections()
                    } else {
                        val msg = res.body()?.error?.message ?: "Failed to unfollow"
                        Toast.makeText(this@ConnectionsActivity, msg, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ConnectionsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    inner class ConnectionsAdapter(private val list: List<UserConnectionDto>) : RecyclerView.Adapter<ConnectionsAdapter.ViewHolder>() {
        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvInitials: TextView = view.findViewById(R.id.tvPersonInitials)
            val tvName: TextView = view.findViewById(R.id.tvPersonName)
            val tvEmail: TextView = view.findViewById(R.id.tvPersonEmail)
            val tvBadge: TextView = view.findViewById(R.id.tvPersonBadge)
            val btnFollow: MaterialButton = view.findViewById(R.id.btnFollowToggle)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_people_result, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            val fullName = "${item.firstname} ${item.lastname}".trim()
            holder.tvName.text = fullName
            holder.tvEmail.text = item.email
            holder.tvInitials.text = fullName.take(2).uppercase()
            
            holder.tvBadge.visibility = if (item.mutual) View.VISIBLE else View.GONE
            
            if (currentType == "following") {
                holder.btnFollow.visibility = View.VISIBLE
                holder.btnFollow.text = "Unfollow"
                holder.btnFollow.setOnClickListener {
                    handleUnfollow(item.id)
                }
            } else {
                if (item.mutual) {
                    holder.btnFollow.visibility = View.VISIBLE
                    holder.btnFollow.text = "Unfollow"
                    holder.btnFollow.setOnClickListener {
                        handleUnfollow(item.id)
                    }
                } else {
                    holder.btnFollow.visibility = View.GONE
                }
            }
        }

        override fun getItemCount() = list.size
    }
}