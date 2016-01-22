package com.github.mitchwongho.android.beacon.app

import android.app.Activity
import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import com.github.mitchwongho.android.beacon.service.BeaconService

/**
 *
 */
public class MainApplication : Application() {

    val TAG = MainApplication::class.java.simpleName

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
            service = Intent( this@MainApplication , BeaconService::class.java )
            startService( service )
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
    }

    override fun onTerminate() {
        super.onTerminate()
        unregisterActivityLifecycleCallbacks(lifecycleCallbackListener)
    }


}