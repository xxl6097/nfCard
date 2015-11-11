package com.sinpo.xnfc.adpter;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.sinpo.xnfc.R;
import com.sinpo.xnfc.model.TagModel;

import java.util.List;

/**
 * Created by uuxia-mac on 15/10/26.
 */
public class TagAdpter extends CommonAdapter<TagModel> {
    public TagAdpter(Context context, List<TagModel> mDatas, int itemLayoutId) {
        super(context, mDatas, itemLayoutId);
    }

    @Override
    public void convert(MyViewHolder helper, TagModel item, int pos) {
        helper.setText(R.id.name, item.getName());
        helper.setText(R.id.tag, item.toStrings());
    }
}
