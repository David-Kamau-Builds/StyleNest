package com.app.shoppy

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.app.shoppy.data.remote.model.OrderDto
import com.app.shoppy.data.remote.model.OrderItemDto
import com.app.shoppy.databinding.ActivityCheckoutBinding
import com.app.shoppy.ui.viewmodel.CartViewModel
import com.app.shoppy.ui.viewmodel.OrderViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class CheckoutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCheckoutBinding
    private val cartViewModel: CartViewModel by viewModels()
    private val orderViewModel: OrderViewModel by viewModels()

    private var currentStep = 0
    private var totalAmount = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        val countries = arrayOf("Kenya", "Uganda", "Tanzania", "Rwanda", "South Africa", "Nigeria", "United States", "United Kingdom")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, countries)
        binding.autoCompleteCountry.setAdapter(adapter)
        binding.autoCompleteCountry.setText(countries[0], false)

        // Read total from cart state
        lifecycleScope.launch {
            cartViewModel.cartItems.collect { items ->
                totalAmount = items.sumOf { it.price * it.quantity }
            }
        }

        binding.ccp.registerCarrierNumberEditText(binding.etPhone)
        binding.ccp.setDefaultCountryUsingNameCode("KE")
        binding.ccp.resetToDefaultCountry()

        val isDarkMode = (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
        if (isDarkMode) {
            binding.ccp.setContentColor(android.graphics.Color.WHITE)
            binding.ccp.setDialogTextColor(android.graphics.Color.WHITE)
            binding.ccp.setDialogBackgroundColor(getColor(com.app.shoppy.R.color.matte_grey))
        } else {
            binding.ccp.setContentColor(android.graphics.Color.BLACK)
            binding.ccp.setDialogTextColor(android.graphics.Color.BLACK)
            binding.ccp.setDialogBackgroundColor(android.graphics.Color.WHITE)
        }

        val sharedPrefs = getSharedPreferences("shoppy_prefs", android.content.Context.MODE_PRIVATE)
        binding.etFullName.setText(sharedPrefs.getString("user_name", ""))
        binding.etEmail.setText(sharedPrefs.getString("user_email", ""))
        val savedPhone = sharedPrefs.getString("user_phone", "")
        if (!savedPhone.isNullOrBlank()) binding.etPhone.setText(savedPhone)
        val savedCountry = sharedPrefs.getString("user_country", "")
        if (!savedCountry.isNullOrBlank()) binding.autoCompleteCountry.setText(savedCountry, false)
        binding.etCity.setText(sharedPrefs.getString("user_city", ""))
        binding.etDistrict.setText(sharedPrefs.getString("user_district", ""))
        binding.etStreet.setText(sharedPrefs.getString("user_street", ""))

        updateStepUI()

        binding.btnNext.setOnClickListener { handleNextStep() }
        binding.btnPrevious.setOnClickListener { handlePreviousStep() }

        binding.rgPaymentMethod.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == com.app.shoppy.R.id.rbMpesa) {
                binding.mpesaContainer.visibility = View.VISIBLE
                binding.cardContainer.visibility = View.GONE
            } else {
                binding.mpesaContainer.visibility = View.GONE
                binding.cardContainer.visibility = View.VISIBLE
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
            if (name.isBlank()) { binding.tilFullName.error = "Required *"; isValid = false } else binding.tilFullName.error = null
            if (email.isBlank()) { binding.tilEmail.error = "Required *"; isValid = false } else binding.tilEmail.error = null
            if (phone.isBlank()) {
                binding.tilPhone.error = "Required *"; isValid = false
            } else if (!binding.ccp.isValidFullNumber) {
                binding.tilPhone.error = "Invalid phone format *"; isValid = false
            } else binding.tilPhone.error = null
            if (country.isBlank()) { binding.tilCountry.error = "Required *"; isValid = false } else binding.tilCountry.error = null
            if (city.isBlank()) { binding.tilCity.error = "Required *"; isValid = false } else binding.tilCity.error = null
            if (district.isBlank()) { binding.tilDistrict.error = "Required *"; isValid = false } else binding.tilDistrict.error = null
            if (street.isBlank()) { binding.tilStreet.error = "Required *"; isValid = false } else binding.tilStreet.error = null

            if (!isValid) { Toast.makeText(this, "Please fix the highlighted errors.", Toast.LENGTH_SHORT).show(); return }

            getSharedPreferences("shoppy_prefs", android.content.Context.MODE_PRIVATE).edit().apply {
                putString("user_name", name); putString("user_email", email)
                putString("user_phone", binding.etPhone.text.toString())
                putString("user_country", country); putString("user_city", city)
                putString("user_district", district); putString("user_street", street)
                apply()
            }
        }

        if (currentStep == 1) {
            val isMpesa = binding.rgPaymentMethod.checkedRadioButtonId == com.app.shoppy.R.id.rbMpesa
            var isValid = true

            if (isMpesa) {
                val mpesaPhone = binding.etMpesaPhone.text.toString().trim()
                val mpesaRegex = "^(?:254|\\+254|0)?([17][0-9]{8})$".toRegex()
                if (mpesaPhone.isBlank()) { binding.tilMpesaPhone.error = "Required *"; isValid = false }
                else if (!mpesaRegex.matches(mpesaPhone)) { binding.tilMpesaPhone.error = "Invalid M-Pesa number *"; isValid = false }
                else binding.tilMpesaPhone.error = null
            } else {
                val cardNum = binding.etCardNumber.text.toString().replace(" ", "").trim()
                val cardExp = binding.etCardExpiry.text.toString().trim()
                val cardCvv = binding.etCardCvv.text.toString().trim()
                if (cardNum.isBlank() || cardNum.length < 13 || cardNum.length > 19 || !cardNum.all { it.isDigit() }) {
                    binding.tilCardNumber.error = "Invalid Card Number *"; isValid = false
                } else binding.tilCardNumber.error = null
                if (!("^(0[1-9]|1[0-2])/?([0-9]{2})$".toRegex()).matches(cardExp)) {
                    binding.tilCardExpiry.error = "Format MM/YY *"; isValid = false
                } else binding.tilCardExpiry.error = null
                if (cardCvv.isBlank() || cardCvv.length !in 3..4 || !cardCvv.all { it.isDigit() }) {
                    binding.tilCardCvv.error = "Invalid CVV *"; isValid = false
                } else binding.tilCardCvv.error = null
            }
            if (!isValid) { Toast.makeText(this, "Please fix payment errors.", Toast.LENGTH_SHORT).show(); return }

            val name = binding.etFullName.text.toString()
            val email = binding.etEmail.text.toString()
            val phone = binding.ccp.fullNumberWithPlus
            val fullAddress = "${binding.etStreet.text}\n${binding.etDistrict.text}, ${binding.etCity.text}\n${binding.autoCompleteCountry.text}"
            val paymentMethod = if (isMpesa) "M-Pesa (${binding.etMpesaPhone.text})" else "Credit Card (ending in ${binding.etCardNumber.text.toString().trim().takeLast(4).padEnd(4, '*')})"

            binding.tvConfirmName.text = name
            binding.tvConfirmAddress.text = fullAddress
            binding.tvConfirmContact.text = "$phone\n$email"
            binding.tvConfirmPayment.text = paymentMethod
            binding.tvConfirmTotal.text = String.format("KSh %.2f", totalAmount)
        }

        if (currentStep == 3) {
            binding.paymentProgress.visibility = View.VISIBLE
            binding.tvPaymentInstructions.text = "Processing payment..."
            binding.btnNext.isEnabled = false
            binding.btnPrevious.isEnabled = false
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({ processCheckout() }, 2000)
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
            0 -> { binding.btnPrevious.visibility = View.INVISIBLE; binding.btnNext.text = "NEXT" }
            1 -> { binding.btnPrevious.visibility = View.VISIBLE; binding.btnNext.text = "NEXT" }
            2 -> { binding.btnPrevious.visibility = View.VISIBLE; binding.btnNext.text = "CONFIRM" }
            3 -> {
                binding.btnPrevious.visibility = View.VISIBLE
                val isMpesa = binding.rgPaymentMethod.checkedRadioButtonId == com.app.shoppy.R.id.rbMpesa
                binding.tvPaymentInstructions.text = if (isMpesa)
                    "We will send an M-Pesa prompt to ${binding.etMpesaPhone.text}. Click 'Pay Now' to continue."
                else
                    "We will securely charge your card ending in ${binding.etCardNumber.text.toString().takeLast(4).padEnd(4, '*')}. Click 'Pay Now' to complete order."
                binding.btnNext.text = "PAY NOW"
            }
        }
    }

    private fun processCheckout() {
        val cartItems = cartViewModel.cartItems.value
        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Your cart is empty!", Toast.LENGTH_SHORT).show()
            return
        }

        val sharedPrefs = getSharedPreferences("shoppy_prefs", android.content.Context.MODE_PRIVATE)
        val userEmail = sharedPrefs.getString("user_email", "") ?: ""

        val name = binding.etFullName.text.toString()
        val phone = binding.ccp.fullNumberWithPlus
        val fullAddress = "${binding.etStreet.text}, ${binding.etDistrict.text}, ${binding.etCity.text}, ${binding.autoCompleteCountry.text}"
        val isMpesa = binding.rgPaymentMethod.checkedRadioButtonId == com.app.shoppy.R.id.rbMpesa
        val paymentMethod = if (isMpesa) "M-Pesa" else "Credit Card"
        val orderNumber = "ST-${System.currentTimeMillis().toString().takeLast(6)}"
        val dateString = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())

        val orderItems = cartItems.map { item ->
            OrderItemDto(
                id = null,
                productId = item.productId.toLong(),
                size = item.selectedSize,
                quantity = item.quantity,
                price = item.price
            )
        }

        val order = OrderDto(
            id = null,
            userEmail = userEmail,
            orderNumber = orderNumber,
            customerName = name,
            phone = phone,
            deliveryAddress = fullAddress,
            paymentMethod = paymentMethod,
            totalAmount = totalAmount,
            orderDate = dateString,
            status = "Pending Delivery",
            orderItems = orderItems
        )

        orderViewModel.submitOrder(
            order = order,
            onSuccess = {
                // Clear the local Room cart after successful order
                cartItems.forEach { cartViewModel.removeCartItem(it.id.toLong()) }

                com.app.shoppy.utils.NotificationHelper.showOrderSuccessNotification(this, orderNumber)

                val intent = Intent(this, OrderSuccessActivity::class.java)
                intent.putExtra("ORDER_NUMBER", orderNumber)
                intent.putExtra("TOTAL_AMOUNT", totalAmount)
                startActivity(intent)
                finish()
            },
            onError = { errorMsg ->
                binding.paymentProgress.visibility = View.GONE
                binding.btnNext.isEnabled = true
                binding.btnPrevious.isEnabled = true
                Toast.makeText(this, "Order failed: $errorMsg", Toast.LENGTH_LONG).show()
            }
        )
    }
}
