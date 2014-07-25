package com.example.tntapp;

import java.io.IOException;
import java.util.Stack;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.renderscript.Type.CubemapFace;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;

public class VideoPlayerActivity extends Activity implements
		SurfaceHolder.Callback, MediaPlayer.OnPreparedListener,
		VideoControllerView.MediaPlayerControl, OnClickListener {

	SurfaceView 					videoSurface;
	MediaPlayer 					player;
	VideoControllerView 			controller;
	String 							path;
	Context							context;
	int								mCurrentPosition = 0;
	Stack<String> 					queue = new Stack<String>();
	boolean 						mRotate = true, mLandscape = false;
	public static final String 		LANDSCAPE_MODE = "LANDSCAPE_MODE";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		mLandscape = getIntent().getExtras().getBoolean(LANDSCAPE_MODE, false);
		if (mLandscape)
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		setContentView(R.layout.activity_video_player);
		context = this;
		if (savedInstanceState != null) {
			queue = (Stack<String>) savedInstanceState.getSerializable("queue");
			path = savedInstanceState.getString("current_data");
			mCurrentPosition = savedInstanceState.getInt("current_position", 0);
			queue.push(path);
			mRotate = true;
		} else {
			mRotate = false;
			path = getIntent().getExtras().getString("data");
			if (path.contains(",")) {
				while (path.contains(",")) {
					queue.push(path.substring(0, path.indexOf(",")));
					path = path.substring(path.indexOf(",") + 1);
				}
				queue.push(path);
			} else {
				queue.push(path);
			}
		}

		videoSurface = (SurfaceView) findViewById(R.id.videoSurface);
		SurfaceHolder videoHolder = videoSurface.getHolder();
		videoHolder.addCallback(this);
		player = new MediaPlayer();
		controller = new VideoControllerView(this, player, false);
		controller.setPrevNextListeners(this, mExitListener);

		try {
			player.setAudioStreamType(AudioManager.STREAM_MUSIC);
			path =queue.pop();
			player.setDataSource(this, Uri
					.parse(path));
			
			player.setOnPreparedListener(this);
			player.setOnCompletionListener(new OnCompletionListener() {
				
				@Override
				public void onCompletion(MediaPlayer mp) {
					if (!queue.isEmpty()) {

						try {
							if (mRotate) {
								mRotate = false;
								return;
							}
							player.reset();
							path = queue.pop();
							player.setDataSource(context, Uri
									.parse(path));
							mCurrentPosition = 0;
							player.prepare();
						} catch (Exception e) {
							Log.d("logs", "Неккоректная ссылка: " + e.toString());
						}
					}
					
				}
			});
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected void onSaveInstanceState(Bundle outState) {
		outState.putSerializable("queue", queue);
		outState.putInt("current_position", player.getCurrentPosition());
		outState.putString("current_data", path);
	};
	
	
	OnClickListener mExitListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			player.stop();
			//player.release();
			finish();
		}
	};
	
	@Override
    public void onConfigurationChanged(Configuration newConfig) {  
        super.onConfigurationChanged(newConfig);  
    }

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		controller.show();
		return false;
	}

	// Implement SurfaceHolder.Callback
	public void surfaceCreatefaceChanged(SurfaceHolder holder, int format,
			int width, int height) {
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		player.setDisplay(holder);
		player.prepareAsync();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
	} // End SurfaceHolder.Callback

	@Override
	public void onPrepared(MediaPlayer mp) {
		controller.setMediaPlayer(this);
		controller
				.setAnchorView((FrameLayout) findViewById(R.id.videoSurfaceContainer));
		controller.setVideoTitle("Трейлер");
		player.seekTo(mCurrentPosition);
		player.start();
	}

	@Override
	public boolean canPause() {
		return true;
	}

	@Override
	public boolean canSeekBackward() {
		return true;
	}

	@Override
	public boolean canSeekForward() {
		return true;
	}

	@Override
	public int getBufferPercentage() {
		return 0;
	}

	@Override
	public int getCurrentPosition() {
		return player.getCurrentPosition();
	}

	@Override
	public int getDuration() {
		return player.getDuration();
	}

	@Override
	public boolean isPlaying() {
		return player.isPlaying();
	}

	@Override
	public void pause() {
		player.pause();
	}

	@Override
	public void seekTo(int i) {
		player.seekTo(i);
	}

	@Override
	public void start() {
		player.start();
	}

	@Override
	public boolean isFullScreen() {
		return false;
	}

	@Override
	public void toggleFullScreen() {
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}
}
