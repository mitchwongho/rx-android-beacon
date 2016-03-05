package com.github.mitchwongho.android.beacon.bluetooth.rx;

import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.util.Log;

import com.github.mitchwongho.android.beacon.ext.MonitorBeaconsInRegion;
import com.github.mitchwongho.android.beacon.ext.RangeBeaconsInRegion;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.service.MonitorState;

import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;

import rx.Observable;
import rx.Subscriber;
import rx.android.MainThreadSubscription;

/**
 *
 */
public class MonitorBeaconsInRegionOnSubscribe implements Observable.OnSubscribe<MonitorBeaconsInRegion> {

    public final String TAG = MonitorBeaconsInRegionOnSubscribe.class.getSimpleName();

    final BeaconManager beaconManager;

    /**
     * Constructor
     * @param beaconManager
     */
    public MonitorBeaconsInRegionOnSubscribe(@NonNull final BeaconManager beaconManager) {
        this.beaconManager = beaconManager;
    }

    @Override
    public void call(@NonNull final Subscriber<? super MonitorBeaconsInRegion> subscriber) {

        beaconManager.setMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(@NonNull final Region region) {
                Log.d(TAG, "didEnterRegion()");
            }

            @Override
            public void didExitRegion(@NonNull final Region region) {
                Log.d(TAG, "didExitRegion()");
            }

            @Override
            public void didDetermineStateForRegion(int i, @NonNull final Region region) {
                Log.d(TAG, String.format("didDetermineStateForRegion() {state=%d,address=%s}", i, region.getBluetoothAddress()));
                subscriber.onNext(new MonitorBeaconsInRegion((MonitorNotifier.INSIDE == i), region));
            }
        });

        final UUID uuid = UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D");
        final Region region1 = new Region(TAG, Identifier.fromUuid(uuid), Identifier.fromInt(24024), Identifier.fromInt(48056));
        final Region region2 = new Region(TAG, Identifier.fromUuid(uuid), Identifier.fromInt(37537), Identifier.fromInt(61569));

        try {
            beaconManager.startMonitoringBeaconsInRegion(region2);
            beaconManager.startMonitoringBeaconsInRegion(region1);
        } catch (Throwable t) {
            subscriber.onError(t);
        }

        subscriber.add(new MainThreadSubscription() {
            @Override
            protected void onUnsubscribe() {
                try {
                    beaconManager.stopMonitoringBeaconsInRegion(region1);
                    beaconManager.setRangeNotifier(null);
                } catch (Exception e) {}
            }
        });
    }
}
