package com.freetime.data.repository

import com.freetime.domain.model.Product
import com.freetime.domain.network.NetworkService
import com.freetime.domain.network.ResultWrapper
import com.freetime.domain.repository.ProductRepository

class ProductRepositoryImpl(private val networkService: NetworkService) : ProductRepository {
    override suspend fun getProducts(): ResultWrapper<List<Product>> {
        return networkService.getProducts()
    }
}