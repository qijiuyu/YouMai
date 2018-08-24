package com.youmai.project.fragment.order;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.youmai.project.R;
import com.youmai.project.activity.main.BuyGoodsActivity;
import com.youmai.project.activity.order.OrderActivity;
import com.youmai.project.adapter.OrderAdapter;
import com.youmai.project.bean.GoodsBean;
import com.youmai.project.bean.HttpBaseBean;
import com.youmai.project.callback.TradingPlay;
import com.youmai.project.fragment.BaseFragment;
import com.youmai.project.http.HandlerConstant;
import com.youmai.project.http.HttpMethod;
import com.youmai.project.utils.JsonUtils;
import com.youmai.project.view.DialogView;
import com.youmai.project.view.RefreshLayout;
import java.util.ArrayList;
import java.util.List;

/**
 * 推荐的fragment
 * Created by Administrator on 2018/1/3 0003.
 */

public class OrderFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener,RefreshLayout.OnLoadListener {

    private RefreshLayout swipeLayout;
    private ListView listView;
    private OrderAdapter orderAdapter;
    private List<GoodsBean> listBeanAll=new ArrayList<>();
    private List<GoodsBean> listComplete=new ArrayList<>();
    private List<GoodsBean> listCancle=new ArrayList<>();
    private List<GoodsBean> listWait=new ArrayList<>();
    private List<String> keyList=new ArrayList<>();
    private boolean isTotal=false;
    //fragment是否可见
    private boolean isVisibleToUser=false;
    //订单对象
    private GoodsBean goodsBean;
    //页码
    private int page=1;
    private DialogView dialogView;
    //此刻是第几个fragment
    private int index;
    private final static String ACTION_GOODS_COMPLETE_SUCCESS = "net.youmai.adminapp.action.goods.complete.success";
    private final static String ACTION_GOODS_CANCEL_SUCCESS = "net.youmai.adminapp.action.goods.cancel.success";
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(keyList.size()==0){
            keyList.add(null);
            keyList.add("1");
            keyList.add("2");
            keyList.add("4");
        }
        //注册广播
        registerReceiver();
    }

    View view;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_recommended, container, false);
        swipeLayout=(RefreshLayout)view.findViewById(R.id.swipe_container);
        listView=(ListView)view.findViewById(R.id.list);
        listView.setDividerHeight(0);
        swipeLayout.setColorSchemeResources(R.color.color_bule2,
                R.color.color_bule,
                R.color.color_bule2,
                R.color.color_bule3);
        swipeLayout.setOnRefreshListener(OrderFragment.this);
        swipeLayout.setOnLoadListener(OrderFragment.this);
        swipeLayout.post(new Thread(new Runnable() {
            public void run() {
                swipeLayout.setRefreshing(true);
            }
        }));
        //查询订单列表
        getOrderList();
        return view;
    }


    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            clearTask();
            String message;
            switch (msg.what){
                case HandlerConstant.GET_PAY_ORDER_SUCCESS:
                    message= (String) msg.obj;
                    setDataByIndex(1);
                    refresh(message);
                    swipeLayout.setRefreshing(false);
                    break;
                case HandlerConstant.GET_PAY_ORDER_SUCCESS2:
                    message= (String) msg.obj;
                    refresh(message);
                    swipeLayout.setLoading(false);
                    break;
                 //交易完成
                 case HandlerConstant.SET_ORDER_COMPLETE_SUCCESS:
                 //交易取消
                case HandlerConstant.SET_ORDER_CANCLE_SUCCESS:
                      HttpBaseBean httpBaseBean= (HttpBaseBean) msg.obj;
                      if(null==httpBaseBean){
                         return;
                     }
                     if(!httpBaseBean.isSussess()){
                         return;

                     }
                     if(msg.what==HandlerConstant.SET_ORDER_COMPLETE_SUCCESS){
                       mActivity.sendBroadcast(new Intent(ACTION_GOODS_COMPLETE_SUCCESS));
                     }
                     if(msg.what==HandlerConstant.SET_ORDER_CANCLE_SUCCESS){
                         mActivity.sendBroadcast(new Intent(ACTION_GOODS_CANCEL_SUCCESS));
                     }
                     break;
                case HandlerConstant.REQUST_ERROR:
                    showMsg(getString(R.string.http_error));
                    break ;
                default:
                    break;
            }
        }
    };


    /**
     * 解析并刷新数据
     */
    List<GoodsBean> list;
    private void refresh(String message){
        list= JsonUtils.getGoods2(message);
        setDataByIndex(0);
        if(null==orderAdapter){
            setDataByIndex(2);
            listView.setAdapter(orderAdapter);
        }else{
            orderAdapter.notifyDataSetChanged();
        }
        orderAdapter.setCallBack(tradingPlay);
        if(list.size()<20){
            isTotal=true;
            swipeLayout.setFooter(isTotal);
        }
    }


    private void setDataByIndex(int type){
        switch (index){
            case 0:
                if(type==0){
                    listBeanAll.addAll(list);
                }else if(type==1){
                    listBeanAll.clear();
                }else if(type==2){
                    orderAdapter=new OrderAdapter(mActivity,listBeanAll);
                }
                break;
            case 1:
                if(type==0){
                    listWait.addAll(list);
                }else if(type==1){
                    listWait.clear();
                }else if(type==2){
                    orderAdapter=new OrderAdapter(mActivity,listWait);
                }
                break;
            case 2:
                if(type==0){
                    listComplete.addAll(list);
                }else if(type==1){
                    listComplete.clear();
                }else if(type==2){
                    orderAdapter=new OrderAdapter(mActivity,listComplete);
                }
                break;
            case 3:
                if(type==0){
                    listCancle.addAll(list);
                }else if(type==1){
                    listCancle.clear();
                }else if(type==2){
                    orderAdapter=new OrderAdapter(mActivity,listCancle);
                }
                break;
            default:
                break;
        }
    }

    private TradingPlay tradingPlay=new TradingPlay() {
        /**
         * 交易完成或者重新购买
         * @param goodsBean
         */
        public void complete(final GoodsBean goodsBean) {
            if(null==goodsBean){
                return;
            }
            OrderFragment.this.goodsBean=goodsBean;
            if(goodsBean.getStated()==1){
                dialogView = new DialogView(dialogView, mActivity, "确定完成交易吗？",
                        "确定", "取消", new View.OnClickListener() {
                    public void onClick(View v) {
                        dialogView.dismiss();
                        showProgress("设置中...");
                        HttpMethod.setOrderComplete(goodsBean.getOrderId(),mHandler);
                    }
                }, null);
                dialogView.show();
            }else if(goodsBean.getStated()==4){
                Intent intent=new Intent(mActivity, BuyGoodsActivity.class);
                Bundle bundle=new Bundle();
                bundle.putSerializable("goodsBean",goodsBean);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        }

        /**
         * 交易取消
         * @param goodsBean
         */
        public void cancle(final GoodsBean goodsBean) {
            if(null==goodsBean){
                return;
            }
            OrderFragment.this.goodsBean=goodsBean;
            dialogView = new DialogView(dialogView, mActivity, "确定取消交易吗？",
                    "确定", "取消", new View.OnClickListener() {
                public void onClick(View v) {
                    dialogView.dismiss();
                    showProgress("设置中...");
                    HttpMethod.setOrderCancle(goodsBean.getOrderId(),mHandler);
                }
            }, null);
            dialogView.show();
        }

        /**
         * 联系卖家
         * @param phone
         */
        public void playPhon(String phone) {

        }
    };


    /**
     * 注册广播
     */
    private void registerReceiver() {
        IntentFilter myIntentFilter = new IntentFilter();
        myIntentFilter.addAction(BuyGoodsActivity.ACTION_GOODS_PAYSUCCESS);
        myIntentFilter.addAction(ACTION_GOODS_COMPLETE_SUCCESS);
        myIntentFilter.addAction(ACTION_GOODS_CANCEL_SUCCESS);
        // 注册广播监听
        mActivity.registerReceiver(mBroadcastReceiver, myIntentFilter);
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action){
                //商品购买成功后的广播
                case BuyGoodsActivity.ACTION_GOODS_PAYSUCCESS:
                     final String goodId=intent.getStringExtra("goodId");
                     if(TextUtils.isEmpty(goodId)){
                        return;
                     }
                     //删除已取消集合该商品对象
                     for(int i=0,len=listCancle.size();i<len;i++){
                        if(listCancle.get(i).getId().equals(goodId)){
                            listCancle.remove(i);
                            break;
                        }
                     }
                    //修改全部订单集合中该商品的状态
                    for(int i=0,len=listBeanAll.size();i<len;i++){
                        if(listBeanAll.get(i).getId().equals(goodId)){
                            listBeanAll.get(i).setStated(1);
                            break;
                        }
                    }
                    if(null!=orderAdapter){
                        orderAdapter.notifyDataSetChanged();
                    }
                    break;
                //交易完成
                case ACTION_GOODS_COMPLETE_SUCCESS:
                     setList(1);
                     break;
                //交易取消
                case ACTION_GOODS_CANCEL_SUCCESS:
                     setList(2);
                     break;
                    default:
                        break;
            }
        }
    };


    /**
     * 交易完成或取消后更新列表
     */
    private void setList(int type){
        for(int i=0;i<listWait.size();i++){
            if(goodsBean.getOrderId().equals(listWait.get(i).getOrderId())){
                listWait.remove(i);
                break;
            }
        }
        if(type==1){
            if(listComplete.size()>0){
                listComplete.add(0,goodsBean);
            }
        }
        if(type==2){
            if(listCancle.size()>0){
                listCancle.add(0,goodsBean);
            }
        }
        if(null!=orderAdapter){
            orderAdapter.notifyDataSetChanged();
        }
    }


    /**
     * 下拉刷新
     */
    public void onRefresh() {
        swipeLayout.postDelayed(new Runnable() {
            public void run() {
                page=1;
                isTotal=false;
                swipeLayout.setFooter(isTotal);
                getData(HandlerConstant.GET_PAY_ORDER_SUCCESS);
            }
        }, 200);
    }


    /**
     * 上啦加载更多
     */
    public void onLoad() {
        if(isTotal){
            swipeLayout.setLoading(false);
            return;
        }
        swipeLayout.postDelayed(new Runnable() {
            public void run() {
                page++;
                getData(HandlerConstant.GET_PAY_ORDER_SUCCESS2);
            }
        }, 200);

    }


    /**
     * 查询订单列表
     */
    private void getOrderList(){
        index=OrderActivity.index;
        if(isVisibleToUser && view!=null && listBeanAll.size()==0){
            if(index==0 && listBeanAll.size()>0){
                return;

            }else if(index==1 && listWait.size()>0){
                return;

            }else if(index==2 && listComplete.size()>0){
                return;

            }else if(index==3 && listCancle.size()>0){
                return;
            }
            swipeLayout.postDelayed(new Runnable() {
                public void run() {
                    listView.addHeaderView(new View(mActivity));
                    getData(HandlerConstant.GET_PAY_ORDER_SUCCESS);
                }
            }, 0);
        }
    }

    private void getData(int handlerIndex){
        HttpMethod.getPayOrderList(keyList.get(index),page,handlerIndex,mHandler);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        this.isVisibleToUser=isVisibleToUser;
        //查询订单列表
        getOrderList();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mBroadcastReceiver);
    }
}
