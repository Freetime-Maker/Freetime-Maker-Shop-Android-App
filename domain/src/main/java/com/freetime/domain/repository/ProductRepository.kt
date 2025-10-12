package com.freetime.domain.repository

import com.freetime.domain.model.Product
import com.freetime.domain.network.ResultWrapper

interface ProductRepository {
    suspend fun getProducts(): ResultWrapper<List<Product>>
}