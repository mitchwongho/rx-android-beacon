package com.github.mitchwongho.android.beacon.bluetooth.rx

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.AdvertiseSettings
import rx.Observable

/**
 *
 */

data class LeScanResult(val deviceName: String, val deviceAddress: String, val rssi: Int, val scanRecord: Array<Byte>, val timestamp: Long, var age: Long = 0, var oob: Int)

inline fun BluetoothAdapter.startScan(interval: Long, pollingEnabled: Boolean): Observable<List<LeScanResult>> = RxBluetoothAdapter.startLeScan(this, interval, pollingEnabled)

data class LeAdvertiseStarted(public val settings: AdvertiseSettings)

sealed class LeAdvertiseStartError(val errorCode: Int) : Exception()

inline fun BluetoothAdapter.advertise(): Observable<LeAdvertiseStarted> = RxBluetoothAdapter.advertise(this)