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

    private var currentStep = 0
    private var totalAmount = 0.0

    // Launcher that handles the result of the login gate at checkout
    private val loginForCheckout = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        val sharedPrefs = getSharedPreferences("stylenest_prefs", android.content.Context.MODE_PRIVATE)
        if (sharedPrefs.getBoolean("is_logged_in", false)) {
            // User just logged in — set up the checkout UI now
            setupCheckoutUI()
        } else {
            // User dismissed login without signing in — go back to cart
            Toast.makeText(this, "Sign in required to checkout", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = StyleNestRepository(this)
        dbHelper = DatabaseHelper(this)

        binding.btnBack.setOnClickListener { finish() }

        val sharedPrefs = getSharedPreferences("stylenest_prefs", android.content.Context.MODE_PRIVATE)
        val isLoggedIn = sharedPrefs.getBoolean("is_logged_in", false)

        if (!isLoggedIn) {
            // Show a friendly dialog explaining why login is needed
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("🛒 Sign in to Checkout")
                .setMessage("Your cart is saved! Sign in to complete your order and track your delivery.")
                .setPositiveButton("Sign In") { _, _ ->
                    loginForCheckout.launch(Intent(this, LoginActivity::class.java))
                }
                .setNegativeButton("Not Now") { _, _ -> finish() }
                .setCancelable(false)
                .show()
        } else {
            setupCheckoutUI()
        }
    }

    private fun setupCheckoutUI() {

        val countries = arrayOf("Kenya", "Uganda", "Tanzania", "Rwanda", "South Africa", "Nigeria", "United States", "United Kingdom")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, countries)
        binding.autoCompleteCountry.setAdapter(adapter)
        binding.autoCompleteCountry.setText(countries[0], false) // Pre-select Kenya

        val cartItems = repository.getCartItems()
        for (item in cartItems) {
            totalAmount += (item.price * item.quantity)
        }

        binding.ccp.registerCarrierNumberEditText(binding.etPhone)
        
        // Ensure a default country is selected if auto-detect fails
        binding.ccp.setDefaultCountryUsingNameCode("KE")
        binding.ccp.resetToDefaultCountry()
        
        // Set CCP colors from the active Material theme — no hardcoded BLACK/WHITE
        val ccpContentColor = com.google.android.material.color.MaterialColors.getColor(
            binding.ccp, com.google.android.material.R.attr.colorOnSurface, android.graphics.Color.BLACK
        )
        val ccpBgColor = com.google.android.material.color.MaterialColors.getColor(
            binding.ccp, com.google.android.material.R.attr.colorSurface, android.graphics.Color.WHITE
        )
        binding.ccp.setContentColor(ccpContentColor)
        binding.ccp.setDialogTextColor(ccpContentColor)
        binding.ccp.setDialogBackgroundColor(ccpBgColor)
        
        val sharedPrefs = getSharedPreferences("stylenest_prefs", android.content.Context.MODE_PRIVATE)
        binding.etFullName.setText(sharedPrefs.getString("user_name", ""))
        binding.etEmail.setText(sharedPrefs.getString("user_email", ""))
        val savedPhone = sharedPrefs.getString("user_phone", "")
        if (!savedPhone.isNullOrBlank()) binding.etPhone.setText(savedPhone)
        
        val savedCountry = sharedPrefs.getString("user_country", "")
        if (!savedCountry.isNullOrBlank()) {
            binding.autoCompleteCountry.setText(savedCountry, false)
        }
        
        binding.etCity.setText(sharedPrefs.getString("user_city", ""))
        binding.etDistrict.setText(sharedPrefs.getString("user_district", ""))
        binding.etStreet.setText(sharedPrefs.getString("user_street", ""))

        updateStepUI()

        binding.btnNext.setOnClickListener {
            handleNextStep()
        }

        binding.btnPrevious.setOnClickListener {
            handlePreviousStep()
        }
        
        binding.rgPaymentMethod.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == com.app.stylenest.R.id.rbMpesa) {
                binding.mpesaContainer.visibility = android.view.View.VISIBLE
                binding.cardContainer.visibility = android.view.View.GONE
            } else {
                binding.mpesaContainer.visibility = android.view.View.GONE
                binding.cardContainer.visibility = android.view.View.VISIBLE
            }
        }
    }

    private fun handleNextStep() {
        if (currentStep == 0) {
            val name = binding.etFullName.text.toString()
            val email = binding.etEmail.text.toString()
            val phone = binding.etPhone.text.toString()
            val country = binding.autoCompleteCountry.text.toString()
            val city = binding.etCity.text.toString()
            val district = binding.etDistrict.text.toString()
            val street = binding.etStreet.text.toString()

            var isValid = true

            if (name.isBlank()) { binding.tilFullName.error = "Required *"; isValid = false } else { binding.tilFullName.error = null }
            if (email.isBlank()) { binding.tilEmail.error = "Required *"; isValid = false } else { binding.tilEmail.error = null }
            
            if (phone.isBlank()) {
                binding.tilPhone.error = "Required *"
                isValid = false
            } else if (!binding.ccp.isValidFullNumber) {
                binding.tilPhone.error = "Invalid phone format *"
                isValid = false
            } else {
                binding.tilPhone.error = null
            }
            
            if (country.isBlank()) { binding.tilCountry.error = "Required *"; isValid = false } else { binding.tilCountry.error = null }
            if (city.isBlank()) { binding.tilCity.error = "Required *"; isValid = false } else { binding.tilCity.error = null }
            if (district.isBlank()) { binding.tilDistrict.error = "Required *"; isValid = false } else { binding.tilDistrict.error = null }
            if (street.isBlank()) { binding.tilStreet.error = "Required *"; isValid = false } else { binding.tilStreet.error = null }

            if (!isValid) {
                Toast.makeText(this, "Please fix the highlighted errors.", Toast.LENGTH_SHORT).show()
                return
            }
            
            // Auto-save checkout details for next time so the user doesn't have to re-enter
            getSharedPreferences("stylenest_prefs", android.content.Context.MODE_PRIVATE).edit().apply {
                putString("user_name", name)
                putString("user_email", email)
                putString("user_phone", binding.etPhone.text.toString())
                putString("user_country", country)
                putString("user_city", city)
                putString("user_district", district)
                putString("user_street", street)
                apply()
            }
        }
        
        if (currentStep == 1) {
            val isMpesa = binding.rgPaymentMethod.checkedRadioButtonId == com.app.stylenest.R.id.rbMpesa
            var isValid = true
            
            if (isMpesa) {
                val mpesaPhone = binding.etMpesaPhone.text.toString().trim()
                val mpesaRegex = "^(?:254|\\+254|0)?([17][0-9]{8})$".toRegex()
                if (mpesaPhone.isBlank()) {
                    binding.tilMpesaPhone.error = "Required *"
                    isValid = false
                } else if (!mpesaRegex.matches(mpesaPhone)) {
                    binding.tilMpesaPhone.error = "Invalid M-Pesa number (e.g. 0712345678) *"
                    isValid = false
                } else {
                    binding.tilMpesaPhone.error = null
                }
            } else {
                val cardNum = binding.etCardNumber.text.toString().replace(" ", "").trim()
                val cardExp = binding.etCardExpiry.text.toString().trim()
                val cardCvv = binding.etCardCvv.text.toString().trim()
                
                if (cardNum.isBlank()) { 
                    binding.tilCardNumber.error = "Required *"
                    isValid = false 
                } else if (cardNum.length < 13 || cardNum.length > 19 || !cardNum.all { it.isDigit() }) {
                    binding.tilCardNumber.error = "Invalid Card Number *"
                    isValid = false 
                } else { 
                    binding.tilCardNumber.error = null 
                }
                
                val expRegex = "^(0[1-9]|1[0-2])/?([0-9]{2})$".toRegex()
                if (cardExp.isBlank()) { 
                    binding.tilCardExpiry.error = "Required *"
                    isValid = false 
                } else if (!expRegex.matches(cardExp)) {
                    binding.tilCardExpiry.error = "Format MM/YY *"
                    isValid = false 
                } else { 
                    binding.tilCardExpiry.error = null 
                }
                
                if (cardCvv.isBlank()) { 
                    binding.tilCardCvv.error = "Required *"
                    isValid = false 
                } else if (cardCvv.length !in 3..4 || !cardCvv.all { it.isDigit() }) {
                    binding.tilCardCvv.error = "Invalid CVV *"
                    isValid = false 
                } else { 
                    binding.tilCardCvv.error = null 
                }
            }
            
            if (!isValid) {
                Toast.makeText(this, "Please fix payment errors.", Toast.LENGTH_SHORT).show()
                return
            }

            val name = binding.etFullName.text.toString()
            val email = binding.etEmail.text.toString()
            val phone = binding.ccp.fullNumberWithPlus
            val fullAddress = "${binding.etStreet.text}\n${binding.etDistrict.text}, ${binding.etCity.text}\n${binding.autoCompleteCountry.text}"
            val paymentMethod = if(isMpesa) "M-Pesa (${binding.etMpesaPhone.text})" else "Credit Card (ending in ${binding.etCardNumber.text.toString().trim().takeLast(4).padEnd(4, '*')})"
            
            binding.tvConfirmName.text = name
            binding.tvConfirmAddress.text = fullAddress
            binding.tvConfirmContact.text = "$phone\n$email"
            binding.tvConfirmPayment.text = paymentMethod
            binding.tvConfirmTotal.text = String.format("KSh %.2f", totalAmount)
        }

        if (currentStep == 3) {
            binding.paymentProgress.visibility = android.view.View.VISIBLE
            binding.tvPaymentInstructions.text = "Processing payment..."
            binding.btnNext.isEnabled = false
            binding.btnPrevious.isEnabled = false
            
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                processCheckout()
            }, 2000)
            return
        }

        if (currentStep < 3) {
            currentStep++
            binding.viewFlipper.displayedChild = currentStep
            updateStepUI()
        }
    }

    private fun handlePreviousStep() {
        if (currentStep > 0) {
            currentStep--
            binding.viewFlipper.displayedChild = currentStep
            updateStepUI()
        }
    }

    private fun updateStepUI() {
        binding.tvStepIndicator.text = "Step ${currentStep + 1} of 4"
        
        when (currentStep) {
            0 -> {
                binding.btnPrevious.visibility = android.view.View.INVISIBLE
                binding.btnNext.text = "NEXT"
            }
            1 -> {
                binding.btnPrevious.visibility = android.view.View.VISIBLE
                binding.btnNext.text = "NEXT"
            }
            2 -> {
                binding.btnPrevious.visibility = android.view.View.VISIBLE
                binding.btnNext.text = "CONFIRM"
            }
            3 -> {
                binding.btnPrevious.visibility = android.view.View.VISIBLE
                val isMpesa = binding.rgPaymentMethod.checkedRadioButtonId == com.app.stylenest.R.id.rbMpesa
                if (isMpesa) {
                    binding.tvPaymentInstructions.text = "We will send an M-Pesa prompt to ${binding.etMpesaPhone.text}. Click 'Pay Now' to continue."
                    binding.btnNext.text = "PAY NOW"
                } else {
                    binding.tvPaymentInstructions.text = "We will securely charge your card ending in ${binding.etCardNumber.text.toString().takeLast(4).padEnd(4, '*')}. Click 'Pay Now' to complete order."
                    binding.btnNext.text = "PAY NOW"
                }
            }
        }
    }

    private fun processCheckout() {
        val name = binding.etFullName.text.toString()
        val email = binding.etEmail.text.toString()
        val phone = binding.ccp.fullNumberWithPlus
        val fullAddress = "${binding.etStreet.text}, ${binding.etDistrict.text}, ${binding.etCity.text}, ${binding.autoCompleteCountry.text}"
        val isMpesa = binding.rgPaymentMethod.checkedRadioButtonId == com.app.stylenest.R.id.rbMpesa
        val paymentMethod = if(isMpesa) "M-Pesa" else "Credit Card"

        val cartItems = repository.getCartItems()
        if(cartItems.isEmpty()) {
            Toast.makeText(this, "Your cart is empty!", Toast.LENGTH_SHORT).show()
            return
        }

        val sharedPrefs = getSharedPreferences("stylenest_prefs", android.content.Context.MODE_PRIVATE)
        val userEmail = sharedPrefs.getString("user_email", "") ?: ""

        val orderNumber = "ST-${System.currentTimeMillis().toString().takeLast(6)}"
        val dateString = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())

        val db = dbHelper.writableDatabase
        
        val orderValues = ContentValues().apply {
            put("user_email", userEmail)
            put("order_number", orderNumber)
            put("customer_name", name)
            put("phone", phone)
            put("delivery_address", fullAddress)
            put("payment_method", paymentMethod)
            put("total_amount", totalAmount)
            put("order_date", dateString)
            put("status", "Pending Delivery")
        }
        val orderId = db.insert("orders", null, orderValues)

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

        db.delete("cart", "user_email = ?", arrayOf(userEmail))

        // Show push notification!
        com.app.stylenest.utils.NotificationHelper.showOrderSuccessNotification(this, orderNumber)

        val intent = Intent(this, OrderSuccessActivity::class.java)
        intent.putExtra("ORDER_NUMBER", orderNumber)
        startActivity(intent)
        finish()
    }
}
