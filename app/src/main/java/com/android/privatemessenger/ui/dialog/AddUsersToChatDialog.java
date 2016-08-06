package com.android.privatemessenger.ui.dialog;

import android.app.Dialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.privatemessenger.R;
import com.android.privatemessenger.data.api.RetrofitAPI;
import com.android.privatemessenger.data.model.Chat;
import com.android.privatemessenger.data.model.ErrorResponse;
import com.android.privatemessenger.data.model.User;
import com.android.privatemessenger.sharedprefs.SharedPrefUtils;
import com.android.privatemessenger.ui.adapter.OnItemClickListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddUsersToChatDialog extends BottomSheetDialogFragment {
    @BindView(R.id.recycler_view)
    public RecyclerView recyclerView;

    @BindView(R.id.rl_loading_bar_container)
    public RelativeLayout progressBarContainer;

    private LinearLayoutManager layoutManager;
    private AddUsersToChatAdapter adapter;
    private ArrayList<User> dataSet;

    private Chat chat;

    @Override
    public void setupDialog(final Dialog dialog, int style) {
        View rootView = getActivity().getLayoutInflater().inflate(R.layout.bottom_sheet_dialog_add_user, null);
        ButterKnife.bind(this, rootView);

        if (chat == null) {
            return;
        }

        loadUserSet();

        dialog.setContentView(rootView);
        super.setupDialog(dialog, style);
    }

    private void deactivateLoadingScreen() {
        progressBarContainer.setVisibility(View.INVISIBLE);
    }

    private void activateLoadingScreen() {
        progressBarContainer.setVisibility(View.INVISIBLE);
    }

    private void loadUserSet() {
        dataSet = new ArrayList<>();

        RetrofitAPI.getInstance().getRegisteredUsers(SharedPrefUtils.getInstance(getActivity()).getUser().getToken())
                .enqueue(new Callback<List<User>>() {
                    @Override
                    public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                        if (response == null || response.body() == null) {
                            Toast.makeText(getActivity(), getResources().getString(R.string.toast_loading_error), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        dataSet.addAll(response.body());

                        RetrofitAPI.getInstance().getChatParticipants(chat.getId()).enqueue(new Callback<List<User>>() {
                            @Override
                            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                                if (response == null || response.body() == null) {
                                    Toast.makeText(getActivity(), getResources().getString(R.string.toast_loading_error), Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                for (Iterator<User> iterator = dataSet.iterator(); iterator.hasNext(); ) {
                                    User user = iterator.next();

                                    for (User loadedUser : response.body()) {
                                        if (user.equals(loadedUser)) {
                                            iterator.remove();
                                        }
                                    }
                                }

                                setupRecyclerView();
                                deactivateLoadingScreen();
                            }

                            @Override
                            public void onFailure(Call<List<User>> call, Throwable t) {
                                Toast.makeText(getActivity(), getResources().getString(R.string.toast_loading_error), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<List<User>> call, Throwable t) {
                        Toast.makeText(getActivity(), getResources().getString(R.string.toast_loading_error), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupRecyclerView() {
        layoutManager = new LinearLayoutManager(getActivity());
        adapter = new AddUsersToChatAdapter(dataSet);

        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onClick(Object clickedItem) {
                activateLoadingScreen();

                RetrofitAPI.getInstance().addUserToChat(
                        chat.getId(),
                        SharedPrefUtils.getInstance(getActivity()).getUser().getToken(),
                        ((User) clickedItem).getId()
                ).enqueue(new Callback<ErrorResponse>() {
                    @Override
                    public void onResponse(Call<ErrorResponse> call, Response<ErrorResponse> response) {
                        if (response == null || response.body() == null || response.body().isError()) {
                            Toast.makeText(
                                    getActivity(), getResources().getString(R.string.toast_loading_error), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Toast.makeText(
                                getActivity(), getResources().getString(R.string.toast_added), Toast.LENGTH_SHORT).show();
                        AddUsersToChatDialog.this.dismiss();
                    }

                    @Override
                    public void onFailure(Call<ErrorResponse> call, Throwable t) {
                        Toast.makeText(
                                getActivity(), getResources().getString(R.string.toast_loading_error), Toast.LENGTH_SHORT).show();
                        AddUsersToChatDialog.this.dismiss();
                    }
                });
            }

            @Override
            public void onLongClick(Object clickedItem) {

            }
        });

        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }

    public class AddUsersToChatAdapter extends RecyclerView.Adapter<AddUsersToChatAdapter.BaseViewHolder> {
        private final String TAG = getClass().getSimpleName();
        private ArrayList<User> dataSet;
        private OnItemClickListener onItemClickListener;

        public AddUsersToChatAdapter(ArrayList<User> dataSet) {
            this.dataSet = dataSet;
        }

        public class BaseViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.text_view_title)
            public TextView TVTitle;

            @BindView(R.id.image_view)
            public ImageView IVActionImage;

            @BindView(R.id.root_view)
            public LinearLayout LLRootView;

            public BaseViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }

        @Override
        public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new BaseViewHolder(
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_dialog_action_imaged, parent, false));
        }

        @Override
        public void onBindViewHolder(BaseViewHolder holder, int position) {
            if (dataSet == null) {
                Log.e(TAG, "onBindViewHolder()-> dataSet is null");
                return;
            }

            final User user = dataSet.get(position);

            holder.TVTitle.setText(user.getName());
            holder.LLRootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onClick(user);
                    }
                }
            });
            holder.LLRootView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onClick(user);
                    }
                    return false;
                }
            });
        }

        @Override
        public int getItemCount() {
            try {
                return dataSet.size();
            } catch (Exception ex) {
                Log.e(TAG, "getItemCount()-> ", ex);
                return 0;
            }
        }

        public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
        }
    }
}
