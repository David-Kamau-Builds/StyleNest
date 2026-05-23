package com.app.shoppy

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.app.shoppy.adapter.ProductAdapter
import com.app.shoppy.databinding.FragmentWishlistBinding
import com.app.shoppy.ui.viewmodel.SharedProductViewModel
import com.app.shoppy.ui.viewmodel.CartViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WishlistFragment : Fragment() {

    private var _binding: FragmentWishlistBinding? = null
    private val binding get() = _binding!!
    
    private val productViewModel: SharedProductViewModel by activityViewModels()
    private val cartViewModel: CartViewModel by activityViewModels()
    
    private lateinit var productAdapter: ProductAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentWishlistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupWishlist()
        observeWishlist()
    }

    private fun setupWishlist() {
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
        binding.rvWishlist.layoutManager = GridLayoutManager(context, 2)
        binding.rvWishlist.adapter = productAdapter
    }

    private fun observeWishlist() {
        viewLifecycleOwner.lifecycleScope.launch {
            productViewModel.favoriteProductIds.collect { favIds ->
                productAdapter.updateFavorites(favIds)
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            combine(
                productViewModel.products,
                productViewModel.favoriteProductIds
            ) { products, favIds ->
                products.filter { favIds.contains(it.id) }.map { it.toProduct() }
            }.collect { items ->
                if (items.isEmpty()) {
                    binding.rvWishlist.visibility = View.GONE
                    binding.tvEmptyWishlist.visibility = View.VISIBLE
                } else {
                    binding.rvWishlist.visibility = View.VISIBLE
                    binding.tvEmptyWishlist.visibility = View.GONE
                    productAdapter.updateProducts(items)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
