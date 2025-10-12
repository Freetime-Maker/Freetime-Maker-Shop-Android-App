package com.freetime.data.di

import org.koin.dsl.module;

val DataModule = module {
    includes(NetworkModule, RepositoryModule)
}