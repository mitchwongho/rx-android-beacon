package com.github.mitchwongho.android.beacon.content

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.github.mitchwongho.android.beacon.service.BeaconService

/**
 *
 */
public class BootstrapReceiver : BroadcastReceiver() {

    val TAG = BootstrapReceiver::class.simpleName

    override fun onReceive(context: Context, intent: Intent?) {
        // start service (see manifest)
        Log.d(TAG, "onReceive")
        val service = Intent(context, BeaconService::class.java)
        context.startService(service)
    }
}