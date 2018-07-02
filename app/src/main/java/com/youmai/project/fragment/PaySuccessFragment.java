package com.youmai.project.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.youmai.project.R;
import com.youmai.project.activity.order.ChatActivity;
import com.youmai.project.adapter.PaySuccessAdapter;
import com.youmai.project.view.RefreshLayout;

/**
 * 推荐的fragment
 * Created by Administrator on 2018/1/3 0003.
 */

public class PaySuccessFragment extends BaseFragment  implements SwipeRefreshLayout.OnRefreshListener,RefreshLayout.OnLoadListener {

    private RefreshLayout swipeLayout;
    private ListView listView;
    private PaySuccessAdapter paySuccessAdapter;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recommended, container, false);
        swipeLayout=(RefreshLayout)view.findViewById(R.id.swipe_container);
        listView=(ListView)view.findViewById(R.id.list);
        swipeLayout.setColorSchemeResources(R.color.color_bule2,
                R.color.color_bule,
                R.color.color_bule2,
                R.color.color_bule3);
        swipeLayout.setOnRefreshListener(PaySuccessFragment.this);
        swipeLayout.setOnLoadListener(PaySuccessFragment.this);
        swipeLayout.post(new Thread(new Runnable() {
            public void run() {
                swipeLayout.setRefreshing(true);
            }
        }));
        swipeLayout.postDelayed(new Runnable() {
            public void run() {
                listView.addHeaderView(new View(getActivity()));
                paySuccessAdapter=new PaySuccessAdapter(getActivity());
                listView.setAdapter(paySuccessAdapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent=new Intent(getActivity(), ChatActivity.class);
                        startActivity(intent);
                    }
                });
                swipeLayout.setRefreshing(false);

            }
        }, 200);
        return view;
    }

    @Override
    public void onRefresh() {

    }

    @Override
    public void onLoad() {

    }
}
