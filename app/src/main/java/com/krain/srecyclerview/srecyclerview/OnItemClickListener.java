package com.krain.srecyclerview.srecyclerview;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by 胡亚敏 on 2016/4/6.
 */
public interface OnItemClickListener {
    void onItemClick(int position, int viewType, RecyclerView.ViewHolder holder, View v);

}
