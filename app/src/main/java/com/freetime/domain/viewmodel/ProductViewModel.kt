package com.freetime.domain.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freetime.domain.model.Wallpaper
import com.freetime.domain.model.WallpaperCategory
import com.freetime.domain.model.Resolution
import com.freetime.domain.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProductViewModel(
    private val productRepository: ProductRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<ProductUIState>(ProductUIState.Loading)
    val uiState: StateFlow<ProductUIState> = _uiState.asStateFlow()
    
    private val _products = MutableStateFlow<List<Wallpaper>>(emptyList())
    val products: StateFlow<List<Wallpaper>> = _products.asStateFlow()
    
    private val _filteredProducts = MutableStateFlow<List<Wallpaper>>(emptyList())
    val filteredProducts: StateFlow<List<Wallpaper>> = _filteredProducts.asStateFlow()
    
    private val _selectedCategory = MutableStateFlow<WallpaperCategory?>(null)
    val selectedCategory: StateFlow<WallpaperCategory?> = _selectedCategory.asStateFlow()
    
    private val _selectedPlatform = MutableStateFlow<Resolution?>(null)
    val selectedPlatform: StateFlow<Resolution?> = _selectedPlatform.asStateFlow()
    
    init {
        loadProducts()
    }
    
    private fun loadProducts() {
        viewModelScope.launch {
            try {
                _uiState.value = ProductUIState.Loading
                val result = productRepository.getProducts()
                if (result.isSuccess) {
                    val wallpapers = result.getOrNull() ?: emptyList()
                    _products.value = wallpapers
                    _filteredProducts.value = wallpapers
                    _uiState.value = ProductUIState.Success(wallpapers)
                } else {
                    _uiState.value = ProductUIState.Error(result.exceptionOrNull()?.message ?: "Failed to load products")
                }
            } catch (e: Exception) {
                _uiState.value = ProductUIState.Error(e.message ?: "Failed to load products")
            }
        }
    }
    
    fun filterByCategory(category: WallpaperCategory?) {
        _selectedCategory.value = category
        applyFilters()
    }
    
    fun filterByPlatform(platform: Resolution?) {
        _selectedPlatform.value = platform
        applyFilters()
    }
    
    fun clearFilters() {
        _selectedCategory.value = null
        _selectedPlatform.value = null
        _filteredProducts.value = _products.value
    }
    
    private fun applyFilters() {
        val category = _selectedCategory.value
        val platform = _selectedPlatform.value
        
        val filtered = _products.value.filter { wallpaper ->
            val categoryMatch = category == null || wallpaper.category == category
            val platformMatch = platform == null || wallpaper.resolution == platform
            categoryMatch && platformMatch
        }
        
        _filteredProducts.value = filtered
    }
    
    fun getProductById(id: String): Wallpaper? {
        return _products.value.find { it.id == id }
    }
    
    fun refreshProducts() {
        loadProducts()
    }
}

sealed class ProductUIState {
    object Loading : ProductUIState()
    data class Success(val wallpapers: List<Wallpaper>) : ProductUIState()
    data class Error(val message: String) : ProductUIState()
}
