package com.krain.srecyclerview.srecyclerview;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;


/**
 * Class description
 *
 * @author yamin
 * @date 2015/8/13 上午 11:55
 */
public abstract class BaseRecyclerViewAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    private OnItemClickListener mItemClickListener;
    private OnItemLongClickListener mItemLongClickListener;

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = getItemView(viewType, parent);
        VH vh = null;
        if (viewType == SRecyclerView.SINGLE_TYPE)
            vh = getViewHolder(view);
        else vh = getViewHolder(view, viewType);
        //Don't call setOnClickListener for an AdapterView.
        if (!(view instanceof AdapterView)){
            view.setOnClickListener(new OnRecyclerAdapterclickListener(vh, viewType));
            view.setOnLongClickListener(new OnRecyclerAdapterclickListener(vh, viewType));
        }
        return vh;
    }

    public abstract VH getViewHolder(View itemView);

    public VH getViewHolder(View itemView, int viewType) {
        return null;
    }

    @Override
    public int getItemViewType(int position) {
        return SRecyclerView.SINGLE_TYPE;
    }



    /**
     * 返回item的view
     *
     * @return
     */
    public abstract View getItemView(int viewType, ViewGroup parent);

    /**
     * 返回Adapter每个itemn的数据 可选
     */
    public Object getItem(int position) {
        return null;
    }



    /**
     * item点击事件接口
     *
     * @param mItemListener
     */
    public void setOnItemClickListener(OnItemClickListener mItemListener) {
        if (mItemListener != null) {
            this.mItemClickListener = mItemListener;
        }

    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        if (onItemLongClickListener != null)
            mItemLongClickListener = onItemLongClickListener;
    }

    @Override
    public abstract void onBindViewHolder(VH holder, int position);

    @Override
    public abstract int getItemCount();


    class OnRecyclerAdapterclickListener implements View.OnClickListener, View.OnLongClickListener {
        VH viewholder;
        int viewType;

        public OnRecyclerAdapterclickListener(VH viewholder, int viewType) {
            this.viewholder = viewholder;
            this.viewType = viewType;
        }

        @Override
        public void onClick(View v) {
            if (mItemClickListener != null && viewholder.getAdapterPosition() != RecyclerView.NO_POSITION) {
                mItemClickListener.onItemClick(viewholder.getAdapterPosition(), viewType, viewholder, v);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (mItemLongClickListener != null && viewholder.getAdapterPosition() != RecyclerView.NO_POSITION) {
                mItemLongClickListener.onItemLongClick(viewholder.getAdapterPosition(), viewType, viewholder, v);
            }
            return false;
        }
    }


}
