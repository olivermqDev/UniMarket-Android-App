package com.atom.unimarket

import android.app.Application
import com.atom.unimarket.presentation.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class UniMarketApp : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            // (Opcional) Un logger para ver qué está haciendo Koin en el Logcat
            androidLogger()
            // Provee el contexto de Android a Koin
            androidContext(this@UniMarketApp)
            // Carga tus módulos
            modules(appModule)
        }
    }
}