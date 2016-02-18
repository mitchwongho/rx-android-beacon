package com.github.mitchwongho.android.beacon.database.rx;

import android.content.Context;
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

    private Context context;
    private RealmObject realmObject;

    public RealmObjectInsertOnSubscribe(@NonNull final Context context, @NonNull final RealmObject realmObject) {
        this.context = context;
        this.realmObject = realmObject;
    }

    @Override
    public void call(final Subscriber<? super RealmObject> subscriber) {

        final Realm realm = Realm.getInstance(context);
        realm.beginTransaction();
        subscriber.onNext( realm.copyToRealmOrUpdate(realmObject) );
        realm.commitTransaction();
        subscriber.onCompleted();

        subscriber.add(new MainThreadSubscription() {
            @Override
            protected void onUnsubscribe() {
                if (!realm.isClosed())
                    realm.close();
                context = null;
                realmObject = null;
            }
        });

    }
}
