package com.github.mitchwongho.android.beacon.service

import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.github.mitchwongho.android.beacon.bluetooth.rx.LeScanResult
import com.github.mitchwongho.android.beacon.bluetooth.rx.startScan
import rx.Observable
import rx.Subscription

/**
 *
 */

public class BeaconServiceBinder(public val service: BeaconService) : Binder() {
    public fun startLeScan(interval: Long = 10000, pollingEnabled: Boolean = false): Observable<List<LeScanResult>>? = service.adapter?.startScan(interval, pollingEnabled)
}

public class BeaconService : Service() {

    val TAG = BeaconService::class.simpleName
    var scanSubscription: Subscription?
    var adapter: BluetoothAdapter?
    val binder: BeaconServiceBinder

    init {
        scanSubscription = null
        adapter = null
        binder = BeaconServiceBinder(this)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }


    override fun onCreate() {
        super.onCreate()
        adapter = (getSystemService(BLUETOOTH_SERVICE) as BluetoothManager).adapter
        //TODO test that the Bluetooth Radio is ON
        val enabled = adapter?.isEnabled ?: false
        Log.e(TAG, "onCreate() {enabled=$enabled}")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.w(TAG, "onDestroy")
        scanSubscription?.unsubscribe()
    }
}