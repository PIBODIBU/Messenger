package com.android.privatemessenger.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.privatemessenger.R;
import com.android.privatemessenger.data.model.Chat;
import com.android.privatemessenger.data.realm.model.UnreadMessage;
import com.android.privatemessenger.sharedprefs.SharedPrefUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChatListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final String TAG = ChatListAdapter.this.getClass().getSimpleName();

    private Context context;
    private ArrayList<Chat> dataSet;
    private ArrayList<UnreadMessage> unreadMessages;
    private RecyclerItemClickListener recyclerItemClickListener;

    // The minimum amount of items to have below your current scroll position before loading more.
    private final int TYPE_LOADING = -1;
    private static final int TYPE_NORMAL = 1;
    private int visibleThreshold = 3;
    private int lastVisibleItem, totalItemCount;
    private boolean loading;

    private OnLoadMoreListener onLoadMoreListener;
    protected AdapterView.OnItemClickListener onItemClickListener;

    public ChatListAdapter(Context context, RecyclerView recyclerView, ArrayList<Chat> dataSet) {
        this.context = context;
        this.dataSet = dataSet;
    }

    public class BaseViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.root_view)
        public RelativeLayout LLRootView;

        @BindView(R.id.tv_name)
        public TextView TVName;

        @BindView(R.id.tv_last_message)
        public TextView TVLastMessage;

        @BindView(R.id.iv_image)
        public ImageView IVImage;

        @BindView(R.id.tv_date)
        public TextView TVDate;

        @BindView(R.id.counter)
        public TextView TVCounter;

        public BaseViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_chat, parent, false);
        return new BaseViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        if (dataSet.get(position) == null) {
            return TYPE_LOADING;
        } else {
            return TYPE_NORMAL;
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof ProgressViewHolder) {
            return;
        } else if (holder instanceof BaseViewHolder) {
            BaseViewHolder baseViewHolder = (BaseViewHolder) holder;
            Chat chat = dataSet.get(position);

            // Chat name
            baseViewHolder.TVName.setText(chat.getName());

            // Check chat type
            if (chat.getType() == Chat.TYPE_PRIVATE) {
                baseViewHolder.IVImage.setImageResource(R.drawable.ic_person_primary_24dp);
            } else if (chat.getType() == Chat.TYPE_PUBLIC) {
                baseViewHolder.IVImage.setImageResource(R.drawable.ic_group_primary_24dp);
            }

            // Check for unread messages
            int unreadCount = 0;
            for (UnreadMessage unreadMessage : unreadMessages) {
                if (unreadMessage == null) {
                    continue;
                }

                Log.d(TAG, "onBindViewHolder()-> loop: chatId: " + unreadMessage.getChatId());

                if (unreadMessage.getChatId() == chat.getId()) {
                    unreadCount = unreadMessage.getUnreadCount();
                    break;
                }
            }
            if (unreadCount == 0) {
                baseViewHolder.TVCounter.setVisibility(View.GONE);
            } else {
                baseViewHolder.TVCounter.setVisibility(View.VISIBLE);
                baseViewHolder.TVCounter.setText(String.valueOf(unreadCount));
            }

            // Last message
            baseViewHolder.TVLastMessage.setText(
                    chat.getLastMessage() == null || chat.getLastMessage().getMessage() == null ||
                            chat.getLastMessage().getMessage().equals("") ?
                            context.getResources().getString(R.string.no_messages_yet) : chat.getLastMessage().getMessage()
            );

            // Date
            baseViewHolder.TVDate.setText(chat.getFormattedDate(context));

            // Click listeners
            baseViewHolder.LLRootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (recyclerItemClickListener != null) {
                        recyclerItemClickListener.onClick(holder.getAdapterPosition());
                    }
                }
            });
            baseViewHolder.LLRootView.setOnLongClickListener(new View.OnLongClickListener() {
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
    }

    public int addRefreshItem() {
        setLoading();
        Chat loadingItem = null;
        dataSet.add(loadingItem);
        notifyItemInserted(dataSet.indexOf(loadingItem));
        return dataSet.indexOf(loadingItem);
    }

    public void setLoaded() {
        this.loading = false;
    }

    public void setLoading() {
        this.loading = true;
    }

    public void removeRefreshItem(int position) {
        try {
            dataSet.remove(position);
        } catch (Exception ex) {
            Log.e(TAG, "removeRefreshItem()-> ", ex);
        }
        notifyItemRemoved(position);
    }

    public ArrayList<UnreadMessage> getUnreadMessages() {
        return unreadMessages;
    }

    public void setUnreadMessages(ArrayList<UnreadMessage> unreadMessages) {
        this.unreadMessages = unreadMessages;
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    public void addItem(Chat chat) {
        dataSet.add(chat);
    }

    public ArrayList<Chat> getDataSet() {
        return dataSet;
    }

    public void setDataSet(ArrayList<Chat> dataSet) {
        this.dataSet = dataSet;
    }

    public void setRecyclerItemClickListener(RecyclerItemClickListener recyclerItemClickListener) {
        this.recyclerItemClickListener = recyclerItemClickListener;
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }
}
