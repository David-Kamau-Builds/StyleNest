package com.app.shoppy

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.app.shoppy.data.StyleNestRepository
import com.app.shoppy.databinding.ActivityMainBinding
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.app.shoppy.utils.NotificationHelper

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    lateinit var repository: StyleNestRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        repository = StyleNestRepository(this)

        // Initialize the notification channel here so it's ready when needed
        NotificationHelper.createNotificationChannel(this)
        
        // Request notification permission if Android 13+
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
        
        // Load default fragment on first creation
        if (savedInstanceState == null) {
            binding.bottomNavigation.selectedItemId = R.id.nav_home
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // We could log this or show a message if denied, but usually it's fine to just ignore
    }

    override fun onResume() {
        super.onResume()
        updateBadges()
    }

    fun updateBadges() {
        val cartCount = repository.getCartCount()
        if (cartCount > 0) {
            val badge = binding.bottomNavigation.getOrCreateBadge(R.id.nav_cart)
            badge.number = cartCount
            badge.backgroundColor = getColor(R.color.gold)
            badge.badgeTextColor = getColor(android.R.color.black)
            badge.isVisible = true
        } else {
            binding.bottomNavigation.removeBadge(R.id.nav_cart)
        }

        val wishlistCount = repository.getWishlistCount()
        if (wishlistCount > 0) {
            val badge = binding.bottomNavigation.getOrCreateBadge(R.id.nav_wishlist)
            badge.number = wishlistCount
            badge.backgroundColor = getColor(android.R.color.holo_red_dark)
            badge.badgeTextColor = getColor(android.R.color.white)
            badge.isVisible = true
        } else {
            binding.bottomNavigation.removeBadge(R.id.nav_wishlist)
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
