package com.github.mitchwongho.android.beacon.database.rx;

import com.github.mitchwongho.android.beacon.domain.ProfileLayout;
import com.github.mitchwongho.android.beacon.domain.ScanProfile;

/**
 *
 */
@io.realm.annotations.RealmModule(classes = {ProfileLayout.class, ScanProfile.class})
public class RealmModule {
}
