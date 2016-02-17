package com.github.mitchwongho.android.beacon.database;

import com.github.mitchwongho.android.beacon.domain.ProfileLayout;

import java.io.Closeable;
import java.util.List;

import io.realm.RealmObject;
import rx.Observable;

/**
 *
 */
public interface DAO extends Closeable {
    RealmObject insertOrUpdate(RealmObject obj);
    Observable<RealmObject> rxInsertOrUpdate(RealmObject obj);

    List<ProfileLayout> fetchAllProfileLayout();
}
