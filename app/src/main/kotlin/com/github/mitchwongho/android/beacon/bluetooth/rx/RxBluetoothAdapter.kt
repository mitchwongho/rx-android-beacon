package com.github.mitchwongho.android.beacon.bluetooth.rx

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import rx.Observable

/**
 *
 */

public data class LeScanResult(val bluetoothDevice: BluetoothDevice, val rssi: Int, val scanRecord: Array<Byte>, val timestamp: Long, var age: Long = 0, var oob: Int)

public inline fun BluetoothAdapter.startScan(duration: Int, interval: Long): Observable<List<LeScanResult>> = RxBluetoothAdapter.startLeScan(this, duration, interval)