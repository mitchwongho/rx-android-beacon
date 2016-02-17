package com.github.mitchwongho.android.beacon.database;

import android.content.Context;
import android.support.annotation.NonNull;

import com.github.mitchwongho.android.beacon.database.rx.RxRealm;
import com.github.mitchwongho.android.beacon.domain.ProfileLayout;

import java.io.IOException;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;
import rx.Observable;

/**
 *
 */
public class RealmDao implements DAO {

    private Realm realm;

    public RealmDao(@NonNull final Context context) {
        this.realm = Realm.getInstance(context);
    }

    @Override
    public RealmObject insertOrUpdate(@NonNull final RealmObject obj) {
        final RealmObject retval;
        realm.beginTransaction();
        retval = realm.copyToRealmOrUpdate(obj);
        realm.commitTransaction();
        return retval;
    }

    @Override
    public Observable<RealmObject> rxInsertOrUpdate(RealmObject obj) {
        return RxRealm.insertOrUpdate(realm, obj);
    }

    @Override
    public List<ProfileLayout> fetchAllProfileLayout() {
        final RealmResults<ProfileLayout> res = realm.where(ProfileLayout.class).findAll();
        return res;
    }

    /**
     * Closes the object and release any system resources it holds.
     * <p/>
     * <p>Although only the first call has any effect, it is safe to call close
     * multiple times on the same object. This is more lenient than the
     * overridden {@code AutoCloseable.close()}, which may be called at most
     * once.
     */
    @Override
    public void close() throws IOException {
        if (realm != null && !realm.isClosed()) {
            realm.close();
        }
    }
}
