package com.github.mitchwongho.android.beacon.database.rx;

import android.content.Context;
import android.support.annotation.NonNull;

import io.realm.RealmObject;
import io.realm.RealmResults;
import rx.Observable;

/**
 *
 */
public class RxRealm {

    public static Observable<RealmObject> insertOrUpdate(@NonNull final Context context, @NonNull final RealmObject realmObject) {
        return Observable.create(new RealmObjectInsertOnSubscribe(context, realmObject));
    }

    public static <T extends RealmObject> Observable<RealmResults<T>> fetchAll(@NonNull final Context context, @NonNull final Class<T> clazz) {
        return Observable.create(new RealmObjectFetchOnSubscribe(context, clazz));
    }

    public static <T extends RealmObject> Observable<RealmResults<T>> fetch(@NonNull final Context context, @NonNull final Class<T> clazz, @NonNull final String uuid) {
        return Observable.create(new RealmObjectFetchOnSubscribe(context, clazz, uuid));
    }

    public static <T extends RealmObject> Observable<T> delete(@NonNull final Context context, @NonNull final Class<T> clazz, @NonNull final String uuid) {
        return Observable.create(new RealmObjectDeleteOnSubscribe(context, clazz, uuid));
    }
}
