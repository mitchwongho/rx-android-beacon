package com.github.mitchwongho.android.beacon.ext

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.support.v7.widget.CardView
import com.github.mitchwongho.android.beacon.content.rx.RxContext
import rx.Observable

/**
 *
 */
public interface ServiceBindEvent

public data class ServiceConnectedEvent(val name: ComponentName?, val service: IBinder?, val conn: ServiceConnection) : ServiceBindEvent
public data class ServiceDisconnectedEvent(val name: ComponentName?) : ServiceBindEvent


public fun Context.rxBindService(intent: Intent, flags: Int): Observable<ServiceBindEvent> =
        RxContext.bindService(this, intent, flags)

/**
 * Extension function provides a compatible Resources.getColor() implementation
 */
public fun CardView.setCardBackgroundColorCompat(res: Int): Unit = (
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            this.setCardBackgroundColor(this.context.resources.getColor(res))
        } else {
            this.setCardBackgroundColor(this.context.resources.getColor(res, this.context.theme))
        }
        )