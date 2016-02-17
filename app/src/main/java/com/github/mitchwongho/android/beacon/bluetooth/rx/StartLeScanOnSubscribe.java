package com.github.mitchwongho.android.beacon.bluetooth.rx;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.android.MainThreadSubscription;
import rx.schedulers.Schedulers;

/**
 *
 */
public class StartLeScanOnSubscribe implements Observable.OnSubscribe<LeScanResult> {

    public String TAG = StartLeScanOnSubscribe.class.getSimpleName();

    private BluetoothAdapter adapter;

    public StartLeScanOnSubscribe(@NonNull final BluetoothAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public void call(final Subscriber<? super LeScanResult> subscriber) {

        final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        final BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                Log.d(TAG, "leScanCallback");
                final Byte[] record = new Byte[scanRecord.length];
                for (int i = 0; i < scanRecord.length; i++) {
                    record[i] = Byte.valueOf(scanRecord[i]);
                }
                subscriber.onNext(new LeScanResult(device.getName(), device.getAddress(), rssi, record, System.currentTimeMillis(), 0L, 0));
            }
        };

//        final UUID uuid0 = UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D");
//        adapter.startLeScan(new UUID[] { uuid0 }, leScanCallback);
        adapter.startLeScan(leScanCallback);

        subscriber.add(new MainThreadSubscription() {
            @Override
            protected void onUnsubscribe() {
                adapter.stopLeScan(leScanCallback);
            }
        });
    }
}
