package com.open.im.adapter;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.AnimationDrawable;
import android.support.v4.widget.CursorAdapter;
import android.text.SpannableStringBuilder;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.open.im.R;
import com.open.im.app.MyApp;
import com.open.im.baidumap.BaiduMapActivity;
import com.open.im.db.DBcolumns;
import com.open.im.utils.MyBitmapUtils;
import com.open.im.utils.MyDateUtils;
import com.open.im.utils.MyFileUtils;
import com.open.im.utils.MyLog;
import com.open.im.utils.MyMediaPlayerUtils;
import com.open.im.utils.MyString2SpannableStringBufferUtils;
import com.open.im.view.ZoomImageView;

/**
 * cursorAdapter填充listView
 * 
 * @author Administrator
 * 
 */
public class MyLVAdapter extends CursorAdapter {

	private Context act;
	private MyBitmapUtils myBitmapUtils;
	private ListView listView;
	private Cursor cursor;
	private boolean isPlaying = false;
	private SimpleDateFormat sdf;
	private SimpleDateFormat sdf2;
	private SimpleDateFormat sdf3;

	public MyLVAdapter(Context context, Cursor cursor, ListView listView) {
		super(context, cursor);
		act = context;
		this.cursor = cursor;
		myBitmapUtils = new MyBitmapUtils(act);
		this.listView = listView;
		sdf = new SimpleDateFormat("yyyy年MM月dd日  HH:mm");
		sdf2 = new SimpleDateFormat("MM月dd日  HH:mm");
		sdf3 = new SimpleDateFormat(" HH:mm");
	}

	@Override
	/**
	 * 当关联的数据库发生变化时，会执行此方法
	 * 此方法的super不能删除 它是更新数据
	 */
	protected void onContentChanged() {
		super.onContentChanged();
		// 每次数据库发生变化时，都让listView显示在最后一行
		listView.setSelection(getCount());
	}

	@Override
	/**
	 * 创建新的view 当convertView为null时，会执行此方法创建新的view
	 */
	public View newView(Context context, Cursor cursor, ViewGroup parent) {

		ViewHolder vh = new ViewHolder();

		View view = View.inflate(act, R.layout.list_item_chat_detail, null);
		TextView tv_date = (TextView) view.findViewById(R.id.tv_date);
		LinearLayout ll_receive = (LinearLayout) view.findViewById(R.id.ll_receive);
		TextView tv_receive_body = (TextView) view.findViewById(R.id.tv_receive_body);
		ImageView iv_receive_image = (ImageView) view.findViewById(R.id.iv_receive_image);
		ImageView iv_receive_audio = (ImageView) view.findViewById(R.id.iv_receive_audio);
		ImageView iv_receive_location = (ImageView) view.findViewById(R.id.iv_receive_location);
		RelativeLayout rl_send = (RelativeLayout) view.findViewById(R.id.rl_send);
		TextView tv_send_body = (TextView) view.findViewById(R.id.tv_send_body);
		ImageView iv_send_image = (ImageView) view.findViewById(R.id.iv_send_image);
		ImageView iv_send_audio = (ImageView) view.findViewById(R.id.iv_send_audio);
		ImageView iv_send_location = (ImageView) view.findViewById(R.id.iv_send_location);

		vh.date = tv_date;
		vh.receive = ll_receive;
		vh.receiveBody = tv_receive_body;
		vh.receiveImage = iv_receive_image;
		vh.receiveAudio = iv_receive_audio;
		vh.receiveLocation = iv_receive_location;

		vh.send = rl_send;
		vh.sendBody = tv_send_body;
		vh.sendImage = iv_send_image;
		vh.sendAudio = iv_send_audio;
		vh.sendLocation = iv_send_location;

		view.setTag(vh);
		return view;
	}

