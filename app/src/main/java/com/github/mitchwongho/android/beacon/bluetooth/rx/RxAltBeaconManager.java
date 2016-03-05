package com.github.mitchwongho.android.beacon.bluetooth.rx;

import android.support.annotation.NonNull;

import com.github.mitchwongho.android.beacon.ext.MonitorBeaconsInRegion;
import com.github.mitchwongho.android.beacon.ext.RangeBeaconsInRegion;

import org.altbeacon.beacon.BeaconManager;

import rx.Observable;

/**
 *
 */
public class RxAltBeaconManager {

    public static String TAG = RxAltBeaconManager.class.getSimpleName();

    public static Observable<RangeBeaconsInRegion> inRange(@NonNull final BeaconManager beaconManager) {
        return Observable.create(new RangeBeaconsInRegionOnSubscribe(beaconManager));
    }
    public static Observable<MonitorBeaconsInRegion> monitor(@NonNull final BeaconManager beaconManager) {
        return Observable.create(new MonitorBeaconsInRegionOnSubscribe(beaconManager));
    }
}
