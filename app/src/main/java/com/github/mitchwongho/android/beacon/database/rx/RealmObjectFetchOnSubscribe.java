package com.github.mitchwongho.android.beacon.database.rx;

import android.content.Context;
import android.support.annotation.NonNull;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmObject;
import io.realm.RealmResults;
import rx.Observable;
import rx.Subscriber;
import rx.android.MainThreadSubscription;

/**
 *
 */
public class RealmObjectFetchOnSubscribe<T extends RealmObject> implements Observable.OnSubscribe<RealmResults<T>> {

    private Context context;
    private Class<T> clazz;

    public RealmObjectFetchOnSubscribe(@NonNull final Context context, @NonNull final Class<T> clazz) {
        this.clazz = clazz;
        this.context = context;
    }

    @Override
    public void call(@NonNull final Subscriber<? super RealmResults<T>> subscriber) {
        final Realm realm = Realm.getInstance(context);

        final RealmResults<T> results = realm.where(clazz).findAll();

        final RealmChangeListener changeListener = new RealmChangeListener() {
            @Override
            public void onChange() {
                if (!subscriber.isUnsubscribed()) {
                    subscriber.onNext(results);
                }
            }
        };

        results.addChangeListener(changeListener);

        subscriber.add(new MainThreadSubscription() {
            @Override
            protected void onUnsubscribe() {
                if (!realm.isClosed()) {
                    results.removeChangeListener(changeListener);
                    realm.close();
                }
                context = null;
                clazz = null;
            }
        });
        // Immediately call onNext with the current value, as due to Realms auto-update,
        // it will be the latest value.
        subscriber.onNext( results );
    }
}
