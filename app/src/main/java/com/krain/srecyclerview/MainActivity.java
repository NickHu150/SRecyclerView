package com.krain.srecyclerview;

import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.krain.srecyclerview.srecyclerview.BaseRecyclerViewAdapter;
import com.krain.srecyclerview.srecyclerview.OnRecyclerStatusChangeListener;
import com.krain.srecyclerview.srecyclerview.SRecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnRecyclerStatusChangeListener{

    List<String> stringList;
    MyAdapter adapter;
    SRecyclerView sRecyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
         sRecyclerView = (SRecyclerView) findViewById(R.id.srecyclerview);
        sRecyclerView.setLoadmore(true);
        sRecyclerView.setOnRecyclerChangeListener(this);
        stringList = new ArrayList<>();
        for (int i = 0; i <20 ; i++) {
            stringList.add("测试啊啊啊啊啊啊啊啊");
        }
        adapter = new MyAdapter();
        sRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onRefresh() {
        Snackbar.make(sRecyclerView,"onRefresh",Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onLoadMore() {
        Snackbar.make(sRecyclerView,"onLoadMore",Snackbar.LENGTH_LONG).show();
        handler.sendEmptyMessageDelayed(0,1000);
    }
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            for (int i = 0; i <10 ; i++) {
                stringList.add("新增的");
            }
            sRecyclerView.notifyDataSetChanged();
        }
    };

    @Override
    public void startRefresh() {
        Snackbar.make(sRecyclerView,"startRefresh",Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void refreshComplete() {
        Snackbar.make(sRecyclerView,"refreshComplete",Snackbar.LENGTH_LONG).show();
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
