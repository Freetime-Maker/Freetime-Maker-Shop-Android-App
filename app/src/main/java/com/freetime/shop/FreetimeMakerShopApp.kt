package com.freetime.shop

import android.app.Application
import com.freetime.shop.di.AppModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class FreetimeMakerShopApp : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@FreetimeMakerShopApp)
            modules(AppModule)
        }
    }
}

