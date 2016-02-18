package com.github.mitchwongho.android.beacon.content.rx;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.RemoteException;
import android.support.annotation.NonNull;

import com.github.mitchwongho.android.beacon.content.AltBeaconServiceConnect;
import com.github.mitchwongho.android.beacon.content.AltBeaconServiceConnectEvent;

import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Region;

import java.util.Iterator;

import rx.Observable;
import rx.Subscriber;
import rx.android.MainThreadSubscription;

/**
 *
 */
public class AltBeaconBindOnSubscribe implements Observable.OnSubscribe<AltBeaconServiceConnect> {

    public final String TAG = AltBeaconBindOnSubscribe.class.getSimpleName();

    private Context context;
    private Long scanOn, scanOff;

    public AltBeaconBindOnSubscribe(@NonNull final Context context,
                                    @NonNull final Long scanOn,
                                    @NonNull final Long scanOff) {
        this.context = context;
        this.scanOn = scanOn;
        this.scanOff = scanOff;
    }

    @Override
    public void call(final Subscriber<? super AltBeaconServiceConnect> subscriber) {
        final BeaconManager bm = BeaconManager.getInstanceForApplication(context);
        BeaconManager.setRegionExitPeriod(60000L);
        BeaconManager.setAndroidLScanningDisabled(true);
        BeaconManager.setsManifestCheckingDisabled(true);

        final BeaconConsumer consumer = new BeaconConsumer() {
            @Override
            public void onBeaconServiceConnect() {
                subscriber.onNext(new AltBeaconServiceConnectEvent(bm));
            }

            @Override
            public Context getApplicationContext() {
                return context.getApplicationContext();
            }

            @Override
            public void unbindService(ServiceConnection serviceConnection) {
                subscriber.onCompleted();
                context.unbindService(serviceConnection);
            }

            //
            @Override
            public boolean bindService(Intent intent, ServiceConnection serviceConnection, int i) {
                return context.bindService(intent, serviceConnection, i);
            }
        };

        // Bind to service
        bm.setBackgroundMode(false);
        bm.setForegroundScanPeriod(scanOn);
        bm.setForegroundBetweenScanPeriod(scanOff);
        bm.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        bm.bind(consumer);

        subscriber.add(new MainThreadSubscription() {
            @Override
            protected void onUnsubscribe() {

                bm.setRangeNotifier(null);
                final Iterator<Region> rregions = bm.getRangedRegions().iterator();
                while (rregions.hasNext()) {
                    try {
                        bm.stopRangingBeaconsInRegion(rregions.next());
                    } catch (final RemoteException e) {
                        //nop
                    }
                }
                //
                bm.setMonitorNotifier(null);
                final Iterator<Region> mregions = bm.getMonitoredRegions().iterator();
                while (mregions.hasNext()) {
                    try {
                        bm.stopRangingBeaconsInRegion(mregions.next());
                    } catch (final RemoteException e) {
                        //nop
                    }
                }
                bm.unbind(consumer);
            }
        });

    }
}
