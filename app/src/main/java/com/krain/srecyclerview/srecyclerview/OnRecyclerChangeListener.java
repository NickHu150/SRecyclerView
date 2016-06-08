package com.krain.srecyclerview.srecyclerview;

/**
 * Created by 胡亚敏 on 2016-5-27.
 */
public abstract class OnRecyclerChangeListener {

    public abstract void onRefresh();

    public abstract void onLoadMore();

    public void startRefresh() {
    }

    ;

    public void refreshComplete() {
    }

    ;
}
