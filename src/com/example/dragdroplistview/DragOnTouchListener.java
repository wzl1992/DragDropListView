package com.example.dragdroplistview;

import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.example.dragdroplistview.DragUITools.OnDragListener;

/**
 * 监听拖动事件
 * 
 * @author planet
 * 
 */
public class DragOnTouchListener implements OnTouchListener {
	private OnDragListener onDragListener;
	private View view;
	private boolean dispatchTouchEvent;

	public DragOnTouchListener(View view, OnDragListener onDragListener, boolean dispatchTouchEvent) {
		this.onDragListener = onDragListener;
		this.view = view;
		this.dispatchTouchEvent = dispatchTouchEvent;
	}

	private int mTouchSlop = 5;
	private boolean mScolling = false;// 手指拖拽状态
	private PointF mLastPoint = null;
	private int currentSlop = 0;
	private boolean enable = false;

	public void setDispatchTouchEvent(boolean dispatchTouchEvent) {
		this.dispatchTouchEvent = dispatchTouchEvent;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	@Override
	public boolean onTouch(View v, final MotionEvent ev) {
		if (!enable)
			return false;
		float eventFloatY = ev.getY();
		if (ev.getAction() == MotionEvent.ACTION_DOWN) {// 记录第一次触摸Y
			mLastPoint = new PointF(ev.getX(), ev.getY());
			// Log.i("", "按下:"+ev.getY()+","+ev.getX());
		}
		if (ev.getAction() == MotionEvent.ACTION_MOVE) {
			if (mLastPoint == null) {
				mLastPoint = new PointF(ev.getX(), ev.getY());
				// Log.i("", "按下:"+ev.getY()+","+ev.getX());
			}
			if (!mScolling) {
				// 计算移动的Y和第一次触摸Y，大于touchSlop开始移动 enablePullDown=true且slop>0时允许下拉
				// enablePullUp=true且slop<0时允许上拉
				float slop = eventFloatY - mLastPoint.y;
				currentSlop = (int) slop;
				// Log.i("", "ACTION_MOVE x="+ev.getX()+", y="+ev.getY());
				if (Math.abs(slop) >= mTouchSlop) {
					// 计算两个坐标x/y的宽高比 如果x/y>1即x大于y，说明是横向滑动，不应该允许拖动
					int dx = Math.abs((int) (mLastPoint.x - ev.getX()));
					int dy = Math.abs((int) (mLastPoint.y - ev.getY()));
					// Log.i("", "dx="+dx+",dy="+dy);
					if (dx < dy) {// 开始scroll
						mScolling = true;
						onDragListener.onDrag(view, ev, OnDragListener.DragEvent.ACTION_START, currentSlop);
					}
				}
			} else {
				view.post(new Runnable() {
					@Override
					public void run() {
						onDragListener.onDrag(view, ev, OnDragListener.DragEvent.ACTION_DRAG, currentSlop);
					}
				});
			}
		} else if (ev.getAction() == MotionEvent.ACTION_CANCEL || ev.getAction() == MotionEvent.ACTION_UP) {
			// TODO 
			mScolling = true;
			// 手指离开屏幕
			if (mScolling) {
				mScolling = false;
				onDragListener.onDrag(view, ev, OnDragListener.DragEvent.ACTION_END, currentSlop);
				return true;
			}
			mLastPoint = null;
		}
		if (dispatchTouchEvent) {
			return false;
		} else {
			return true;
		}
	}
}