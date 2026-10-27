package com.islamshikhi.app

import android.app.Application

class IslamShikhiApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
