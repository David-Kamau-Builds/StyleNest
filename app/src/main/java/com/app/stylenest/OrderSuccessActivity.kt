package com.app.stylenest

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.app.stylenest.databinding.ActivityOrderSuccessBinding

class OrderSuccessActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderSuccessBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderSuccessBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val orderNumber = intent.getStringExtra("ORDER_NUMBER") ?: "#ST-UNKNOWN"
        binding.tvOrderNumber.text = orderNumber

        val navigateHome = Runnable {
            android.widget.Toast.makeText(
                this,
                "Order $orderNumber Confirmed! Track it via Profile -> Order History.",
                android.widget.Toast.LENGTH_LONG
            ).show()

            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        handler.postDelayed(navigateHome, 3500) // 3.5 seconds delay

        binding.btnContinueShopping.setOnClickListener {
            handler.removeCallbacks(navigateHome)
            navigateHome.run()
        }
    }
}
