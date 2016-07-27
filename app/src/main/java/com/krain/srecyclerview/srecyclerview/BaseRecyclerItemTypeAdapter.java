package com.krain.srecyclerview.srecyclerview;

import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Aoto on 2016/7/11.
 */
public abstract class BaseRecyclerItemTypeAdapter extends BaseRecyclerViewAdapter<RecyclerViewHolder> {


    @Override
    public RecyclerViewHolder getViewHolder(View itemView) {
        return null;
    }

    @Override
    public abstract RecyclerViewHolder getViewHolder(View itemView, int viewType);

    @Override
    public abstract View getItemView(int viewType, ViewGroup parent);

    @Override
    public abstract void onBindViewHolder(RecyclerViewHolder holder, int position);

    @Override
    public abstract int getItemCount();

    @Override
    public abstract int getItemViewType(int position) ;
}
