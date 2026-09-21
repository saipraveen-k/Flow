package com.flowos.app

import android.app.Application
import com.flowos.app.di.AppContainer

class FlowOSApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
