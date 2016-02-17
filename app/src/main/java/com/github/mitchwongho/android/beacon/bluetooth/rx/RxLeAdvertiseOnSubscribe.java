package com.github.mitchwongho.android.beacon.bluetooth.rx;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.support.annotation.NonNull;
import android.util.Log;

import rx.Observable;
import rx.Subscriber;
import rx.android.MainThreadSubscription;

/**
 *
 */
public class RxLeAdvertiseOnSubscribe implements Observable.OnSubscribe<LeAdvertiseStarted> {

    public final static String TAG = RxLeAdvertiseOnSubscribe.class.getSimpleName();

    private BluetoothAdapter adapter;
    private AdvertiseSettings settings;
    private AdvertiseData advertiseData;

    public RxLeAdvertiseOnSubscribe(@NonNull final BluetoothAdapter adapter,
                                    @NonNull final AdvertiseSettings settings,
                                    @NonNull final AdvertiseData advertiseData) {
        this.adapter = adapter;
        this.settings = settings;
        this.advertiseData = advertiseData;
    }

    @TargetApi(21)
    @Override
    public void call(final Subscriber<? super LeAdvertiseStarted> subscriber) {
        final AdvertiseCallback callback = new AdvertiseCallback() {
            /**
             * Callback when advertising could not be started.
             *
             * @param errorCode Error code (see ADVERTISE_FAILED_* constants) for advertising start
             *                  failures.
             */
            @Override
            public void onStartFailure(int errorCode) {
//                super.onStartFailure(errorCode);
                Log.e(TAG, "onCall:onStartFailure");
                subscriber.onError(new Exception());
            }

            /**
             * Callback triggered in response to {@link BluetoothLeAdvertiser#startAdvertising} indicating
             * that the advertising has been started successfully.
             *
             * @param settingsInEffect The actual settings used for advertising, which may be different from
             *                         what has been requested.
             */
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
//                super.onStartSuccess(settingsInEffect);
                Log.d(TAG, "onCall:onStartSuccess");
                subscriber.onNext(new LeAdvertiseStarted(settingsInEffect));
            }
        };

        Log.d(TAG, "onCall ->");
        if (adapter.isMultipleAdvertisementSupported()) {
            adapter.enable();
            final BluetoothLeAdvertiser advertiser = adapter.getBluetoothLeAdvertiser();
            Log.d(TAG, "onCall -->");
            advertiser.startAdvertising(settings, advertiseData, callback);
            Log.d(TAG, "onCall <-");
        } else {
            subscriber.onError(new UnsupportedOperationException("Device doesn't support advertising or the Bluetooth radio is off"));
        }

        subscriber.add(new MainThreadSubscription() {
            @Override
            protected void onUnsubscribe() {
                final BluetoothLeAdvertiser advertiser = adapter.getBluetoothLeAdvertiser();
                if (advertiser != null) {
                    advertiser.stopAdvertising(callback);
                }
            }
        });

    }
}
