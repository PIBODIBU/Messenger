package com.android.privatemessenger.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.privatemessenger.R;
import com.android.privatemessenger.data.model.Chat;
import com.android.privatemessenger.data.model.User;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ContactListAdapter extends RecyclerView.Adapter<ContactListAdapter.BaseViewHolder> {
    private final String TAG = ContactListAdapter.this.getClass().getSimpleName();

    private Context context;
    private ArrayList<User> dataSet;
    private RecyclerItemClickListener recyclerItemClickListener;

    public ContactListAdapter(Context context, ArrayList<User> dataSet) {
        this.context = context;
        this.dataSet = dataSet;
    }

    public class BaseViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.root_view)
        public RelativeLayout RLRootView;

        @BindView(R.id.tv_name)
        public TextView TVName;

        @BindView(R.id.tv_phone)
        public TextView TVPhone;

        public BaseViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_contact, parent, false);
        return new BaseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final BaseViewHolder holder, final int position) {
        RelativeLayout LLRootView = holder.RLRootView;
        TextView TVName = holder.TVName;
        TextView TVPhone = holder.TVPhone;
        User user = dataSet.get(position);

        TVName.setText(user.getName());
        TVPhone.setText(user.getPhone());

        LLRootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (recyclerItemClickListener != null) {
                    recyclerItemClickListener.onClick(holder.getAdapterPosition());
                }
            }
        });
        LLRootView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (recyclerItemClickListener != null) {
                    recyclerItemClickListener.onLongClick(holder.getAdapterPosition());
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    public void addItem(User user) {
        dataSet.add(user);
    }

    public ArrayList<User> getDataSet() {
        return dataSet;
    }

    public void setRecyclerItemClickListener(RecyclerItemClickListener recyclerItemClickListener) {
        this.recyclerItemClickListener = recyclerItemClickListener;
    }
}
