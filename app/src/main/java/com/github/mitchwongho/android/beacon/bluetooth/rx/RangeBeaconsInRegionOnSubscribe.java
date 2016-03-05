package com.github.mitchwongho.android.beacon.bluetooth.rx;

import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.util.Log;

import com.github.mitchwongho.android.beacon.ext.RangeBeaconsInRegion;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.logging.LogManager;

import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;

import rx.Observable;
import rx.Subscriber;
import rx.android.MainThreadSubscription;

/**
 *
 */
public class RangeBeaconsInRegionOnSubscribe implements Observable.OnSubscribe<RangeBeaconsInRegion> {

    public final String TAG = RangeBeaconsInRegionOnSubscribe.class.getSimpleName();

    final BeaconManager beaconManager;
    final UUID uuid = UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D");
    final Region region = new Region(TAG, Identifier.fromUuid(uuid), null, null);

    /**
     * Constructor
     * @param beaconManager
     */
    public RangeBeaconsInRegionOnSubscribe(@NonNull final BeaconManager beaconManager) {
        this.beaconManager = beaconManager;
    }

    @Override
    public void call(final Subscriber<? super RangeBeaconsInRegion> subscriber) {
        beaconManager.setDebug(true);
        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> collection, Region region) {
                Log.e(TAG, String.format("setRangeNotifier {found=%d}", collection.size()));
                final Iterator<Beacon> it = collection.iterator();
                while (it.hasNext()) {
                    final Beacon b = it.next();
                    Log.e(TAG, String.format("setRangeNotifier {beacon=%s}", b.toString()));
                }
                if (collection.size() > 0) {
                    subscriber.onNext(new RangeBeaconsInRegion(collection, region));
                }
            }
        });

        try {
            beaconManager.startRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
            subscriber.onError(e);
        }

        subscriber.add(new MainThreadSubscription() {
            @Override
            protected void onUnsubscribe() {
                try {
                    beaconManager.setRangeNotifier(null);
                    beaconManager.stopRangingBeaconsInRegion(region);
                } catch (Exception e){

                }
            }
        });
    }
}
