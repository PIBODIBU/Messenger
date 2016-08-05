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
import com.android.privatemessenger.data.model.Contact;
import com.android.privatemessenger.data.model.ErrorResponse;
import com.android.privatemessenger.data.model.User;
import com.android.privatemessenger.data.model.UserId;
import com.android.privatemessenger.sharedprefs.SharedPrefUtils;
import com.android.privatemessenger.ui.activity.ContactAddActivity;
import com.android.privatemessenger.ui.activity.ContactUpdateActivity;
import com.android.privatemessenger.ui.activity.UserPageActivity;
import com.android.privatemessenger.ui.adapter.ContactsAllAdapter;
import com.android.privatemessenger.ui.adapter.ContactsMyAdapter;
import com.android.privatemessenger.ui.adapter.RecyclerItemClickListener;
import com.android.privatemessenger.ui.dialog.ActionDialog;
import com.android.privatemessenger.ui.dialog.DeleteDialog;
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
            contactSet = (ArrayList<Contact>) savedInstanceState.getSerializable(IntentKeys.ARRAY_LIST_CONTACT);
        } else {
            loadData();
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(IntentKeys.ARRAY_LIST_CONTACT, contactSet);
        super.onSaveInstanceState(outState);
    }

    @OnClick(R.id.fab_add_contact)
    public void addContact() {
        startActivityForResult(new Intent(getActivity(), ContactAddActivity.class), RequestCodes.ACTIVITY_CONTACTS_ADD);
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

                ActionDialog actionDialog = new ActionDialog.Builder(getActivity().getSupportFragmentManager(), getActivity())
                        .setCloseAfterItemSelected(true)

                        .addItem(new ActionDialog.SimpleActionItem("Вызов", new ActionDialog.OnItemClickListener() {
                            @Override
                            public void onClick(ActionDialog.SimpleActionItem clickedItem) {
                                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + contact.getPhone()));
                                startActivity(intent);
                            }

                            @Override
                            public void onLongClick(ActionDialog.SimpleActionItem clickedItem) {
                            }
                        }))
                        .addItem(new ActionDialog.SimpleActionItem("Изменить", new ActionDialog.OnItemClickListener() {
                            @Override
                            public void onClick(ActionDialog.SimpleActionItem clickedItem) {
                                startActivityForResult(new Intent(getActivity(), ContactUpdateActivity.class)
                                        .putExtra(IntentKeys.OBJECT_CONTACT, contact), RequestCodes.ACTIVITY_CONTACTS_UPDATE);
                            }

                            @Override
                            public void onLongClick(ActionDialog.SimpleActionItem clickedItem) {
                            }
                        }))
                        .addItem(new ActionDialog.SimpleActionItem("Удалить", new ActionDialog.OnItemClickListener() {
                            @Override
                            public void onClick(ActionDialog.SimpleActionItem clickedItem) {
                                DeleteDialog.create(getActivity(), new DialogInterface.OnClickListener() {
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
                                });
                            }

                            @Override
                            public void onLongClick(ActionDialog.SimpleActionItem clickedItem) {
                            }
                        }))
                        .addItem(new ActionDialog.SimpleActionItem("Копировать имя", new ActionDialog.OnItemClickListener() {
                            @Override
                            public void onClick(ActionDialog.SimpleActionItem clickedItem) {
                                ((ClipboardManager) getActivity().getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("user_name", contact.getName()));
                                Toast.makeText(
                                        getActivity(),
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
