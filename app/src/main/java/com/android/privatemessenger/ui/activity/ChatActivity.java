package com.android.privatemessenger.ui.activity;

import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.privatemessenger.R;
import com.android.privatemessenger.application.ActivityWatcher;
import com.android.privatemessenger.broadcast.IntentFilters;
import com.android.privatemessenger.data.api.RetrofitAPI;
import com.android.privatemessenger.data.model.Chat;
import com.android.privatemessenger.data.model.Message;
import com.android.privatemessenger.data.model.SendMessageResponse;
import com.android.privatemessenger.data.model.User;
import com.android.privatemessenger.data.realm.model.UnreadMessage;
import com.android.privatemessenger.sharedprefs.SharedPrefUtils;
import com.android.privatemessenger.ui.adapter.ChatAdapter;
import com.android.privatemessenger.ui.adapter.OnLoadMoreListener;
import com.android.privatemessenger.ui.adapter.RecyclerItemClickListener;
import com.android.privatemessenger.ui.dialog.ContactInfoBottomSheet;
import com.android.privatemessenger.ui.dialog.MessageActionDialog;
import com.android.privatemessenger.ui.dialog.MessageErrorActionDialog;
import com.android.privatemessenger.utils.BundleKeys;
import com.android.privatemessenger.utils.IntentKeys;
import com.android.privatemessenger.utils.RequestCodes;
import com.android.privatemessenger.utils.ResultCodes;
import com.android.privatemessenger.utils.Values;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends BaseNavDrawerActivity {
    private final String TAG = ChatActivity.this.getClass().getSimpleName();

    @BindView(R.id.root_view)
    public RelativeLayout rootView;

    @BindView(R.id.recycler_view)
    public RecyclerView recyclerView;

    @BindView(R.id.et_message)
    public EditText ETMessage;

    private ChatAdapter adapter;
    private ArrayList<Message> messageSet;
    private LinearLayoutManager linearLayoutManager;

    private Realm realm;
    private Chat chat;

    private BroadcastReceiver messageReceiver;

    private int loadingOffset = 0;
    private int loadingCount = Values.MESSAGE_LOADING_COUNT;
    private boolean isEndReached = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        if (getIntent() != null) {
            try {
                chat = (Chat) getIntent().getSerializableExtra(IntentKeys.OBJECT_CHAT);
            } catch (Exception ex) {
                Log.e(TAG, "onCreate()-> ", ex);
            }
        } else {
            return;
        }

        ButterKnife.bind(this);
        getDrawer();

        updateToolbar();
        setupRealm();
        deleteUnreadCounter();
        setupRecyclerView();
        setupReceivers();
        loadData(true, true);

        refreshChatsBadge();

        if (savedInstanceState != null) {
            ETMessage.setText(savedInstanceState.getString(BundleKeys.TYPED_MESSAGE, ""));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (ETMessage != null)
            outState.putString(BundleKeys.TYPED_MESSAGE, ETMessage.getText().toString());

        super.onSaveInstanceState(outState);
    }

    private void updateToolbar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(chat.getName());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RequestCodes.ACTIVITY_CHAT_SETTINGS) {
            switch (resultCode) {
                case ResultCodes.CHAT_DELETED: {
                    setResult(ResultCodes.CHAT_DELETED);
                    finish();
                    break;
                }
                case ResultCodes.CHAT_NAME_CHANGED:
                    if (data != null && data.getSerializableExtra(IntentKeys.OBJECT_CHAT) != null) {
                        chat = ((Chat) data.getSerializableExtra(IntentKeys.OBJECT_CHAT));
                        updateToolbar();
                    }
                case ResultCodes.CHAT_LEAVED:
                    setResult(ResultCodes.CHAT_DELETED);
                    finish();
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_settings).setVisible(chat.getType() == Chat.TYPE_PUBLIC);
        menu.findItem(R.id.action_profile).setVisible(chat.getType() == Chat.TYPE_PRIVATE);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivityForResult(new Intent(this, ChatSettingsActivity.class)
                        .putExtra(IntentKeys.OBJECT_CHAT, chat), RequestCodes.ACTIVITY_CHAT_SETTINGS);
                return true;
            case R.id.action_profile:

                RetrofitAPI.getInstance().getChatParticipants(chat.getId()).enqueue(new Callback<List<User>>() {
                    private void onError() {
                        Snackbar.make(
                                rootView, getResources().getString(R.string.toast_loading_error), Snackbar.LENGTH_LONG).show();
                    }

                    @Override
                    public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                        if (response == null || response.body() == null) {
                            onError();
                            return;
                        }
                        int myId = SharedPrefUtils.getInstance(ChatActivity.this).getUser().getId();
                        User user = null;

                        for (User participant : response.body()) {
                            if (participant.getId() != myId) {
                                user = participant;
                                break;
                            }
                        }

                        if (user == null) {
                            onError();
                            return;
                        }

                        ContactInfoBottomSheet bottomSheet = new ContactInfoBottomSheet(ChatActivity.this);
                        bottomSheet.show(user);
                    }

                    @Override
                    public void onFailure(Call<List<User>> call, Throwable t) {
                        onError();
                    }
                });


                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        ActivityWatcher.setChatActivityShowing(true);
        ActivityWatcher.setCurrentChatId(chat.getId());
    }

    @Override
    protected void onPause() {
        ActivityWatcher.dropCurrentChatId();
        ActivityWatcher.setChatActivityShowing(false);
        super.onPause();
    }

    private void setupRealm() {
        realm = Realm.getInstance(new RealmConfiguration.Builder(getApplicationContext())
                .name(Realm.DEFAULT_REALM_NAME)
                .schemaVersion(0)
                .deleteRealmIfMigrationNeeded()
                .build());
    }

    private void deleteUnreadCounter() {
        final RealmResults<UnreadMessage> realmResults = realm.where(UnreadMessage.class).equalTo("chatId", chat.getId()).findAll();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realmResults.deleteAllFromRealm();
            }
        });
    }

    public void sendMessage(final Message message) {
        message.setSendStatus(Message.STATUS_SENDING);
        adapter.getDataSet().add(0, message);
        adapter.notifyItemInserted(adapter.getDataSet().indexOf(message));
        recyclerView.scrollToPosition(adapter.getDataSet().indexOf(message));

        RetrofitAPI.getInstance().sendMessage(
                chat.getId(),
                SharedPrefUtils.getInstance(this).getUser().getToken(),
                message.getMessage()
        ).enqueue(new Callback<SendMessageResponse>() {
            @Override
            public void onResponse(Call<SendMessageResponse> call, Response<SendMessageResponse> response) {
                if (response == null || response.body() == null || response.body().getErrorResponse().isError()) {
                    message.setSendStatus(Message.STATUS_ERROR);
                } else {
                    message.setCreatedAt(response.body().getMessage().getCreatedAt());
                    message.setSendStatus(Message.STATUS_SENT);
                }

                adapter.notifyItemChanged(adapter.getDataSet().indexOf(message));
                setResult(chat.getId(), message);
            }

            @Override
            public void onFailure(Call<SendMessageResponse> call, Throwable t) {
                message.setSendStatus(Message.STATUS_ERROR);
                adapter.notifyItemChanged(adapter.getDataSet().indexOf(message));
                Toast.makeText(ChatActivity.this, getResources().getString(R.string.toast_send_error), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @OnClick(R.id.btn_send)
    public void sendMessage() {
        String message = ETMessage.getText().toString();
        ETMessage.setText("");

        if (message.equals("")) {
            return;
        }

        if (message.length() > Values.MAX_MESSAGE_LENGTH) {
            Toast.makeText(ChatActivity.this, getResources().getString(R.string.toast_too_long_message), Toast.LENGTH_SHORT).show();
            return;
        }

        final Message newMessage = new Message(
                -1,
                chat.getId(),
                SharedPrefUtils.getInstance(this).getUser().getId(),
                message,
                "",
                SharedPrefUtils.getInstance(this).getUser()
        );

        sendMessage(newMessage);
    }

    private void setResult(int chatRoomId, Message lastMessage) {
        setResult(RESULT_OK, new Intent()
                .putExtra(IntentKeys.CHAT_ROOM_ID, chatRoomId)
                .putExtra(IntentKeys.MESSAGE, lastMessage)
        );
    }

    private void loadData(final boolean addLoadingItem, final boolean scrollToEnd) {
        final int loadingItemPosition = addLoadingItem ? adapter.addRefreshItem() : -1;

        RetrofitAPI.getInstance().getChatMessages(
                chat.getId(),
                SharedPrefUtils.getInstance(this).getUser().getToken(),
                loadingCount,
                loadingOffset
        ).enqueue(new Callback<List<Message>>() {
            private void onError() {
                Toast.makeText(ChatActivity.this, getResources().getString(R.string.toast_loading_error), Toast.LENGTH_SHORT).show();
            }

            private void onEnd() {
                if (addLoadingItem)
                    adapter.removeRefreshItem(loadingItemPosition);

                adapter.setLoaded();
            }

            @Override
            public void onResponse(Call<List<Message>> call, Response<List<Message>> response) {
                if (response == null || response.body() == null) {
                    onError();
                    return;
                }

                messageSet.addAll(response.body());
                adapter.notifyDataSetChanged();

                if (scrollToEnd)
                    recyclerView.scrollToPosition(0);

                onEnd();
                incrementLoadingOffset();

                if (response.body().size() == 0)
                    isEndReached = true;
            }

            @Override
            public void onFailure(Call<List<Message>> call, Throwable t) {
                Log.e(TAG, "onFailure()-> Cannot load messages", t);
                onError();
                onEnd();
            }
        });
    }

    private void incrementLoadingOffset() {
        loadingOffset += loadingCount;
    }

    private void setupRecyclerView() {
        messageSet = new ArrayList<>();
        linearLayoutManager = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        adapter = new ChatAdapter(this, recyclerView, messageSet);
        recyclerView.setAdapter(adapter);

        adapter.setRecyclerItemClickListener(new RecyclerItemClickListener() {
            @Override
            public void onClick(final int position) {
                final Message message = adapter.getMessage(position);

                if (message.getSendStatus() == Message.STATUS_ERROR) {
                    MessageErrorActionDialog dialog = new MessageErrorActionDialog();

                    dialog.setMessageErrorActionListener(new MessageErrorActionDialog.MessageErrorActionListener() {
                        @Override
                        public void onRetry() {
                            adapter.getDataSet().remove(message);
                            adapter.notifyItemRemoved(position);
                            sendMessage(message);
                        }

                        @Override
                        public void onDelete() {
                            adapter.removeMessage(position);
                        }
                    });

                    dialog.show(getSupportFragmentManager(), "MessageErrorActionDialog");
                } else if (message.getSendStatus() == Message.STATUS_SENT) {
                    MessageActionDialog dialog = new MessageActionDialog();

                    dialog.setMessageActionListener(new MessageActionDialog.MessageActionListener() {
                        @Override
                        public void onCopy() {
                            ((ClipboardManager) ChatActivity.this.getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("Message", message.getMessage()));
                            Toast.makeText(ChatActivity.this, getResources().getString(R.string.toast_copied), Toast.LENGTH_SHORT).show();
                        }
                    });

                    dialog.show(getSupportFragmentManager(), "MessageActionDialog");
                }
            }

            @Override
            public void onLongClick(int position) {

            }
        });

        adapter.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                if (!isEndReached)
                    loadData(true, false);
            }
        });
    }

    private void setupReceivers() {
        messageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Message message = new Message(
                        intent.getIntExtra(IntentKeys.MESSAGE_ID, -1),
                        intent.getIntExtra(IntentKeys.CHAT_ROOM_ID, -1),
                        intent.getIntExtra(IntentKeys.SENDER_ID, -1),
                        intent.getStringExtra(IntentKeys.MESSAGE),
                        intent.getStringExtra(IntentKeys.CREATED_AT),
                        new User(
                                intent.getIntExtra(IntentKeys.SENDER_ID, -1),
                                "",
                                intent.getStringExtra(IntentKeys.SENDER_NAME),
                                intent.getStringExtra(IntentKeys.SENDER_PHONE),
                                intent.getStringExtra(IntentKeys.SENDER_EMAIl),
                                "",
                                ""
                        )
                );

                adapter.getDataSet().add(0, message);
                adapter.notifyItemInserted(adapter.getDataSet().indexOf(message));
                recyclerView.scrollToPosition(adapter.getDataSet().indexOf(message));

                ChatActivity.this.setResult(chat.getId(), message);
            }
        };

        registerReceiver(messageReceiver, new IntentFilter(IntentFilters.NEW_MESSAGE));
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(messageReceiver);
        super.onDestroy();
    }
}
