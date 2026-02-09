package com.freetime.data.repository

import com.freetime.domain.model.Wallpaper
import com.freetime.domain.model.CartItem
import com.freetime.domain.model.Order
import com.freetime.domain.model.OrderStatus
import com.freetime.domain.model.SampleWallpapers
import com.freetime.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import kotlin.math.round

class ProductRepositoryImpl : ProductRepository {
    
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    
    override suspend fun getProducts(): Result<List<Wallpaper>> {
        return Result.success(SampleWallpapers.wallpapers)
    }
    
    override suspend fun getProductById(id: String): Result<Wallpaper?> {
        val wallpaper = SampleWallpapers.wallpapers.find { it.id == id }
        return Result.success(wallpaper)
    }
    
    override suspend fun getProductsByCategory(category: com.freetime.domain.model.WallpaperCategory): Result<List<Wallpaper>> {
        val filteredWallpapers = SampleWallpapers.wallpapers.filter { it.category == category }
        return Result.success(filteredWallpapers)
    }
    
    override suspend fun getProductsByPlatform(platform: com.freetime.domain.model.Resolution): Result<List<Wallpaper>> {
        val filteredWallpapers = SampleWallpapers.wallpapers.filter { it.resolution == platform }
        return Result.success(filteredWallpapers)
    }
    
    override fun getCartItems(): Flow<List<CartItem>> = _cartItems.asStateFlow()
    
    override suspend fun addToCart(wallpaper: Wallpaper, quantity: Int): Result<Unit> {
        return try {
            val currentCart = _cartItems.value.toMutableList()
            val existingItemIndex = currentCart.indexOfFirst { it.wallpaper.id == wallpaper.id }
            
            if (existingItemIndex >= 0) {
                val existingItem = currentCart[existingItemIndex]
                currentCart[existingItemIndex] = existingItem.copy(quantity = existingItem.quantity + quantity)
            } else {
                currentCart.add(CartItem(wallpaper, quantity))
            }
            
            _cartItems.value = currentCart
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun removeFromCart(wallpaperId: String): Result<Unit> {
        return try {
            val currentCart = _cartItems.value.toMutableList()
            currentCart.removeAll { it.wallpaper.id == wallpaperId }
            _cartItems.value = currentCart
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateCartQuantity(wallpaperId: String, quantity: Int): Result<Unit> {
        return try {
            if (quantity <= 0) {
                return removeFromCart(wallpaperId)
            }
            
            val currentCart = _cartItems.value.toMutableList()
            val itemIndex = currentCart.indexOfFirst { it.wallpaper.id == wallpaperId }
            
            if (itemIndex >= 0) {
                val item = currentCart[itemIndex]
                currentCart[itemIndex] = item.copy(quantity = quantity)
                _cartItems.value = currentCart
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun clearCart(): Result<Unit> {
        return try {
            _cartItems.value = emptyList()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun createOrder(items: List<CartItem>, customerEmail: String): Result<Order> {
        return try {
            val totalAmount = items.sumOf { it.wallpaper.price * it.quantity }
            val order = Order(
                id = UUID.randomUUID().toString(),
                items = items,
                totalAmount = round(totalAmount * 100) / 100, // Round to 2 decimal places
                currency = "USD",
                status = OrderStatus.PENDING,
                createdAt = System.currentTimeMillis(),
                customerEmail = customerEmail
            )
            
            val currentOrders = _orders.value.toMutableList()
            currentOrders.add(order)
            _orders.value = currentOrders
            
            // Clear cart after successful order creation
            clearCart()
            
            Result.success(order)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getOrderHistory(): Result<List<Order>> {
        return Result.success(_orders.value.sortedByDescending { it.createdAt })
    }
    
    override suspend fun getCartTotal(): Result<Double> {
        val total = _cartItems.value.sumOf { it.wallpaper.price * it.quantity }
        return Result.success(round(total * 100) / 100)
    }
}
