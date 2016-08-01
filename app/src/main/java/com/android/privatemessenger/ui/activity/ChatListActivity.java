package com.android.privatemessenger.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.android.privatemessenger.R;
import com.android.privatemessenger.broadcast.IntentFilters;
import com.android.privatemessenger.data.api.RetrofitAPI;
import com.android.privatemessenger.data.model.Chat;
import com.android.privatemessenger.data.model.ErrorResponse;
import com.android.privatemessenger.data.model.Message;
import com.android.privatemessenger.data.model.User;
import com.android.privatemessenger.data.realm.RealmDB;
import com.android.privatemessenger.data.realm.model.UnreadMessage;
import com.android.privatemessenger.sharedprefs.SharedPrefUtils;
import com.android.privatemessenger.ui.adapter.ChatListAdapter;
import com.android.privatemessenger.ui.adapter.OnLoadMoreListener;
import com.android.privatemessenger.ui.adapter.RecyclerItemClickListener;
import com.android.privatemessenger.utils.IntentKeys;
import com.android.privatemessenger.utils.RequestCodes;
import com.android.privatemessenger.utils.Values;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatListActivity extends BaseNavDrawerActivity {

    public final String TAG = ChatListActivity.this.getClass().getSimpleName();

    @BindView(R.id.recycler_view)
    public RecyclerView recyclerView;

    @BindView(R.id.swipe_layout)
    public SwipeRefreshLayout swipeRefreshLayout;

    private ChatListAdapter adapter;
    private ArrayList<Chat> chatSet;
    private ArrayList<UnreadMessage> unreadMessages;
    private LinearLayoutManager layoutManager;

    private Realm realm;
    private BroadcastReceiver messageReceiver;

    private int loadingOffset = 0;
    private int loadingCount = Values.CHAT_LOADING_COUNT;
    private boolean isEndReached = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        ButterKnife.bind(this);
        getDrawer();

        setupRealm();
        getUnreadCount();
        setupRecyclerView();
        setupSwipeRefresh();
        loadData(true);
        setupReceivers();
        updateGCMId();
    }

    private void getUnreadCount() {
        unreadMessages = new ArrayList<>();
        RealmResults<UnreadMessage> realmResults = realm.where(UnreadMessage.class).findAll();
        unreadMessages.addAll(realmResults);

        realmResults.addChangeListener(new RealmChangeListener<RealmResults<UnreadMessage>>() {
            @Override
            public void onChange(RealmResults<UnreadMessage> element) {
                unreadMessages.clear();
                unreadMessages.addAll(element);
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void setupRealm() {
        realm = Realm.getInstance(new RealmConfiguration.Builder(getApplicationContext())
                .name(Realm.DEFAULT_REALM_NAME)
                .schemaVersion(0)
                .deleteRealmIfMigrationNeeded()
                .build());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RequestCodes.ACTIVITY_CHAT) {
            if (data == null) {
                return;
            }

            int chatRoomId = data.getIntExtra(IntentKeys.CHAT_ROOM_ID, -1);
            Message lastMessage = (Message) data.getSerializableExtra(IntentKeys.MESSAGE);

            if (chatRoomId == -1 || lastMessage == null) {
                return;
            }

            updateLastMessage(chatRoomId, lastMessage);
        }
    }

    private void updateLastMessage(int chatRoomId, Message lastMessage) {
        boolean found = false;

        for (Chat chat : adapter.getDataSet()) {
            if (chat.getId() == chatRoomId) {
                chat.setLastMessage(lastMessage);
                found = true;
                break;
            }
        }

        if (found) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(messageReceiver);

        super.onDestroy();
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

                updateLastMessage(
                        intent.getIntExtra(IntentKeys.CHAT_ROOM_ID, -1),
                        message
                );
            }
        };

        registerReceiver(messageReceiver, new IntentFilter(IntentFilters.NEW_MESSAGE));
    }

    private void loadData(final boolean addLoadingItem) {
        final int loadingItemPosition = addLoadingItem ? adapter.addRefreshItem() : -1;

        Log.d(TAG, "loadData()-> Refreshing: " + swipeRefreshLayout.isRefreshing());

        RetrofitAPI.getInstance().getMyChats(
                SharedPrefUtils.getInstance(this).getUser().getToken(),
                loadingCount,
                loadingOffset
        ).enqueue(new Callback<List<Chat>>() {
            private void onEnd() {
                if (addLoadingItem)
                    adapter.removeRefreshItem(loadingItemPosition);

                adapter.setLoaded();
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onResponse(Call<List<Chat>> call, Response<List<Chat>> response) {
                if (response == null || response.body() == null) {
                    return;
                }

                if (response.body().size() == 0) {
                    isEndReached = true;
                }

                for (Chat chat : response.body()) {
                    adapter.addItem(chat);
                }

                adapter.notifyDataSetChanged();
                onEnd();
                incrementLoadingOffset();
            }

            @Override
            public void onFailure(Call<List<Chat>> call, Throwable t) {
                Log.e(TAG, "Error occurred during my chat list fetching", t);

                Toast.makeText(ChatListActivity.this, getResources().getString(R.string.toast_loading_error), Toast.LENGTH_SHORT).show();
                onEnd();
            }
        });
    }

    private void incrementLoadingOffset() {
        loadingOffset += loadingCount;
    }

    private void dropLoadingOffset() {
        loadingOffset = 0;
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorPrimary));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                if (chatSet != null) {
                    chatSet.clear();
                }
                isEndReached = false;
                dropLoadingOffset();
                loadData(false);
            }
        });
    }

    private void setupRecyclerView() {
        chatSet = new ArrayList<>();
        layoutManager = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        adapter = new ChatListAdapter(this, recyclerView, chatSet);
        adapter.setUnreadMessages(unreadMessages);
        recyclerView.setAdapter(adapter);

        adapter.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                if (!isEndReached)
                    loadData(true);
            }
        });

        adapter.setRecyclerItemClickListener(new RecyclerItemClickListener() {
            @Override
            public void onClick(int position) {
                try {
                    Intent intent = new Intent(ChatListActivity.this, ChatActivity.class)
                            .putExtra(com.android.privatemessenger.utils.IntentKeys.OBJECT_CHAT, chatSet.get(position));
                    startActivityForResult(intent, RequestCodes.ACTIVITY_CHAT);
                } catch (IndexOutOfBoundsException ex) {
                    Log.e(TAG, "onClick()-> ", ex);
                }
            }

            @Override
            public void onLongClick(int position) {

            }
        });
    }

    private void updateGCMId() {
        Log.d(TAG, "updateGCMId()-> FCM registration id: " + FirebaseInstanceId.getInstance().getToken());
        RetrofitAPI.getInstance().updateFCMId(
                SharedPrefUtils.getInstance(this).getUser().getToken(),
                FirebaseInstanceId.getInstance().getToken()
        ).enqueue(new Callback<ErrorResponse>() {
            @Override
            public void onResponse(Call<ErrorResponse> call, Response<ErrorResponse> response) {
                if (response.body() != null) {
                    if (response.body().isError()) {
                        Log.e(TAG, "onFailure()-> Error during id update");
                    } else {
                        Log.i(TAG, "onResponse()-> GCM id updated successfully");
                    }
                }
            }

            @Override
            public void onFailure(Call<ErrorResponse> call, Throwable t) {
                Log.e(TAG, "onFailure()-> Error during id update", t);
            }
        });
    }
}
