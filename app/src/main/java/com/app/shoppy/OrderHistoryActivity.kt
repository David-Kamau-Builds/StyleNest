package com.app.shoppy

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.shoppy.adapter.OrderAdapter
import com.app.shoppy.databinding.ActivityOrderHistoryBinding
import com.app.shoppy.ui.viewmodel.OrderState
import com.app.shoppy.ui.viewmodel.OrderViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OrderHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderHistoryBinding
    private val orderViewModel: OrderViewModel by viewModels()
    private lateinit var adapter: OrderAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        val sharedPrefs = getSharedPreferences("shoppy_prefs", android.content.Context.MODE_PRIVATE)
        if (!sharedPrefs.getBoolean("is_logged_in", false)) {
            Toast.makeText(this, "Please sign in to view order history", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        binding.btnBack.setOnClickListener { finish() }

        setupRecyclerView()
        observeViewModel()
        
        // Fetch from API
        orderViewModel.fetchUserOrders()
    }

    private fun setupRecyclerView() {
        adapter = OrderAdapter(emptyList()) { orderNumber ->
            val intent = Intent(this, OrderDetailActivity::class.java)
            intent.putExtra("ORDER_NUMBER", orderNumber)
            startActivity(intent)
        }
        binding.rvOrders.layoutManager = LinearLayoutManager(this)
        binding.rvOrders.adapter = adapter
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            orderViewModel.orderState.collect { state ->
                when (state) {
                    is OrderState.Idle -> { }
                    is OrderState.Loading -> {
                        // We could show a progress bar here
                    }
                    is OrderState.Success -> {
                        if (state.orders.isEmpty()) {
                            // Show empty state if needed
                            Toast.makeText(this@OrderHistoryActivity, "No orders found.", Toast.LENGTH_SHORT).show()
                        }
                        adapter.updateData(state.orders)
                    }
                    is OrderState.Error -> {
                        Toast.makeText(this@OrderHistoryActivity, state.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
