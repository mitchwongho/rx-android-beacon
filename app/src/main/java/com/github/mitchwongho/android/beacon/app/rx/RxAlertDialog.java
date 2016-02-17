package com.github.mitchwongho.android.beacon.app.rx;

import android.app.AlertDialog;
import android.support.annotation.NonNull;

import rx.Observable;

/**
 *
 */
public class RxAlertDialog {

    public final static Observable<AlertDialogButtonClicked> create(@NonNull final AlertDialog.Builder builder, @NonNull final int resIdNegative, @NonNull final int resIdNeutral, @NonNull final int resIdPositive) {
        return Observable.create(new AlertDialogOnSubscribe(builder, resIdNegative, resIdNeutral, resIdPositive));
    }
}
