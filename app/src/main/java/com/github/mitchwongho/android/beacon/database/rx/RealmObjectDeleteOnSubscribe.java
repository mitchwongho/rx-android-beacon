package com.github.mitchwongho.android.beacon.database.rx;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

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
public class RealmObjectDeleteOnSubscribe<T extends RealmObject> implements Observable.OnSubscribe<T> {

    private Context context;
    private Class clazz;
    private String uuid;

    public RealmObjectDeleteOnSubscribe(@NonNull final Context context, @NonNull final Class<T> clazz, @NonNull final String uuid) {
        this.context = context;
        this.clazz = clazz;
        this.uuid = uuid;
    }


    @Override
    public void call(@NonNull final Subscriber<? super T> subscriber) {
        final Realm realm = Realm.getInstance(context);

        realm.beginTransaction();
        try {
            final RealmObject ro = realm.where(clazz).equalTo("uuid", uuid).findFirst();
            ro.removeFromRealm();
            subscriber.onNext((T)ro); //zomg!!!
            realm.commitTransaction();
            subscriber.onCompleted();
            Log.e(RealmObjectDeleteOnSubscribe.class.getSimpleName(), "Realm Object DELETED");
        } catch (Throwable thrown) {
            realm.cancelTransaction();
            Log.e(RealmObjectDeleteOnSubscribe.class.getSimpleName(), "ERROR deleting Realm Object ", thrown);
            subscriber.onError(thrown);
        }

        subscriber.add(new MainThreadSubscription() {
            @Override
            protected void onUnsubscribe() {
                realm.close();
            }
        });
    }
}
