package com.app.shoppy

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.shoppy.adapter.CartAdapter
import com.app.shoppy.databinding.FragmentCartBinding
import com.app.shoppy.ui.viewmodel.CartViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CartFragment : Fragment() {
    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    private val cartViewModel: CartViewModel by activityViewModels()
    private lateinit var cartAdapter: CartAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupCartList()
        observeCartData()

        binding.btnCheckout.setOnClickListener {
            val intent = Intent(requireContext(), CheckoutActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupCartList() {
        cartAdapter = CartAdapter(emptyList()) { item, newQuantity ->
            if (newQuantity == 0) {
                cartViewModel.removeCartItem(item.id.toLong())
            } else {
                cartViewModel.updateQuantity(item.id.toLong(), newQuantity)
            }
        }
        binding.rvCartItems.layoutManager = LinearLayoutManager(context)
        binding.rvCartItems.adapter = cartAdapter
    }

    private fun observeCartData() {
        viewLifecycleOwner.lifecycleScope.launch {
            cartViewModel.cartItems.collect { cartItems ->
                cartAdapter.updateData(cartItems)
                
                if (cartItems.isEmpty()) {
                    binding.tvEmptyCart.visibility = View.VISIBLE
                    binding.checkoutPanel.visibility = View.GONE
                    binding.rvCartItems.visibility = View.GONE
                } else {
                    binding.tvEmptyCart.visibility = View.GONE
                    binding.checkoutPanel.visibility = View.VISIBLE
                    binding.rvCartItems.visibility = View.VISIBLE
                    
                    var total = 0.0
                    for (item in cartItems) {
                        total += (item.price * item.quantity)
                    }
                    binding.tvSubtotal.text = String.format("KSh %.2f", total)
                    binding.tvTotal.text = String.format("KSh %.2f", total)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
