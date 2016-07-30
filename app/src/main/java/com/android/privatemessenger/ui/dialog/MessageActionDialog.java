package com.android.privatemessenger.ui.dialog;

import android.app.Dialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.View;
import android.widget.TextView;

import com.android.privatemessenger.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MessageActionDialog extends BottomSheetDialogFragment{
    @BindView(R.id.tv_copy)
    public TextView TVCopy;

    private MessageActionListener messageActionListener;

    @Override
    public void setupDialog(final Dialog dialog, int style) {
        View rootView = getActivity().getLayoutInflater().inflate(R.layout.bottom_sheet_message_action, null);
        ButterKnife.bind(this, rootView);

        TVCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (messageActionListener != null) {
                    messageActionListener.onCopy();
                }
                dialog.dismiss();
            }
        });

        dialog.setContentView(rootView);
        super.setupDialog(dialog, style);
    }

    public void setMessageActionListener(MessageActionListener messageActionListener) {
        this.messageActionListener = messageActionListener;
    }

    public interface MessageActionListener {
        void onCopy();
    }
}
