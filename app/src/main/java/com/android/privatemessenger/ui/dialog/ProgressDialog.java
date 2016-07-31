package com.android.privatemessenger.ui.dialog;

import android.content.Context;

import com.android.privatemessenger.R;

public class ProgressDialog {
    public static android.app.ProgressDialog create(Context context) {
        android.app.ProgressDialog progressDialog = new android.app.ProgressDialog(context);
        progressDialog.setMessage(context.getResources().getString(R.string.dialog_loading));
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        return progressDialog;
    }
}
