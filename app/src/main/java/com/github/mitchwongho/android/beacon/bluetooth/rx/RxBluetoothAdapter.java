package com.github.mitchwongho.android.beacon.bluetooth.rx;

import android.bluetooth.BluetoothAdapter;
import android.os.Build;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;

/**
 *
 */
public final class RxBluetoothAdapter {

    public static Observable<List<LeScanResult>> startLeScan(@NonNull BluetoothAdapter adapter, @NonNull Integer duration, @NonNull Long interval) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            //pre-Lollipop

        } else {
            // Lollipop+
//            throw new UnsupportedOperationException("Not yet implemented for Lollipop and newer");
        }
//        return Observable.create(new StartLeScanOnSubscribe(adapter, duration)).
//                toMap(leScanResult -> leScanResult.getBluetoothDevice().getAddress()).
//                flatMap( map -> Observable.<List<LeScanResult>>just( new ArrayList(map.values()) ) );
        return Observable.create(new StartLeScanOnSubscribe(adapter, duration)).
                buffer(interval, TimeUnit.MILLISECONDS);
    }
}
