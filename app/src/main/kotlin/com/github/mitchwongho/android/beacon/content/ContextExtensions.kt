package com.github.mitchwongho.android.beacon.content

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.github.mitchwongho.android.beacon.app.SettingsAktivity
import com.github.mitchwongho.android.beacon.content.rx.RxContext
import com.github.mitchwongho.android.beacon.database.rx.RxRealm
import io.realm.RealmObject
import io.realm.RealmResults
import org.altbeacon.beacon.BeaconManager
import rx.Observable

/**
 * AltBeacon
 */
interface ServiceBindEvent

data class ServiceConnectedEvent(val name: ComponentName?, val service: IBinder?, val conn: ServiceConnection) : ServiceBindEvent
data class ServiceDisconnectedEvent(val name: ComponentName?) : ServiceBindEvent

interface AltBeaconServiceConnect
data class AltBeaconServiceConnectEvent(val beaconManager: BeaconManager) : AltBeaconServiceConnect

fun Context.rxBindService(intent: Intent, flags: Int): Observable<ServiceBindEvent> =
        RxContext.bindService(this, intent, flags)

fun Context.rxBindAltBeaconManager(scanOn: Long, scanOff: Long): Observable<AltBeaconServiceConnect> =
        RxContext.bindAltBeaconService(this, scanOn, scanOff)

/**
 * Bluetooth Adapter
 */
data class BluetoothStateChanged(val state: Int)

fun Context.receiverBluetoothState(): Observable<BluetoothStateChanged> =
        RxContext.receiverBluetoothState(this)

/**
 * SharedPreferences
 */
fun Context.prefAltBeaconEnabled(): Boolean =
        this.getSharedPreferences(this.packageName, Context.MODE_PRIVATE).
                getBoolean("altbeacon.enabled", false)

fun Context.prefAltBeaconEnabled(value: Boolean) =
        this.getSharedPreferences(this.packageName, Context.MODE_PRIVATE).
                edit().putBoolean("altbeacon.enabled", value).apply()

fun Context.prefScanOnPeriod(): Int =
        this.getSharedPreferences(this.packageName, Context.MODE_PRIVATE).
                getInt("scan.on.period", SettingsAktivity.SCAN_ON_MIN)

fun Context.prefScanOnPeriod(value: Int) =
        this.getSharedPreferences(this.packageName, Context.MODE_PRIVATE).
                edit().putInt("scan.on.period", value).apply()

fun Context.prefScanOffPeriod(): Int =
        this.getSharedPreferences(this.packageName, Context.MODE_PRIVATE).
                getInt("scan.off.period", SettingsAktivity.SCAN_OFF_MIN)

fun Context.prefScanOffPeriod(value: Int) =
        this.getSharedPreferences(this.packageName, Context.MODE_PRIVATE).
                edit().putInt("scan.off.period", value).apply()

fun Context.prefRadioRestartInterval(value: Int) =
        this.getSharedPreferences(this.packageName, Context.MODE_PRIVATE).
                edit().putInt("radio.restart.interval", value).apply()

fun Context.prefRadioRestartInterval(): Int =
        this.getSharedPreferences(this.packageName, Context.MODE_PRIVATE).
                getInt("radio.restart.interval", SettingsAktivity.RADIO_RESTART_MIN)

fun Context.prefRangingTimeout(value: Int) =
        this.getSharedPreferences(this.packageName, Context.MODE_PRIVATE).
                edit().putInt("ranging.timeout", value).apply()

fun Context.prefRangingTimeout(): Int =
        this.getSharedPreferences(this.packageName, Context.MODE_PRIVATE).
                getInt("ranging.timeout", SettingsAktivity.RADIO_RESTART_MIN)

fun Context.prefBeaconReceiver(): Boolean =
        this.getSharedPreferences(this.packageName, Context.MODE_PRIVATE).
                getBoolean("beacon.mode.receiver", true)

fun Context.prefBeaconReceiver(value: Boolean) =
        this.getSharedPreferences(this.packageName, Context.MODE_PRIVATE).
                edit().putBoolean("beacon.mode.receiver", value).apply()
/**
 * REALM
 */
fun Context.insertOrUpdate(realmObject: RealmObject): Observable<RealmObject> =
        RxRealm.insertOrUpdate(this, realmObject)

fun <T : RealmObject> Context.fetchDO(clazz: Class<T>): Observable<RealmResults<T>> {
    return RxRealm.fetchAll(this, clazz)
}