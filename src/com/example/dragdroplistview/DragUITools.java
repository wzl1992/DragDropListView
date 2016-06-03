package com.example.dragdroplistview;

import android.graphics.Point;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;

public class DragUITools {
	/**
	 * 获取View在屏幕上的位置
	 * 
	 * @param view
	 * @return
	 */
	public static Point getPos(View view) {
		int[] pos = new int[2];
		view.getLocationOnScreen(pos);
		return new Point(pos[0], pos[1]);
	}

	public interface OnDragListener {
		public enum DragEvent {
			/**
			 * 按下开始拖动
			 */
			ACTION_START,
			/**
			 * 拖动
			 */
			ACTION_DRAG,
			/**
			 * 弹起结束拖动
			 */
			ACTION_END
		}

		public void onDrag(View view, MotionEvent event, DragEvent action, int touchSlop);
	}

	/**
	 * 设置拖动事件监听器
	 * 
	 * @param view
	 * @param onDragListener
	 *            拖动事件回调函数
	 * @param dispatchTouchEvent
	 *            是否向下传递事件
	 * @param enable
	 *            监听器是否可用
	 * @return
	 */
	public static DragOnTouchListenerHolder setOnDragListener(final View view, final OnDragListener onDragListener, final boolean dispatchTouchEvent, final boolean enable) {
		final DragOnTouchListenerHolder dragOnTouchListenerHolder = new DragOnTouchListenerHolder();
		DragOnTouchListener d = new DragOnTouchListener(view, onDragListener, dispatchTouchEvent);
		dragOnTouchListenerHolder.dragOnTouchListener = d;
		d.setEnable(enable);
		view.setOnTouchListener(d);
		if (!(view instanceof ListView))
			view.setOnClickListener(null);
		return dragOnTouchListenerHolder;
	}

	public static View getViewByPosition(int position, ListView listView) {
		final int firstListItemPosition = listView.getFirstVisiblePosition();
		final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

		if (position < firstListItemPosition || position > lastListItemPosition) {
			return listView.getAdapter().getView(position, listView.getChildAt(position), listView);
		} else {
			final int childIndex = position - firstListItemPosition;
			return listView.getChildAt(childIndex);
		}
	}
}
