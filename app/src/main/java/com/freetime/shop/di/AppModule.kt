package com.freetime.shop.di

import com.freetime.data.payment.FreetimePaymentManagerImpl
import com.freetime.data.repository.ProductRepositoryImpl
import com.freetime.domain.payment.FreetimePaymentManager
import com.freetime.domain.repository.ProductRepository
import com.freetime.domain.usecase.CheckoutUseCase
import com.freetime.domain.viewmodel.CartViewModel
import com.freetime.domain.viewmodel.CheckoutViewModel
import com.freetime.domain.viewmodel.ProductViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val AppModule = module {
    
    // Repository
    single<ProductRepository> { ProductRepositoryImpl() }
    
    // Payment Manager
    single<FreetimePaymentManager> { FreetimePaymentManagerImpl(androidContext()) }
    
    // Use Cases
    single { CheckoutUseCase(get(), get()) }
    
    // ViewModels
    viewModel { ProductViewModel(get()) }
    viewModel { CartViewModel(get()) }
    viewModel { CheckoutViewModel(get()) }
}
