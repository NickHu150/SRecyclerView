## 实现上拉加载下拉刷新的Recyclerview---SRecyclerview(非基于SwipeRefreshLayout)

> 写在开头：为何我要自己重写一个上拉加载下拉刷新的recyleview，其实也是一个无奈的决定，本来SwipeRefreshLayout加上recyleview已经是不错的选择了，但是有些应用的设计师天生喜欢IOS，而且要求andriod的交互方式跟IOS靠齐，显然android这种下拉之后直接出现一个圈的UI不能满足他们的需要，以前在listview时代有类似于Xlistview这样优秀的开源控件，但是在recyleview时代网上封装大部分都是基于SwipeRefreshLayout，无法实现自定义header头下拉的效果，同时也是为了锻炼自己的技术，自己重写了一个能实现header头下拉的recyleview，同时封装了自己的BaserecyclerviewAdapter，封装了点击事件，方便使用

#### 原理：由于recyleview并不支持headview，所以传统listview用headveiw做刷新头的做法很显然不行，这里的做法参考了android著名的开源控件，PullToRefreshLayout的部分代码，原理相似，这里感谢作者的发明。SRecyclerview的做法就是将headerview和Recyleview放在同一个父容器里面，SRecyclerview其实是一个viewgroup，并不是一个view，实现刷新的过程就是，讲headview和Recyclerview同时使用addview的方式加入在viewgroup中，经过测量和onlayout方法只会将headveiw放在recyleview的上方，然后onlayout执行完成之后将viewgroup整体网上滑动让headview正好隐藏，然后下拉的时候在actionmove方法里面将偏移量减半，实现滑动的阻塞效果，并不能让headview一路滑到底，放开之后用Scroller类移动到到设置好的刷新的位置，刷新结束之后回到初始值

* * *

接下来我们分过程结合代码来解析一下Srecyleview的实现过程吧，

###### 第一步：将子view装进viewgroup并进行测量和布局子view

