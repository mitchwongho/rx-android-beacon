package com.github.mitchwongho.android.beacon.database.rx;

import android.support.annotation.NonNull;
import android.util.Log;

import io.realm.Realm;
import io.realm.RealmObject;
import rx.Observable;
import rx.Subscriber;
import rx.android.MainThreadSubscription;

/**
 *
 */
public class RealmObjectInsertOnSubscribe implements Observable.OnSubscribe<RealmObject> {

    public final static String TAG = RealmObjectInsertOnSubscribe.class.getSimpleName();

    private Realm realm;
    private RealmObject realmObject;

    public RealmObjectInsertOnSubscribe(@NonNull final Realm realm, @NonNull final RealmObject realmObject) {
        this.realm = realm;
        this.realmObject = realmObject;
    }

    @Override
    public void call(final Subscriber<? super RealmObject> subscriber) {

        realm.beginTransaction();
        subscriber.onNext( realm.copyToRealmOrUpdate(realmObject) );
        realm.commitTransaction();
        subscriber.onCompleted();

        subscriber.add(new MainThreadSubscription() {
            @Override
            protected void onUnsubscribe() {
                realm = null;
                realmObject = null;
            }
        });

    }
}
