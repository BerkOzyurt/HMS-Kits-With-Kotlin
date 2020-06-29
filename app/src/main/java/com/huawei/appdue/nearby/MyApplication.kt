package com.huawei.appdue.nearby

import android.app.Application

class MyApplication: Application() {
    private var instance: MyApplication? = null

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    fun getInstance(): MyApplication? {
        return instance
    }
}