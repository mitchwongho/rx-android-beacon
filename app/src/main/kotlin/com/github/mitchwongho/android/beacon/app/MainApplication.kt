package com.github.mitchwongho.android.beacon.app

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import com.github.mitchwongho.android.beacon.ApplicationComponent
import com.github.mitchwongho.android.beacon.service.BeaconService

/**
 *
 */
class MainApplication : BaseApplication() {

    val TAG = MainApplication::class.java.simpleName

    lateinit var component: ApplicationComponent
    var service: Intent?

    init {
        service = null
    }

    //    val conn = object : ServiceConnection {
    //        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
    //            Log.d(TAG, "onServiceConnected")
    //        }
    //
    //        override fun onServiceDisconnected(name: ComponentName?) {
    //            Log.d(TAG, "onServiceDisconnected")
    //        }
    //    }

    val lifecycleCallbackListener = object : Application.ActivityLifecycleCallbacks {

        override fun onActivityStarted(activity: Activity?) {
            service = Intent(this@MainApplication, BeaconService::class.java)
            startService(service)
            //            bindService( service, conn , BIND_AUTO_CREATE)
        }

        override fun onActivityPaused(activity: Activity?) {
            //            unbindService(conn)
        }

        override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
            //nop
        }

        override fun onActivityDestroyed(activity: Activity?) {
            stopService(service)
        }

        override fun onActivityResumed(activity: Activity?) {
            //nop
        }

        override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {
            //nop
        }

        override fun onActivityStopped(activity: Activity?) {
            //nop
        }
    }

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(lifecycleCallbackListener)
        val component = super.initDaggerComponent()
        component.inject(this)
        this.component = component
    }

    override fun onTerminate() {
        super.onTerminate()
        unregisterActivityLifecycleCallbacks(lifecycleCallbackListener)
    }


}