package com.open.im.adapter;

import android.content.Context;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.open.im.R;
import com.open.im.app.MyApp;
import com.open.im.bean.MessageBean;
import com.open.im.db.ChatDao;
import com.open.im.utils.MyLog;

import java.util.List;

public class SwipeAdapter extends BaseAdapter {

    public SwipeAdapter(Context ctx, List<MessageBean> data, int rightWidth) {
        this.ctx = ctx;
        this.data = data;
        mRightWidth = rightWidth;
        dao = ChatDao.getInstance(ctx);
    }

    /**
     * 上下文对象
     */
    private Context ctx;
    private List<MessageBean> data;

    private int mRightWidth = 0;

    static class ViewHolder {
        RelativeLayout item_left;
        RelativeLayout item_right;

        TextView tv_title;
        TextView tv_msg;
        TextView tv_time;
        ImageView iv_icon;

        TextView item_right_txt;
        TextView tv_unread;
    }

    /**
     * 单击事件监听器
     */
    private onRightItemClickListener mListener = null;
    private ChatDao dao;

    public void setOnRightItemClickListener(onRightItemClickListener listener) {
        mListener = listener;
    }

    public interface onRightItemClickListener {
        void onRightItemClick(View v, int position);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view;
        ViewHolder vh;
        if (convertView == null) {
            vh = new ViewHolder();
            view = View.inflate(ctx, R.layout.list_item_news, null);
            vh.item_left = (RelativeLayout) view.findViewById(R.id.item_left);
            vh.item_right = (RelativeLayout) view.findViewById(R.id.item_right);

            vh.iv_icon = (ImageView) view.findViewById(R.id.iv_icon);
            vh.tv_title = (TextView) view.findViewById(R.id.tv_title);
            vh.tv_msg = (TextView) view.findViewById(R.id.tv_msg);
            vh.tv_time = (TextView) view.findViewById(R.id.tv_time);
            vh.tv_unread = (TextView) view.findViewById(R.id.tv_unread_num);

            vh.item_right_txt = (TextView) view.findViewById(R.id.item_right_txt);
            view.setTag(vh);
        } else {
            view = convertView;
            vh = (ViewHolder) view.getTag();
        }
        LinearLayout.LayoutParams lp1 = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        vh.item_left.setLayoutParams(lp1);
        LinearLayout.LayoutParams lp2 = new LayoutParams(mRightWidth, LayoutParams.MATCH_PARENT);
        vh.item_right.setLayoutParams(lp2);

        MessageBean bean = data.get(position);

        String msgBody = bean.getMsgBody().trim();
        int msgType = bean.getType();
        String msgTitle;
        String msgFrom = bean.getFromUser().trim();
        String msgTo = bean.getToUser().trim();
        if (msgFrom.equals(MyApp.username)) {
            msgTitle = msgTo;
        } else {
            msgTitle = msgFrom;
        }
        // 显示时间 如果是今天 则只显示时间
        // 如果不是今天 则显示日期
        Long msgDateLong = bean.getMsgDateLong();

        vh.tv_title.setText(msgTitle);
        if (msgType == 0) {
            vh.tv_msg.setText(msgBody);
        } else if (msgType == 1){
            vh.tv_msg.setText("【图片】");
        }else if (msgType == 2){
            vh.tv_msg.setText("【语音】");
        }else if (msgType == 3){
            vh.tv_msg.setText("【位置】");
        }
        String msgDate;
        if (DateUtils.isToday(msgDateLong)) { // 判断是否是今天
            msgDate = DateFormat.getTimeFormat(ctx).format(msgDateLong);
        } else {
            msgDate = DateFormat.getDateFormat(ctx).format(msgDateLong);
        }
        vh.tv_time.setText(msgDate);

        vh.item_right.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onRightItemClick(v, position);
                }
            }
        });
        // 查询此条目的未读消息个数 并显示
        int unreadMsgCount = dao.queryUnreadMsgCount(bean.getMsgMark());
        if (unreadMsgCount == 0) {
            vh.tv_unread.setVisibility(View.GONE);
        } else {
            vh.tv_unread.setVisibility(View.VISIBLE);
            vh.tv_unread.setText(unreadMsgCount + "");
        }
        MyLog.showLog(bean.getFromUser() + "::" + unreadMsgCount);
        return view;
    }
}
