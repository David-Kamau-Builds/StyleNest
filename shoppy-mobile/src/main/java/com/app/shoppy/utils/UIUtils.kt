package com.app.shoppy.utils

import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerDrawable

object UIUtils {
    fun getShimmerDrawable(): ShimmerDrawable {
        val shimmer = Shimmer.AlphaHighlightBuilder()
            .setDuration(1500)
            .setBaseAlpha(0.85f)
            .setHighlightAlpha(0.6f)
            .setDirection(Shimmer.Direction.LEFT_TO_RIGHT)
            .setAutoStart(true)
            .build()

        return ShimmerDrawable().apply {
            setShimmer(shimmer)
        }
    }
}
