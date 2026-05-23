package com.app.shoppy

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.app.shoppy.databinding.ActivityMainBinding
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.app.shoppy.utils.NotificationHelper
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import dagger.hilt.android.AndroidEntryPoint
import androidx.activity.viewModels

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val cartViewModel: com.app.shoppy.ui.viewmodel.CartViewModel by viewModels()
    private val productViewModel: com.app.shoppy.ui.viewmodel.SharedProductViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        NotificationHelper.createNotificationChannel(this)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.nav_categories -> {
                    loadFragment(CategoriesFragment())
                    true
                }
                R.id.nav_cart -> {
                    loadFragment(CartFragment())
                    true
                }
                R.id.nav_wishlist -> {
                    loadFragment(WishlistFragment())
                    true
                }
                R.id.nav_profile -> {
                    loadFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }
        
        if (savedInstanceState == null) {
            binding.bottomNavigation.selectedItemId = R.id.nav_home
        }

        observeBadges()
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Ignored
    }

    private fun observeBadges() {
        lifecycleScope.launch {
            cartViewModel.cartItems.collect { items ->
                val cartCount = items.sumOf { it.quantity }
                val bottomNav = binding.bottomNavigation
                if (cartCount > 0) {
                    bottomNav.getOrCreateBadge(R.id.nav_cart).apply {
                        number = cartCount
                        backgroundColor = ContextCompat.getColor(this@MainActivity, android.R.color.holo_red_dark)
                    }
                } else {
                    bottomNav.removeBadge(R.id.nav_cart)
                }
            }
        }
    }

    fun updateBadges() {
        // Kept for backwards compatibility with un-refactored fragments.
        // It's empty now because we use observeBadges() which updates reactively.
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}

