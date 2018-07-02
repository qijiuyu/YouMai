package com.youmai.project.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.youmai.project.R;
import com.youmai.project.activity.main.BuyGoodsActivity;
import com.youmai.project.bean.MainBean;
import com.youmai.project.utils.Util;
import com.youmai.project.view.ClickTextView;

import java.util.List;

public class RecommendedAdapter extends BaseAdapter{

	private Context context;
	private List<MainBean> list;
	private MainBean mainBean;
	public RecommendedAdapter(Context context,List<MainBean> list) {
		super();
		this.context = context;
		this.list=list;
	}

	@Override
	public int getCount() {
		return list==null ? 0 : list.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		ViewHolder holder = null;
		if(view==null){
			holder = new ViewHolder(); 
			view = LayoutInflater.from(context).inflate(R.layout.recommended_item, null);
			holder.tvContext=(TextView)view.findViewById(R.id.tv_ri_content);
            holder.tvOldMoney=(TextView)view.findViewById(R.id.tv_ri_oldMoney);
			holder.tvLocation=(TextView)view.findViewById(R.id.tv_ri_location);
			holder.tvNewMoney=(TextView)view.findViewById(R.id.tv_ri_newMoney);
			holder.imgIcon=(ImageView)view.findViewById(R.id.img_ri_icon);
			holder.tvBuy=(ClickTextView)view.findViewById(R.id.tv_ri_buy);
			view.setTag(holder);
		}else{
			holder=(ViewHolder)view.getTag();
		}
		mainBean=list.get(position);
		if(mainBean!=null){
			holder.tvContext.setText(mainBean.getDescription());
			holder.tvLocation.setText(mainBean.getAddress());
			holder.tvNewMoney.setText("现价：¥"+ Util.setDouble(mainBean.getPresentPrice()/100));
			holder.tvOldMoney.setText("原价：¥"+Util.setDouble(mainBean.getOriginalPrice()/100));
			holder.tvOldMoney.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
			if(null!=mainBean.getImgList() && mainBean.getImgList().size()>0){
				Glide.with(context).load(mainBean.getImgList().get(0)).error(R.mipmap.icon).into(holder.imgIcon);
			}
			holder.tvBuy.setTag(mainBean);
			holder.tvBuy.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					if(v.getTag()!=null){
						MainBean mainBean1= (MainBean) v.getTag();
						if(mainBean1!=null){
							Intent intent=new Intent(context, BuyGoodsActivity.class);
							Bundle bundle=new Bundle();
							bundle.putSerializable("MainBean",mainBean1);
							intent.putExtras(bundle);
							context.startActivity(intent);
						}
					}
				}
			});
		}
		return view;
	}
	
	private class ViewHolder{
		private TextView tvContext,tvLocation,tvNewMoney,tvOldMoney;
		private ClickTextView tvBuy;
		private ImageView imgIcon;
	 }
}