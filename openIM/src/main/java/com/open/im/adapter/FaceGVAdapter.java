package com.open.im.adapter;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.open.im.R;
/**
 * 表情GridView 的 adapter
 * @author Administrator
 *
 */
public class FaceGVAdapter extends BaseAdapter {
	private List<String> list;
	private Context ctx;

	public FaceGVAdapter(List<String> list, Context ctx) {
		super();
		this.list = list;
		this.ctx = ctx;
	}

	public void clear() {
		this.ctx = null;
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHodler hodler;
		if (convertView == null) {
			hodler = new ViewHodler();
			convertView = LayoutInflater.from(ctx).inflate(R.layout.grid_face_item, null);
			hodler.iv = (ImageView) convertView.findViewById(R.id.iv_face);
			hodler.tv = (TextView) convertView.findViewById(R.id.tv_face);
			convertView.setTag(hodler);
		} else {
			hodler = (ViewHodler) convertView.getTag();
		}
		try {
			Bitmap mBitmap = BitmapFactory.decodeStream(ctx.getAssets().open("face/png/" + list.get(position)));
			hodler.iv.setImageBitmap(mBitmap);
		} catch (IOException e) {
			e.printStackTrace();
		}
		hodler.tv.setText("face/png/" + list.get(position));

		return convertView;
	}

	class ViewHodler {
		ImageView iv;
		TextView tv;
	}
}
