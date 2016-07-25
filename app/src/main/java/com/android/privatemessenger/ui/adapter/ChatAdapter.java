package com.android.privatemessenger.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.privatemessenger.R;
import com.android.privatemessenger.data.model.Message;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.BaseViewHolder> {

    private final String TAG = ChatAdapter.this.getClass().getSimpleName();

    private Context context;
    private ArrayList<Message> dataSet;
    private RecyclerItemClickListener recyclerItemClickListener;

    public ChatAdapter(Context context, ArrayList<Message> data) {
        this.context = context;
        this.dataSet = data;
    }

    public class BaseViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_message)
        public TextView TVMessage;

        @BindView(R.id.tv_sender)
        public TextView TVSender;

        @BindView(R.id.tv_time)
        public TextView TVTime;

        public BaseViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        Log.d(TAG, "onCreateViewHolder()-> viewType: " + viewType);

        if (viewType == Message.TYPE_MY) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_message_my, parent, false);
        } else if (viewType == Message.TYPE_FOREIGN) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_message_foreign, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_message_foreign, parent, false);
        }

        return new BaseViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        Log.d(TAG, "getItemViewType()-> " + dataSet.get(position).getType(context));
        return dataSet.get(position).getType(context);
    }

    @Override
    public void onBindViewHolder(final BaseViewHolder holder, final int position) {
        TextView TVMessage = holder.TVMessage;
        TextView TVSender = holder.TVSender;
        TextView TVTime = holder.TVTime;
        Message message = dataSet.get(position);

        TVMessage.setText(message.getMessage());
        TVSender.setText(message.getSender() == null ? "" : message.getSender().getName());
        TVTime.setText(message.getFormattedDate());

        TVMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (recyclerItemClickListener != null) {
                    recyclerItemClickListener.onClick(holder.getAdapterPosition());
                }
            }
        });
        TVMessage.setOnLongClickListener(new View.OnLongClickListener() {
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

    public void setRecyclerItemClickListener(RecyclerItemClickListener recyclerItemClickListener) {
        this.recyclerItemClickListener = recyclerItemClickListener;
    }
}
