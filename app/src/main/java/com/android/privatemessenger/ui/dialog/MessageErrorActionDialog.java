package com.android.privatemessenger.ui.dialog;

import android.app.Dialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.View;
import android.widget.TextView;

import com.android.privatemessenger.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MessageErrorActionDialog extends BottomSheetDialogFragment {

    @BindView(R.id.tv_retry)
    public TextView TVRetry;

    @BindView(R.id.tv_delete)
    public TextView TVDelete;

    private MessageErrorActionListener messageErrorActionListener;

    @Override
    public void setupDialog(final Dialog dialog, int style) {
        View rootView = getActivity().getLayoutInflater().inflate(R.layout.bottom_sheet_message_error_action, null);
        ButterKnife.bind(this, rootView);

        TVRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (messageErrorActionListener != null) {
                    messageErrorActionListener.onRetry();
                }
                dialog.dismiss();
            }
        });

        TVDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (messageErrorActionListener != null) {
                    messageErrorActionListener.onDelete();
                }
                dialog.dismiss();
            }
        });

        dialog.setContentView(rootView);
        super.setupDialog(dialog, style);
    }

    public void setMessageErrorActionListener(MessageErrorActionListener messageErrorActionListener) {
        this.messageErrorActionListener = messageErrorActionListener;
    }

    public interface MessageErrorActionListener {
        void onRetry();

        void onDelete();
    }
}
