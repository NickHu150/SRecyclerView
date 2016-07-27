package com.krain.srecyclerview;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.krain.srecyclerview.srecyclerview.BaseRecyclerViewAdapter;
import com.krain.srecyclerview.srecyclerview.OnItemClickLisener;
import com.krain.srecyclerview.srecyclerview.OnItemClickListener;
import com.krain.srecyclerview.srecyclerview.OnRecyclerStatusChangeListener;
import com.krain.srecyclerview.srecyclerview.SRecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnRecyclerStatusChangeListener {

    List<String> stringList;
    MyAdapter adapter;
    SRecyclerView sRecyclerView;
    int index;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
         sRecyclerView = (SRecyclerView) findViewById(R.id.srecyclerview);
        sRecyclerView.setLoadmore(true);
        sRecyclerView.setOnRecyclerChangeListener(this);
        sRecyclerView.setMaxPage(2);
        stringList = new ArrayList<>();
        for (int i = 0; i <17 ; i++) {
            stringList.add("测试啊啊啊啊啊啊啊啊");
        }
        adapter = new MyAdapter();
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position, int viewType, RecyclerView.ViewHolder holder, View v) {
                stringList.remove(position);
                sRecyclerView.notifyDataRemove(position);
            }
        });

        sRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onRefresh() {
        for (int i = 0; i <20 ; i++) {
            stringList.add("测试");
        }
        sRecyclerView.notifyDataSetChanged();

    }

    @Override
    public void onLoadMore() {
        handler.sendEmptyMessageDelayed(0,1000);
    }
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            index++;
            for (int i = 0; i <10 ; i++) {
                stringList.add("新增的");
            }
            sRecyclerView.notifyDataInsert(adapter.getItemCount()-1,10);
        }
    };

    @Override
    public void startRefresh() {
    }

    @Override
    public void refreshComplete() {
    }


    class MyAdapter extends BaseRecyclerViewAdapter<MyAdapter.ViewHolder> {



        @Override
        public ViewHolder getViewHolder(View itemView) {
            return new ViewHolder(itemView);
        }

        @Override
        public View getItemView(int viewType, ViewGroup parent) {
            return new TextView(MainActivity.this);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.textView.setTextSize(20);
            holder.textView.setText(stringList.get(position));
        }

        @Override
        public int getItemCount() {
            return stringList.size();
        }



        class ViewHolder extends RecyclerView.ViewHolder {
            public TextView textView;

            public ViewHolder(View itemView) {
                super(itemView);
                textView = (TextView) itemView;
            }
        }
    }
}
