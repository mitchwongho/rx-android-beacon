package com.github.mitchwongho.android.beacon.bluetooth.rx;

import android.bluetooth.BluetoothAdapter;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.MainThreadSubscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 *
 */
public class StartLeScanOnSubscribe implements Observable.OnSubscribe<LeScanResult> {

    public String TAG = StartLeScanOnSubscribe.class.getSimpleName();

    private BluetoothAdapter adapter;
    private Long duration = 0L;

    public StartLeScanOnSubscribe(@NonNull final BluetoothAdapter adapter, @NonNull final Integer duration) {
        this.adapter = adapter;
        this.duration = duration.longValue();
    }

    @Override
    public void call(final Subscriber<? super LeScanResult> subscriber) {

        final BluetoothAdapter.LeScanCallback leScanCallback = (device, rssi, scanRecord) -> {
            final Byte[] record = new Byte[scanRecord.length];
            for(int i=0; i<scanRecord.length; i++) {
                record[i] = Byte.valueOf(scanRecord[i]);
            }
            subscriber.onNext(new LeScanResult(device, rssi, record, System.currentTimeMillis(), 0L, 0));
        };

        if (duration > 0) {
            final Subscription sub = Observable.timer(duration, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.trampoline())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(aLong -> {
                        //
                        Log.w(TAG, String.format("onNext -> Timer Expired {duration=%dms}", duration.longValue()));
                    }, throwable -> {
                        // onError
                        Log.e(TAG, "onError: ", throwable);
                        subscriber.onError(throwable);
                    }, () -> {
                        Log.d(TAG, "onCompleted");
                        subscriber.onCompleted();
                    });

            subscriber.add(sub);
        }

        adapter.startLeScan(leScanCallback);

        subscriber.add(new MainThreadSubscription() {
            @Override
            protected void onUnsubscribe() {
                adapter.stopLeScan(leScanCallback);
            }
        });
    }
}
