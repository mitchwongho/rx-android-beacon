package com.github.mitchwongho.android.beacon.database.rx;

import android.support.annotation.NonNull;

import io.realm.Realm;
import io.realm.RealmObject;
import rx.Observable;

/**
 *
 */
public class RxRealm {

    public static Observable<RealmObject> insertOrUpdate(@NonNull final Realm realm, @NonNull final RealmObject realmObject) {
        return Observable.create(new RealmObjectInsertOnSubscribe(realm, realmObject));
    }
}