	@Override
	/**
	 * 为view设置数据
	 */
	public void bindView(View view, Context context, Cursor cursor) {

		final int position = cursor.getPosition();
		MyLog.showLog("图片position::" + position);

		final ViewHolder vh = (ViewHolder) view.getTag();
		// cursor是前面查询数据后 返回的那个cursor 也是创建cursorAdapter时的参数
		// 通过cursor获取查询到的的cursor指向的表里的数据
		// int isComing =
		// cursor.getInt(cursor.getColumnIndex(DBcolumns.MSG_ISCOMING));
		final String msgBody = cursor.getString(cursor.getColumnIndex(DBcolumns.MSG_BODY)).trim();
		String msgImg = cursor.getString(cursor.getColumnIndex(DBcolumns.MSG_IMG)).trim();
		String msgFrom = cursor.getString(cursor.getColumnIndex(DBcolumns.MSG_FROM));
		final int msgType = cursor.getInt(cursor.getColumnIndex(DBcolumns.MSG_TYPE));
		// 显示时间 如果是今天 则只显示时间
		// 如果不是今天 则显示日期
		Long msgDateLong = cursor.getLong(cursor.getColumnIndex(DBcolumns.MSG_DATE));
		String msgDate;
		if (DateUtils.isToday(msgDateLong)) { // 判断是否是今天
			msgDate = sdf3.format(new Date(msgDateLong));
		} else if (MyDateUtils.isThisYear(msgDateLong)) {
			msgDate = sdf2.format(new Date(msgDateLong));
		} 
		else {
			msgDate = sdf.format(new Date(msgDateLong));
//			msgDate = DateFormat.getDateFormat(act).format(msgDateLong);
		}
		
		vh.date.setText(msgDate);
		// BitmapUtils bitmapUtils = new BitmapUtils(act);
		// 判断是收消息 还是发消息
		if (!msgFrom.equals(MyApp.username)) { // 0表示收到消息 1表示发出消息
			vh.send.setVisibility(View.GONE);
			vh.receive.setVisibility(View.VISIBLE);
			if (msgType == 1) { // 1表示图片
				// vh.receiveImage.setImageResource(R.drawable.kangyikun);
				vh.receiveImage.setTag(position);
				vh.receiveBody.setVisibility(View.GONE);
				vh.receiveAudio.setVisibility(View.GONE);
				vh.receiveLocation.setVisibility(View.GONE);
				// bitmapUtils.display(vh.receiveImage, msgBody);
				MyLog.showLog("收到图片地址::" + msgBody);

				if (msgImg != null) {
					String imgPath = msgImg.substring(msgImg.indexOf("h"));
					myBitmapUtils.display(vh.receiveImage, imgPath);
					MyLog.showLog("小图地址:" + imgPath);
				}
				vh.receiveImage.setVisibility(View.VISIBLE);
				vh.receiveImage.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (msgBody == null) {
							MyLog.showLog("路径为null");
						} else {
							showImgDialog(msgBody);
						}
					}
				});

			} else if (msgType == 2) { // 2表示语音
				vh.receiveBody.setVisibility(View.GONE);
				vh.receiveImage.setVisibility(View.GONE);
				vh.receiveLocation.setVisibility(View.GONE);
				vh.receiveAudio.setVisibility(View.VISIBLE);
				vh.receiveAudio.setImageResource(R.drawable.voice_from_icon);

				final AnimationDrawable an = (AnimationDrawable) vh.receiveAudio.getDrawable();
				// 设置动画初始状态
				an.stop();
				an.selectDrawable(2);
				final String audioPath = msgBody.substring(msgBody.indexOf("h"));
				MyLog.showLog("语音::" + msgBody); // 语音::audio:http://121.42.153.9/group1/M00/18/A4/i4GBYVbX3M-AfvENAAAZQIBGrpU231.jpg
				vh.receiveAudio.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						// 开始播放声音
						MyMediaPlayerUtils.play(act, audioPath, vh.receiveAudio);
					}
				});

			} else if (msgType == 3) { // 3表示位置
				vh.receiveLocation.setTag(position);
				vh.receiveBody.setVisibility(View.GONE);
				vh.receiveImage.setVisibility(View.GONE);
				vh.receiveAudio.setVisibility(View.GONE);
				vh.receiveLocation.setVisibility(View.VISIBLE);
				final String[] split = msgBody.split("#");
				String snapShotPath = split[5];
				MyLog.showLog("snapShotPath:" + snapShotPath);
				myBitmapUtils.display(vh.receiveLocation, snapShotPath);

				vh.receiveLocation.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						MyLog.showLog("--这是地图--");
						double latitude = Double.parseDouble(split[1]);
						double longitude = Double.parseDouble(split[2]);
						String locationAddress = split[3];
						MyLog.showLog("latitude:" + latitude);
						Intent intent = new Intent(act, BaiduMapActivity.class);
						intent.putExtra(BaiduMapActivity.LATITUDE, latitude);
						intent.putExtra(BaiduMapActivity.LONGITUDE, longitude);
						intent.putExtra(BaiduMapActivity.ADDRESS, locationAddress);
						act.startActivity(intent);
					}
				});

			} else {
				vh.receiveImage.setVisibility(View.GONE);
				vh.receiveAudio.setVisibility(View.GONE);
				vh.receiveLocation.setVisibility(View.GONE);
				vh.receiveBody.setVisibility(View.VISIBLE);
				SpannableStringBuilder msg = MyString2SpannableStringBufferUtils.handler(act, vh.receiveBody, msgBody);
				vh.receiveBody.setText(msg);
				MyLog.showLog("收到文本");
			}
		} else {
			// adapter定理 有if必有else 不然会乱跳
			vh.send.setVisibility(View.VISIBLE);
			vh.receive.setVisibility(View.GONE);
			if (msgType == 1) { // 1表示图片
				// vh.receiveImage.setImageResource(R.drawable.kangyikun);
				vh.sendImage.setTag(position);
				vh.sendBody.setVisibility(View.GONE);
				vh.sendAudio.setVisibility(View.GONE);
				vh.sendLocation.setVisibility(View.GONE);
				if (msgImg != null) {
					String imgPath = msgImg.substring(msgImg.indexOf("h"));
					myBitmapUtils.display(vh.sendImage, imgPath);
					MyLog.showLog("小图地址:" + imgPath);
				}
				vh.sendImage.setVisibility(View.VISIBLE);
				vh.sendImage.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						MyLog.showLog("这是图片:" + position);
						String picPath = msgBody.substring(msgBody.indexOf("h"));
						MyLog.showLog("大图地址:" + picPath);
						showImgDialog(picPath);
						// 显示大图Dialog
						// showActivity(picPath);
					}

				});

				// bitmapUtils.display(vh.sendImage, msgBody);
			} else if (msgType == 2) { // 2表示语音
				vh.sendBody.setVisibility(View.GONE);
				vh.sendAudio.setVisibility(View.VISIBLE);
				vh.sendImage.setVisibility(View.GONE);
				vh.sendLocation.setVisibility(View.GONE);
				vh.sendAudio.setImageResource(R.drawable.voice_to_icon);
				// vh.sendAudio.setBackgroundResource(R.drawable.voice_to_icon);

				final AnimationDrawable an = (AnimationDrawable) vh.sendAudio.getDrawable();
				// 设置动画初始状态
				an.stop();
				an.selectDrawable(2);
				final String audioPath = msgBody.substring(msgBody.indexOf("h"));
				MyLog.showLog("语音::" + msgBody); // 语音::audio:http://121.42.153.9/group1/M00/18/A4/i4GBYVbX3M-AfvENAAAZQIBGrpU231.jpg
				vh.sendAudio.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						// 开始播放声音
						MyMediaPlayerUtils.play(act, audioPath, vh.sendAudio);

						// if (an.isRunning()) {
						// an.stop();
						// an.selectDrawable(2);
						// } else {
						// an.start();
						// }
					}
				});

			} else if (msgType == 3) { // 3表示位置
				vh.sendLocation.setTag(position);
				vh.sendBody.setVisibility(View.GONE);
				vh.sendAudio.setVisibility(View.GONE);
				vh.sendImage.setVisibility(View.GONE);
				vh.sendLocation.setVisibility(View.VISIBLE);
				final String[] split = msgBody.split("#");
				String snapShotPath = split[5];
				MyLog.showLog("snapShotPath:" + snapShotPath);
				myBitmapUtils.display(vh.sendLocation, snapShotPath);

				vh.sendLocation.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						MyLog.showLog("msgType::" + msgType);
						MyLog.showLog("--这是地图--");
						double latitude = Double.parseDouble(split[1]);
						double longitude = Double.parseDouble(split[2]);
						String locationAddress = split[3];
						MyLog.showLog("latitude:" + latitude);
						Intent intent = new Intent(act, BaiduMapActivity.class);
						intent.putExtra(BaiduMapActivity.LATITUDE, latitude);
						intent.putExtra(BaiduMapActivity.LONGITUDE, longitude);
						intent.putExtra(BaiduMapActivity.ADDRESS, locationAddress);
						act.startActivity(intent);
					}
				});

			} else {
				vh.sendImage.setVisibility(View.GONE);
				vh.sendAudio.setVisibility(View.GONE);
				vh.sendLocation.setVisibility(View.GONE);
				vh.sendBody.setVisibility(View.VISIBLE);
				SpannableStringBuilder msg = MyString2SpannableStringBufferUtils.handler(act, vh.sendBody, msgBody);
				vh.sendBody.setText(msg);
				// vh.sendBody.setOnClickListener(new OnClickListener() {
				//
				// @Override
				// public void onClick(View v) {
				//
				// }
				// });
			}
		}
	}

	/**
	 * 使用viewHolder减少findviewbyid的次数 缩短显示条目的时间
	 * 
	 * @author Administrator
	 * 
	 */
	private class ViewHolder {

		public ImageView sendLocation;
		public ImageView receiveLocation;
		public ImageView sendAudio;
		public ImageView receiveAudio;
		public ImageView sendImage;
		public ImageView receiveImage;
		public TextView date;
		public TextView sendBody;
		public TextView receiveBody;
		public RelativeLayout send;
		public LinearLayout receive;

	}

	/**
	 * 方法 点击小图时，加载大图片 
	 * 
	 * @param picPath
	 */
	private void showImgDialog(final String picPath) {
		final AlertDialog dialog = new AlertDialog.Builder(act, R.style.Lam_Dialog_FullScreen).create();
		Window win = dialog.getWindow();
		win.setGravity(Gravity.FILL);
		// 隐藏手机最上面的状态栏
		win.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		dialog.show();

		View view = View.inflate(act, R.layout.dialog_big_image, null);
		ZoomImageView imgView = (ZoomImageView) view.findViewById(R.id.iv_image);

		imgView.setTag(-2);
		
		myBitmapUtils.display(imgView, picPath);
		MyFileUtils.scanFileToPhotoAlbum(act, picPath);
		imgView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		win.setContentView(view);
	}
}
