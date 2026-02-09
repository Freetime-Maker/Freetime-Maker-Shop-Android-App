package com.freetime.domain.repository

import com.freetime.domain.model.Wallpaper
import com.freetime.domain.model.CartItem
import com.freetime.domain.model.Order
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    suspend fun getProducts(): Result<List<Wallpaper>>
    suspend fun getProductById(id: String): Result<Wallpaper?>
    suspend fun getProductsByCategory(category: com.freetime.domain.model.WallpaperCategory): Result<List<Wallpaper>>
    suspend fun getProductsByPlatform(platform: com.freetime.domain.model.Resolution): Result<List<Wallpaper>>
    fun getCartItems(): Flow<List<CartItem>>
    suspend fun addToCart(wallpaper: Wallpaper, quantity: Int = 1): Result<Unit>
    suspend fun removeFromCart(wallpaperId: String): Result<Unit>
    suspend fun updateCartQuantity(wallpaperId: String, quantity: Int): Result<Unit>
    suspend fun clearCart(): Result<Unit>
    suspend fun createOrder(items: List<CartItem>, customerEmail: String): Result<Order>
    suspend fun getOrderHistory(): Result<List<Order>>
    suspend fun getCartTotal(): Result<Double>
}
