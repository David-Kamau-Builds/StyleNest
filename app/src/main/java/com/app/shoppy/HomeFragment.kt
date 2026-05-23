package com.app.shoppy

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import android.view.inputmethod.EditorInfo
import com.app.shoppy.ui.viewmodel.SharedProductViewModel
import com.app.shoppy.ui.viewmodel.CartViewModel
import androidx.recyclerview.widget.GridLayoutManager
import com.app.shoppy.adapter.ProductAdapter
import com.app.shoppy.databinding.FragmentHomeBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val productViewModel: SharedProductViewModel by activityViewModels()
    private val cartViewModel: CartViewModel by activityViewModels()
    private lateinit var productAdapter: ProductAdapter

    data class BentoSlide(val title: String, val imageUrl: String)
    private val bentoSlides = listOf(
        BentoSlide("Urban\nWear", "https://images.unsplash.com/photo-1523381210434-271e8be1f52b?q=80&w=600&auto=format&fit=crop"),
        BentoSlide("Street\nStyle", "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?q=80&w=600&auto=format&fit=crop"),
        BentoSlide("Autumn\nVibes", "https://images.unsplash.com/photo-1550614000-4b95d466f288?q=80&w=600&auto=format&fit=crop"),
        BentoSlide("Evening\nElegance", "https://images.unsplash.com/photo-1490481651871-ab68de25d43d?q=80&w=600&auto=format&fit=crop")
    )
    private var currentSlideIndex = 0
    private val slideHandler = Handler(Looper.getMainLooper())
    private lateinit var slideRunnable: Runnable

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Load static small bento images
        Glide.with(this)
            .load("https://images.unsplash.com/photo-1445205170230-053b83016050?q=80&w=400&auto=format&fit=crop")
            .placeholder(com.app.shoppy.utils.UIUtils.getShimmerDrawable())
            .into(binding.ivBentoTopRight)

        Glide.with(this)
            .load("https://images.unsplash.com/photo-1483985988355-763728e1935b?q=80&w=400&auto=format&fit=crop")
            .placeholder(com.app.shoppy.utils.UIUtils.getShimmerDrawable())
            .into(binding.ivBentoBottomRight)
            
        binding.cardNewArrivals.setOnClickListener {
            val fragment = PromotionFragment().apply {
                arguments = Bundle().apply {
                    putString("PROMO_TYPE", "NEW")
                }
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
        
        binding.cardSale.setOnClickListener {
            val fragment = PromotionFragment().apply {
                arguments = Bundle().apply {
                    putString("PROMO_TYPE", "SALE")
                }
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }

        startBentoSlideshow()
        setupProducts()
        setupSearchAndFilter()
    }

    private fun setupSearchAndFilter() {
        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.etSearch.text.toString()
                if (query.isNotEmpty()) {
                    productViewModel.searchProducts(query)
                } else {
                    productViewModel.clearSearchAndFilter()
                }
                true
            } else {
                false
            }
        }

        binding.btnFilter.setOnClickListener {
            // Show bottom sheet
            val bottomSheet = FilterBottomSheetFragment()
            bottomSheet.show(parentFragmentManager, "FilterBottomSheet")
        }
    }

    private fun startBentoSlideshow() {
        slideRunnable = object : Runnable {
            override fun run() {
                currentSlideIndex = (currentSlideIndex + 1) % bentoSlides.size
                val slide = bentoSlides[currentSlideIndex]
                
                // Animate text fade out/in
                binding.tvBentoTitle.animate().alpha(0f).setDuration(400).withEndAction {
                    if (_binding != null) {
                        binding.tvBentoTitle.text = slide.title
                        binding.tvBentoTitle.animate().alpha(1f).setDuration(400).start()
                    }
                }.start()

                // Load next image with crossfade
                if (_binding != null) {
                    Glide.with(this@HomeFragment)
                        .load(slide.imageUrl)
                        .placeholder(com.app.shoppy.utils.UIUtils.getShimmerDrawable())
                        .transition(DrawableTransitionOptions.withCrossFade(800))
                        .into(binding.ivBentoMain)
                }
                    
                slideHandler.postDelayed(this, 5000) // 5 seconds per slide
            }
        }
        
        // Load initial slide immediately
        val initialSlide = bentoSlides[0]
        binding.tvBentoTitle.text = initialSlide.title
        Glide.with(this)
            .load(initialSlide.imageUrl)
            .placeholder(com.app.shoppy.utils.UIUtils.getShimmerDrawable())
            .into(binding.ivBentoMain)
            
        slideHandler.postDelayed(slideRunnable, 5000)
    }

    private fun setupProducts() {
        viewLifecycleOwner.lifecycleScope.launch {
            productViewModel.products.collect { entities ->
                val products = entities.map { it.toProduct() }
                productAdapter.updateProducts(products)
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            productViewModel.favoriteProductIds.collect { favIds ->
                productAdapter.updateFavorites(favIds)
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            productViewModel.searchResults.collect { searchResults ->
                if (searchResults != null) {
                    val products = searchResults.map { dto ->
                        com.app.shoppy.model.Product(
                            id = dto.id.toInt(),
                            name = dto.name,
                            description = dto.description ?: "",
                            richDescription = dto.richDescription ?: "",
                            category = dto.category,
                            subCategory = dto.subCategory ?: "",
                            sizes = dto.sizes,
                            price = dto.price,
                            imageUrl = dto.imageUrl,
                            images = dto.images ?: dto.imageUrl
                        )
                    }
                    productAdapter.updateProducts(products)
                } else {
                    // Fall back to normal products
                    val products = productViewModel.products.value.map { it.toProduct() }
                    productAdapter.updateProducts(products)
                }
            }
        }
        productAdapter = ProductAdapter(
            products = emptyList(),
            onProductClick = { product ->
                val intent = Intent(requireContext(), ProductDetailActivity::class.java)
                intent.putExtra("PRODUCT_ID", product.id)
                startActivity(intent)
            },
            onQuickAddClick = { product ->
                val sizesArray = product.sizes.split(",").map { it.trim() }.toTypedArray()
                if (sizesArray.isEmpty() || sizesArray[0].isEmpty()) {
                    cartViewModel.addToCart(product.id.toLong(), "One Size", 1, product.price)
                    Toast.makeText(context, "Added to cart!", Toast.LENGTH_SHORT).show()
                } else {
                    android.app.AlertDialog.Builder(requireContext())
                        .setTitle("Select Size")
                        .setItems(sizesArray) { _, which ->
                            val selectedSize = sizesArray[which]
                            cartViewModel.addToCart(product.id.toLong(), selectedSize, 1, product.price)
                            Toast.makeText(context, "Added to cart!", Toast.LENGTH_SHORT).show()
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }
            },
            onFavoriteClick = { product ->
                productViewModel.toggleWishlist(product.id.toLong())
            }
        )
        binding.rvProducts.layoutManager = GridLayoutManager(context, 2)
        binding.rvProducts.adapter = productAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::slideRunnable.isInitialized) {
            slideHandler.removeCallbacks(slideRunnable)
        }
        _binding = null
    }
}
