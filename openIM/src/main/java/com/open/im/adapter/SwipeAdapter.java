package com.open.im.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.text.SpannableStringBuilder;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.open.im.R;
import com.open.im.app.MyApp;
import com.open.im.db.DBcolumns;
import com.open.im.utils.MyString2SpannableStringBufferUtils;

public class SwipeAdapter extends CursorAdapter {

	public SwipeAdapter(Context context, Cursor c, int rightWidth) {
		super(context, c, rightWidth);
		ctx = context;
		mRightWidth = rightWidth;
	}

	/**
	 * 上下文对象
	 */
	private Context ctx;

	private int mRightWidth = 0;


	static class ViewHolder {
		RelativeLayout item_left;
		RelativeLayout item_right;

		TextView tv_title;
		TextView tv_msg;
		TextView tv_time;
		ImageView iv_icon;

		TextView item_right_txt;
	}

	/**
	 * 单击事件监听器
	 */
	private onRightItemClickListener mListener = null;

	public void setOnRightItemClickListener(onRightItemClickListener listener) {
		mListener = listener;
	}

	public interface onRightItemClickListener {
		void onRightItemClick(View v, int position);
	}

	@Override
	protected void onContentChanged() {
		super.onContentChanged();
//		MyLog.showLog("消息列表界面发生变化");
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		ViewHolder vh = new ViewHolder();
		View view = View.inflate(ctx, R.layout.list_item_news, null);
		vh.item_left = (RelativeLayout) view.findViewById(R.id.item_left);
		vh.item_right = (RelativeLayout) view.findViewById(R.id.item_right);

		vh.iv_icon = (ImageView) view.findViewById(R.id.iv_icon);
		vh.tv_title = (TextView) view.findViewById(R.id.tv_title);
		vh.tv_msg = (TextView) view.findViewById(R.id.tv_msg);
		vh.tv_time = (TextView) view.findViewById(R.id.tv_time);

		vh.item_right_txt = (TextView) view.findViewById(R.id.item_right_txt);
		view.setTag(vh);
//		MyLog.showLog("消息列表界面发生变化1");
		return view;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		final int position = cursor.getPosition();
		ViewHolder vh = (ViewHolder) view.getTag();
		LinearLayout.LayoutParams lp1 = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		vh.item_left.setLayoutParams(lp1);
		LinearLayout.LayoutParams lp2 = new LayoutParams(mRightWidth, LayoutParams.MATCH_PARENT);
		vh.item_right.setLayoutParams(lp2);

		String msgBody = cursor.getString(cursor.getColumnIndex(DBcolumns.MSG_BODY)).trim();
		String msgTitle = "";
		String msgFrom = cursor.getString(cursor.getColumnIndex(DBcolumns.MSG_FROM));
		String msgTo = cursor.getString(cursor.getColumnIndex(DBcolumns.MSG_TO));
		if (msgFrom.equals(MyApp.username)) {
			msgTitle = msgTo;
		} else {
			msgTitle = msgFrom;
		}
		// 显示时间 如果是今天 则只显示时间
		// 如果不是今天 则显示日期
		Long msgDateLong = cursor.getLong(cursor.getColumnIndex(DBcolumns.MSG_DATE));

		vh.tv_title.setText(msgTitle);
		SpannableStringBuilder msg = MyString2SpannableStringBufferUtils.handler(ctx, vh.tv_msg, msgBody);
		vh.tv_msg.setText(msg);
		String msgDate;
		if (DateUtils.isToday(msgDateLong)) { // 判断是否是今天
			msgDate = DateFormat.getTimeFormat(ctx).format(msgDateLong);
		} else {
			msgDate = DateFormat.getDateFormat(ctx).format(msgDateLong);
		}
		vh.tv_time.setText(msgDate);

		vh.iv_icon.setImageResource(R.drawable.ic_launcher);

		vh.item_right.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mListener != null) {
					mListener.onRightItemClick(v, position);
				}
			}
		});
//		MyLog.showLog("消息列表界面发生变化2");
	}
}
