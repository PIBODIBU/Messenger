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
import com.android.privatemessenger.broadcast.IntentKeys;
import com.android.privatemessenger.data.api.RetrofitAPI;
import com.android.privatemessenger.data.model.Chat;
import com.android.privatemessenger.data.model.ErrorResponse;
import com.android.privatemessenger.data.model.Message;
import com.android.privatemessenger.sharedprefs.SharedPrefUtils;
import com.android.privatemessenger.ui.adapter.ChatListAdapter;
import com.android.privatemessenger.ui.adapter.RecyclerItemClickListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
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
    private LinearLayoutManager layoutManager;

    private BroadcastReceiver messageReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        ButterKnife.bind(this);
        getDrawer();

        setupRecyclerView();
        setupSwipeRefresh();
        if (savedInstanceState != null && savedInstanceState.getSerializable(com.android.privatemessenger.utils.IntentKeys.ARRAY_LIST_CHAT) != null) {
            chatSet = (ArrayList<Chat>) getIntent().getSerializableExtra(com.android.privatemessenger.utils.IntentKeys.ARRAY_LIST_CHAT);
        } else {
            loadData();
        }
        setupReceivers();
        updateGCMId();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(messageReceiver);

        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(com.android.privatemessenger.utils.IntentKeys.ARRAY_LIST_CHAT, chatSet);
        super.onSaveInstanceState(outState);
    }

    private void setupReceivers() {
        messageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
              /*  int chatId = intent.getExtras().getInt(IntentKeys.CHAT_ID);
                String message = intent.getExtras().getString(IntentKeys.MESSAGE);

                Log.d(TAG, "onMessageReceived()-> " +
                        "\nChat id:" + chatId +
                        "\nMessage: " + message);

                for (Chat chat : adapter.getDataSet()) {
                    if (chat.getId() == chatId) {
                        chat.setLastMessage(new Message(
                                chat.getLastMessage().getMessageId(),
                                chat.getLastMessage().getChatRoomId(),
                                chat.getLastMessage().getUserId(),
                                message,
                                chat.getLastMessage().getCreatedAt(),
                                null
                        ));
                    }
                }

                adapter.notifyDataSetChanged();*/
            }
        };
        registerReceiver(messageReceiver, new IntentFilter(IntentFilters.NEW_MESSAGE));
    }

    private void loadData() {
        swipeRefreshLayout.setRefreshing(true);

        RetrofitAPI.getInstance().getMyChats(SharedPrefUtils.getInstance(this).getUser().getToken()).enqueue(new Callback<List<Chat>>() {
            private void onComplete() {
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onResponse(Call<List<Chat>> call, Response<List<Chat>> response) {
                if (response.body() != null) {
                    for (Chat chat : response.body()) {
                        adapter.addItem(chat);
                    }

                    adapter.notifyDataSetChanged();
                    onComplete();
                }
            }

            @Override
            public void onFailure(Call<List<Chat>> call, Throwable t) {
                Log.e(TAG, "Error occurred during my chat list fetching", t);

                Toast.makeText(ChatListActivity.this, getResources().getString(R.string.toast_loading_error), Toast.LENGTH_SHORT).show();
                onComplete();
            }
        });
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorPrimary));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                chatSet.clear();
                loadData();
            }
        });
    }

    private void setupRecyclerView() {
        chatSet = new ArrayList<>();

        adapter = new ChatListAdapter(this, chatSet);
        adapter.setRecyclerItemClickListener(new RecyclerItemClickListener() {
            @Override
            public void onClick(int position) {
                Intent intent = new Intent(ChatListActivity.this, ChatActivity.class)
                        .putExtra(com.android.privatemessenger.utils.IntentKeys.OBJECT_CHAT, chatSet.get(position));
                startActivity(intent);
            }

            @Override
            public void onLongClick(int position) {

            }
        });

        layoutManager = new LinearLayoutManager(this);

        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    private void updateGCMId() {
        RetrofitAPI.getInstance().updateFCMId(FirebaseInstanceId.getInstance().getToken()).enqueue(new Callback<ErrorResponse>() {
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
