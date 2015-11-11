package com.sinpo.xnfc.adpter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

/**
 * 定义一个通用的Adapter适配器
 * **/
public abstract class CommonAdapter<T> extends BaseAdapter {
	// 加载布局
	protected LayoutInflater mInflater;
	// 上下文
	protected Context mContext;
	// 数据列表
	protected List<T> mDatas;
	// 加载Item布局ID
	protected final int mItemLayoutId;

	public CommonAdapter(Context context, List<T> mDatas, int itemLayoutId) {
		this.mContext = context;
		this.mInflater = LayoutInflater.from(mContext);
		this.mDatas = mDatas;
		this.mItemLayoutId = itemLayoutId;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mDatas.size();
	}

	@Override
	public T getItem(int position) {
		// TODO Auto-generated method stub
		return mDatas.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final MyViewHolder viewHolder = getViewHolder(position, convertView,
				parent);
		convert(viewHolder, getItem(position),position);
		return viewHolder.getConvertView();
	}

	private MyViewHolder getViewHolder(int position, View convertView,
			ViewGroup parent) {
		return MyViewHolder.get(mContext, convertView, parent, mItemLayoutId,
				position);
	}

	public abstract void convert(MyViewHolder helper, T item,int pos);
}
