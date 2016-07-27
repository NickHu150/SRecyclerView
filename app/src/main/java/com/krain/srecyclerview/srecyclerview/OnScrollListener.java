package com.krain.srecyclerview.srecyclerview;

import android.support.v7.widget.RecyclerView;

/**
 * Created by Aoto on 2016/7/18.
 */
public interface OnScrollListener {
    public void onScrollStateChanged(RecyclerView recyclerView, int newState);
    public void onScrolled(RecyclerView recyclerView, int dx, int dy);
}
