package com.krain.srecyclerview.srecyclerview;

/**
 * Created by 胡亚敏 on 2016-5-27.
 */
public interface OnRecyclerStatusChangeListener {

    public void onRefresh();

    public void onLoadMore();

    public void startRefresh();

    public void refreshComplete();
}