初始化Scroller类

     <span class="hljs-keyword">*</span><span class="hljs-keyword">*</span>
     <span class="hljs-keyword">*</span> 初始化操作
     <span class="hljs-keyword">*</span>
     <span class="hljs-keyword">*</span> <span class="hljs-comment">@param context</span>
     <span class="hljs-keyword">*</span>/
    void init(Context context) {
        this.context = context;
        mScroller = new Scroller(context);
        addChildView(context);
    }

    addview的操作，初始化Recyclerview
    `</pre>

       /**

    <pre>` * 增加子View
     *
     * <span class="hljs-annotation">@param</span> context
     */
    <span class="hljs-function"><span class="hljs-keyword">void</span> <span class="hljs-title">addChildView</span><span class="hljs-params">(Context context)</span> </span>{
        ViewGroup.LayoutParams params = <span class="hljs-keyword">new</span> ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mHeaderView = <span class="hljs-keyword">new</span> FruitView(context);
        mRecyclerView = <span class="hljs-keyword">new</span> RecyclerView(context);
        addView(mHeaderView);
        addView(mRecyclerView);
        mLayoutManager = <span class="hljs-keyword">new</span> LinearLayoutManager(context);
        mRecyclerView.setLayoutManager(mLayoutManager);
        <span class="hljs-comment">// 设置Item增加、移除默认动画</span>
        mRecyclerView.setItemAnimator(<span class="hljs-keyword">new</span> DefaultItemAnimator());
        mRecyclerView.addOnScrollListener(onScrollListener);
        mRecyclerView.setHasFixedSize(<span class="hljs-keyword">true</span>);
        mRecyclerView.setLayoutParams(params);
    }

    onMeasure测量  onLayout进行布局

       <span class="hljs-annotation">@Override</span>
    <span class="hljs-function"><span class="hljs-keyword">protected</span> <span class="hljs-keyword">void</span> <span class="hljs-title">onMeasure</span><span class="hljs-params">(<span class="hljs-keyword">int</span> widthMeasureSpec, <span class="hljs-keyword">int</span> heightMeasureSpec)</span> </span>{
        <span class="hljs-keyword">int</span> width = MeasureSpec.getSize(widthMeasureSpec);
        <span class="hljs-keyword">int</span> height = <span class="hljs-number">0</span>;
        <span class="hljs-keyword">for</span> (<span class="hljs-keyword">int</span> i = <span class="hljs-number">0</span>; i < getChildCount(); i++) {
            View child = getChildAt(i);
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
            height += child.getMeasuredHeight();
        }
        setMeasuredDimension(width, height);
    }

    <span class="hljs-annotation">@Override</span>
    <span class="hljs-function"><span class="hljs-keyword">protected</span> <span class="hljs-keyword">void</span> <span class="hljs-title">onLayout</span><span class="hljs-params">(<span class="hljs-keyword">boolean</span> changed, <span class="hljs-keyword">int</span> l, <span class="hljs-keyword">int</span> t, <span class="hljs-keyword">int</span> r, <span class="hljs-keyword">int</span> b)</span> </span>{
        <span class="hljs-keyword">int</span> left = getPaddingLeft();
        <span class="hljs-keyword">int</span> top = getPaddingTop();
        <span class="hljs-keyword">for</span> (<span class="hljs-keyword">int</span> i = <span class="hljs-number">0</span>; i < getChildCount(); i++) {
            View child = getChildAt(i);
            <span class="hljs-keyword">if</span> (i == <span class="hljs-number">0</span>) {<span class="hljs-comment">//当是header的时候居中显示</span>
                <span class="hljs-keyword">int</span> headerLeft = getMeasuredWidth() / <span class="hljs-number">2</span> - child.getMeasuredWidth() / <span class="hljs-number">2</span>;
                child.layout(headerLeft, top, headerLeft + child.getMeasuredWidth(), top + child.getMeasuredHeight());
            } <span class="hljs-keyword">else</span>
                child.layout(left, top, left + child.getMeasuredWidth(), top + child.getMeasuredHeight());
            top += child.getMeasuredHeight();
        }
        mHeadviewHeight = getPaddingTop() + mHeaderView.getMeasuredHeight();
        scrollTo(<span class="hljs-number">0</span>, mHeadviewHeight);<span class="hljs-comment">//移动到header下方以显示recyleview</span>
        mFristScollerY = getScrollY();
    }

    在onmeasure方法里面主要测量子view的宽高，然后把headview和recyleview的高度相加得出Srecyleview的高度
    OnLayout方法里面主要就是吧两个view的顺序排列一下，headview居中置顶现实，recyleview在headview的下面，最后布局结束之后呢，我们通过scrollto方法将viewgroup往上移动headview的高度，使其正好隐藏，到此，我们完成了整个界面的UI部分，这个时候是没有任何功能的

    > 在我们的意识里，下拉刷新肯定是要等到下面的recyleview滑动到顶部，然后下拉的时候才出现headview，思考一下如何实现这个效果，我们这里肯定需要标志位的支持了，监听Recyleview的onscroll监听，滑动到顶部的时候我们设置mIsTop = <span class="hljs-keyword">true</span> ; 然后在mIsTop = <span class="hljs-keyword">true</span>的时候拦截触摸时间。，让recyleview无法响应触摸事件，往下滑动的时候解禁
    `</pre>> <pre>`    recyleview的触摸事件，
