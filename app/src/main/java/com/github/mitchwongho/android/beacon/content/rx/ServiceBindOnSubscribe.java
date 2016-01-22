package com.github.mitchwongho.android.beacon.content.rx;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.NonNull;

import com.github.mitchwongho.android.beacon.ext.ServiceBindEvent;
import com.github.mitchwongho.android.beacon.ext.ServiceConnectedEvent;
import com.github.mitchwongho.android.beacon.ext.ServiceDisconnectedEvent;

import java.util.HashMap;
import java.util.Map;

import rx.Observable;
import rx.Subscriber;
import rx.android.MainThreadSubscription;

/**
 *
 */
public class ServiceBindOnSubscribe implements Observable.OnSubscribe<ServiceBindEvent> {

    private Context context;
    private Intent intent;
    private int flags;

    private static Map<Class, ServiceConnection> map = new HashMap();

    public ServiceBindOnSubscribe(@NonNull final Intent intent, @NonNull final Context context, @NonNull final Integer flags) {
        this.context = context;
        this.intent = intent;
        this.flags = flags;
    }

    @Override
    public void call(final Subscriber<? super ServiceBindEvent> subscriber) {

        final ServiceConnection conn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                subscriber.onNext(new ServiceConnectedEvent(name, service, this));
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                subscriber.onNext(new ServiceDisconnectedEvent(name));
            }
        };

        context.bindService(intent, conn, flags);

        subscriber.add(new MainThreadSubscription() {
            @Override
            protected void onUnsubscribe() {
                context.unbindService(conn);
            }
        });

    }
}
