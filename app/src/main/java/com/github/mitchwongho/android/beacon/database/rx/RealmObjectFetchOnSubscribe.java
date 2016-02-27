package com.github.mitchwongho.android.beacon.database.rx;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmObject;
import io.realm.RealmQuery;
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
    private String modelUUID;

    public RealmObjectFetchOnSubscribe(@NonNull final Context context, @NonNull final Class<T> clazz) {
        this(context, clazz, null);
    }
    public RealmObjectFetchOnSubscribe(@NonNull final Context context, @NonNull final Class<T> clazz, @NonNull final String uuid) {
        this.clazz = clazz;
        this.context = context;
        modelUUID = uuid;
    }

    @Override
    public void call(@NonNull final Subscriber<? super RealmResults<T>> subscriber) {
        final Realm realm = Realm.getInstance(context);

        RealmQuery<T> query = realm.where(clazz);
        if (!TextUtils.isEmpty(modelUUID)) {
            query.equalTo("uuid", modelUUID);
        }

        final RealmResults<T> results = query.findAll();

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
