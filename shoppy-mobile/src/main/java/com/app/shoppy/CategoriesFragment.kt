package com.app.shoppy

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.app.shoppy.adapter.CategoryAdapter
import com.app.shoppy.adapter.ProductAdapter
import com.app.shoppy.databinding.FragmentCategoriesBinding
import com.app.shoppy.ui.viewmodel.CartViewModel
import com.app.shoppy.ui.viewmodel.SharedProductViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CategoriesFragment : Fragment() {
    private var _binding: FragmentCategoriesBinding? = null
    private val binding get() = _binding!!

    private val productViewModel: SharedProductViewModel by activityViewModels()
    private val cartViewModel: CartViewModel by activityViewModels()
    private lateinit var productAdapter: ProductAdapter

    private var currentCategory = "All"
    private var minPrice = 0.0
    private var maxPrice = 10000.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupDrawer()
        setupCategories()
        setupProducts()
        setupAdvancedFilters()
        observeData()
    }

    private fun setupAdvancedFilters() {
        binding.priceSlider.values = listOf(0f, 10000f)
        binding.priceSlider.addOnChangeListener { slider, _, _ ->
            val values = slider.values
            minPrice = values[0].toDouble()
            maxPrice = values[1].toDouble()
            binding.tvPriceRange.text = "KSh ${minPrice.toInt()} - KSh ${maxPrice.toInt()}"
            applyFilters()
        }
        
        binding.btnClearFilters.setOnClickListener {
            currentCategory = "All"
            binding.tvCategoriesTitle.text = "Browse Categories"
            binding.priceSlider.values = listOf(0f, 10000f)
            minPrice = 0.0
            maxPrice = 10000.0
            applyFilters()
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    private fun applyFilters() {
        viewLifecycleOwner.lifecycleScope.launch {
            val allEntities = productViewModel.products.value
            var filtered = allEntities.map { it.toProduct() }
            
            if (currentCategory != "All") {
                filtered = filtered.filter { 
                    it.category == currentCategory || it.subCategory == currentCategory
                }
            }
            filtered = filtered.filter { it.price in minPrice..maxPrice }
            
            productAdapter.updateProducts(filtered)
        }
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            productViewModel.products.collect {
                applyFilters()
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            productViewModel.favoriteProductIds.collect { favIds ->
                productAdapter.updateFavorites(favIds)
            }
        }
    }

    private fun setupDrawer() {
        binding.btnMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
        binding.btnCloseDrawer.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    private fun setupCategories() {
        val groupList = listOf("All", "Shirts", "Trousers", "Dresses", "Shoes", "Accessories")
        val childList = mapOf(
            "All" to emptyList(),
            "Shirts" to listOf("Polos", "Tees", "Button-Downs", "Long-Sleeve"),
            "Trousers" to listOf("Jeans", "Chinos", "Sweatpants", "Cargo"),
            "Dresses" to listOf("Maxi", "Midi", "Summer", "Evening"),
            "Shoes" to listOf("Sneakers", "Boots", "Loafers", "Heels"),
            "Accessories" to listOf("Belts", "Socks", "Perfumes", "Watches")
        )
        
        val adapter = com.app.shoppy.adapter.SidebarExpandableAdapter(requireContext(), groupList, childList)
        binding.expandableCategoryList.setAdapter(adapter)
        
        binding.expandableCategoryList.setOnChildClickListener { _, _, groupPosition, childPosition, _ ->
            val selectedSubCategory = childList[groupList[groupPosition]]!![childPosition]
            binding.tvCategoriesTitle.text = selectedSubCategory
            currentCategory = selectedSubCategory
            applyFilters()
            binding.drawerLayout.closeDrawer(androidx.core.view.GravityCompat.START)
            true
        }
        
        binding.expandableCategoryList.setOnGroupClickListener { _, _, groupPosition, _ ->
            val mainCategory = groupList[groupPosition]
            if (mainCategory == "All") {
                binding.tvCategoriesTitle.text = mainCategory
                currentCategory = mainCategory
                applyFilters()
                binding.drawerLayout.closeDrawer(androidx.core.view.GravityCompat.START)
                return@setOnGroupClickListener true
            } else {
                binding.tvCategoriesTitle.text = mainCategory
                currentCategory = mainCategory
                applyFilters()
                return@setOnGroupClickListener false
            }
        }
    }

    private fun setupProducts() {
        productAdapter = ProductAdapter(
            products = emptyList(),
            onProductClick = { product ->
                val intent = Intent(requireContext(), ProductDetailActivity::class.java)
                intent.putExtra("PRODUCT_ID", product.id)
                startActivity(intent)
            },
            onQuickAddClick = { product ->
                if (!cartViewModel.isLoggedIn()) {
                    Toast.makeText(context, "Please sign in to add items to your cart", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(requireContext(), LoginActivity::class.java))
                    return@ProductAdapter
                }
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
                if (!productViewModel.isLoggedIn()) {
                    Toast.makeText(context, "Please sign in to add items to your wishlist", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(requireContext(), LoginActivity::class.java))
                    return@ProductAdapter
                }
                productViewModel.toggleWishlist(product.id.toLong())
            }
        )
        val gridLayoutManager = GridLayoutManager(context, 2)
        binding.rvCategoryProducts.layoutManager = gridLayoutManager
        binding.rvCategoryProducts.adapter = productAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
