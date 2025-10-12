package com.freetime.domain.usecase

import com.freetime.domain.repository.ProductRepository

class GetProductUseCase (private val repository: ProductRepository) {
    suspend fun execute(let: Unit) = repository.getProducts()
}