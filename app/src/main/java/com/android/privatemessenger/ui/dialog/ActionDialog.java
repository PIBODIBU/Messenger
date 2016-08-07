package com.android.privatemessenger.ui.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.privatemessenger.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ActionDialog extends BottomSheetDialogFragment {
    private final String TAG = getClass().getSimpleName();

    @BindView(R.id.recycler_view)
    public RecyclerView recyclerView;

    private Builder.ActionAdapter adapter;
    private FragmentManager fragmentManager;
    private LinearLayoutManager layoutManager;

    private View rootView = null;

    @Override
    public void setupDialog(Dialog dialog, int style) {
        ButterKnife.bind(this, rootView);

        layoutManager = new LinearLayoutManager(getActivity());

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        dialog.setContentView(rootView);
        super.setupDialog(dialog, style);
    }

    public ActionDialog setAdapter(Builder.ActionAdapter adapter) {
        this.adapter = adapter;
        return this;
    }

    public ActionDialog setFragmentManager(FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
        return this;
    }

    public View getRootView() {
        return rootView;
    }

    public void setRootView(View rootView) {
        this.rootView = rootView;
    }

    public void show() {
        show(fragmentManager, TAG);
    }

    public static class Builder {
        private final String TAG = ActionDialog.class.getSimpleName() + "." + this.getClass().getSimpleName();

        private Activity activity;
        private ArrayList<AbstractActionItem> dataSet;
        private ActionAdapter adapter;
        private ActionDialog dialog;

        public Builder(FragmentManager fragmentManager, Activity activity) {
            this.activity = activity;
            dataSet = new ArrayList<>();
            adapter = new ActionAdapter(dataSet);
            dialog = new ActionDialog();

            dialog.setAdapter(adapter);
            dialog.setFragmentManager(fragmentManager);
        }

        public Builder addItem(AbstractActionItem item) {
            if (item != null) {
                dataSet.add(item);
            }

            adapter.notifyItemInserted(dataSet.indexOf(item));

            return this;
        }

        public Builder withCloseAfterItemSelected(boolean closeAfterItemSelected) {
            adapter.setCloseAfterItemSelected(closeAfterItemSelected);
            return this;
        }

        public Builder withCustomLayout(@LayoutRes int layoutId) {
            dialog.setRootView(dialog.getActivity().getLayoutInflater().inflate(layoutId, null));
            return this;
        }

        public ActionDialog build() {
            if (dialog.getRootView() == null) {
                dialog.setRootView(activity.getLayoutInflater().inflate(R.layout.bottom_sheet_dialog_action, null));
            }
            return dialog;
        }

        private class ActionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
            private ArrayList<AbstractActionItem> dataSet;

            private boolean closeAfterItemSelected = true;

            public ActionAdapter(ArrayList<AbstractActionItem> dataSet) {
                this.dataSet = dataSet;
            }

            public class SimpleViewHolder extends RecyclerView.ViewHolder {
                private TextView TVTitle;

                public SimpleViewHolder(View itemView) {
                    super(itemView);
                    TVTitle = (TextView) itemView.findViewById(R.id.text_view_title);
                }
            }

            public class ImagedViewHolder extends RecyclerView.ViewHolder {
                private TextView TVTitle;
                private ImageView IVActionImage;
                private LinearLayout LLRootView;

                public ImagedViewHolder(View itemView) {
                    super(itemView);
                    TVTitle = (TextView) itemView.findViewById(R.id.text_view_title);
                    IVActionImage = (ImageView) itemView.findViewById(R.id.image_view);
                    LLRootView = (LinearLayout) itemView.findViewById(R.id.root_view);
                }
            }

            @Override
            public int getItemViewType(int position) {
                try {
                    AbstractActionItem abstractActionItem = dataSet.get(position);

                    if (abstractActionItem instanceof SimpleActionItem) {
                        return AbstractActionItem.TYPE_SIMPLE;
                    } else if (abstractActionItem instanceof ImagedActionItem) {
                        return AbstractActionItem.TYPE_IMAGED;
                    }
                } catch (Exception ex) {
                    Log.e(TAG, "getItemViewType()-> ", ex);
                }
                return super.getItemViewType(position);
            }

            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                RecyclerView.ViewHolder viewHolder = null;

                switch (viewType) {
                    case AbstractActionItem.TYPE_SIMPLE:
                        viewHolder = new SimpleViewHolder(
                                LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_dialog_action_simple, parent, false));
                        break;
                    case AbstractActionItem.TYPE_IMAGED:
                        viewHolder = new ImagedViewHolder(
                                LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_dialog_action_imaged, parent, false));
                        break;
                }

                return viewHolder;
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                if (dataSet == null) {
                    Log.e(TAG, "onBindViewHolder()-> dataSet is null");
                    return;
                }

                if (holder instanceof SimpleViewHolder) {
                    final SimpleActionItem actionItem = (SimpleActionItem) dataSet.get(position);
                    final SimpleViewHolder viewHolder = (SimpleViewHolder) holder;

                    viewHolder.TVTitle.setText(actionItem.getTitle());
                    viewHolder.TVTitle.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (actionItem.getOnItemClickListener() != null) {
                                actionItem.getOnItemClickListener().onClick(actionItem);
                            }

                            if (closeAfterItemSelected && dialog != null)
                                dialog.dismiss();
                        }
                    });
                    viewHolder.TVTitle.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            if (actionItem.getOnItemClickListener() != null) {
                                actionItem.getOnItemClickListener().onLongClick(actionItem);
                                return true;
                            }
                            if (closeAfterItemSelected && dialog != null)
                                dialog.dismiss();
                            return false;
                        }
                    });
                } else if (holder instanceof ImagedViewHolder) {
                    final ImagedActionItem actionItem = (ImagedActionItem) dataSet.get(position);
                    final ImagedViewHolder viewHolder = (ImagedViewHolder) holder;

                    viewHolder.TVTitle.setText(actionItem.getTitle());
                    viewHolder.IVActionImage.setImageResource(actionItem.getImageId());
                    viewHolder.LLRootView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (actionItem.getOnItemClickListener() != null) {
                                actionItem.getOnItemClickListener().onClick(actionItem);
                            }

                            if (closeAfterItemSelected && dialog != null)
                                dialog.dismiss();
                        }
                    });
                    viewHolder.LLRootView.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            if (actionItem.getOnItemClickListener() != null) {
                                actionItem.getOnItemClickListener().onLongClick(actionItem);
                                return true;
                            }
                            if (closeAfterItemSelected && dialog != null)
                                dialog.dismiss();
                            return false;
                        }
                    });
                }
            }

            @Override
            public int getItemCount() {
                return dataSet == null ? 0 : dataSet.size();
            }

            public ArrayList<AbstractActionItem> getDataSet() {
                return dataSet;
            }

            public void setCloseAfterItemSelected(boolean closeAfterItemSelected) {
                this.closeAfterItemSelected = closeAfterItemSelected;
            }
        }
    }

    public abstract static class AbstractActionItem {
        public static final int TYPE_SIMPLE = 0;
        public static final int TYPE_IMAGED = 1;
    }

    public static class SimpleActionItem extends AbstractActionItem {
        private String title;
        private OnItemClickListener onItemClickListener;
        private Object payload;

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

        public Object getPayload() {
            return payload;
        }

        public void setPayload(Object payload) {
            this.payload = payload;
        }
    }

    public static class ImagedActionItem extends AbstractActionItem {
        private String title;
        private int imageId;
        private OnItemClickListener onItemClickListener;

        public ImagedActionItem(String title, @DrawableRes int imageId, OnItemClickListener onItemClickListener) {
            this.title = title;
            this.imageId = imageId;
            this.onItemClickListener = onItemClickListener;
        }

        public String getTitle() {
            return title;
        }

        public int getImageId() {
            return imageId;
        }

        public OnItemClickListener getOnItemClickListener() {
            return onItemClickListener;
        }
    }

    public static interface OnItemClickListener {
        void onClick(AbstractActionItem clickedItem);

        void onLongClick(AbstractActionItem clickedItem);
    }
}