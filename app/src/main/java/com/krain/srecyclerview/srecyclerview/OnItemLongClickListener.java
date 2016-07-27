package com.krain.srecyclerview.srecyclerview;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by Aoto on 2016/7/5.
 */
public interface OnItemLongClickListener {
    void onItemLongClick(int position, int viewType, RecyclerView.ViewHolder holder, View v);
}
