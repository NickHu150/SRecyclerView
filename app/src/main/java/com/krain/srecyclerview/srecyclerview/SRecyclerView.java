package com.krain.srecyclerview.srecyclerview;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

import com.krain.srecyclerview.R;
import com.krain.srecyclerview.fruitview.FruitView;

/**
 * Created by 胡亚敏 on 2016-5-27.
 */
public class SRecyclerView extends ViewGroup {
    Context context;
    RecyclerView mRecyclerView;
    FruitView mHeaderView;
    AdapterWrapper mAdapter;
    boolean mIsTop = true;//是否滑动到了最顶部
    RecyclerView.LayoutManager mLayoutManager;
    int mLastVisibleItem;
    int mFirstVisibleItem;
    OnRecyclerStatusChangeListener mRecyclerChangeListener;
    int mStatus;//当前状态
    int mHeadviewHeight;//headview的高度
    Scroller mScroller;
    int mFristScollerY;//最开始的getscrolly
    boolean mHasFooter;//是否有上拉加载的功能
    boolean mHasRefresh = true;//是否支持下拉刷新
    private final int STATUS_NORMAL = 0, STATUS_REFRESH = 1, STATUS_LOAD = 2;

    public SRecyclerView(Context context) {
        super(context);
        init(context);
    }

    public SRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    /**
     * 是否支持上拉加载
     *
     * @param hasLoadmore
     */
    public void setLoadmore(boolean hasLoadmore) {
        mHasFooter = hasLoadmore;
    }

    /**
     * 是否支持下拉刷新
     *
     * @param hasRefresh
     */
    public void setRefresh(boolean hasRefresh) {
        mHasRefresh = hasRefresh;
    }

    public void setAdapter(BaseRecyclerViewAdapter adapter) {
        mAdapter = new AdapterWrapper(context, adapter);
        mRecyclerView.setAdapter(mAdapter);
    }

    /**
     * 获取viewgroup里面的recyclerview
     *
     * @return
     */
    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    public void setOnRecyclerChangeListener(OnRecyclerStatusChangeListener listener) {
        mRecyclerChangeListener = listener;
    }

    /**
     * 设置RecyclerView添加、删除Item的动画
     *
     * @param animator
     */
    public void setItemAnimator(RecyclerView.ItemAnimator animator) {
        mRecyclerView.setItemAnimator(animator);
    }

    public void notifyDataSetChanged(){
        mStatus = STATUS_NORMAL;//重新set数据代表loadmore结束了，此时恢复成普通状态
        mAdapter.notifyDataSetChanged();
    }

