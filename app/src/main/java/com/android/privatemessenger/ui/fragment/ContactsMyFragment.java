package com.android.privatemessenger.ui.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import com.android.privatemessenger.data.model.Contact;
import com.android.privatemessenger.data.model.ErrorResponse;
import com.android.privatemessenger.data.model.User;
import com.android.privatemessenger.data.model.UserId;
import com.android.privatemessenger.sharedprefs.SharedPrefUtils;
import com.android.privatemessenger.ui.activity.ChatActivity;
import com.android.privatemessenger.ui.activity.ContactAddActivity;
import com.android.privatemessenger.ui.activity.ContactUpdateActivity;
import com.android.privatemessenger.ui.adapter.ContactsMyAdapter;
import com.android.privatemessenger.ui.adapter.RecyclerItemClickListener;
import com.android.privatemessenger.ui.dialog.ActionDialog;
import com.android.privatemessenger.ui.dialog.AttentionDialog;
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

public class ContactsMyFragment extends Fragment {
    private final String TAG = getClass().getSimpleName();

    @BindView(R.id.recycler_view)
    public RecyclerView recyclerView;

    @BindView(R.id.swipe_layout)
    public SwipeRefreshLayout swipeRefreshLayout;

    private ContactsMyAdapter adapter;
    private ArrayList<Contact> contactSet;
    private LinearLayoutManager layoutManager;

    private MenuItem cancelMenuItem;

