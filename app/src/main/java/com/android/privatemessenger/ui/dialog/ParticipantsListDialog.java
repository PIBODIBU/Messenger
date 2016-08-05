package com.android.privatemessenger.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.privatemessenger.R;
import com.android.privatemessenger.data.model.User;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ParticipantsListDialog extends BottomSheetDialogFragment {
    private final String TAG = getClass().getSimpleName();

    @BindView(R.id.recycler_view)
    public RecyclerView recyclerView;

    private Builder.ActionAdapter adapter;
    private FragmentManager fragmentManager;
    private LinearLayoutManager layoutManager;

    @Override
    public void setupDialog(Dialog dialog, int style) {
        View rootView = getActivity().getLayoutInflater().inflate(R.layout.bottom_sheet_dialog_action, null);
        ButterKnife.bind(this, rootView);

        layoutManager = new LinearLayoutManager(getActivity());

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        dialog.setContentView(rootView);
        super.setupDialog(dialog, style);
    }

    public ParticipantsListDialog setAdapter(Builder.ActionAdapter adapter) {
        this.adapter = adapter;
        return this;
    }

    public ParticipantsListDialog setFragmentManager(FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
        return this;
    }

    public void show() {
        show(fragmentManager, TAG);
    }

    public static class Builder {
        private final String TAG = ActionDialog.class.getSimpleName() + "." + this.getClass().getSimpleName();

        private Context context;
        private ArrayList<User> dataSet;
        private ActionAdapter adapter;
        private ParticipantsListDialog dialog;

        public Builder(FragmentManager fragmentManager, Context context) {
            this.context = context;
            dataSet = new ArrayList<>();
            adapter = new ActionAdapter(dataSet);
            dialog = new ParticipantsListDialog();

            dialog.setAdapter(adapter);
            dialog.setFragmentManager(fragmentManager);
        }

        public Builder addParticipant(List<User> users) {
            dataSet.addAll(users);
            adapter.notifyDataSetChanged();

            return this;
        }

        public Builder addParticipant(User user) {
            if (user != null) {
                dataSet.add(user);
            }

            adapter.notifyItemInserted(dataSet.indexOf(user));

            return this;
        }

        public Builder setCloseAfterItemSelected(boolean closeAfterItemSelected) {
            adapter.setCloseAfterItemSelected(closeAfterItemSelected);
            return this;
        }

        public ParticipantsListDialog build() {
            return dialog;
        }

        private class ActionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

            private ArrayList<User> dataSet;

            private OnItemClickListener onItemCLickListener;
            private boolean closeAfterItemSelected = true;

            public ActionAdapter(ArrayList<User> dataSet) {
                this.dataSet = dataSet;
            }

            public class ActionViewHolder extends RecyclerView.ViewHolder {
                private TextView TVTitle;

                public ActionViewHolder(View itemView) {
                    super(itemView);
                    TVTitle = (TextView) itemView.findViewById(R.id.text_view_title);
                }
            }

            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return new ActionViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_dialog_action_with_photo, parent, false));
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                if (!(holder instanceof ActionViewHolder)) {
                    Log.e(TAG, "onBindViewHolder()-> holder is not instance of ActionViewHolder");
                    return;
                }
                if (dataSet == null) {
                    Log.e(TAG, "onBindViewHolder()-> dataSet is null");
                    return;
                }

                ActionViewHolder viewHolder = (ActionViewHolder) holder;
                final User user = dataSet.get(position);

                viewHolder.TVTitle.setText(dataSet.get(position).getName());
                viewHolder.TVTitle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onItemCLickListener != null) {
                            onItemCLickListener.onClick(user);
                        }

                        if (closeAfterItemSelected && dialog != null)
                            dialog.dismiss();
                    }
                });
            }

            @Override
            public int getItemCount() {
                return dataSet == null ? 0 : dataSet.size();
            }

            public ArrayList<User> getDataSet() {
                return dataSet;
            }

            public void setCloseAfterItemSelected(boolean closeAfterItemSelected) {
                this.closeAfterItemSelected = closeAfterItemSelected;
            }

            public void setOnItemCLickListener(OnItemClickListener onItemCLickListener) {
                this.onItemCLickListener = onItemCLickListener;
            }
        }
    }

    public interface OnItemClickListener {
        void onClick(User clickedItem);
    }
}