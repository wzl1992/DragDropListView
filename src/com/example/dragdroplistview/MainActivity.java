package com.example.dragdroplistview;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.dragdroplistview.DragUITools.OnDragListener;

/**
 * ListView拖动排序
 * 
 * @author wanghaifei
 * 
 */
public class MainActivity extends Activity {
	String TAG = "MainActivity";
	private ListView lvDrag;
	private List<Info> data;
	private DragListAdapter mAdapter;

	/**
	 * 长按拖动的时候，用来显示被拖拽的项目的图片
	 */
	private ImageView ivDrag;
	/**
	 * 拖动监听器,在拖动的时候监听y并移动ImageView的位置
	 */
	private DragOnTouchListenerHolder dragOnTouchListener;
	/**
	 * 最后一次移动到的位置，松开手以后将dragPosition位置的项移动到此位置
	 */
	private int lastDropPosition = -1;
	/**
	 * 长按需要排序的位置
	 */
	private int dragPosition = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		lvDrag = (ListView) findViewById(R.id.lv_drag);
		ivDrag = (ImageView) findViewById(R.id.iv_drag);
		ivDrag.setDrawingCacheEnabled(true);

		dragOnTouchListener = DragUITools.setOnDragListener(lvDrag, new OnDragListener() {
			int lastPos = -1;

			@Override
			public void onDrag(View view, MotionEvent event, DragEvent action, int touchSlop) {
				if (action == DragEvent.ACTION_DRAG) {
					// Log.i(TAG, "move="+action);
					int y = (int) event.getY();
					Point parentP = DragUITools.getPos((View) lvDrag.getParent());
					int touchTop = y - parentP.y;
					int top = touchTop - ivDrag.getMeasuredHeight();
					ivDrag.layout(0, top, ivDrag.getMeasuredWidth(), top + ivDrag.getMeasuredHeight());
					// 获取图片当前位置的x
					int targetX = parentP.x + ivDrag.getMeasuredWidth() / 2;
					int targetY = top + ivDrag.getMeasuredHeight() / 2;
					int tarPos = lvDrag.pointToPosition(targetX, targetY);
					if (tarPos >= 0)
						if (lastPos != tarPos) {
							// Log.i(TAG, "当前pos="+tarPos);
							View tarView = DragUITools.getViewByPosition(tarPos, lvDrag);
							lastDropPosition = tarPos;
							if (tarPos > lastPos) {
								// 向后滚动一个item
								tarView.removeCallbacks(scrollRunnable);
								scrollRunnable.setDistance(tarView.getMeasuredHeight());
								tarView.postDelayed(scrollRunnable, 700);
							} else {
								// 向前滚动一个item
								tarView.removeCallbacks(scrollRunnable);
								scrollRunnable.setDistance(-tarView.getMeasuredHeight());
								tarView.postDelayed(scrollRunnable, 700);
							}
							lastPos = tarPos;
						}
				}
				if (action == DragEvent.ACTION_END) {
					 Log.i(TAG, "拖动结束 ！！！");
					if (lastDropPosition != -1) {
						Log.i(TAG, "要移动的位置=" + dragPosition + " 目标位置:" + lastDropPosition);
						Info dataToRemove = data.remove(dragPosition);
						
						Log.d(TAG, "dragPosition-------->"+dragPosition);
						Log.d(TAG, "dataToRemove name--->"+dataToRemove.getName());
						Log.d(TAG, "lastDropPosition---->"+lastDropPosition);
						
						data.add(lastDropPosition, dataToRemove);
						((BaseAdapter) lvDrag.getAdapter()).notifyDataSetChanged();
					}
					// 恢复
					dragPosition = -1;
					lastDropPosition = -1;
					ivDrag.setVisibility(View.INVISIBLE);
					dragOnTouchListener.dragOnTouchListener.setEnable(false);
				}
				
				for(int i=0;i<data.size();i++){
					Log.e(TAG, "name-->"+data.get(i).getName());
				}
			}
		}, false, false);

		data = new ArrayList<Info>();
		for (int i = 0; i < 10; i++) {
			Info info = new Info();
			info.setName("问题"+(i+1));
			
			data.add(info);
		}
//		data = new ArrayList<String>();
//		for (int i = 0; i < 10; i++) {
//			data.add("问题"+i);
//		}
		mAdapter = new DragListAdapter(getApplicationContext(), data);
		lvDrag.setAdapter(mAdapter);

		// 长按ListView开始拖动
		lvDrag.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				View itemView = DragUITools.getViewByPosition(position, lvDrag);
				lastDropPosition = position;
				dragPosition = position;
				itemView.setDrawingCacheEnabled(true);
				Bitmap b = itemView.getDrawingCache();
				ivDrag.getLayoutParams().width = b.getWidth();
				ivDrag.getLayoutParams().height = b.getHeight();
				// 透明度
				ivDrag.setAlpha(175);
				ivDrag.setImageBitmap(b);
				ivDrag.post(new Runnable() {
					@Override
					public void run() {
						Point p = DragUITools.getPos(DragUITools.getViewByPosition(lastDropPosition, lvDrag));
						Point pParent = DragUITools.getPos((View) lvDrag.getParent());
						// TODO
						int top = p.y - pParent.y /*- ivDrag.getLayoutParams().height / 2*/;
						ivDrag.layout(0, top, ivDrag.getMeasuredWidth(), top + ivDrag.getMeasuredHeight());
						ivDrag.setVisibility(View.VISIBLE);
						Log.i(TAG, "长按postion=" + lastDropPosition);
						dragOnTouchListener.dragOnTouchListener.setEnable(true);
					}
				});
				return true;
			}

		});
	}

	private ScrollRunnable scrollRunnable = new ScrollRunnable();

	private class ScrollRunnable implements Runnable {
		private int distance = 0;

		public void setDistance(int distance) {
			this.distance = distance;
		}

		@Override
		public void run() {
			lvDrag.smoothScrollBy(distance, 300);
		}
	}
	
	
	public class DragListAdapter extends BaseAdapter{
		private Context context;
		private List<Info> list;
		public DragListAdapter(Context context,List<Info> list) {
			this.list = list;
			this.context = context;
		}
		
		//利用模板布局不同的listview
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			view = LayoutInflater.from(context).inflate(R.layout.textandimage, null);
			TextView textView = (TextView) view.findViewById(R.id.headtext);
			textView.setText(list.get(position).getName());
			
			TextView btn = (TextView) view.findViewById(R.id.imageView1);
			btn.setText(position + 1 +"");
			return view;
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object getItem(int arg0) {
			return list.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}
	}
	
	class Info{
		private String name;
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
	}
}
