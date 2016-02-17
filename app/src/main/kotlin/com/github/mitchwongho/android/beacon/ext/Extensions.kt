package com.github.mitchwongho.android.beacon.ext

import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.support.v7.widget.CardView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.github.mitchwongho.android.beacon.app.SettingsAktivity
import com.github.mitchwongho.android.beacon.app.rx.AlertDialogButtonClicked
import com.github.mitchwongho.android.beacon.app.rx.RxAlertDialog
import com.github.mitchwongho.android.beacon.bluetooth.rx.RxAltBeaconManager
import com.github.mitchwongho.android.beacon.content.rx.RxContext
import io.realm.Realm
import io.realm.RealmObject
import org.altbeacon.beacon.Beacon
import org.altbeacon.beacon.BeaconManager
import org.altbeacon.beacon.Region
import rx.Observable
import java.nio.ByteBuffer
import java.util.*

/**
 *
 */

interface Event

interface ServiceBindEvent

data class ServiceConnectedEvent(val name: ComponentName?, val service: IBinder?, val conn: ServiceConnection) : ServiceBindEvent
data class ServiceDisconnectedEvent(val name: ComponentName?) : ServiceBindEvent

interface AltBeaconServiceConnect
data class AltBeaconServiceConnectEvent(val beaconManager: BeaconManager) : AltBeaconServiceConnect

fun Context.rxBindService(intent: Intent, flags: Int): Observable<ServiceBindEvent> =
        RxContext.bindService(this, intent, flags)

fun Context.rxBindAltBeaconManager(scanOn: Long, scanOff: Long): Observable<AltBeaconServiceConnect> =
        RxContext.bindAltBeaconService(this, scanOn, scanOff)

data class RangeBeaconsInRegion(val beacons: Collection<Beacon>, val region: Region)

fun BeaconManager.rxNotifyInRange(): Observable<RangeBeaconsInRegion> =
        RxAltBeaconManager.inRange(this)

data class BluetoothStateChanged(val state: Int)

fun Context.receiverBluetoothState(): Observable<BluetoothStateChanged> =
        RxContext.receiverBluetoothState(this)

/**
 * Extension function provides a compatible Resources.getColor() implementation
 */
fun CardView.setCardBackgroundColorCompat(res: Int): Unit {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        this.setCardBackgroundColor(this.context.resources.getColor(res))
    } else {
        this.setCardBackgroundColor(this.context.resources.getColor(res, this.context.theme))
    }
}
/**
 * Extension function provides a compatible Resources.getColor() implementation
 */
fun View.setBackgroundColorCompat(res: Int): Unit {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        this.setBackgroundColor(this.context.resources.getColor(res))
    } else {
        this.setBackgroundColor(this.context.resources.getColor(res, this.context.theme))
    }
}

enum class FilterType {
    TIMEOUT, RSSI
}

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

fun AlertDialog.Builder.create(resIdNegative: Int = 0, resIdNeutral: Int = 0, resIdPositive: Int = 0): Observable<AlertDialogButtonClicked> =
        RxAlertDialog.create(this, resIdNegative, resIdNeutral, resIdPositive)

fun AlertDialog.Builder.setCustomView(resId: Int) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
        this.setView( LayoutInflater.from(this.context).inflate(resId, null))
    } else {
        this.setView(resId)
    }
}

fun AlertDialog.getCustomView(): View? =
        (this.findViewById(android.R.id.custom) as FrameLayout).getChildAt(0)
