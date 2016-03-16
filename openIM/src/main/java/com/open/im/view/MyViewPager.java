package com.open.im.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * viewpager懒加载
 * 
 * @author Administrator
 * 
 */
public class MyViewPager extends LazyViewPager {

	public MyViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MyViewPager(Context context) {
		super(context);
	}

	@Override
	/**
	 * 不处理事件
	 */
	public boolean onTouchEvent(MotionEvent event) {
		return false;
	}

	@Override
	/**
	 * 不中断事件
	 */
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		return false;
	}

	@Override
	/**
	 * false表示切换viewPager条目不需要时间
	 * 这个方法里的super调用下面那个方法，并把第二个参数置为false
	 */
	public void setCurrentItem(int item) {
		super.setCurrentItem(item, false);
	}

	@Override
	public void setCurrentItem(int item, boolean smoothScroll) {
		super.setCurrentItem(item, smoothScroll);
	}

}
