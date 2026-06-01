package com.app.stylenest

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.stylenest.adapter.OrderAdapter
import com.app.stylenest.data.DatabaseHelper
import com.app.stylenest.databinding.ActivityOrderHistoryBinding
import com.app.stylenest.model.Order

class OrderHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderHistoryBinding
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        dbHelper = DatabaseHelper(this)

        binding.btnBack.setOnClickListener { finish() }

        loadOrders()
    }

    private fun loadOrders() {
        val ordersList = mutableListOf<Order>()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT id, order_number, total_amount, order_date, status FROM orders ORDER BY id DESC", null)
        
        if (cursor.moveToFirst()) {
            do {
                ordersList.add(Order(
                    id = cursor.getInt(0),
                    orderNumber = cursor.getString(1),
                    totalAmount = cursor.getDouble(2),
                    orderDate = cursor.getString(3),
                    status = cursor.getString(4)
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()

        val adapter = OrderAdapter(ordersList)
        binding.rvOrders.layoutManager = LinearLayoutManager(this)
        binding.rvOrders.adapter = adapter
    }
}
