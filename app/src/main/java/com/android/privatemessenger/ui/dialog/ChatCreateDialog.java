package com.android.privatemessenger.ui.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatEditText;
import android.view.View;

import com.android.privatemessenger.R;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ChatCreateDialog extends DialogFragment {

    private ChatCreateCallbacks chatCreateCallbacks;

    @BindView(R.id.et_chat_name)
    public AppCompatEditText ETChatName;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View rootView = getActivity().getLayoutInflater().inflate(R.layout.dialog_chat_create, null);
        ButterKnife.bind(this, rootView);

        final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setCancelable(false)
                .setView(rootView)
                .setPositiveButton(getResources().getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        if (chatCreateCallbacks != null) {
                            chatCreateCallbacks.onChatCreate(ETChatName.getText().toString());
                        }
                    }
                })
                .setNegativeButton(getResources().getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .create();

        dialog.setCanceledOnTouchOutside(false);
        setCancelable(false);

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(getActivity(), R.color.md_red_500));
            }
        });

        return dialog;
    }

    public void setChatCreateCallbacks(ChatCreateCallbacks chatCreateCallbacks) {
        this.chatCreateCallbacks = chatCreateCallbacks;
    }

    public interface ChatCreateCallbacks {
        void onChatCreate(String chatName);
    }
}
