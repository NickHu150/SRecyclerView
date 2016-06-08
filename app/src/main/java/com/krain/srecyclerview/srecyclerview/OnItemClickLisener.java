package com.krain.srecyclerview.srecyclerview;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by ZHANGPING129 on 2015-10-26.
 */
  public interface OnItemClickLisener {
    void onItemClick(int position, int viewType, RecyclerView.ViewHolder holder, View v);
    void onItemLongClick(int position, int viewType, RecyclerView.ViewHolder holder, View v);
}
