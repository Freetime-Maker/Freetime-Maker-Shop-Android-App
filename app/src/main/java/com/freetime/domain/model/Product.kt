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
    CAT,
    SELFMADE
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
            id = "first_background",
            title = "First Background",
            description = "This is my First Background I made.",
            price = 10.00,
            category = WallpaperCategory.SELFMADE,
            resolution = Resolution.MOBILE,
            imageUrl = "https://freetimemaker.github.io/Freetime-Maker-Shop/images/fb.png",
            downloadUrl = "https://1024terabox.com/s/1i7NtDzduOsy47_LRtfOHhg",
            fileSize = 588093,
            tags = listOf("selfmade", "crazy")
        )
    )
}