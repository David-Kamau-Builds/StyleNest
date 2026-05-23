package com.app.shoppy

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.shoppy.adapter.OrderAdapter
import com.app.shoppy.data.DatabaseHelper
import com.app.shoppy.databinding.ActivityOrderHistoryBinding
import com.app.shoppy.model.Order

class OrderHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderHistoryBinding
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        dbHelper = DatabaseHelper(this)

        val sharedPrefs = getSharedPreferences("shoppy_prefs", android.content.Context.MODE_PRIVATE)
        if (!sharedPrefs.getBoolean("is_logged_in", false)) {
            android.widget.Toast.makeText(this, "Please sign in to view order history", android.widget.Toast.LENGTH_SHORT).show()
            startActivity(android.content.Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        binding.btnBack.setOnClickListener { finish() }

        loadOrders()
    }

    private fun loadOrders() {
        val sharedPrefs = getSharedPreferences("shoppy_prefs", android.content.Context.MODE_PRIVATE)
        val email = sharedPrefs.getString("user_email", "") ?: ""
        
        val ordersList = mutableListOf<Order>()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT id, order_number, total_amount, order_date, status FROM orders WHERE user_email = ? ORDER BY id DESC", arrayOf(email))
        
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
