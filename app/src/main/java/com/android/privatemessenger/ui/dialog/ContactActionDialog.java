package com.android.privatemessenger.ui.dialog;

import android.app.Dialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.View;
import android.widget.TextView;

import com.android.privatemessenger.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ContactActionDialog extends BottomSheetDialogFragment {
    @BindView(R.id.tv_call)
    public TextView TVCall;

    @BindView(R.id.tv_start_dialog)
    public TextView TVStartDialog;

    @BindView(R.id.tv_copy_name)
    public TextView TVCopyName;

    @BindView(R.id.tv_user_page)
    public TextView TVUserPage;

    private MessageActionListener messageActionListener;

    @Override
    public void setupDialog(final Dialog dialog, int style) {
        View rootView = getActivity().getLayoutInflater().inflate(R.layout.bottom_sheet_contact_action, null);
        ButterKnife.bind(this, rootView);

        TVCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (messageActionListener != null) {
                    messageActionListener.onCall();
                }
                dialog.dismiss();
            }
        });

        TVStartDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (messageActionListener != null) {
                    messageActionListener.onStartDialog();
                }
                dialog.dismiss();
            }
        });

        TVCopyName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (messageActionListener != null) {
                    messageActionListener.onCopyName();
                }
                dialog.dismiss();
            }
        });

        TVUserPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (messageActionListener != null) {
                    messageActionListener.onUserProfile();
                }
            }
        });

        dialog.setContentView(rootView);
        super.setupDialog(dialog, style);
    }

    public void setMessageActionListener(MessageActionListener messageActionListener) {
        this.messageActionListener = messageActionListener;
    }

    public interface MessageActionListener {
        void onCall();

        void onStartDialog();

        void onCopyName();

        void onUserProfile();
    }
}
