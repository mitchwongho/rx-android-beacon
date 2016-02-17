package com.github.mitchwongho.android.beacon.content.rx;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.github.mitchwongho.android.beacon.ext.AltBeaconServiceConnect;
import com.github.mitchwongho.android.beacon.ext.BluetoothStateChanged;
import com.github.mitchwongho.android.beacon.ext.ServiceBindEvent;

import rx.Observable;

/**
 *
 */
public final class RxContext {

    public static Observable<ServiceBindEvent> bindService(@NonNull Context context, @NonNull Intent intent, @NonNull Integer flags) {
        return Observable.create(new ServiceBindOnSubscribe(intent, context, flags));
    }

    public static Observable<AltBeaconServiceConnect> bindAltBeaconService(@NonNull Context context, @NonNull Long scanOn, @NonNull Long scanOff) {
        return Observable.create( new AltBeaconBindOnSubscribe(context, scanOn, scanOff));
    }

    public static Observable<BluetoothStateChanged> receiverBluetoothState(@NonNull Context context) {
        return Observable.create( new BluetoothStateOnSubscribe(context) );
    }
}
