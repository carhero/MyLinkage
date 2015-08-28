package com.libre.client.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;

public class MySlider extends SeekBar {
	private Drawable mThumb;
	private OnSliderClickListener mlistener;

	public MySlider(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public MySlider(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public void setOnSliderClickListener(MySlider.OnSliderClickListener onSliderClickListener) {
		mlistener = onSliderClickListener;
	}
	
	public interface OnSliderClickListener {
		void OnClick();
	}
	 
	@Override
	public void setThumb(Drawable thumb) {
		super.setThumb(thumb);
		mThumb = thumb;
	}
	
	
	
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			// adding width to increase the touch affection
			if (event.getX() >= mThumb.getBounds().left - mThumb.getBounds().width()
					&& event.getX() <= mThumb.getBounds().right + mThumb.getBounds().width()
					&& event.getY() <= mThumb.getBounds().bottom
					&& event.getY() >= mThumb.getBounds().top) {
//			if (mThumb.getBounds().contains((int)event.getX(), (int)event.getY())) {
				return super.onTouchEvent(event);
			}
			
			if (mlistener != null)
				mlistener.OnClick();

			return false;
		}
		return super.onTouchEvent(event);
	}
}
