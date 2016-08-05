package com.android.privatemessenger.ui.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;

import com.android.privatemessenger.R;

public class DeleteDialog {
    public static AlertDialog create(final Activity activity, DialogInterface.OnClickListener onDeleteAction) {
        final AlertDialog alertDialog = new AlertDialog.Builder(activity)
                .setMessage(activity.getResources().getString(R.string.dialog_delete_message))
                .setPositiveButton(activity.getResources().getString(R.string.dialog_yes), onDeleteAction)
                .setNegativeButton(activity.getResources().getString(R.string.dialog_no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setTextColor(ContextCompat.getColor(activity, R.color.colorPrimary));

                alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                        .setTextColor(ContextCompat.getColor(activity, R.color.md_red_500));
            }
        });

        return alertDialog;
    }
}
