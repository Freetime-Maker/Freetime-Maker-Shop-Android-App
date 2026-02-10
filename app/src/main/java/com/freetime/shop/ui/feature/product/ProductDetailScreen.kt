package com.freetime.shop.ui.feature.product

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel
import com.freetime.domain.model.Wallpaper
import com.freetime.domain.viewmodel.CartViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: String,
    navController: NavController,
    cartViewModel: CartViewModel = koinViewModel()
) {
    // This would typically come from a ProductViewModel
    // For now, we'll use a sample wallpaper or get it from repository
    var wallpaper by remember { mutableStateOf<Wallpaper?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var quantity by remember { mutableStateOf(1) }
    
    LaunchedEffect(productId) {
        // In a real app, you'd get this from ProductViewModel
        // For demo purposes, we'll create a sample wallpaper
        isLoading = false
        wallpaper = Wallpaper(
            id = productId,
            title = "Sample Wallpaper",
            description = "This is a sample wallpaper description. It would contain details about the wallpaper style, resolution, and visual characteristics.",
            price = 4.99,
            category = com.freetime.domain.model.WallpaperCategory.ABSTRACT,
            resolution = com.freetime.domain.model.Resolution.UHD_4K,
            imageUrl = "https://picsum.photos/800/600?random=1",
            downloadUrl = "https://freetimemaker.github.io/Freetime-Maker-Shop/download/",
            fileSize = 8547328,
            tags = listOf("abstract", "colorful", "modern")
        )
    }
    
    when {
        isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        wallpaper == null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Wallpaper not found")
            }
        }
        else -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                TopAppBar(
                    title = { Text(wallpaper!!.title) },
                    navigationIcon = {
                        IconButton(
                            onClick = { navController.navigateUp() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { navController.navigate("cart") }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "Cart"
                            )
                        }
                    }
                )
                
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Wallpaper Image Placeholder
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Wallpaper Preview",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Wallpaper Title and Price
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = wallpaper!!.title,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "$${wallpaper!!.price}",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        // Category and Resolution badges
                        Column {
                            SuggestionChip(
                                onClick = { },
                                label = { Text(wallpaper!!.category.name.replace("_", " ")) }
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            SuggestionChip(
                                onClick = { },
                                label = { Text(wallpaper!!.resolution.name.replace("_", " ")) }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Description
                    Text(
                        text = "Description",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = wallpaper!!.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Tags
                    if (wallpaper!!.tags.isNotEmpty()) {
                        Text(
                            text = "Tags",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        wallpaper!!.tags.forEach { tag ->
                            SuggestionChip(
                                onClick = { },
                                label = { Text(tag) },
                                modifier = Modifier.padding(end = 8.dp, bottom = 4.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    // File Info
                    Text(
                        text = "File Information",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "File Size: ${wallpaper!!.fileSize / (1024 * 1024)} MB",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    // Quantity Selector
                    Text(
                        text = "Quantity",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { if (quantity > 1) quantity-- }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Decrease quantity"
                            )
                        }
                        
                        Text(
                            text = quantity.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        
                        IconButton(
                            onClick = { quantity++ }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Increase quantity"
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Add to Cart Button
                    Button(
                        onClick = {
                            wallpaper?.let { 
                                cartViewModel.addToCart(it, quantity)
                                navController.navigate("cart")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add to Cart - $${wallpaper!!.price * quantity}")
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Download Button
                    Button(
                        onClick = {
                            wallpaper?.let { 
                                cartViewModel.addToCart(it, quantity)
                                navController.navigate("checkout")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Download Now - $${wallpaper!!.price * quantity}")
                    }
                }
            }
        }
    }
}
