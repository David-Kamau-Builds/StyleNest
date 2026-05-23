package com.app.shoppy.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.shoppy.databinding.ItemReviewBinding
import com.app.shoppy.data.remote.model.ReviewDto

class ReviewAdapter(private var reviews: List<ReviewDto>) : RecyclerView.Adapter<ReviewAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemReviewBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val review = reviews[position]
        with(holder.binding) {
            tvReviewerName.text = review.userName
            tvReviewerInitial.text = review.userName.take(1).uppercase()
            ratingBar.rating = review.rating.toFloat()
            tvReviewDate.text = review.date ?: ""
            tvReviewComment.text = review.comment
            
            // Generate deterministic color for circle background based on name hash
            val colors = listOf(
                android.graphics.Color.parseColor("#E57373"),
                android.graphics.Color.parseColor("#F06292"),
                android.graphics.Color.parseColor("#BA68C8"),
                android.graphics.Color.parseColor("#9575CD"),
                android.graphics.Color.parseColor("#7986CB"),
                android.graphics.Color.parseColor("#64B5F6"),
                android.graphics.Color.parseColor("#4DD0E1"),
                android.graphics.Color.parseColor("#4DB6AC"),
                android.graphics.Color.parseColor("#81C784"),
                android.graphics.Color.parseColor("#AED581"),
                android.graphics.Color.parseColor("#FF8A65")
            )
            val colorIndex = Math.abs(review.userName.hashCode()) % colors.size
            tvReviewerInitial.background.setTint(colors[colorIndex])
        }
    }

    override fun getItemCount(): Int = reviews.size

    fun updateData(newReviews: List<ReviewDto>) {
        this.reviews = newReviews
        notifyDataSetChanged()
    }
}
