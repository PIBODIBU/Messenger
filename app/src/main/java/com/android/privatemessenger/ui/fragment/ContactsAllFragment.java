package com.android.privatemessenger.ui.fragment;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.privatemessenger.R;
import com.android.privatemessenger.data.api.IAPIService;
import com.android.privatemessenger.data.api.RetrofitAPI;
import com.android.privatemessenger.data.model.Chat;
import com.android.privatemessenger.data.model.User;
import com.android.privatemessenger.data.model.UserId;
import com.android.privatemessenger.sharedprefs.SharedPrefUtils;
import com.android.privatemessenger.ui.activity.ChatActivity;
import com.android.privatemessenger.ui.activity.UserPageActivity;
import com.android.privatemessenger.ui.adapter.ContactsAllAdapter;
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

import static android.content.Context.CLIPBOARD_SERVICE;

public class ContactsAllFragment extends Fragment {
    private final String TAG = getClass().getSimpleName();

    @BindView(R.id.recycler_view)
    public RecyclerView recyclerView;

    @BindView(R.id.swipe_layout)
    public SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.fab_create_chat)
    public FloatingActionButton FABChatCreate;

    public ContactsAllAdapter adapter;
    private ArrayList<User> contactSet;
    private LinearLayoutManager layoutManager;

    private MenuItem cancelMenuItem;

    public ContactsAllFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts_all, container, false);
        ButterKnife.bind(this, view);

        setupRecyclerView();
        setupSwipeRefresh();

        if (savedInstanceState != null && savedInstanceState.getSerializable(IntentKeys.ARRAY_LIST_USER) != null) {
            contactSet = (ArrayList<User>) savedInstanceState.getSerializable(IntentKeys.ARRAY_LIST_USER);
        } else {
            loadData();
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(IntentKeys.ARRAY_LIST_USER, contactSet);
        super.onSaveInstanceState(outState);
    }

    private void activateSelectionMode() {
        FABChatCreate.setImageResource(R.drawable.ic_done_white_24dp);
        adapter.setSelectionModeActivated(true);
        cancelMenuItem.setVisible(true);
    }

    private void deactivateSelectionMode() {
        FABChatCreate.setImageResource(R.drawable.ic_add_white_24dp);
        adapter.setSelectionModeActivated(false);
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

        userIds.add(new UserId(SharedPrefUtils.getInstance(getActivity()).getUser().getId()));

        final HashMap<String, Object> data = new HashMap<>();
        data.put("user_ids", userIds);

        ChatCreateDialog dialog = new ChatCreateDialog();
        dialog.setChatCreateCallbacks(new ChatCreateDialog.ChatCreateCallbacks() {
            @Override
            public void onChatCreate(String chatName) {
                data.put("chat_name", chatName);

                final ProgressDialog progressDialog = new ProgressDialog(getActivity());
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
                            Toast.makeText(getActivity(), getResources().getString(R.string.toast_create_error), Toast.LENGTH_SHORT).show();
                        }

                        progressDialog.cancel();
                    }

                    @Override
                    public void onFailure(Call<Chat> call, Throwable t) {
                        Log.e(TAG, "onFailure()-> ", t);
                        Toast.makeText(getActivity(), getResources().getString(R.string.toast_create_error), Toast.LENGTH_SHORT).show();
                        progressDialog.cancel();
                    }
                });
            }
        });
        dialog.show(getActivity().getSupportFragmentManager(), "ChatCreateDialog");
    }

    private void loadData() {
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
            }
        });

        RetrofitAPI.getInstance().getUsers(SharedPrefUtils.getInstance(getActivity()).getUser().getToken()).enqueue(new Callback<List<User>>() {
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

                Toast.makeText(getActivity(), getResources().getString(R.string.toast_loading_error), Toast.LENGTH_SHORT).show();
                onComplete();
            }
        });
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
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
            Intent intent = new Intent(getActivity(), ChatActivity.class)
                    .putExtra(com.android.privatemessenger.utils.IntentKeys.OBJECT_CHAT, chat);
            startActivityForResult(intent, RequestCodes.ACTIVITY_CHAT);
        } catch (Exception ex) {
            Log.e(TAG, "onClick()-> ", ex);
        }
    }

    private void setupRecyclerView() {
        contactSet = new ArrayList<>();

        adapter = new ContactsAllAdapter(getActivity(), contactSet);
        adapter.setRecyclerItemClickListener(new RecyclerItemClickListener() {
            @Override
            public void onClick(int position) {
                try {
                    final User user = adapter.getDataSet().get(position);
                    ActionDialog actionDialog = new ActionDialog.Builder(getActivity().getSupportFragmentManager(), getActivity())
                            .withCloseAfterItemSelected(true)
                            .addItem(new ActionDialog.SimpleActionItem(
                                    getResources().getString(R.string.dialog_action_profile),
                                    new ActionDialog.OnItemClickListener() {
                                        @Override
                                        public void onClick(ActionDialog.AbstractActionItem clickedItem) {
                                            Intent intent = new Intent(getActivity(), UserPageActivity.class)
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
                                    getResources().getString(R.string.dialog_action_start_conversation),
                                    new ActionDialog.OnItemClickListener() {
                                        @Override
                                        public void onClick(ActionDialog.AbstractActionItem clickedItem) {
                                            final ProgressDialog progressDialog = com.android.privatemessenger.ui.dialog.ProgressDialog.create(getActivity());
                                            progressDialog.show();

                                            HashMap<String, Object> data = new HashMap<>();
                                            List<UserId> userIds = new ArrayList<>();

                                            userIds.add(new UserId(user.getId()));
                                            userIds.add(new UserId(SharedPrefUtils.getInstance(getActivity()).getUser().getId()));

                                            data.put(IAPIService.PARAM_USER_IDS, userIds);
                                            data.put(IAPIService.PARAM_CHAT_NAME, "Private chat");

                                            RetrofitAPI.getInstance().createChat(data).enqueue(new Callback<Chat>() {
                                                @Override
                                                public void onResponse(Call<Chat> call, Response<Chat> response) {
                                                    if (response != null & response.body() != null) {
                                                        redirectToChatRoom(response.body());
                                                    } else {
                                                        Toast.makeText(getActivity(), getResources().getString(R.string.toast_create_error), Toast.LENGTH_SHORT).show();
                                                    }

                                                    progressDialog.dismiss();
                                                }

                                                @Override
                                                public void onFailure(Call<Chat> call, Throwable t) {
                                                    Log.e(TAG, "onFailure()-> ", t);
                                                    Toast.makeText(getActivity(), getResources().getString(R.string.toast_create_error), Toast.LENGTH_SHORT).show();
                                                    progressDialog.dismiss();
                                                }
                                            });
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
                                            ((ClipboardManager) getActivity().getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("user_name", user.getName()));
                                            Toast.makeText(
                                                    getActivity(),
                                                    getResources().getString(R.string.toast_copied),
                                                    Toast.LENGTH_SHORT).show();
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

        layoutManager = new LinearLayoutManager(getActivity());

        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    public MenuItem getCancelMenuItem() {
        return cancelMenuItem;
    }

    public void setCancelMenuItem(MenuItem cancelMenuItem) {
        this.cancelMenuItem = cancelMenuItem;
    }
}
