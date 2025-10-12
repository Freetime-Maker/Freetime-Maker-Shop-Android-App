package com.freetime.domain.di

import org.koin.dsl.module

val DomainModule = module {
    includes(UseCaseModule)
}