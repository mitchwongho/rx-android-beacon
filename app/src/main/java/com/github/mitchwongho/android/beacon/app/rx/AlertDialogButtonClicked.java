package com.github.mitchwongho.android.beacon.app.rx;

import android.app.AlertDialog;
import android.support.annotation.NonNull;

import com.github.mitchwongho.android.beacon.ext.Event;

/**
 *
 */
public class AlertDialogButtonClicked implements Event {
    private AlertDialog dialog;
    private Integer button;

    public AlertDialogButtonClicked(@NonNull final AlertDialog dialog, @NonNull final Integer button) {
        this.button = button;
        this.dialog = dialog;
    }

    public Integer getButton() {
        return button;
    }

    public AlertDialog getAlertDialog() {
        return dialog;
    }
}
