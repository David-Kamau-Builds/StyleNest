package com.app.shoppy.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.shoppy.databinding.ItemOrderBinding
import com.app.shoppy.model.Order

class OrderAdapter(private val orders: List<Order>) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(val binding: ItemOrderBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        with(holder.binding) {
            tvOrderNumber.text = order.orderNumber
            tvOrderDate.text = order.orderDate
            tvOrderTotal.text = "KSh ${order.totalAmount}"
            tvOrderStatus.text = order.status
        }
    }

    override fun getItemCount(): Int = orders.size
}
