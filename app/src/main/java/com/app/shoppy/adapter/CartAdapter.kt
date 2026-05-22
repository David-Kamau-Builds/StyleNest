package com.app.shoppy.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.shoppy.databinding.ItemCartBinding
import com.app.shoppy.model.CartItem

class CartAdapter(
    private var cartItems: List<CartItem>,
    private val onQuantityChange: (CartItem, Int) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    inner class CartViewHolder(val binding: ItemCartBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = cartItems[position]
        with(holder.binding) {
            cartItemName.text = item.productName
            cartItemSize.text = "Size: ${item.selectedSize}"
            cartItemPrice.text = "KSh ${item.price}"
            cartItemQuantity.text = item.quantity.toString()

            val context = root.context
            val resourceId = context.resources.getIdentifier(item.imageName, "mipmap", context.packageName)
            if(resourceId != 0) {
                cartItemImage.setImageResource(resourceId)
            }

            btnIncrease.setOnClickListener {
                onQuantityChange(item, item.quantity + 1)
            }
            
            btnDecrease.setOnClickListener {
                if (item.quantity > 1) {
                    onQuantityChange(item, item.quantity - 1)
                } else {
                    onQuantityChange(item, 0) // Treat 0 as remove item
                }
            }
        }
    }

    override fun getItemCount(): Int = cartItems.size
    
    fun updateData(newItems: List<CartItem>) {
        cartItems = newItems
        notifyDataSetChanged()
    }
}
