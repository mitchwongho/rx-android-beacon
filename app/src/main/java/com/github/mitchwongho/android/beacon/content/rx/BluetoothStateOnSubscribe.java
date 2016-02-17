package com.github.mitchwongho.android.beacon.content.rx;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;

import com.github.mitchwongho.android.beacon.ext.BluetoothStateChanged;

import rx.Observable;
import rx.Subscriber;
import rx.android.MainThreadSubscription;

/**
 *
 */
public class BluetoothStateOnSubscribe implements Observable.OnSubscribe<BluetoothStateChanged> {

    private Context context;

    public BluetoothStateOnSubscribe(@NonNull final Context context) {
        this.context = context;
    }

    @Override
    public void call(final Subscriber<? super BluetoothStateChanged> subscriber) {
        final BroadcastReceiver br = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, final Intent intent) {
                subscriber.onNext( new BluetoothStateChanged(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF)));
            }
        };

        context.registerReceiver( br, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

        subscriber.add(new MainThreadSubscription() {
            @Override
            protected void onUnsubscribe() {
                context.unregisterReceiver(br);
            }
        });

    }
}
