package com.android.privatemessenger.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.privatemessenger.R;
import com.android.privatemessenger.data.model.Contact;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ContactsMyAdapter extends RecyclerView.Adapter<ContactsMyAdapter.BaseViewHolder> {
    private final String TAG = ContactsMyAdapter.this.getClass().getSimpleName();

    private Context context;
    private ArrayList<Contact> dataSet;
    private RecyclerItemClickListener recyclerItemClickListener;

    public ContactsMyAdapter(Context context, ArrayList<Contact> dataSet) {
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

        public BaseViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    @Override
    public ContactsMyAdapter.BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_contact, parent, false);
        return new ContactsMyAdapter.BaseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ContactsMyAdapter.BaseViewHolder holder, final int position) {
        if (dataSet == null) {
            return;
        }

        final Contact contact = dataSet.get(position);

        if (contact == null) {
            return;
        }

        holder.TVName.setText(contact.getName());
        holder.TVPhone.setText(contact.getPhone());
        holder.TVContactImageText.setText(String.valueOf(contact.getName().charAt(0)).toUpperCase());

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

    public void addItem(Contact contact) {
        dataSet.add(contact);
        notifyItemInserted(dataSet.indexOf(contact));
    }

    public ArrayList<Contact> getDataSet() {
        return dataSet;
    }

    public void setDataSet(ArrayList<Contact> dataSet) {
        this.dataSet = dataSet;
    }

    public void setRecyclerItemClickListener(RecyclerItemClickListener recyclerItemClickListener) {
        this.recyclerItemClickListener = recyclerItemClickListener;
    }
}
