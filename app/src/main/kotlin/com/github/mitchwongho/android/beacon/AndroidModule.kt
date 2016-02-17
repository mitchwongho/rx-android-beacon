package com.github.mitchwongho.android.beacon

import android.app.Application
import android.content.Context
import com.github.mitchwongho.android.beacon.app.BaseApplication
import com.github.mitchwongho.android.beacon.app.ProfileLayoutsAktivity
import com.github.mitchwongho.android.beacon.database.DAO
import com.github.mitchwongho.android.beacon.database.RealmDao
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * Components bind @modules to @inject
 */
@Singleton
@Component(modules = arrayOf(AndroidModule::class))
interface ApplicationComponent {
    fun inject(application: BaseApplication)
    fun inject(profileLayoutActivity: ProfileLayoutsAktivity)
}
//
/**
 * A module for Android-specific dependencies which require
 * a [Context] or [android.app.Application] to create.
 */
@Module
class AndroidModule(private val application: Application) {

    /**
     * Allow the application context to be injected but require that it be
     * annotated with [@Annotation][ForApplication] to explicitly differentiate it from an activity context.
     */
    @Provides
    @Singleton
    @ForApplication
    fun provideApplicationContext(): Context {
        return application
    }

    @Provides
    @Singleton
    fun provideDAO(): DAO {
        return RealmDao(application)
    }
}