package com.app.shoppy.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.app.shoppy.data.local.dao.CartDao
import com.app.shoppy.data.local.dao.ProductDao
import com.app.shoppy.data.local.dao.WishlistDao
import com.app.shoppy.data.local.entity.CartEntity
import com.app.shoppy.data.local.entity.ProductEntity
import com.app.shoppy.data.local.entity.WishlistEntity

@Database(
    entities = [ProductEntity::class, CartEntity::class, WishlistEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun cartDao(): CartDao
    abstract fun wishlistDao(): WishlistDao
}
