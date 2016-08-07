package com.android.privatemessenger.ui.dialog;

import android.app.Dialog;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.privatemessenger.R;
import com.android.privatemessenger.data.model.User;
import com.android.privatemessenger.ui.adapter.OnItemClickListener;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class UserListBottomSheet extends BottomSheetDialogFragment {
    private final String TAG = getClass().getSimpleName();

    @BindView(R.id.recycler_view)
    public RecyclerView recyclerView;

    @BindView(R.id.btn_cancel)
    public Button BTNCancel;

    private LinearLayoutManager layoutManager;
    private UserListAdapter adapter;
    private ArrayList<User> dataSet;

    //private Chat chat;
    public OnItemClickListener onItemClickListener;

    @Override
    public void setupDialog(final Dialog dialog, int style) {
        View rootView = getActivity().getLayoutInflater().inflate(R.layout.bottom_sheet_dialog_add_user, null);
        ButterKnife.bind(this, rootView);

        setupRecyclerView();

        dialog.setContentView(rootView);
        super.setupDialog(dialog, style);
    }

    @OnClick(R.id.btn_cancel)
    public void closeDialog() {
        dismiss();
    }

    public void setDataSet(ArrayList<User> dataSet) {
        this.dataSet = dataSet;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    private void setupRecyclerView() {
        if (dataSet == null) {
            return;
        }

        layoutManager = new LinearLayoutManager(getActivity());
        adapter = new UserListAdapter(dataSet);

        adapter.setOnItemClickListener(onItemClickListener);

        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.BaseViewHolder> {
        private final String TAG = getClass().getSimpleName();
        private ArrayList<User> dataSet;
        private OnItemClickListener onItemClickListener;

        public UserListAdapter(ArrayList<User> dataSet) {
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
