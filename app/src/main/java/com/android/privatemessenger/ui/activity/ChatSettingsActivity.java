package com.android.privatemessenger.ui.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.privatemessenger.R;
import com.android.privatemessenger.data.api.RetrofitAPI;
import com.android.privatemessenger.data.model.Chat;
import com.android.privatemessenger.data.model.ErrorResponse;
import com.android.privatemessenger.data.model.User;
import com.android.privatemessenger.sharedprefs.SharedPrefUtils;
import com.android.privatemessenger.ui.adapter.ContactsAllAdapter;
import com.android.privatemessenger.ui.adapter.OnItemClickListener;
import com.android.privatemessenger.ui.adapter.RecyclerItemClickListener;
import com.android.privatemessenger.ui.dialog.ActionDialog;
import com.android.privatemessenger.ui.dialog.AttentionDialog;
import com.android.privatemessenger.ui.dialog.ProgressDialog;
import com.android.privatemessenger.ui.dialog.UserListBottomSheet;
import com.android.privatemessenger.utils.IntentKeys;
import com.android.privatemessenger.utils.ResultCodes;
import com.android.privatemessenger.utils.Values;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatSettingsActivity extends BaseActivity {
    private final String TAG = getClass().getSimpleName();

    @BindView(R.id.coordinatorLayout)
    public CoordinatorLayout coordinatorLayout;
    @BindView(R.id.toolbar)
    public Toolbar toolbar;

    @BindView(R.id.card_chat_name)
    public CardView CVChatName;

    @BindView(R.id.card_chat_participants)
    public CardView CVParticipants;

    @BindView(R.id.card_chat_leave_chat)
    public CardView CVLeaveChat;

    @BindView(R.id.et_chat_name)
    public EditText ETVChatName;
    @BindView(R.id.loading_bar_change_name)
    public MaterialProgressBar PBChatName;
    @BindView(R.id.btn_change_chat_name)
    public Button BTNChangeChatName;

    @BindView(R.id.rl_add_participant)
    public RelativeLayout RLAddParticipant;

    @BindView(R.id.loading_bar_part_add)
    public MaterialProgressBar PBParticipantsAdd;

    @BindView(R.id.recycler_view_participants)
    public RecyclerView recyclerView;

    @BindView(R.id.rl_container_loading)
    public RelativeLayout RLParticipantsLoading;
    @BindView(R.id.loading_bar)
    public MaterialProgressBar PBParticipantList;
    @BindView(R.id.btn_retry)
    public Button BTNRetry;

    private final int SCREEN_RETRY = 0;
    private final int SCREEN_OFF = 1;
    private final int SCREEN_ON = 2;

    private LinearLayoutManager layoutManager;
    private ContactsAllAdapter adapter;
    private ArrayList<User> participants;

    private Chat chat;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_settings);
        ButterKnife.bind(this);

        if (getIntent() != null && getIntent().getSerializableExtra(IntentKeys.OBJECT_CHAT) != null) {
            chat = (Chat) getIntent().getSerializableExtra(IntentKeys.OBJECT_CHAT);
        } else {
            return;
        }

        ETVChatName.clearFocus();

        setupToolbar();
        setupLayout();
        setupRecyclerView();
    }

    private void setupLayout() {
        ETVChatName.setText(chat.getName());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                return true;
            }

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void toggleParticipantsAddPB(boolean show) {
        PBParticipantsAdd.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }

    private void toggleParticipantsLoadingScreen(int type) {
        switch (type) {
            case SCREEN_RETRY: {
                RLParticipantsLoading.setVisibility(View.VISIBLE);
                PBParticipantList.setVisibility(View.INVISIBLE);
                BTNRetry.setVisibility(View.VISIBLE);
                break;
            }
            case SCREEN_ON: {
                RLParticipantsLoading.setVisibility(View.VISIBLE);
                PBParticipantList.setVisibility(View.VISIBLE);
                BTNRetry.setVisibility(View.GONE);
                break;
            }
            case SCREEN_OFF: {
                RLParticipantsLoading.setVisibility(View.INVISIBLE);
                break;
            }
        }
    }

    private void toggleChatNamePB(boolean show) {
        PBChatName.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }

    private void setupRecyclerView() {
        layoutManager = new LinearLayoutManager(this);
        participants = new ArrayList<>();
        adapter = new ContactsAllAdapter(this, participants);

        adapter.setRecyclerItemClickListener(new RecyclerItemClickListener() {
            @Override
            public void onClick(int position) {
                try {
                    final User user = adapter.getDataSet().get(position);

                    if (user.getId() == SharedPrefUtils.getInstance(ChatSettingsActivity.this).getUser().getId()) {
                        return;
                    }

                    ActionDialog actionDialog = new ActionDialog.Builder(
                            getSupportFragmentManager(), ChatSettingsActivity.this)
                            .withCloseAfterItemSelected(true)
                            .addItem(new ActionDialog.SimpleActionItem(
                                    getResources().getString(R.string.dialog_action_profile),
                                    new ActionDialog.OnItemClickListener() {
                                        @Override
                                        public void onClick(ActionDialog.AbstractActionItem clickedItem) {
                                            Intent intent = new Intent(ChatSettingsActivity.this, UserPageActivity.class)
                                                    .putExtra(IntentKeys.OBJECT_USER, user);
                                            startActivity(intent);
                                        }

                                        @Override
                                        public void onLongClick(ActionDialog.AbstractActionItem clickedItem) {
                                        }
                                    }))
                            .addItem(new ActionDialog.SimpleActionItem(
                                    getResources().getString(R.string.dialog_action_call),
                                    new ActionDialog.OnItemClickListener() {
                                        @Override
                                        public void onClick(ActionDialog.AbstractActionItem clickedItem) {
                                            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + user.getPhone()));
                                            startActivity(intent);
                                        }

                                        @Override
                                        public void onLongClick(ActionDialog.AbstractActionItem clickedItem) {
                                        }
                                    }))
                            .addItem(new ActionDialog.SimpleActionItem(
                                    getResources().getString(R.string.dialog_action_copy_name),
                                    new ActionDialog.OnItemClickListener() {
                                        @Override
                                        public void onClick(ActionDialog.AbstractActionItem clickedItem) {
                                            ((ClipboardManager) ChatSettingsActivity.this.getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("user_name", user.getName()));
                                            Toast.makeText(
                                                    ChatSettingsActivity.this,
                                                    getResources().getString(R.string.toast_copied),
                                                    Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onLongClick(ActionDialog.AbstractActionItem clickedItem) {
                                        }
                                    }))
                            .addItem(new ActionDialog.SimpleActionItem(
                                    getResources().getString(R.string.dialog_action_remove_user),
                                    new ActionDialog.OnItemClickListener() {
                                        @Override
                                        public void onClick(ActionDialog.AbstractActionItem clickedItem) {
                                            AttentionDialog.create(
                                                    ChatSettingsActivity.this,
                                                    getResources().getString(R.string.dialog_remove_user),
                                                    new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            RetrofitAPI.getInstance().deleteUserFromChat(
                                                                    chat.getId(),
                                                                    SharedPrefUtils.getInstance(ChatSettingsActivity.this).getUser().getToken(),
                                                                    user.getId()
                                                            ).enqueue(new Callback<ErrorResponse>() {
                                                                private void onError() {
                                                                    Toast.makeText(
                                                                            ChatSettingsActivity.this,
                                                                            getResources().getString(R.string.toast_loading_error),
                                                                            Toast.LENGTH_SHORT).show();
                                                                }

                                                                @Override
                                                                public void onResponse(Call<ErrorResponse> call, Response<ErrorResponse> response) {
                                                                    if (response == null || response.body() == null || response.body().isError()) {
                                                                        onError();
                                                                        return;
                                                                    }

                                                                    Snackbar.make(
                                                                            coordinatorLayout,
                                                                            getResources().getString(R.string.toast_user_removed),
                                                                            Snackbar.LENGTH_LONG
                                                                    ).show();

                                                                    loadParticipants();
                                                                }

                                                                @Override
                                                                public void onFailure(Call<ErrorResponse> call, Throwable t) {
                                                                    onError();
                                                                }
                                                            });
                                                        }
                                                    }).show();
                                        }

                                        @Override
                                        public void onLongClick(ActionDialog.AbstractActionItem clickedItem) {
                                        }
                                    }))
                            .build();

                    actionDialog.show();
                } catch (Exception ex) {
                    Log.e(TAG, "onClick()-> ", ex);
                }
            }

            @Override
            public void onLongClick(int position) {

            }
        });

        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(layoutManager);

        loadParticipants();
    }

    private void loadParticipants() {
        toggleParticipantsLoadingScreen(SCREEN_ON);

        RetrofitAPI.getInstance().getChatParticipants(chat.getId()).enqueue(new Callback<List<User>>() {
            private void onError() {
                Toast.makeText(
                        ChatSettingsActivity.this, getResources().getString(R.string.toast_loading_error), Toast.LENGTH_SHORT).show();
                toggleParticipantsLoadingScreen(SCREEN_RETRY);
            }

            private void onSuccess() {
                toggleParticipantsLoadingScreen(SCREEN_OFF);
            }

            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response == null || response.body() == null) {
                    onError();
                    return;
                }

                participants.clear();
                participants.addAll(response.body());
                adapter.notifyDataSetChanged();
                onSuccess();
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                onError();
            }
        });
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(chat.getName());
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @OnClick(R.id.btn_change_chat_name)
    public void changeChatName() {
        toggleChatNamePB(true);
        final String newChatName = ETVChatName.getText().toString();

        if (newChatName.equals(chat.getName())) {
            return;
        }

        RetrofitAPI.getInstance().updateChatName(
                chat.getId(),
                SharedPrefUtils.getInstance(this).getUser().getToken(),
                newChatName).enqueue(new Callback<ErrorResponse>() {
            @Override
            public void onResponse(Call<ErrorResponse> call, Response<ErrorResponse> response) {
                if (response == null || response.body() == null || response.body().isError()) {
                    onError();
                    return;
                }

                chat.setName(newChatName);
                setupToolbar();
                setResult(ResultCodes.CHAT_NAME_CHANGED, new Intent()
                        .putExtra(IntentKeys.OBJECT_CHAT, chat));

                Snackbar.make(
                        coordinatorLayout, getResources().getString(R.string.toast_chat_name_changed), Snackbar.LENGTH_LONG
                ).show();
                onEnd();
            }

            private void onError() {
                Toast.makeText(
                        ChatSettingsActivity.this, getResources().getString(R.string.toast_loading_error), Toast.LENGTH_SHORT).show();
                toggleChatNamePB(false);
            }

            private void onEnd() {
                toggleChatNamePB(false);
            }

            @Override
            public void onFailure(Call<ErrorResponse> call, Throwable t) {
                onError();
            }
        });
    }

    @OnClick(R.id.tv_leave_chat)
    public void leaveChat() {
        AttentionDialog.create(
                ChatSettingsActivity.this,
                getResources().getString(R.string.dialog_leave_chat),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        RetrofitAPI.getInstance().deleteUserFromChat(
                                chat.getId(),
                                SharedPrefUtils.getInstance(ChatSettingsActivity.this).getUser().getToken(),
                                SharedPrefUtils.getInstance(ChatSettingsActivity.this).getUser().getId()
                        ).enqueue(new Callback<ErrorResponse>() {
                            private void onError() {
                                Toast.makeText(
                                        ChatSettingsActivity.this,
                                        getResources().getString(R.string.toast_loading_error),
                                        Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onResponse(Call<ErrorResponse> call, Response<ErrorResponse> response) {
                                if (response == null || response.body() == null || response.body().isError()) {
                                    onError();
                                    return;
                                }

                                setResult(ResultCodes.CHAT_LEAVED);
                                finish();
                            }

                            @Override
                            public void onFailure(Call<ErrorResponse> call, Throwable t) {
                                onError();
                            }
                        });
                    }
                }).show();
    }

    @OnClick(R.id.tv_delete_chat)
    public void deleteChat() {
        AttentionDialog.createDeleteDialog(this, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final android.app.ProgressDialog dialog = ProgressDialog.create(ChatSettingsActivity.this);
                dialog.show();

                RetrofitAPI.getInstance().deleteChat(
                        chat.getId(),
                        SharedPrefUtils.getInstance(ChatSettingsActivity.this).getUser().getToken()).enqueue(new Callback<ErrorResponse>() {
                    private void onEnd() {
                        dialog.dismiss();
                    }

                    private void onError() {
                        Toast.makeText(ChatSettingsActivity.this, getResources().getString(R.string.toast_loading_error), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(Call<ErrorResponse> call, Response<ErrorResponse> response) {
                        if (response == null || response.body() == null || response.body().isError()) {
                            onError();
                            return;
                        }

                        setResult(ResultCodes.CHAT_DELETED);
                        finish();

                        onEnd();
                    }

                    @Override
                    public void onFailure(Call<ErrorResponse> call, Throwable t) {
                        onEnd();
                        onError();
                    }
                });
            }
        }).show();
    }

    @OnClick(R.id.rl_add_participant)
    public void addParticipant() {
        if (participants == null) {
            Log.e(TAG, "addParticipant()-> participants set is null");
            return;
        }

        if (participants.size() > Values.CHAT_MAX_PARTICIPANTS) {
            Snackbar.make(
                    coordinatorLayout, getResources().getString(R.string.toast_chat_limit_reached), Snackbar.LENGTH_LONG).show();
            return;
        }

        toggleParticipantsAddPB(true);

        final ArrayList<User> filteredUsers = new ArrayList<>();
        final UserListBottomSheet bottomSheet = new UserListBottomSheet();

        // Add user to chat
        bottomSheet.setOnItemClickListener(
                new OnItemClickListener() {
                    @Override
                    public void onClick(Object clickedItem) {
                        RetrofitAPI.getInstance().addUserToChat(
                                chat.getId(),
                                SharedPrefUtils.getInstance(ChatSettingsActivity.this).getUser().getToken(),
                                ((User) clickedItem).getId()
                        ).enqueue(new Callback<ErrorResponse>() {
                            @Override
                            public void onResponse(Call<ErrorResponse> call, Response<ErrorResponse> response) {
                                if (response == null || response.body() == null || response.body().isError()) {
                                    Toast.makeText(
                                            ChatSettingsActivity.this, getResources().getString(R.string.toast_loading_error), Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                Snackbar.make(
                                        coordinatorLayout, getResources().getString(R.string.toast_added), Snackbar.LENGTH_LONG).show();
                                loadParticipants();
                                bottomSheet.dismiss();
                            }

                            @Override
                            public void onFailure(Call<ErrorResponse> call, Throwable t) {
                                Toast.makeText(
                                        ChatSettingsActivity.this, getResources().getString(R.string.toast_loading_error), Toast.LENGTH_SHORT).show();
                                bottomSheet.dismiss();
                            }
                        });
                    }

                    @Override
                    public void onLongClick(Object clickedItem) {

                    }
                }
        );

        // Get list of users, which can be added to chat
        RetrofitAPI.getInstance().getUsers(SharedPrefUtils.getInstance(this).getUser().getToken())
                .enqueue(new Callback<List<User>>() {
                    @Override
                    public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                        if (response == null || response.body() == null) {
                            Toast.makeText(ChatSettingsActivity.this, getResources().getString(R.string.toast_loading_error), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        filteredUsers.addAll(response.body());

                        for (Iterator<User> iterator = filteredUsers.iterator(); iterator.hasNext(); ) {
                            User user = iterator.next();

                            for (User loadedUser : participants) {
                                if (user.equals(loadedUser)) {
                                    iterator.remove();
                                }
                            }
                        }

                        bottomSheet.setDataSet(filteredUsers);
                        bottomSheet.show(getSupportFragmentManager(), "UserListBottomSheet");
                        toggleParticipantsAddPB(false);
                    }

                    @Override
                    public void onFailure(Call<List<User>> call, Throwable t) {
                        Toast.makeText(ChatSettingsActivity.this, getResources().getString(R.string.toast_loading_error), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