    public ContactsMyFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts_my, container, false);
        ButterKnife.bind(this, view);

        setupRecyclerView();
        setupSwipeRefresh();

        if (savedInstanceState != null && savedInstanceState.getSerializable(IntentKeys.ARRAY_LIST_CONTACT) != null) {
            adapter.setDataSet((ArrayList<Contact>) savedInstanceState.getSerializable(IntentKeys.ARRAY_LIST_CONTACT));
            adapter.notifyDataSetChanged();
        } else {
            loadData();
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(IntentKeys.ARRAY_LIST_CONTACT, adapter.getDataSet());

        super.onSaveInstanceState(outState);
    }

    public View.OnClickListener getFabClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(getActivity(), ContactAddActivity.class), RequestCodes.ACTIVITY_CONTACTS_ADD);
            }
        };
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RequestCodes.ACTIVITY_CONTACTS_ADD) {
            if (resultCode == Activity.RESULT_OK) {
                loadData();
                return;
            }
        }

        if (requestCode == RequestCodes.ACTIVITY_CONTACTS_UPDATE) {
            if (resultCode == Activity.RESULT_OK) {
                loadData();
                return;
            }
        }
    }

    private void loadData() {
        if (contactSet != null) {
            contactSet.clear();
        }

        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
            }
        });

        RetrofitAPI.getInstance().getMyContacts(
                SharedPrefUtils.getInstance(getActivity()).getUser().getToken()).enqueue(new Callback<List<Contact>>() {
            private void onComplete() {
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onResponse(Call<List<Contact>> call, Response<List<Contact>> response) {
                if (response.body() != null) {
                    for (Contact contact : response.body()) {
                        adapter.addItem(contact);
                    }

                    adapter.notifyDataSetChanged();
                    onComplete();
                }
            }

            @Override
            public void onFailure(Call<List<Contact>> call, Throwable t) {
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

    private void setupRecyclerView() {
        contactSet = new ArrayList<>();

        adapter = new ContactsMyAdapter(getActivity(), contactSet);
        adapter.setRecyclerItemClickListener(new RecyclerItemClickListener() {
            @Override
            public void onClick(int position) {
                final Contact contact = adapter.getDataSet().get(position);

                ActionDialog.Builder actionDialogBuilder = new ActionDialog.Builder(getActivity().getSupportFragmentManager(), getActivity())
                        .withCloseAfterItemSelected(true)
                        .addItem(new ActionDialog.SimpleActionItem(
                                getResources().getString(R.string.dialog_action_call),
                                new ActionDialog.OnItemClickListener() {
                                    @Override
                                    public void onClick(ActionDialog.AbstractActionItem clickedItem) {
                                        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + contact.getPhone()));
                                        startActivity(intent);
                                    }

                                    @Override
                                    public void onLongClick(ActionDialog.AbstractActionItem clickedItem) {
                                    }
                                }))
                        .addItem(new ActionDialog.SimpleActionItem(
                                getResources().getString(R.string.dialog_action_change),
                                new ActionDialog.OnItemClickListener() {
                                    @Override
                                    public void onClick(ActionDialog.AbstractActionItem clickedItem) {
                                        startActivityForResult(new Intent(getActivity(), ContactUpdateActivity.class)
                                                .putExtra(IntentKeys.OBJECT_CONTACT, contact), RequestCodes.ACTIVITY_CONTACTS_UPDATE);
                                    }

                                    @Override
                                    public void onLongClick(ActionDialog.AbstractActionItem clickedItem) {
                                    }
                                }))
                        .addItem(new ActionDialog.SimpleActionItem(
                                getResources().getString(R.string.dialog_action_contact_delete),
                                new ActionDialog.OnItemClickListener() {
                                    @Override
                                    public void onClick(ActionDialog.AbstractActionItem clickedItem) {
                                        AttentionDialog.createDeleteDialog(getActivity(), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                RetrofitAPI.getInstance().deleteContact(
                                                        contact.getId(),
                                                        SharedPrefUtils.getInstance(getActivity()).getUser().getToken())
                                                        .enqueue(new Callback<ErrorResponse>() {
                                                            @Override
                                                            public void onResponse(Call<ErrorResponse> call, Response<ErrorResponse> response) {
                                                                if (response == null || response.body() == null || response.body().isError()) {
                                                                    Toast.makeText(getActivity(), getResources().getString(R.string.toast_loading_error), Toast.LENGTH_SHORT).show();
                                                                    return;
                                                                }

                                                                loadData();
                                                            }

                                                            @Override
                                                            public void onFailure(Call<ErrorResponse> call, Throwable t) {
                                                                Toast.makeText(getActivity(), getResources().getString(R.string.toast_loading_error), Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                            }
                                        }).show();
                                    }

                                    @Override
                                    public void onLongClick(ActionDialog.AbstractActionItem clickedItem) {
                                    }
                                }))
                        /*.addItem(new ActionDialog.SimpleActionItem(
                                getResources().getString(R.string.dialog_action_copy_name),
                                new ActionDialog.OnItemClickListener() {
                                    @Override
                                    public void onClick(ActionDialog.AbstractActionItem clickedItem) {
                                        ((ClipboardManager) getActivity().getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("user_name", contact.getName()));
                                        Toast.makeText(
                                                getActivity(),
                                                getResources().getString(R.string.toast_copied),
                                                Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onLongClick(ActionDialog.AbstractActionItem clickedItem) {
                                    }
                                }))*/;

                if (contact.isRegistered()) {
                    actionDialogBuilder
                            .addItem(new ActionDialog.SimpleActionItem(
                                    getResources().getString(R.string.dialog_action_start_conversation),
                                    new ActionDialog.OnItemClickListener() {
                                        final ProgressDialog progressDialog = com.android.privatemessenger.ui.dialog.ProgressDialog.create(getActivity());

                                        void onError() {
                                            Toast.makeText(getActivity(), getResources().getString(R.string.toast_create_error), Toast.LENGTH_SHORT).show();
                                            progressDialog.dismiss();
                                        }

                                        @Override
                                        public void onClick(ActionDialog.AbstractActionItem clickedItem) {
                                            progressDialog.show();

                                            // Get user by phone
                                            RetrofitAPI.getInstance().getUserByPhone(
                                                    SharedPrefUtils.getInstance(getActivity()).getUser().getToken(),
                                                    contact.getPhone()
                                            ).enqueue(new Callback<User>() {
                                                @Override
                                                public void onResponse(Call<User> call, Response<User> response) {
                                                    if (response == null || response.body() == null) {
                                                        onError();
                                                        return;
                                                    }

                                                    final User user = response.body();

                                                    RetrofitAPI.getInstance().getMyChats(
                                                            SharedPrefUtils.getInstance(getActivity()).getUser().getToken()
                                                    ).enqueue(new Callback<List<Chat>>() {
                                                        @Override
                                                        public void onResponse(Call<List<Chat>> call, Response<List<Chat>> response) {
                                                            if (response == null || response.body() == null) {
                                                                onError();
                                                                return;
                                                            }

                                                            for (Chat chat : response.body()) {
                                                                if (chat.getType() == Chat.TYPE_PRIVATE && chat.getFriendId() == user.getId()) {
                                                                    redirectToChatRoom(chat);
                                                                    progressDialog.dismiss();
                                                                    return;
                                                                }
                                                            }

                                                            HashMap<String, Object> data = new HashMap<>();
                                                            List<UserId> userIds = new ArrayList<>();
                                                            // Add users to the new conversation
                                                            userIds.add(new UserId(user.getId()));
                                                            userIds.add(new UserId(SharedPrefUtils.getInstance(getActivity()).getUser().getId()));
                                                            data.put(IAPIService.PARAM_USER_IDS, userIds);
                                                            data.put(IAPIService.PARAM_CHAT_NAME, "Private chat");
                                                            RetrofitAPI.getInstance().createChat(data).enqueue(new Callback<Chat>() {
                                                                @Override
                                                                public void onResponse(Call<Chat> call, Response<Chat> response) {
                                                                    if (response == null || response.body() == null) {
                                                                        onError();
                                                                        return;
                                                                    }

                                                                    redirectToChatRoom(response.body());
                                                                    progressDialog.dismiss();
                                                                }

                                                                @Override
                                                                public void onFailure(Call<Chat> call, Throwable t) {
                                                                    onError();
                                                                }
                                                            });
                                                        }

                                                        @Override
                                                        public void onFailure(Call<List<Chat>> call, Throwable t) {
                                                            onError();
                                                        }
                                                    });
                                                }

                                                @Override
                                                public void onFailure(Call<User> call, Throwable t) {
                                                    onError();
                                                }
                                            });
                                        }

                                        @Override
                                        public void onLongClick(ActionDialog.AbstractActionItem clickedItem) {
                                        }
                                    }));
                }

                actionDialogBuilder.build().show();
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

    private void redirectToChatRoom(Chat chat) {
        try {
            Intent intent = new Intent(getActivity(), ChatActivity.class)
                    .putExtra(com.android.privatemessenger.utils.IntentKeys.OBJECT_CHAT, chat);
            startActivityForResult(intent, RequestCodes.ACTIVITY_CHAT);
        } catch (Exception ex) {
            Log.e(TAG, "onClick()-> ", ex);
        }
    }

    public MenuItem getCancelMenuItem() {
        return cancelMenuItem;
    }

    public void setCancelMenuItem(MenuItem cancelMenuItem) {
        this.cancelMenuItem = cancelMenuItem;
    }
}
