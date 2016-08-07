package com.android.privatemessenger.ui.adapter;

import android.content.Context;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.privatemessenger.R;
import com.android.privatemessenger.data.model.User;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ParticipantListAdapter extends RecyclerView.Adapter<ParticipantListAdapter.BaseViewHolder> {
    private final String TAG = ParticipantListAdapter.this.getClass().getSimpleName();

    private Context context;
    private ArrayList<User> dataSet;
    private RecyclerItemClickListener recyclerItemClickListener;

    private boolean selectionModeActivated = false;

    public ParticipantListAdapter(Context context, ArrayList<User> dataSet) {
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

        @BindView(R.id.tv_contact_image)
        public TextView TVContactImageText;

        @BindView(R.id.cb_select)
        public AppCompatCheckBox APCBSelect;

        public BaseViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    @Override
    public ParticipantListAdapter.BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_contact, parent, false);
        return new ParticipantListAdapter.BaseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ParticipantListAdapter.BaseViewHolder holder, final int position) {
        if (dataSet == null) {
            return;
        }

        final User user = dataSet.get(position);

        if (user == null) {
            return;
        }

        if (isSelectionModeActivated()) {
            holder.APCBSelect.setVisibility(View.VISIBLE);
        } else {
            holder.APCBSelect.setVisibility(View.INVISIBLE);
        }

        holder.TVName.setText(user.getName());
        holder.TVPhone.setText(user.getPhone());
        holder.APCBSelect.setChecked(user.isSelected());
        holder.TVContactImageText.setText(String.valueOf(user.getName().charAt(0)).toUpperCase());

        holder.APCBSelect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                user.setSelected(isChecked);
            }
        });

        holder.RLRootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (recyclerItemClickListener != null) {
                    recyclerItemClickListener.onClick(holder.getAdapterPosition());
                }
            }
        });
        holder.RLRootView.setOnLongClickListener(new View.OnLongClickListener() {
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

    public boolean isSelectionModeActivated() {
        return selectionModeActivated;
    }

    public void setSelectionModeActivated(boolean selectionModeActivated) {
        this.selectionModeActivated = selectionModeActivated;
        notifyDataSetChanged();
    }

    public void setRecyclerItemClickListener(RecyclerItemClickListener recyclerItemClickListener) {
        this.recyclerItemClickListener = recyclerItemClickListener;
    }
}
