package com.android.privatemessenger.modules;

import android.app.Dialog;
import android.content.Context;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.privatemessenger.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ActionDialog extends BottomSheetDialogFragment {

    private FragmentManager fragmentManager;

    private final String TAG = getClass().getSimpleName();

    @BindView(R.id.recycler_view)
    public RecyclerView recyclerView;

    @Override
    public void setupDialog(Dialog dialog, int style) {
        View rootView = getActivity().getLayoutInflater().inflate(R.layout.bottom_sheet_dialog_action, null);
        ButterKnife.bind(this, rootView);

        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        dialog.setContentView(rootView);
        super.setupDialog(dialog, style);
    }

    public ActionDialog setAdapter(Builder.ActionAdapter adapter) {
        recyclerView.setAdapter(adapter);
        return this;
    }

    public ActionDialog setFragmentManager(FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
        return this;
    }

    public void show() {
        show(fragmentManager, TAG);
    }

    public static class Builder {
        private final String TAG = ActionDialog.class.getSimpleName() + "." + this.getClass().getSimpleName();

        private Context context;
        private ArrayList<SimpleActionItem> dataSet;
        private ActionAdapter adapter;
        private ActionDialog dialog;

        public Builder(FragmentManager fragmentManager, Context context) {
            this.context = context;
            dataSet = new ArrayList<>();
            adapter = new ActionAdapter(dataSet);
            dialog = new ActionDialog();

            dialog.setAdapter(adapter);
            dialog.setFragmentManager(fragmentManager);
        }

        public Builder addItem(SimpleActionItem item) {
            if (item != null) {
                dataSet.add(item);
            }

            adapter.notifyItemInserted(dataSet.indexOf(item));

            return this;
        }

        public ActionDialog build() {
            return dialog;
        }

        private class ActionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

            private ArrayList<SimpleActionItem> dataSet;

            public ActionAdapter(ArrayList<SimpleActionItem> dataSet) {
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
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_dialog_action, parent, false));
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                if (!(holder instanceof ActionViewHolder)) {
                    return;
                }
                if (dataSet == null) {
                    return;
                }

                ActionViewHolder viewHolder = (ActionViewHolder) holder;
                final SimpleActionItem simpleActionItem = dataSet.get(position);

                viewHolder.TVTitle.setText(dataSet.get(position).getTitle());
                viewHolder.TVTitle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (simpleActionItem.getOnItemClickListener() != null) {
                            simpleActionItem.getOnItemClickListener().onClick(simpleActionItem);
                        }
                    }
                });

                viewHolder.TVTitle.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if (simpleActionItem.getOnItemClickListener() != null) {
                            simpleActionItem.getOnItemClickListener().onLongClick(simpleActionItem);
                            return true;
                        }
                        return false;
                    }
                });
            }

            @Override
            public int getItemCount() {
                return dataSet == null ? 0 : dataSet.size();
            }

            public ArrayList<SimpleActionItem> getDataSet() {
                return dataSet;
            }
        }
    }

    public static class SimpleActionItem {
        private String title;
        private OnItemClickListener onItemClickListener;

        public SimpleActionItem(String title, OnItemClickListener onItemClickListener) {
            this.title = title;
            this.onItemClickListener = onItemClickListener;
        }

        public String getTitle() {
            return title;
        }

        public OnItemClickListener getOnItemClickListener() {
            return onItemClickListener;
        }
    }

    public static interface OnItemClickListener {
        void onClick(SimpleActionItem clickedItem);

        void onLongClick(SimpleActionItem clickedItem);
    }
}