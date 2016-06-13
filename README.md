## 实现上拉加载下拉刷新的Recyclerview---SRecyclerview(非基于SwipeRefreshLayout)

> 写在开头：为何我要自己重写一个上拉加载下拉刷新的recyleview，其实也是一个无奈的决定，本来SwipeRefreshLayout加上recyleview已经是不错的选择了，但是有些应用的设计师天生喜欢IOS，而且要求andriod的交互方式跟IOS靠齐，显然android这种下拉之后直接出现一个圈的UI不能满足他们的需要，以前在listview时代有类似于Xlistview这样优秀的开源控件，但是在recyleview时代网上封装大部分都是基于SwipeRefreshLayout，无法实现自定义header头下拉的效果，同时也是为了锻炼自己的技术，自己重写了一个能实现header头下拉的recyleview，同时封装了自己的BaserecyclerviewAdapter，封装了点击事件，方便使用

###先看效果：
![image](http://upload-images.jianshu.io/upload_images/2229897-0ed43111252d4fd7.gif?imageMogr2/auto-orient/strip)

#### 原理：由于recyleview并不支持headview，所以传统listview用headveiw做刷新头的做法很显然不行，这里的做法参考了android著名的开源控件，PullToRefreshLayout的部分代码，原理相似，这里感谢作者的发明。SRecyclerview的做法就是将headerview和Recyleview放在同一个父容器里面，SRecyclerview其实是一个viewgroup，并不是一个view，实现刷新的过程就是，讲headview和Recyclerview同时使用addview的方式加入在viewgroup中，经过测量和onlayout方法只会将headveiw放在recyleview的上方，然后onlayout执行完成之后将viewgroup整体网上滑动让headview正好隐藏，然后下拉的时候在actionmove方法里面将偏移量减半，实现滑动的阻塞效果，并不能让headview一路滑到底，放开之后用Scroller类移动到到设置好的刷新的位置，刷新结束之后回到初始值

* * *

接下来我们分过程结合代码来解析一下Srecyleview的实现过程吧，

###### 第一步：将子view装进viewgroup并进行测量和布局子view

初始化Scroller类

     **
     * 初始化操作
     *
     * @param context
     */
    void init(Context context) {
        this.context = context;
        mScroller = new Scroller(context);
        addChildView(context);
    }
    
    addview的操作，初始化Recyclerview

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


    onMeasure测量  onLayout进行布局

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

    在onmeasure方法里面主要测量子view的宽高，然后把headview和recyleview的高度相加得出Srecyleview的高度
    OnLayout方法里面主要就是吧两个view的顺序排列一下，headview居中置顶现实，recyleview在headview的下面，最后布局结束之后呢，我们通过scrollto方法将viewgroup往上移动headview的高度，使其正好隐藏，到此，我们完成了整个界面的UI部分，这个时候是没有任何功能的

    > 在我们的意识里，下拉刷新肯定是要等到下面的recyleview滑动到顶部，然后下拉的时候才出现headview，思考一下如何实现这个效果，我们这里肯定需要标志位的支持了，监听Recyleview的onscroll监听，滑动到顶部的时候我们设置mIsTop = true ; 然后在mIsTop = true的时候拦截触摸时间。，让recyleview无法响应触摸事件，往下滑动的时候解禁
>         recyleview的触摸事件，
>         /**
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

	

    在onTouchEvent里面的ACTION_MOVE里面我们判断如果是往下拉的时候就执行scrollToOffset方法，这个方法里主要执行频繁的scrollto操作，实现一个连贯的下拉动画效果

   
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
    
###源代码地址：https://github.com/Huyamin150/SRecyclerView