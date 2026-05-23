package com.app.shoppy

import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.shoppy.adapter.OrderProductAdapter
import com.app.shoppy.databinding.ActivityOrderDetailBinding
import com.app.shoppy.ui.viewmodel.OrderViewModel
import com.app.shoppy.ui.viewmodel.SharedProductViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OrderDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderDetailBinding
    private val orderViewModel: OrderViewModel by viewModels()
    private val productViewModel: SharedProductViewModel by viewModels()
    private lateinit var adapter: OrderProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val orderNumber = intent.getStringExtra("ORDER_NUMBER")
        if (orderNumber == null) {
            Toast.makeText(this, "Order not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.btnBack.setOnClickListener { finish() }

        adapter = OrderProductAdapter(emptyList(), productViewModel)
        binding.rvOrderItems.layoutManager = LinearLayoutManager(this)
        binding.rvOrderItems.adapter = adapter

        observeViewModel()
        orderViewModel.fetchOrder(orderNumber)
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            orderViewModel.selectedOrder.collect { order ->
                if (order != null) {
                    binding.tvOrderNumber.text = order.orderNumber
                    binding.tvOrderDate.text = order.orderDate
                    binding.tvTotalAmount.text = String.format("KSh %.2f", order.totalAmount)
                    binding.tvDeliveryAddress.text = order.deliveryAddress
                    
                    adapter.updateData(order.orderItems)
                    updateStepper(order.status ?: "Pending Delivery")
                }
            }
        }
    }

    private fun updateStepper(status: String) {
        val colorGold = getColor(R.color.gold)
        val colorOutline = getColor(com.google.android.material.R.color.material_on_surface_stroke)
        
        val colorGoldList = ColorStateList.valueOf(colorGold)
        val colorOutlineList = ColorStateList.valueOf(colorOutline)

        // Reset all
        binding.ivStep1.imageTintList = colorOutlineList
        binding.ivStep2.imageTintList = colorOutlineList
        binding.ivStep3.imageTintList = colorOutlineList
        binding.ivStep4.imageTintList = colorOutlineList
        
        binding.ivStep1.setImageResource(R.drawable.ic_circle_outline)
        binding.ivStep2.setImageResource(R.drawable.ic_circle_outline)
        binding.ivStep3.setImageResource(R.drawable.ic_circle_outline)
        binding.ivStep4.setImageResource(R.drawable.ic_circle_outline)

        when (status) {
            "Pending Delivery" -> {
                binding.ivStep1.setImageResource(R.drawable.ic_check_circle)
                binding.ivStep1.imageTintList = colorGoldList
            }
            "Processing" -> {
                binding.ivStep1.setImageResource(R.drawable.ic_check_circle)
                binding.ivStep1.imageTintList = colorGoldList
                
                binding.ivStep2.setImageResource(R.drawable.ic_check_circle)
                binding.ivStep2.imageTintList = colorGoldList
            }
            "Shipped" -> {
                binding.ivStep1.setImageResource(R.drawable.ic_check_circle)
                binding.ivStep1.imageTintList = colorGoldList
                
                binding.ivStep2.setImageResource(R.drawable.ic_check_circle)
                binding.ivStep2.imageTintList = colorGoldList
                
                binding.ivStep3.setImageResource(R.drawable.ic_check_circle)
                binding.ivStep3.imageTintList = colorGoldList
            }
            "Delivered" -> {
                binding.ivStep1.setImageResource(R.drawable.ic_check_circle)
                binding.ivStep1.imageTintList = colorGoldList
                
                binding.ivStep2.setImageResource(R.drawable.ic_check_circle)
                binding.ivStep2.imageTintList = colorGoldList
                
                binding.ivStep3.setImageResource(R.drawable.ic_check_circle)
                binding.ivStep3.imageTintList = colorGoldList
                
                binding.ivStep4.setImageResource(R.drawable.ic_check_circle)
                binding.ivStep4.imageTintList = colorGoldList
            }
            else -> {
                // Default to Pending Delivery
                binding.ivStep1.setImageResource(R.drawable.ic_check_circle)
                binding.ivStep1.imageTintList = colorGoldList
            }
        }
    }
}
