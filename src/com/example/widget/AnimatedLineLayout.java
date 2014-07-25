package com.example.widget;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.example.tntapp.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AnimatedLineLayout extends LinearLayout implements AnimationListener {

	private boolean mVisible = true;
	private boolean mHeader = false;
	

	private final Animation mShow = new TranslateAnimation(
			Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0,
			Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
	private final Animation mHide = new TranslateAnimation(
			Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0,
			Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, -1.0f);;

	public AnimatedLineLayout(Context context) {
		super(context);
	}
	
	public boolean isVisible() {
		return mVisible;
	}
	
	public void repaintChild(int color) {
		int childCount = getChildCount();
		for (int k = 0; k < childCount; k++) {
			if (getChildAt(k) instanceof TextView)
				getChildAt(k).setBackgroundColor(color);
		}
	}

	public AnimatedLineLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		mShow.setDuration(700);
		mShow.setAnimationListener(this);
		mShow.setFillAfter(true);
		mHide.setDuration(700);
		mHide.setAnimationListener(this);
		mHide.setFillAfter(true);
	}
	
	public void constructMenu() {
		
	}

	public void show() {
		if (!mVisible) {
			//disableEnableControls(true, this);
			//this.startAnimation(mShow);
			//setEnabled(true);
			this.setVisibility(View.VISIBLE);
			YoYo.with(Techniques.BounceInDown)
		    .duration(1000)
		    .playOn(this);
			mVisible = true;
		}
	}
	
	public void hide() {
		if (mVisible) {
			YoYo.with(Techniques.SlideOutUp)
		    .duration(1000)
		    .playOn(this);
			//this.startAnimation(mHide);
			mVisible = false;
			//setEnabled(false);
		}
	}
	
	public void toggle() {
		if (mVisible)
			hide();
		else
			show();
	}

	public void setVisible(boolean visible) {
		mVisible = visible;
		setEnabled(visible);
	}
	
	public void setHeader(boolean header) {
		mHeader = header;
	}
	
	private void disableEnableControls(boolean enable, ViewGroup vg){
	    for (int i = 0; i < vg.getChildCount(); i++){
	       View child = vg.getChildAt(i);
	       child.setEnabled(enable);
	       if (!mHeader)
	    	   child.setVisibility(enable ? View.VISIBLE : View.GONE);
	       if (child instanceof ViewGroup){ 
	          disableEnableControls(enable, (ViewGroup)child);
	       }
	    }
	}

	@Override
	public void onAnimationStart(Animation animation) {
		/*if (animation.equals(mShow))
			disableEnableControls(true, this);*/
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		 /*if (animation.equals(mHide))
			disableEnableControls(false, this);*/
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
		// TODO Auto-generated method stub
		
	}

}
