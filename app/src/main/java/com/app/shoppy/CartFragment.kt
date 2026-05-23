package com.app.shoppy

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.shoppy.adapter.CartAdapter
import com.app.shoppy.data.DatabaseHelper
import com.app.shoppy.data.StyleNestRepository
import com.app.shoppy.databinding.FragmentCartBinding

class CartFragment : Fragment() {
    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: StyleNestRepository
    private lateinit var cartAdapter: CartAdapter
    private var dbHelper: DatabaseHelper? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        repository = StyleNestRepository(requireContext())
        dbHelper = DatabaseHelper(requireContext())
        
        setupCartList()
        loadCartData()

        binding.btnCheckout.setOnClickListener {
            // We will transition to CheckoutActivity shortly
            val intent = Intent(requireContext(), CheckoutActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupCartList() {
        cartAdapter = CartAdapter(emptyList()) { item, newQuantity ->
            val db = dbHelper?.writableDatabase
            if (newQuantity == 0) {
                // If quantity reaches 0, safely remove the item from cart cache
                db?.delete("cart", "id = ?", arrayOf(item.id.toString()))
            } else {
                // Otherwise increment/decrement the SQLite count natively
                val values = ContentValues().apply { put("quantity", newQuantity) }
                db?.update("cart", values, "id = ?", arrayOf(item.id.toString()))
            }
            loadCartData() // Reload UI seamlessly
            (activity as? MainActivity)?.updateBadges()
        }
        binding.rvCartItems.layoutManager = LinearLayoutManager(context)
        binding.rvCartItems.adapter = cartAdapter
    }

    private fun loadCartData() {
        val cartItems = repository.getCartItems()
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

    // Force reloading the cart whenever returning to the tab
    override fun onResume() {
        super.onResume()
        loadCartData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
