package com.freetime.domain.di

import com.freetime.domain.usecase.GetProductUseCase
import org.koin.dsl.module

val UseCaseModule = module {
    factory { GetProductUseCase(get()) }
}