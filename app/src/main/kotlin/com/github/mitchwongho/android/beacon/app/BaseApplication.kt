package com.github.mitchwongho.android.beacon.app

import android.app.Application
import com.github.mitchwongho.android.beacon.ApplicationComponent
import com.github.mitchwongho.android.beacon.AndroidModule
import com.github.mitchwongho.android.beacon.DaggerApplicationComponent

/**
 *
 */
abstract class BaseApplication : Application() {

    protected fun initDaggerComponent(): ApplicationComponent {
        return DaggerApplicationComponent.builder().androidModule(AndroidModule(this)).build()
    }
}