package com.android.privatemessenger.ui.fragment;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CollapsingToolbarLayout;
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
import com.android.privatemessenger.ui.activity.ContactListActivity;
import com.android.privatemessenger.ui.adapter.ContactsAllAdapter;
import com.android.privatemessenger.ui.adapter.RecyclerItemClickListener;
import com.android.privatemessenger.ui.dialog.ChatCreateDialog;
import com.android.privatemessenger.utils.BundleKeys;
import com.android.privatemessenger.utils.IntentKeys;
import com.android.privatemessenger.utils.RequestCodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
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

    private View rootView;

    public ContactsAllAdapter adapter;
    private ArrayList<User> contactSet;
    private LinearLayoutManager layoutManager;

    private MenuItem cancelMenuItem;

    private BottomSheetBehavior behaviorContactInfo;

    private User clickedUser;
    private int bottomSheetState = BottomSheetBehavior.STATE_COLLAPSED;

    public ContactsAllFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_contacts_all, container, false);
        ButterKnife.bind(this, rootView);

        setupLayout();
        setupRecyclerView();
        setupSwipeRefresh();

        if (savedInstanceState != null) {
            if (savedInstanceState.getSerializable(IntentKeys.ARRAY_LIST_USER) != null) {
                adapter.setDataSet((ArrayList<User>) savedInstanceState.getSerializable(IntentKeys.ARRAY_LIST_USER));
                adapter.notifyDataSetChanged();
            }

            if (savedInstanceState.getSerializable(BundleKeys.USER) != null) {
                User user = (User) savedInstanceState.getSerializable(BundleKeys.USER);
                clickedUser = user;
                behaviorContactInfo = BottomSheetBehavior.from(refreshBottomSheet(user));
            }

            if (behaviorContactInfo != null) {
                behaviorContactInfo.setState(savedInstanceState.getInt(BundleKeys.BOTTOM_SHEET_STATE, -1) == -1 ?
                        BottomSheetBehavior.STATE_COLLAPSED : savedInstanceState.getInt(BundleKeys.BOTTOM_SHEET_STATE, -1));
            }
        } else {
            loadData();
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(IntentKeys.ARRAY_LIST_USER, adapter.getDataSet());
        outState.putSerializable(BundleKeys.USER, clickedUser);
        if (behaviorContactInfo != null)
            outState.putInt(BundleKeys.BOTTOM_SHEET_STATE, behaviorContactInfo.getState());

        super.onSaveInstanceState(outState);
    }

    private void activateSelectionMode() {
        ((ContactListActivity) getActivity()).fab.setImageResource(R.drawable.ic_done_white_24dp);
        adapter.setSelectionModeActivated(true);
        cancelMenuItem.setVisible(true);
    }

    public View.OnClickListener getFabClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
        };
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
//                swipeRefreshLayout.setRefreshing(false);
                swipeRefreshLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
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

    private void setupLayout() {

    }

    private View refreshBottomSheet(final User user) {
        final View bottomSheet = getActivity().findViewById(R.id.bottom_sheet_contact);

        bottomSheet.findViewById(R.id.ll_container_phone).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + user.getPhone()));
                startActivity(intent);
            }
        });
        bottomSheet.findViewById(R.id.ll_container_start_chat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                ((ClipboardManager) getActivity().getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("user_name", user.getName()));
                Toast.makeText(
                        getActivity(),
                        getResources().getString(R.string.toast_name_copied),
                        Toast.LENGTH_SHORT).show();
            }
        });
        bottomSheet.findViewById(R.id.toolbar_menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "menu", Toast.LENGTH_SHORT).show();
            }
        });

        ((CollapsingToolbarLayout) bottomSheet.findViewById(R.id.collapsing_toolbar_dialog)).setTitle(
                user.getName() == null || user.getName().equals("")
                        ? getResources().getString(R.string.no_info) : user.getName());
        ((TextView) bottomSheet.findViewById(R.id.tv_phone)).setText(
                user.getPhone() == null || user.getPhone().equals("")
                        ? getResources().getString(R.string.no_info) : user.getPhone());
        ((TextView) bottomSheet.findViewById(R.id.tv_email)).setText(
                user.getEmail() == null || user.getEmail().equals("")
                        ? getResources().getString(R.string.no_info) : user.getEmail());

        return bottomSheet;
    }

    private void setupRecyclerView() {
        contactSet = new ArrayList<>();

        adapter = new ContactsAllAdapter(getActivity(), contactSet);
        adapter.setRecyclerItemClickListener(new RecyclerItemClickListener() {
            @Override
            public void onClick(int position) {
                try {
                    final User user = adapter.getDataSet().get(position);
                    clickedUser = user;

                    behaviorContactInfo = BottomSheetBehavior.from(refreshBottomSheet(user));
                    behaviorContactInfo.setState(BottomSheetBehavior.STATE_EXPANDED);
                    behaviorContactInfo.setPeekHeight(0);
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
