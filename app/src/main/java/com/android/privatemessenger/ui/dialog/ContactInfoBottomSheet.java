package com.android.privatemessenger.ui.dialog;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CollapsingToolbarLayout;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.privatemessenger.R;
import com.android.privatemessenger.data.api.IAPIService;
import com.android.privatemessenger.data.api.RetrofitAPI;
import com.android.privatemessenger.data.model.Chat;
import com.android.privatemessenger.data.model.User;
import com.android.privatemessenger.data.model.UserId;
import com.android.privatemessenger.sharedprefs.SharedPrefUtils;
import com.android.privatemessenger.ui.activity.ChatActivity;
import com.android.privatemessenger.utils.RequestCodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.CLIPBOARD_SERVICE;

public class ContactInfoBottomSheet {
    public static final String TAG = "ContactInfoBottomSheet";

    private BottomSheetBehavior behaviorContactInfo = null;
    private Activity activity;

    public ContactInfoBottomSheet(Activity activity) {
        this.activity = activity;
    }

    public void show(final User user) {
        final View bottomSheet = activity.findViewById(R.id.bottom_sheet_contact);

        if (bottomSheet == null) {
            throw new NullPointerException("Can't find FrameLayout with id 'bottom_sheet_contact'");
        }

        bottomSheet.findViewById(R.id.ll_container_phone).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + user.getPhone()));
                activity.startActivity(intent);
            }
        });
        bottomSheet.findViewById(R.id.ll_container_start_chat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final android.app.ProgressDialog progressDialog = com.android.privatemessenger.ui.dialog.ProgressDialog
                        .create(activity);
                progressDialog.show();

                HashMap<String, Object> data = new HashMap<>();
                List<UserId> userIds = new ArrayList<>();

                userIds.add(new UserId(user.getId()));
                userIds.add(new UserId(SharedPrefUtils.getInstance(activity).getUser().getId()));

                data.put(IAPIService.PARAM_USER_IDS, userIds);
                data.put(IAPIService.PARAM_CHAT_NAME, "Private chat");

                RetrofitAPI.getInstance().createChat(data).enqueue(new Callback<Chat>() {
                    @Override
                    public void onResponse(Call<Chat> call, Response<Chat> response) {
                        if (response != null & response.body() != null) {
                            try {
                                Intent intent = new Intent(activity, ChatActivity.class)
                                        .putExtra(com.android.privatemessenger.utils.IntentKeys.OBJECT_CHAT, response.body());
                                activity.startActivityForResult(intent, RequestCodes.ACTIVITY_CHAT);
                            } catch (Exception ex) {
                                Log.e(TAG, "onClick()-> ", ex);
                            }
                        } else {
                            Toast.makeText(activity, activity.getResources().getString(R.string.toast_create_error), Toast.LENGTH_SHORT).show();
                        }

                        progressDialog.dismiss();
                    }

                    @Override
                    public void onFailure(Call<Chat> call, Throwable t) {
                        Log.e(TAG, "onFailure()-> ", t);
                        Toast.makeText(activity, activity.getResources().getString(R.string.toast_create_error), Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                });
            }
        });
        bottomSheet.findViewById(R.id.toolbar_close_dialog).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (behaviorContactInfo != null) {
                    behaviorContactInfo.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });
        bottomSheet.findViewById(R.id.toolbar_copy_name).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((ClipboardManager) activity.getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("user_name", user.getName()));
                Toast.makeText(
                        activity,
                        activity.getString(R.string.toast_name_copied),
                        Toast.LENGTH_SHORT).show();
            }
        });
        bottomSheet.findViewById(R.id.toolbar_menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(activity, "menu", Toast.LENGTH_SHORT).show();
            }
        });

        ((CollapsingToolbarLayout) bottomSheet.findViewById(R.id.collapsing_toolbar_dialog)).setTitle(
                user.getName() == null || user.getName().equals("")
                        ? activity.getResources().getString(R.string.no_info) : user.getName());
        ((TextView) bottomSheet.findViewById(R.id.tv_phone)).setText(
                user.getPhone() == null || user.getPhone().equals("")
                        ? activity.getResources().getString(R.string.no_info) : user.getPhone());
        ((TextView) bottomSheet.findViewById(R.id.tv_email)).setText(
                user.getEmail() == null || user.getEmail().equals("")
                        ? activity.getResources().getString(R.string.no_info) : user.getEmail());

        behaviorContactInfo = BottomSheetBehavior.from(bottomSheet);
        behaviorContactInfo.setState(BottomSheetBehavior.STATE_EXPANDED);
    }
}
