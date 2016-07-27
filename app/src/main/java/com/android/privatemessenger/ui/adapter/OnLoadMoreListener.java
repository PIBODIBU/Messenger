package com.android.privatemessenger.ui.adapter;

public interface OnLoadMoreListener {
    /**
     * Called when user reached end of RecyclerView. Is used for loading new data to RecyclerView.Adapter
     **/
    void onLoadMore();
}