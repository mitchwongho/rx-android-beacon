package com.github.mitchwongho.android.beacon.bluetooth.rx;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.os.Build;
import android.support.annotation.NonNull;

import com.github.mitchwongho.android.beacon.util.UUIDUtils;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import rx.Observable;

/**
 *
 */
public final class RxBluetoothAdapter {

    public static Observable<List<LeScanResult>> startLeScan(@NonNull final BluetoothAdapter adapter, @NonNull final Long interval, @NonNull final Boolean pollingEnabled) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            //pre-Lollipop

        } else {
            // Lollipop+
//            throw new UnsupportedOperationException("Not yet implemented for Lollipop and newer");
        }
//        return Observable.create(new StartLeScanOnSubscribe(adapter, duration)).
//                toMap(leScanResult -> leScanResult.getBluetoothDevice().getAddress()).
//                flatMap( map -> Observable.<List<LeScanResult>>just( new ArrayList(map.values()) ) );
        return Observable.create(new StartLeScanOnSubscribe(adapter))
                .buffer(interval, TimeUnit.MILLISECONDS);
    }

    public static Observable<LeAdvertiseStarted> advertise(@NonNull final BluetoothAdapter adapter) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            //pre-Lollipop
            return Observable.<LeAdvertiseStarted>error(new UnsupportedOperationException("Beacon advertising only supported in Android SDK > 21"));
        } else {
            // Lollipop+
            final AdvertiseSettings.Builder asBuilder = new AdvertiseSettings.Builder();
            asBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER);
            asBuilder.setConnectable(false);
            asBuilder.setTimeout(0); //continous
            asBuilder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM);
            final AdvertiseSettings advertiseSettings = asBuilder.build();

            final AdvertiseData.Builder adBuilder = new AdvertiseData.Builder();
            ByteBuffer manufacturerData = ByteBuffer.allocate(24);
            final byte[] uuid0 = UUIDUtils.toBytes(UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"));
            final byte[] data = new byte[24];
            data[0] = (byte)0xBE;
            data[1] = (byte)0xBE;
            System.arraycopy(uuid0, 0, data, 2, uuid0.length);
            data[18] = (byte)0x96;
            data[19] = (byte)0xC6;
            data[20] = (byte)0xF4;
            data[21] = (byte)0xA6;
            data[22] = (byte)0xB5;
            manufacturerData.put(data);
            adBuilder.addManufacturerData(224, manufacturerData.array());
            final AdvertiseData advertiseData = adBuilder.build();

            return Observable.create(new RxLeAdvertiseOnSubscribe(adapter, advertiseSettings, advertiseData));
        }
    }
}
