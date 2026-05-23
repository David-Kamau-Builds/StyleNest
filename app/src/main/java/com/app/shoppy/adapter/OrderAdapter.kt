package com.app.shoppy.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.shoppy.databinding.ItemOrderBinding
import com.app.shoppy.data.remote.model.OrderDto

class OrderAdapter(
    private var orders: List<OrderDto>,
    private val onOrderClick: (String) -> Unit
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(val binding: ItemOrderBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onOrderClick(orders[position].orderNumber)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        with(holder.binding) {
            tvOrderNumber.text = order.orderNumber
            tvOrderDate.text = order.orderDate
            tvOrderTotal.text = String.format("KSh %.2f", order.totalAmount)
            tvOrderStatus.text = order.status ?: "Pending Delivery"
        }
    }

    override fun getItemCount(): Int = orders.size

    fun updateData(newOrders: List<OrderDto>) {
        this.orders = newOrders
        notifyDataSetChanged()
    }
}
