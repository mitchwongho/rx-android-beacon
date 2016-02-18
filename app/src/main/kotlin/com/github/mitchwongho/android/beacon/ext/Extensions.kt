package com.github.mitchwongho.android.beacon.ext

import android.app.AlertDialog
import android.os.Build
import android.support.v7.widget.CardView
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.github.mitchwongho.android.beacon.app.rx.AlertDialogButtonClicked
import com.github.mitchwongho.android.beacon.app.rx.RxAlertDialog
import com.github.mitchwongho.android.beacon.bluetooth.rx.RxAltBeaconManager
import org.altbeacon.beacon.Beacon
import org.altbeacon.beacon.BeaconManager
import org.altbeacon.beacon.Region
import rx.Observable

/**
 *
 */

interface Event

data class RangeBeaconsInRegion(val beacons: Collection<Beacon>, val region: Region)

fun BeaconManager.rxNotifyInRange(): Observable<RangeBeaconsInRegion> =
        RxAltBeaconManager.inRange(this)

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

fun AlertDialog.Builder.create(resIdNegative: Int = 0, resIdNeutral: Int = 0, resIdPositive: Int = 0): Observable<AlertDialogButtonClicked> =
        RxAlertDialog.create(this, resIdNegative, resIdNeutral, resIdPositive)

fun AlertDialog.Builder.setCustomView(resId: Int) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
        this.setView(LayoutInflater.from(this.context).inflate(resId, null))
    } else {
        this.setView(resId)
    }
}

fun AlertDialog.getCustomView(): View? =
        (this.findViewById(android.R.id.custom) as FrameLayout).getChildAt(0)
