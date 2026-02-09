package com.freetime.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Wallpaper(
    val id: String,
    val title: String,
    val description: String,
    val price: Double,
    val currency: String = "USD",
    val category: WallpaperCategory,
    val resolution: Resolution,
    val imageUrl: String? = null,
    val downloadUrl: String,
    val fileSize: Long,
    val tags: List<String> = emptyList()
)

@Serializable
enum class WallpaperCategory {
    ABSTRACT,
    NATURE,
    CITYSCAPE,
    MINIMALIST,
    TECHNOLOGY,
    ARTISTIC,
    SPACE,
    ANIMALS,
    VINTAGE,
    GEOMETRIC,
    CAT
}

@Serializable
enum class Resolution {
    HD_720P,
    FULL_HD_1080P,
    QHD_1440P,
    UHD_4K,
    UHD_8K,
    MOBILE,
    ULTRAWIDE,
    SQUARE
}

@Serializable
data class CartItem(
    val wallpaper: Wallpaper,
    val quantity: Int
)

@Serializable
data class Order(
    val id: String,
    val items: List<CartItem>,
    val totalAmount: Double,
    val currency: String,
    val status: OrderStatus,
    val createdAt: Long,
    val customerEmail: String,
    val paymentId: String? = null
)

enum class OrderStatus {
    PENDING,
    PAID,
    SHIPPED,
    DELIVERED,
    CANCELLED
}

// Predefined wallpapers collection
object SampleWallpapers {
    val wallpapers = listOf(
        Wallpaper(
            id = "black_cat",
            title = "Black Cat",
            description = "Beautiful Black Cat with a black Background.",
            price = 4.99,
            category = WallpaperCategory.CAT,
            resolution = Resolution.MOBILE,
            imageUrl = "https://images.unsplash.com/photo-1720064921203-22ba396237c2?w=400&auto=format&fit=crop&q=60&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8NHx8TW9iaWxlJTIwV2FsbHBhcGVyJTIwS2F0emV8ZW58MHx8MHx8fDA%3D",
            downloadUrl = "file:///android_asset/wallpapers/black_cat.jpg",
            fileSize = 8547328,
            tags = listOf("cat", "black", "black cat")
        )
    )
}
