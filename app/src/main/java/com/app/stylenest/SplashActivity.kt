package com.app.stylenest

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AlphaAnimation
import androidx.appcompat.app.AppCompatActivity
import com.app.stylenest.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Simple fade-in animation for the minimalist logo
        val fadeIn = AlphaAnimation(0f, 1f)
        fadeIn.duration = 1500
        binding.logoText.startAnimation(fadeIn)

        // Delay 2 seconds then transition to MainActivity
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 2000)
    }
}
