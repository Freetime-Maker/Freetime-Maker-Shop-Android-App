package com.freetime.data.di

import com.freetime.data.repository.ProductRepositoryImpl
import com.freetime.domain.repository.ProductRepository
import org.koin.dsl.module

val RepositoryModule = module {
    single<ProductRepository> { ProductRepositoryImpl(get()) }
}