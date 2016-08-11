package com.android.privatemessenger.ui.activity;

import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.privatemessenger.R;
import com.android.privatemessenger.data.realm.RealmDB;
import com.android.privatemessenger.data.realm.model.UnreadMessage;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;

public class DebugActivity extends BaseNavDrawerActivity {
    private final String TAG = getClass().getSimpleName();

    @BindView(R.id.recycler_view)
    public RecyclerView recyclerView;

    private ArrayList<UnreadMessage> dataSet;
    private RealmAdapter adapter;
    private LinearLayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);
        ButterKnife.bind(this);

        getDrawer();

        setupRecyclerView();
    }

    private void setupRecyclerView() {
        Realm realm = RealmDB.getDefault(this);
        dataSet = new ArrayList<>();

        RealmResults<UnreadMessage> results = realm.where(UnreadMessage.class).findAll();
        dataSet.addAll(results);

        adapter = new RealmAdapter(dataSet);
        layoutManager = new LinearLayoutManager(this);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);
    }

    public static class RealmAdapter extends RecyclerView.Adapter<RealmAdapter.BaseViewHolder> {
        private static final String TAG = "RealmAdapter";

        private ArrayList<UnreadMessage> dataSet;

        public RealmAdapter(ArrayList<UnreadMessage> dataSet) {
            this.dataSet = dataSet;
        }

        @Override
        public RealmAdapter.BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_debug, parent, false);
            return new RealmAdapter.BaseViewHolder(view);
        }

        @Override
        public void onBindViewHolder(BaseViewHolder holder, int position) {
            final UnreadMessage unreadMessage = dataSet.get(position);

            if (unreadMessage == null) {
                Log.e(TAG, "onBindViewHolder()-> message is null");
                return;
            }

            holder.TVId.setText(holder.TVId.getText().toString().concat(String.valueOf(unreadMessage.getId())));
            holder.TVChatId.setText(holder.TVChatId.getText().toString().concat(String.valueOf(unreadMessage.getChatId())));
            holder.TVCounter.setText(holder.TVCounter.getText().toString().concat(String.valueOf(unreadMessage.getUnreadCount())));
        }

        @Override
        public int getItemCount() {
            return dataSet.size();
        }

        public static class BaseViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.tv_debug_id)
            public TextView TVId;

            @BindView(R.id.tv_debug_chat_id)
            public TextView TVChatId;

            @BindView(R.id.tv_debug_counter)
            public TextView TVCounter;

            public BaseViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }
}
