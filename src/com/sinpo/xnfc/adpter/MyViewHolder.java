package com.sinpo.xnfc.adpter;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class MyViewHolder {
	private final SparseArray<View> mViews;
	private int mPosition;
	private View mConvertView;

	private MyViewHolder(Context context, ViewGroup parent, int layoutId,
			int position) {
		this.mPosition = position;
		this.mViews = new SparseArray<View>();
		mConvertView = LayoutInflater.from(context).inflate(layoutId, parent,
				false);
		mConvertView.setTag(this);
	}

	/**
	 * 拿到一个MyViewHolder对象
	 * 
	 * @param context
	 * @param convertView
	 * @param parent
	 * @param layoutId
	 * @param position
	 * @return
	 */
	public static MyViewHolder get(Context context, View convertView,
			ViewGroup parent, int layoutId, int position) {
		MyViewHolder holder = null;
		if (convertView == null) {
			holder = new MyViewHolder(context, parent, layoutId, position);
		} else {
			holder = (MyViewHolder) convertView.getTag();
			holder.mPosition = position;
		}
		return holder;
	}

	public View getConvertView() {
		return mConvertView;
	}

	/**
	 * 通过控件的Id获取对于的控件，如果没有则加入views
	 * 
	 * @param viewId
	 * @return
	 */
	public <T extends View> T getView(int viewId) {
		View view = mViews.get(viewId);
		if (view == null) {
			view = mConvertView.findViewById(viewId);
			mViews.put(viewId, view);
		}
		return (T) view;
	}

	/**
	 * 为TextView设置字符串
	 * 
	 * @param viewId
	 * @param text
	 * @return
	 */
	public MyViewHolder setText(int viewId, String text) {
		TextView view = getView(viewId);
		view.setText(text);
		return this;
	}

	public MyViewHolder setEidtText(int viewId, String text) {
		EditText view = getView(viewId);
		view.setText(text);
		return this;
	}


	/**
	 * 为ImageView设置图片
	 * 
	 * @param viewId
	 * @param drawableId
	 * @return
	 */
	public MyViewHolder setImageResource(int viewId, int drawableId) {
		ImageView view = getView(viewId);
		view.setImageResource(drawableId);
		return this;
	}

	/**
	 * 为ImageView设置图片
	 * 
	 * @param viewId
	 * @return
	 */
	public MyViewHolder setImageBitmap(int viewId, Bitmap bm) {
		ImageView view = getView(viewId);
		view.setImageBitmap(bm);
		return this;
	}


	public int getPosition() {
		return mPosition;
	}
}
