package com.github.mitchwongho.android.beacon.app.rx;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import rx.Observable;
import rx.Subscriber;
import rx.android.MainThreadSubscription;

/**
 *
 */
public class AlertDialogOnSubscribe implements Observable.OnSubscribe<AlertDialogButtonClicked> {

    private AlertDialog.Builder builder;
    private int resIdNegative, resIdNeutral, resIdPositive;

    public AlertDialogOnSubscribe(@NonNull final AlertDialog.Builder builder,
                                  @NonNull final int resIdNegative,
                                  @NonNull final int resIdNeutral,
                                  @NonNull final int resIdPositive) {
        this.builder = builder;
        this.resIdNeutral = resIdNeutral;
        this.resIdNegative = resIdNegative;
        this.resIdPositive = resIdPositive;
    }



    @Override
    public void call(final Subscriber<? super AlertDialogButtonClicked> subscriber) {

        if (resIdNegative > 0) {
            builder.setNegativeButton(resIdNegative, null);
        }
        if (resIdNeutral > 0) {
            builder.setNeutralButton(resIdNeutral, null);
        }
        if (resIdPositive > 0) {
            builder.setPositiveButton(resIdPositive, null);
        }

        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                         @Override
                                         public void onDismiss(DialogInterface dialog) {
                                             subscriber.unsubscribe();
                                         }
                                     });

        final AlertDialog ad = builder.create();

        ad.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                if (resIdNegative > 0) {
                    final Button neg = ad.getButton(AlertDialog.BUTTON_NEGATIVE);
                    neg.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            subscriber.onNext(new AlertDialogButtonClicked(ad, AlertDialog.BUTTON_NEGATIVE));
                        }
                    });
                }
                if (resIdNeutral > 0) {
                    final Button neu = ad.getButton(AlertDialog.BUTTON_NEUTRAL);
                    neu.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            subscriber.onNext(new AlertDialogButtonClicked(ad, AlertDialog.BUTTON_NEUTRAL));
                        }
                    });
                }
                if (resIdPositive > 0) {
                    final Button pos = ad.getButton(AlertDialog.BUTTON_POSITIVE);
                    pos.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            subscriber.onNext(new AlertDialogButtonClicked(ad, AlertDialog.BUTTON_POSITIVE));
                        }
                    });
                }
            }
        });

        ad.show();

        subscriber.add(new MainThreadSubscription() {
            @Override
            protected void onUnsubscribe() {
                builder = null;
                if (ad.isShowing()) {
                    ad.dismiss();
                };
            }
        });
    }
}