>         /<span class="hljs-keyword">*</span><span class="hljs-keyword">*</span>
>      <span class="hljs-keyword">*</span> 屏蔽Recyclerview的触摸事件（下拉刷新的时候）
>      <span class="hljs-keyword">*</span>/
>     float lastY;
>     `</pre>
    <pre>`@<span class="hljs-function">Override
    <span class="hljs-keyword">public</span> boolean <span class="hljs-title">onInterceptTouchEvent</span><span class="hljs-params">(MotionEvent ev)</span> </span>{
        final <span class="hljs-keyword">int</span> action = MotionEventCompat.getActionMasked(ev);
        <span class="hljs-keyword">switch</span> (action) {
            <span class="hljs-keyword">case</span> MotionEvent.ACTION_DOWN:
                lastY = ev.getRawY();
                <span class="hljs-keyword">break</span>;
            <span class="hljs-keyword">case</span> MotionEvent.ACTION_UP:
            <span class="hljs-keyword">case</span> MotionEvent.ACTION_CANCEL:
                <span class="hljs-keyword">return</span> <span class="hljs-keyword">false</span>;
            <span class="hljs-keyword">case</span> MotionEvent.ACTION_MOVE:
                <span class="hljs-keyword">if</span> (mHasRefresh && mIsTop && ev.getRawY() > lastY)
                    <span class="hljs-keyword">return</span> <span class="hljs-keyword">true</span>;
                <span class="hljs-keyword">break</span>;

        }
        <span class="hljs-keyword">return</span> <span class="hljs-keyword">false</span>;
    }

    <span class="hljs-keyword">float</span> offsetY = <span class="hljs-number">0</span>;

    @<span class="hljs-function">Override
    <span class="hljs-keyword">public</span> boolean <span class="hljs-title">onTouchEvent</span><span class="hljs-params">(MotionEvent <span class="hljs-keyword">event</span>)</span> </span>{
        <span class="hljs-keyword">switch</span> (<span class="hljs-keyword">event</span>.getAction()) {
            <span class="hljs-keyword">case</span> MotionEvent.ACTION_MOVE:
                offsetY = Math.abs(<span class="hljs-keyword">event</span>.getRawY() - lastY); <span class="hljs-comment">//Y差值绝对值</span>
                <span class="hljs-keyword">if</span> (offsetY > <span class="hljs-number">0</span>)
                    scrollToOffset(offsetY);
                <span class="hljs-keyword">else</span> {
                    mIsTop = <span class="hljs-keyword">false</span>;
                }
                <span class="hljs-keyword">break</span>;
            <span class="hljs-keyword">case</span> MotionEvent.ACTION_UP:
                <span class="hljs-keyword">if</span> (getScrollY() <= <span class="hljs-number">0</span>) {
                    doRefresh();
                } <span class="hljs-function"><span class="hljs-keyword">else</span> <span class="hljs-title">complete</span><span class="hljs-params">()</span></span>;
                <span class="hljs-keyword">break</span>;
        }
        <span class="hljs-keyword">return</span> super.onTouchEvent(<span class="hljs-keyword">event</span>);
    }

    在onTouchEvent里面的ACTION_MOVE里面我们判断如果是往下拉的时候就执行scrollToOffset方法，这个方法里主要执行频繁的scrollto操作，实现一个连贯的下拉动画效果

    <span class="hljs-comment">/**
     * 滚动这个view到手滑动的位置
     *
     * @param offsetY Y轴偏移量
     */</span>
    <span class="hljs-function"><span class="hljs-keyword">void</span> <span class="hljs-title">scrollToOffset</span><span class="hljs-params">(<span class="hljs-keyword">float</span> offsetY)</span> </span>{
        <span class="hljs-comment">//假如正在刷新并且现在的scrolly和初始值一样的时候，代表准备下拉开始刷新，并执行一次only</span>
        <span class="hljs-keyword">if</span> (getScrollY() == mFristScollerY && mRecyclerChangeListener != <span class="hljs-keyword">null</span>)
            mRecyclerChangeListener.startRefresh();
        <span class="hljs-keyword">int</span> <span class="hljs-keyword">value</span> = Math.round(offsetY / <span class="hljs-number">2.0</span>F);
        <span class="hljs-keyword">value</span> = mFristScollerY - <span class="hljs-keyword">value</span>;
        scrollTo(<span class="hljs-number">0</span>, <span class="hljs-keyword">value</span>);
    }

    <span class="hljs-comment">/**
     * 执行刷新操作,移动到header刚出来的位置
     */</span>
    <span class="hljs-function"><span class="hljs-keyword">void</span> <span class="hljs-title">doRefresh</span><span class="hljs-params">()</span> </span>{
        mStatus = STATUS_REFRESH;
        <span class="hljs-keyword">int</span> currentY = getScrollY();
        mScroller.startScroll(<span class="hljs-number">0</span>, currentY, <span class="hljs-number">0</span>, (mFristScollerY - mHeadviewHeight) - currentY);
        invalidate();
        <span class="hljs-keyword">if</span> (mRecyclerChangeListener != <span class="hljs-keyword">null</span>) mRecyclerChangeListener.onRefresh();
        handler.sendEmptyMessageDelayed(<span class="hljs-number">0</span>, <span class="hljs-number">1000</span>);
    }

    Handler handler = <span class="hljs-keyword">new</span> Handler() {
        @<span class="hljs-function">Override
        <span class="hljs-keyword">public</span> <span class="hljs-keyword">void</span> <span class="hljs-title">handleMessage</span><span class="hljs-params">(Message msg)</span> </span>{
            super.handleMessage(msg);
            complete();
        }
    };
    