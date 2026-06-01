package com.app.stylenest

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.app.stylenest.data.DatabaseHelper
import com.app.stylenest.data.StyleNestRepository
import com.app.stylenest.databinding.ActivityCheckoutBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CheckoutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCheckoutBinding
    private lateinit var repository: StyleNestRepository
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = StyleNestRepository(this)
        dbHelper = DatabaseHelper(this)

        binding.btnBack.setOnClickListener { finish() }

        // Localized Nairobi dropdown as requested in the brief
        val estates = arrayOf("Westlands", "Kilimani", "CBD", "Ngong Road", "Lang'ata", "Kileleshwa", "South B", "South C")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, estates)
        binding.spinnerEstate.adapter = adapter

        binding.btnConfirmOrder.setOnClickListener {
            processCheckout()
        }
    }

    private fun processCheckout() {
        val name = binding.etFullName.text.toString()
        val phone = binding.etPhone.text.toString()
        val estate = binding.spinnerEstate.selectedItem.toString()
        
        val paymentMethod = if(binding.rgPaymentMethod.checkedRadioButtonId == R.id.rbMpesa) {
            "M-Pesa"
        } else {
            "Cash on Delivery"
        }

        if(name.isBlank() || phone.isBlank()) {
            Toast.makeText(this, "Please fill in all delivery details.", Toast.LENGTH_SHORT).show()
            return
        }

        val cartItems = repository.getCartItems()
        if(cartItems.isEmpty()) {
            Toast.makeText(this, "Your cart is empty!", Toast.LENGTH_SHORT).show()
            return
        }

        var totalAmount = 0.0
        for (item in cartItems) {
            totalAmount += (item.price * item.quantity)
        }

        val orderNumber = "ST-${System.currentTimeMillis().toString().takeLast(6)}"
        val dateString = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())

        val db = dbHelper.writableDatabase
        
        // 1. Insert the Order Log into SQLite
        val orderValues = ContentValues().apply {
            put("order_number", orderNumber)
            put("customer_name", name)
            put("phone", phone)
            put("delivery_address", estate)
            put("payment_method", paymentMethod)
            put("total_amount", totalAmount)
            put("order_date", dateString)
            put("status", "Pending Delivery")
        }
        val orderId = db.insert("orders", null, orderValues)

        // 2. Insert individual Order Items
        for(item in cartItems) {
            val itemValues = ContentValues().apply {
                put("order_id", orderId)
                put("product_id", item.productId)
                put("size", item.selectedSize)
                put("quantity", item.quantity)
                put("price", item.price)
            }
            db.insert("order_items", null, itemValues)
        }

        // 3. Automatically clear the user's active Cart
        db.delete("cart", null, null)

        // 4. Transition to Success Screen
        val intent = Intent(this, OrderSuccessActivity::class.java)
        intent.putExtra("ORDER_NUMBER", orderNumber)
        startActivity(intent)
        finish()
    }
}
