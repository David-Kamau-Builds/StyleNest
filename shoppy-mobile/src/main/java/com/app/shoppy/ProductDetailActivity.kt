package com.app.shoppy

import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.shoppy.adapter.ProductImageAdapter
import com.app.shoppy.adapter.ReviewAdapter
import com.app.shoppy.adapter.ProductAdapter
import com.app.shoppy.databinding.ActivityProductDetailBinding
import com.app.shoppy.data.remote.model.ReviewDto
import com.app.shoppy.ui.viewmodel.CartViewModel
import com.app.shoppy.ui.viewmodel.ReviewState
import com.app.shoppy.ui.viewmodel.ReviewViewModel
import com.app.shoppy.ui.viewmodel.SharedProductViewModel
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProductDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductDetailBinding
    private val productViewModel: SharedProductViewModel by viewModels()
    private val cartViewModel: CartViewModel by viewModels()
    private val reviewViewModel: ReviewViewModel by viewModels()
    
    private var selectedSize: String? = null
    private lateinit var reviewAdapter: ReviewAdapter
    private lateinit var recommendationAdapter: ProductAdapter
    private var currentProductId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val productId = intent.getIntExtra("PRODUCT_ID", -1)
        currentProductId = productId.toLong()

        setupAdapters()
        observeViewModels()

        // Fetch phase 5 and 6 data
        reviewViewModel.fetchReviews(currentProductId)
        productViewModel.fetchRecommendations(currentProductId)

        lifecycleScope.launch {
            // Wait for first non-empty emission of products
            productViewModel.products.collect { entities ->
                if (entities.isEmpty()) return@collect

                val product = entities.firstOrNull { it.id == currentProductId }?.toProduct()
                if (product == null) {
                    Toast.makeText(this@ProductDetailActivity, "Product not found", Toast.LENGTH_SHORT).show()
                    finish()
                    return@collect
                }

                binding.tvProductName.text = product.name
                binding.tvProductCategory.text = "${product.category} > ${product.subCategory}"
                binding.tvProductPrice.text = String.format("KSh %.2f", product.price)
                binding.tvRichDescription.text = product.richDescription

                // Heart Icon Logic
                launch {
                    productViewModel.favoriteProductIds.collect { favIds ->
                        val isFav = favIds.contains(product.id.toLong())
                        if (isFav) {
                            binding.btnFavoriteDetail.setColorFilter(android.graphics.Color.RED)
                        } else {
                            binding.btnFavoriteDetail.setColorFilter(getColor(R.color.matte_grey))
                        }
                    }
                }

                binding.btnFavoriteDetail.setOnClickListener {
                    if (!productViewModel.isLoggedIn()) {
                        Toast.makeText(this@ProductDetailActivity, "Please sign in to add items to your wishlist", Toast.LENGTH_SHORT).show()
                        startActivity(android.content.Intent(this@ProductDetailActivity, LoginActivity::class.java))
                        return@setOnClickListener
                    }
                    productViewModel.toggleWishlist(product.id.toLong())
                }

                // Setup Image Slideshow
                val imagesList = product.images.split(",")
                val imageAdapter = ProductImageAdapter(imagesList)
                binding.vpProductImages.adapter = imageAdapter

                // Setup Size Chips
                val sizes = product.sizes.split(",")
                binding.cgSizes.removeAllViews()
                for ((index, size) in sizes.withIndex()) {
                    val chip = Chip(this@ProductDetailActivity).apply {
                        id = android.view.View.generateViewId()
                        text = size.trim()
                        isCheckable = true
                        isClickable = true
                        chipBackgroundColor = getColorStateList(R.color.matte_grey)
                        setTextColor(getColor(android.R.color.white))
                    }
                    binding.cgSizes.addView(chip)
                    if (index == 0) {
                        binding.cgSizes.check(chip.id)
                        selectedSize = chip.text.toString()
                    }
                }

                binding.cgSizes.setOnCheckedStateChangeListener { group, checkedIds ->
                    selectedSize = if (checkedIds.isNotEmpty()) {
                        group.findViewById<Chip>(checkedIds.first()).text.toString()
                    } else null
                }

                binding.btnAddToCart.setOnClickListener {
                    if (!cartViewModel.isLoggedIn()) {
                        Toast.makeText(this@ProductDetailActivity, "Please sign in to add items to your cart", Toast.LENGTH_SHORT).show()
                        startActivity(android.content.Intent(this@ProductDetailActivity, LoginActivity::class.java))
                        return@setOnClickListener
                    }
                    if (selectedSize == null) {
                        Toast.makeText(this@ProductDetailActivity, "Please select a size first", Toast.LENGTH_SHORT).show()
                    } else {
                        cartViewModel.addToCart(product.id.toLong(), selectedSize!!, 1, product.price)
                        Toast.makeText(this@ProductDetailActivity, "${product.name} added to cart!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }

                return@collect
            }
        }

        binding.btnAddReview.setOnClickListener {
            showAddReviewDialog()
        }

        binding.btnBack.setOnClickListener { finish() }
    }

    private fun setupAdapters() {
        reviewAdapter = ReviewAdapter(emptyList())
        binding.rvReviews.layoutManager = LinearLayoutManager(this)
        binding.rvReviews.adapter = reviewAdapter

        // Use existing ProductAdapter for recommendations
        recommendationAdapter = ProductAdapter(
            products = emptyList(),
            onProductClick = { product ->
                val intent = android.content.Intent(this, ProductDetailActivity::class.java)
                intent.putExtra("PRODUCT_ID", product.id)
                startActivity(intent)
            },
            onQuickAddClick = { /* no quick-add in recommendations */ },
            onFavoriteClick = { product ->
                productViewModel.toggleWishlist(product.id.toLong())
            }
        )
        // Ensure horizontal scrolling is applied (already set in XML, but can be forced here)
        binding.rvRecommendations.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvRecommendations.adapter = recommendationAdapter
    }

    private fun observeViewModels() {
        // Observe Reviews
        lifecycleScope.launch {
            reviewViewModel.reviewState.collect { state ->
                when (state) {
                    is ReviewState.Success -> {
                        reviewAdapter.updateData(state.reviews)
                    }
                    else -> {}
                }
            }
        }

        // Observe Recommendations
        lifecycleScope.launch {
            productViewModel.recommendations.collect { recs ->
                // Convert Dto back to local model for adapter
                val models = recs.map { com.app.shoppy.model.Product(
                    id = it.id.toInt(),
                    name = it.name,
                    description = it.description ?: "",
                    richDescription = it.richDescription ?: "",
                    category = it.category,
                    subCategory = it.subCategory ?: "",
                    sizes = it.sizes,
                    price = it.price,
                    imageUrl = it.imageUrl,
                    images = it.images ?: it.imageUrl
                )}
                recommendationAdapter.updateData(models)
            }
        }
    }

    private fun showAddReviewDialog() {
        val sharedPrefs = getSharedPreferences("shoppy_prefs", android.content.Context.MODE_PRIVATE)
        if (!sharedPrefs.getBoolean("is_logged_in", false)) {
            Toast.makeText(this, "Please sign in to leave a review.", Toast.LENGTH_SHORT).show()
            return
        }

        val userName = sharedPrefs.getString("user_name", "User") ?: "User"
        val userEmail = sharedPrefs.getString("user_email", "") ?: ""

        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_add_review)
        dialog.window?.setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT)

        val ratingBar = dialog.findViewById<RatingBar>(R.id.ratingBarInput)
        val etComment = dialog.findViewById<EditText>(R.id.etReviewComment)
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)
        val btnSubmit = dialog.findViewById<Button>(R.id.btnSubmit)

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnSubmit.setOnClickListener {
            val rating = ratingBar.rating.toInt()
            val comment = etComment.text.toString().trim()

            if (rating == 0) {
                Toast.makeText(this, "Please select a rating", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (comment.isEmpty()) {
                Toast.makeText(this, "Please enter a comment", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newReview = ReviewDto(
                id = null,
                productId = currentProductId,
                userEmail = userEmail,
                userName = userName,
                rating = rating,
                comment = comment,
                date = null // Backend handles this
            )

            reviewViewModel.submitReview(
                review = newReview,
                onSuccess = {
                    Toast.makeText(this, "Review submitted!", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                },
                onError = { err ->
                    Toast.makeText(this, err, Toast.LENGTH_SHORT).show()
                }
            )
        }

        dialog.show()
    }
}
