package com.app.stylenest

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.stylenest.adapter.CategoryAdapter
import com.app.stylenest.adapter.ProductAdapter
import com.app.stylenest.data.StyleNestRepository
import com.app.stylenest.databinding.FragmentCategoriesBinding

class CategoriesFragment : Fragment() {
    private var _binding: FragmentCategoriesBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: StyleNestRepository
    private lateinit var productAdapter: ProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        repository = StyleNestRepository(requireContext())
        
        setupDrawer()
        setupCategories()
        setupProducts()
    }

    private fun setupDrawer() {
        binding.btnMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    private fun setupCategories() {
        val categories = listOf("All", "Shirts", "Trousers", "Dresses", "Shoes", "Accessories")
        val categoryAdapter = CategoryAdapter(categories) { selectedCategory ->
            binding.tvCategoriesTitle.text = selectedCategory
            if (selectedCategory == "All") {
                productAdapter.updateData(repository.getAllProducts())
            } else {
                productAdapter.updateData(repository.getProductsByCategory(selectedCategory))
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }
        
        binding.rvCategoryList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.rvCategoryList.adapter = categoryAdapter
    }

    private fun setupProducts() {
        productAdapter = ProductAdapter(
            products = repository.getAllProducts(),
            onProductClick = { product ->
                val intent = Intent(requireContext(), ProductDetailActivity::class.java)
                intent.putExtra("PRODUCT_ID", product.id)
                startActivity(intent)
            },
            onQuickAddClick = { product ->
                val defaultSize = product.sizes.split(",").firstOrNull() ?: "One Size"
                repository.addToCart(product.id, defaultSize, 1)
                Toast.makeText(context, "${product.name} added to cart!", Toast.LENGTH_SHORT).show()
            }
        )
        binding.rvCategoryProducts.layoutManager = GridLayoutManager(context, 2)
        binding.rvCategoryProducts.adapter = productAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
