package com.freetime.shop.ui.feature.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel
import com.freetime.domain.model.Product
import com.freetime.domain.model.ProductCategory
import com.freetime.domain.model.Platform
import com.freetime.domain.model.Wallpaper
import com.freetime.domain.model.WallpaperCategory
import com.freetime.domain.viewmodel.ProductViewModel
import com.freetime.domain.viewmodel.ProductUIState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, viewModel: ProductViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val filteredProducts by viewModel.filteredProducts.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedPlatform by viewModel.selectedPlatform.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Freetime Maker Shop",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            IconButton(
                onClick = { navController.navigate("cart") }
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = "Cart"
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Category Filter
        CategoryFilter(
            selectedCategory = selectedCategory,
            onCategorySelected = viewModel::filterByCategory
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Platform Filter
        PlatformFilter(
            selectedPlatform = selectedPlatform,
            onPlatformSelected = viewModel::filterByPlatform
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Clear Filters Button
        if (selectedCategory != null || selectedPlatform != null) {
            Button(
                onClick = { viewModel.clearFilters() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Clear Filters")
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // Content
        when (uiState) {
            is ProductUIState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            is ProductUIState.Success -> {
                if (filteredProducts.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No products found")
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredProducts) { product ->
                            ProductCard(
                                product = product,
                                onProductClick = { 
                                    navController.navigate("product/${product.id}")
                                },
                                onAddToCart = { 
                                    navController.navigate("product/${product.id}")
                                }
                            )
                        }
                    }
                }
            }
            
            is ProductUIState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = (uiState as ProductUIState.Error).message,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.refreshProducts() }
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryFilter(
    selectedCategory: WallpaperCategory?,
    onCategorySelected: (WallpaperCategory?) -> Unit
) {
    val categories: List<Pair<WallpaperCategory?, String>> = listOf(
        null to "All",
        WallpaperCategory.ABSTRACT to "Abstract",
        WallpaperCategory.NATURE to "Nature",
        WallpaperCategory.CITYSCAPE to "Cityscape",
        WallpaperCategory.MINIMALIST to "Minimalist",
        WallpaperCategory.TECHNOLOGY to "Technology",
        WallpaperCategory.ARTISTIC to "Artistic",
        WallpaperCategory.SPACE to "Space",
        WallpaperCategory.ANIMALS to "Animals",
        WallpaperCategory.VINTAGE to "Vintage",
        WallpaperCategory.GEOMETRIC to "Geometric",
        WallpaperCategory.CAT to "Cat"
    )
    
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(categories.size) { index ->
            val (category, name) = categories[index] as Pair<WallpaperCategory, String>
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                label = { Text(name) }
            )
        }
    }
}

@Composable
fun PlatformFilter(
    selectedPlatform: Platform?,
    onPlatformSelected: (Platform?) -> Unit
) {
    val platforms: List<Pair<Platform?, String>> = listOf(
        null to "All",
        Platform.ANDROID to "Android",
        Platform.WINDOWS to "Windows",
        Platform.LINUX to "Linux"
    )
    
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(platforms.size) { index ->
            val (platform, name) = platforms[index] as Pair<Platform, String>
            FilterChip(
                selected = selectedPlatform == platform,
                onClick = { onPlatformSelected(platform) },
                label = { Text(name) }
            )
        }
    }
}

@Composable
fun ProductCard(
    wallpaper: Wallpaper,
    onProductClick: () -> Unit,
    onAddToCart: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        onClick = onProductClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = wallpaper.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = wallpaper.category.name.replace("_", " "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = wallpaper.resolution.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            
            Column {
                Text(
                    text = "$${wallpaper.price}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Button(
                    onClick = onAddToCart,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View Details")
                }
            }
        }
    }
}