    /**
     * 初始化操作
     *
     * @param context
     */
    void init(Context context) {
        this.context = context;
        mScroller = new Scroller(context);
        addChildView(context);
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }

    }

    /**
     * 增加子View
     *
     * @param context
     */
    void addChildView(Context context) {
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mHeaderView = new FruitView(context);
        mRecyclerView = new RecyclerView(context);
        addView(mHeaderView);
        addView(mRecyclerView);
        mLayoutManager = new LinearLayoutManager(context);
        mRecyclerView.setLayoutManager(mLayoutManager);
        // 设置Item增加、移除默认动画
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addOnScrollListener(onScrollListener);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutParams(params);
    }

    /**
     * 屏蔽Recyclerview的触摸事件（下拉刷新的时候）
     */
    float lastY;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                lastY = ev.getRawY();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                return false;
            case MotionEvent.ACTION_MOVE:
                if (mHasRefresh && mIsTop && ev.getRawY() > lastY)
                    return true;
                break;

        }
        return false;
    }


    float offsetY = 0;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                offsetY = Math.abs(event.getRawY() - lastY); //Y差值绝对值
                if (offsetY > 0)
                    scrollToOffset(offsetY);
                else {
                    mIsTop = false;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (getScrollY() <= 0) {
                    doRefresh();
                } else complete();
                break;
        }
        return super.onTouchEvent(event);
    }


    /**
     * 滚动这个view到手滑动的位置
     *
     * @param offsetY Y轴偏移量
     */
    void scrollToOffset(float offsetY) {
        //假如正在刷新并且现在的scrolly和初始值一样的时候，代表准备下拉开始刷新，并执行一次only
        if (getScrollY() == mFristScollerY && mRecyclerChangeListener != null)
            mRecyclerChangeListener.startRefresh();
        int value = Math.round(offsetY / 2.0F);
        value = mFristScollerY - value;
        scrollTo(0, value);
    }

    /**
     * 执行刷新操作,移动到header刚出来的位置
     */
    void doRefresh() {
        mStatus = STATUS_REFRESH;
        int currentY = getScrollY();
        mScroller.startScroll(0, currentY, 0, (mFristScollerY - mHeadviewHeight) - currentY);
        invalidate();
        if (mRecyclerChangeListener != null) mRecyclerChangeListener.onRefresh();
        handler.sendEmptyMessageDelayed(0, 1000);
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            complete();
        }
    };


    /**
     * header返回原处完全隐藏
     */
    public void complete() {
        if (mRecyclerChangeListener != null) mRecyclerChangeListener.refreshComplete();
        mStatus = STATUS_NORMAL;
        int currentY = getScrollY();
        mScroller.startScroll(0, currentY, 0, mFristScollerY - currentY);
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
            height += child.getMeasuredHeight();
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int left = getPaddingLeft();
        int top = getPaddingTop();
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (i == 0) {//当是header的时候居中显示
                int headerLeft = getMeasuredWidth() / 2 - child.getMeasuredWidth() / 2;
                child.layout(headerLeft, top, headerLeft + child.getMeasuredWidth(), top + child.getMeasuredHeight());
            } else
                child.layout(left, top, left + child.getMeasuredWidth(), top + child.getMeasuredHeight());
            top += child.getMeasuredHeight();
        }
        mHeadviewHeight = getPaddingTop() + mHeaderView.getMeasuredHeight();
        scrollTo(0, mHeadviewHeight);//移动到header下方以显示recyleview
        mFristScollerY = getScrollY();
    }


    /**
     * RecyclerView的滑动监听事件
     */
    RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);

            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                //滑动到了顶部
                if (mFirstVisibleItem == 0) {
                    mIsTop = true;
                } else {
                    mIsTop = false;
                    if (mStatus != STATUS_LOAD && mHasFooter && mLastVisibleItem + 1 == mAdapter.getItemCount()) {
                        mStatus = STATUS_LOAD;
                        if (mRecyclerChangeListener != null) {
                            mRecyclerChangeListener.onLoadMore();
                        }
                    }
                }


            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if (mLayoutManager instanceof LinearLayoutManager) {
                mLastVisibleItem = ((LinearLayoutManager) mLayoutManager).findLastVisibleItemPosition();
                mFirstVisibleItem = ((LinearLayoutManager) mLayoutManager).findFirstCompletelyVisibleItemPosition();
            } else if (mLayoutManager instanceof GridLayoutManager) {
                mLastVisibleItem = ((GridLayoutManager) mLayoutManager).findLastVisibleItemPosition();
                mFirstVisibleItem = ((GridLayoutManager) mLayoutManager).findFirstCompletelyVisibleItemPosition();
            } else if (mLayoutManager instanceof StaggeredGridLayoutManager) {
                //因为StaggeredGridLayoutManager的特殊性可能导致最后显示的item存在多个，所以这里取到的是一个数组
                //得到这个数组后再取到数组中position值最大的那个就是最后显示的position值了
                int[] lastPositions = new int[((StaggeredGridLayoutManager) mLayoutManager).getSpanCount()];
                ((StaggeredGridLayoutManager) mLayoutManager).findLastVisibleItemPositions(lastPositions);
                mLastVisibleItem = findMax(lastPositions);
                mFirstVisibleItem = ((StaggeredGridLayoutManager) mLayoutManager).findFirstVisibleItemPositions(lastPositions)[0];
            }
        }
    };

    private int findMax(int[] positions) {
        int max = positions[0];
        for (int value : positions) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }

    private class AdapterWrapper extends RecyclerView.Adapter {

        private static final int TYPE_ITEM = 0;
        private static final int TYPE_FOOTER = 1;
        private RecyclerView.Adapter mAdapter;
        private Context mContext;

        public AdapterWrapper(Context context, RecyclerView.Adapter wrappedAdapter) {
            this.mContext = context;
            this.mAdapter = wrappedAdapter;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder holder = null;
            switch (viewType) {
                case TYPE_ITEM:
                    holder = mAdapter.onCreateViewHolder(parent, viewType);
                    break;
                case TYPE_FOOTER:
                    View footer = LayoutInflater.from(mContext).inflate(R.layout.lib_recyle_footview, null);
                    footer.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
                    holder = new FooterViewHolder(footer);
                    break;
            }
            return holder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (!mHasFooter || position + 1 != getItemCount()) {
                mAdapter.onBindViewHolder(holder, position);
            }
        }

        @Override
        public int getItemCount() {
            return mHasFooter ? mAdapter.getItemCount() + 1 : mAdapter.getItemCount();
        }

        @Override
        public int getItemViewType(int position) {
            if (mHasFooter && position + 1 == getItemCount()) {
                return TYPE_FOOTER;
            } else {
                return TYPE_ITEM;
            }
        }

        private class FooterViewHolder extends RecyclerView.ViewHolder {

            public FooterViewHolder(View itemView) {
                super(itemView);
            }
        }
    }
}
