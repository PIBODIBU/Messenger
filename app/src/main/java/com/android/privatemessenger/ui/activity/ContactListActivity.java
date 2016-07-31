package com.android.privatemessenger.ui.activity;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.privatemessenger.R;
import com.android.privatemessenger.data.api.IAPIService;
import com.android.privatemessenger.data.api.RetrofitAPI;
import com.android.privatemessenger.data.model.Chat;
import com.android.privatemessenger.data.model.User;
import com.android.privatemessenger.data.model.UserId;
import com.android.privatemessenger.sharedprefs.SharedPrefUtils;
import com.android.privatemessenger.ui.adapter.ContactListAdapter;
import com.android.privatemessenger.ui.adapter.RecyclerItemClickListener;
import com.android.privatemessenger.ui.dialog.ActionDialog;
import com.android.privatemessenger.ui.dialog.ChatCreateDialog;
import com.android.privatemessenger.utils.IntentKeys;
import com.android.privatemessenger.utils.RequestCodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ContactListActivity extends BaseNavDrawerActivity {

    public final String TAG = ContactListActivity.this.getClass().getSimpleName();

    @BindView(R.id.recycler_view)
    public RecyclerView recyclerView;

    @BindView(R.id.swipe_layout)
    public SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.fab_create_chat)
    public FloatingActionButton FABChatCreate;

    private ContactListAdapter adapter;
    private ArrayList<User> contactSet;
    private LinearLayoutManager layoutManager;
    private MenuItem cancelMenulItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);

        ButterKnife.bind(this);
        getDrawer();

        setupRecyclerView();
        setupSwipeRefresh();
        if (savedInstanceState != null && savedInstanceState.getSerializable(IntentKeys.ARRAY_LIST_CONTACT) != null) {
            contactSet = (ArrayList<User>) getIntent().getSerializableExtra(IntentKeys.ARRAY_LIST_CONTACT);
        } else {
            loadData();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_contact_list, menu);

        cancelMenulItem = menu.findItem(R.id.item_cancel);
        cancelMenulItem.setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_cancel:
                deactivateSelectionMode();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @OnClick(R.id.fab_create_chat)
    public void createChat() {
        if (!adapter.isSelectionModeActivated()) {
            activateSelectionMode();
            return;
        }

        List<UserId> userIds = new ArrayList<>();
        for (User user : adapter.getDataSet()) {
            if (user.isSelected()) {
                userIds.add(new UserId(user.getId()));
            }
        }

        if (userIds.size() == 0) {
            return;
        }

        userIds.add(new UserId(SharedPrefUtils.getInstance(this).getUser().getId()));

        final HashMap<String, Object> data = new HashMap<>();
        data.put("user_ids", userIds);

        ChatCreateDialog dialog = new ChatCreateDialog();
        dialog.setChatCreateCallbacks(new ChatCreateDialog.ChatCreateCallbacks() {
            @Override
            public void onChatCreate(String chatName) {
                data.put("chat_name", chatName);

                final ProgressDialog progressDialog = new ProgressDialog(ContactListActivity.this);
                progressDialog.setCancelable(false);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.setMessage(getResources().getString(R.string.dialog_loading));
                progressDialog.show();

                RetrofitAPI.getInstance().createChat(data).enqueue(new Callback<Chat>() {
                    @Override
                    public void onResponse(Call<Chat> call, Response<Chat> response) {
                        if (response != null & response.body() != null) {
                            redirectToChatRoom(response.body());
                        } else {
                            Toast.makeText(ContactListActivity.this, getResources().getString(R.string.toast_create_error), Toast.LENGTH_SHORT).show();
                        }

                        progressDialog.cancel();
                    }

                    @Override
                    public void onFailure(Call<Chat> call, Throwable t) {
                        Log.e(TAG, "onFailure()-> ", t);
                        Toast.makeText(ContactListActivity.this, getResources().getString(R.string.toast_create_error), Toast.LENGTH_SHORT).show();
                        progressDialog.cancel();
                    }
                });
            }
        });
        dialog.show(getSupportFragmentManager(), "ChatCreateDialog");
    }

    private void activateSelectionMode() {
        FABChatCreate.setImageResource(R.drawable.ic_done_white_24dp);
        adapter.setSelectionModeActivated(true);
        cancelMenulItem.setVisible(true);
    }

    private void deactivateSelectionMode() {
        FABChatCreate.setImageResource(R.drawable.ic_add_white_24dp);
        adapter.setSelectionModeActivated(false);
        cancelMenulItem.setVisible(false);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(IntentKeys.ARRAY_LIST_CONTACT, contactSet);
        super.onSaveInstanceState(outState);
    }

    private void loadData() {
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
            }
        });

        RetrofitAPI.getInstance().getContacts(SharedPrefUtils.getInstance(this).getUser().getToken()).enqueue(new Callback<List<User>>() {
            private void onComplete() {
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.body() != null) {
                    for (User user : response.body()) {
                        adapter.addItem(user);
                    }

                    adapter.notifyDataSetChanged();
                    onComplete();
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                Log.e(TAG, "Error occurred during my chat list fetching", t);

                Toast.makeText(ContactListActivity.this, getResources().getString(R.string.toast_loading_error), Toast.LENGTH_SHORT).show();
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
                contactSet.clear();
                loadData();
            }
        });
    }

    private void redirectToChatRoom(Chat chat) {
        try {
            Intent intent = new Intent(ContactListActivity.this, ChatActivity.class)
                    .putExtra(com.android.privatemessenger.utils.IntentKeys.OBJECT_CHAT, chat);
            startActivityForResult(intent, RequestCodes.ACTIVITY_CHAT);
        } catch (Exception ex) {
            Log.e(TAG, "onClick()-> ", ex);
        }
    }

    private void setupRecyclerView() {
        contactSet = new ArrayList<>();

        adapter = new ContactListAdapter(this, contactSet);
        adapter.setRecyclerItemClickListener(new RecyclerItemClickListener() {
            @Override
            public void onClick(int position) {
                final User user = adapter.getDataSet().get(position);

                ActionDialog actionDialog = new ActionDialog.Builder(getSupportFragmentManager(), ContactListActivity.this)
                        .setCloseAfterItemSelected(true)
                        .addItem(new ActionDialog.SimpleActionItem("Профиль", new ActionDialog.OnItemClickListener() {
                            @Override
                            public void onClick(ActionDialog.SimpleActionItem clickedItem) {
                                Intent intent = new Intent(ContactListActivity.this, UserPageActivity.class)
                                        .putExtra(IntentKeys.OBJECT_USER, user);
                                startActivity(intent);
                            }

                            @Override
                            public void onLongClick(ActionDialog.SimpleActionItem clickedItem) {
                            }
                        }))
                        .addItem(new ActionDialog.SimpleActionItem("Вызов", new ActionDialog.OnItemClickListener() {
                            @Override
                            public void onClick(ActionDialog.SimpleActionItem clickedItem) {
                                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + user.getPhone()));
                                startActivity(intent);
                            }

                            @Override
                            public void onLongClick(ActionDialog.SimpleActionItem clickedItem) {
                            }
                        }))
                        .addItem(new ActionDialog.SimpleActionItem("Начать беседу", new ActionDialog.OnItemClickListener() {
                            @Override
                            public void onClick(ActionDialog.SimpleActionItem clickedItem) {
                                HashMap<String, Object> data = new HashMap<String, Object>();
                                List<UserId> userIds = new ArrayList<>();

                                userIds.add(new UserId(user.getId()));
                                userIds.add(new UserId(SharedPrefUtils.getInstance(ContactListActivity.this).getUser().getId()));

                                data.put(IAPIService.PARAM_USER_IDS, userIds);
                                data.put(IAPIService.PARAM_CHAT_NAME, "Private chat");

                                RetrofitAPI.getInstance().createChat(data).enqueue(new Callback<Chat>() {
                                    @Override
                                    public void onResponse(Call<Chat> call, Response<Chat> response) {
                                        if (response != null & response.body() != null) {
                                            redirectToChatRoom(response.body());
                                        } else {
                                            Toast.makeText(ContactListActivity.this, getResources().getString(R.string.toast_create_error), Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<Chat> call, Throwable t) {
                                        Log.e(TAG, "onFailure()-> ", t);
                                        Toast.makeText(ContactListActivity.this, getResources().getString(R.string.toast_create_error), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            @Override
                            public void onLongClick(ActionDialog.SimpleActionItem clickedItem) {
                            }
                        }))
                        .addItem(new ActionDialog.SimpleActionItem("Копировать имя", new ActionDialog.OnItemClickListener() {
                            @Override
                            public void onClick(ActionDialog.SimpleActionItem clickedItem) {
                                ((ClipboardManager) getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("user_name", user.getName()));
                                Toast.makeText(
                                        ContactListActivity.this,
                                        getResources().getString(R.string.toast_copied),
                                        Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onLongClick(ActionDialog.SimpleActionItem clickedItem) {
                            }
                        }))
                        .build();

                actionDialog.show();
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
}