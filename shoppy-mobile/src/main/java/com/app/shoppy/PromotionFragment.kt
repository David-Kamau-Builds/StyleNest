package com.app.shoppy

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
import com.app.shoppy.databinding.FragmentPromotionBinding
import com.app.shoppy.ui.viewmodel.CartViewModel
import com.app.shoppy.ui.viewmodel.SharedProductViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PromotionFragment : Fragment() {
    private var _binding: FragmentPromotionBinding? = null
    private val binding get() = _binding!!

    private val productViewModel: SharedProductViewModel by activityViewModels()
    private val cartViewModel: CartViewModel by activityViewModels()
    private lateinit var productAdapter: ProductAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPromotionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val promoType = arguments?.getString("PROMO_TYPE") ?: "NEW"

        binding.tvPromoTitle.text = if (promoType == "NEW") "New Arrivals" else "50% Off Sale"

        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        productAdapter = ProductAdapter(
            products = emptyList(),
            promoType = promoType,
            onProductClick = { product ->
                val intent = android.content.Intent(requireContext(), ProductDetailActivity::class.java)
                intent.putExtra("PRODUCT_ID", product.id)
                startActivity(intent)
            },
            onQuickAddClick = { product ->
                if (!cartViewModel.isLoggedIn()) {
                    Toast.makeText(requireContext(), "Please sign in to add items to your cart", Toast.LENGTH_SHORT).show()
                    val intent = android.content.Intent(requireContext(), LoginActivity::class.java)
                    startActivity(intent)
                    return@ProductAdapter
                }
                val sizesArray = product.sizes.split(",").map { it.trim() }.toTypedArray()
                val finalPrice = if (promoType == "SALE") product.price * 0.5 else product.price
                if (sizesArray.isEmpty() || sizesArray[0].isEmpty()) {
                    cartViewModel.addToCart(product.id.toLong(), "One Size", 1, finalPrice)
                    Toast.makeText(requireContext(), "Added to cart!", Toast.LENGTH_SHORT).show()
                } else {
                    android.app.AlertDialog.Builder(requireContext())
                        .setTitle("Select Size")
                        .setItems(sizesArray) { _, which ->
                            val selectedSize = sizesArray[which]
                            cartViewModel.addToCart(product.id.toLong(), selectedSize, 1, finalPrice)
                            Toast.makeText(requireContext(), "Added to cart!", Toast.LENGTH_SHORT).show()
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }
            },
            onFavoriteClick = { product ->
                if (!productViewModel.isLoggedIn()) {
                    Toast.makeText(requireContext(), "Please sign in to add items to your wishlist", Toast.LENGTH_SHORT).show()
                    val intent = android.content.Intent(requireContext(), LoginActivity::class.java)
                    startActivity(intent)
                    return@ProductAdapter
                }
                productViewModel.toggleWishlist(product.id.toLong())
            }
        )
        binding.rvPromoProducts.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvPromoProducts.adapter = productAdapter

        // Observe products and favorites reactively
        viewLifecycleOwner.lifecycleScope.launch {
            productViewModel.products.collect { entities ->
                val all = entities.map { it.toProduct() }
                val filtered = if (promoType == "NEW") {
                    all.sortedByDescending { it.id }.take(10)
                } else {
                    all.filter { it.id % 2 == 0 }
                }

                if (filtered.isEmpty()) {
                    binding.tvEmptyState.visibility = View.VISIBLE
                    binding.tvEmptyState.text = if (promoType == "NEW") "No new arrivals yet" else "No sale items at the moment."
                    binding.rvPromoProducts.visibility = View.GONE
                } else {
                    binding.tvEmptyState.visibility = View.GONE
                    binding.rvPromoProducts.visibility = View.VISIBLE
                    productAdapter.updateProducts(filtered)
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            productViewModel.favoriteProductIds.collect { favIds ->
                productAdapter.updateFavorites(favIds)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
