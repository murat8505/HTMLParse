package com.example.tntapp;

import java.util.Stack;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnInfoListener;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.VideoView;

public class VideoActivity extends Activity {
	Stack<String> queue = new Stack<String>();
	VideoView video;
	int currentVolume, mMaxVolume;
	AudioManager am;
	MediaController mc;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_video);

		am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		mc = new MyController(this);
		currentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
		mMaxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

		Intent intent = getIntent();
		String path = intent.getStringExtra("data");
		if (path.contains(",")) {
			while (path.contains(",")) {
				queue.push(path.substring(0, path.indexOf(",")));
				path = path.substring(path.indexOf(",") + 1);
			}
			queue.push(path);
			// path = path.substring(path.indexOf(",")+1);
		} else {
			queue.push(path);
		}

		video = (VideoView) findViewById(R.id.videoView1);
		video.setVideoURI(Uri.parse(queue.pop()));
		Log.d("logs", "Current: " + video.getCurrentPosition() + " " + " All: "
				+ video.getDuration());
		video.setMediaController(mc);
		video.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				if (!queue.isEmpty()) {

					try {
						video.setVideoURI(Uri.parse(queue.pop()));
						video.start();

					} catch (Exception e) {
						Log.d("logs", "Неккоректная ссылка: ");
					}
				}
			}
		});
		try {
			video.start();
		} catch (RuntimeException e) {
			Log.d("logs", "Неккоректная ссылка: ");
		}
	}

	public class MyController extends MediaController {
		Context context;

		public MyController(Context context) {
			super(context);
			this.context = context;
			// TODO Auto-generated constructor stub
		}

		@Override
		public void setAnchorView(View view) {
			// TODO Auto-generated method stub
			super.setAnchorView(view);
			LayoutInflater inflate = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View v = inflate.inflate(R.layout.media_cntrl, null);
			Button muteUpButton = (Button) v.findViewById(R.id.button1);
			muteUpButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (currentVolume < mMaxVolume) {
						currentVolume++;
						am.setStreamVolume(AudioManager.STREAM_MUSIC,
								currentVolume, 0);
					}
					show(3000);
				}
			});
			Button exitButton = (Button) v.findViewById(R.id.button2);
			exitButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					finish();
				}
			});

			Button muteDownButton = (Button) v.findViewById(R.id.button3);
			muteDownButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if (currentVolume > 0) {
						currentVolume--;
						am.setStreamVolume(AudioManager.STREAM_MUSIC,
								currentVolume, 0);
					}
					show(3000);
				}
			});

			addView(v);
			/*
			 * LinearLayout ll = new LinearLayout(context); Button muteUpButton
			 * = new Button(context); muteUpButton.setText("Up");
			 * muteUpButton.setOnClickListener(new OnClickListener() {
			 * 
			 * @Override public void onClick(View v) { if (currentVolume <
			 * mMaxVolume) { currentVolume++;
			 * am.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0);
			 * 
			 * } show(3000);
			 * 
			 * } }); ll.addView(muteUpButton);
			 * 
			 * Button muteDownButton = new Button(context);
			 * muteUpButton.setText("Down");
			 * muteDownButton.setOnClickListener(new OnClickListener() {
			 * 
			 * @Override public void onClick(View v) { // TODO Auto-generated
			 * method stub if (currentVolume > 0) { currentVolume--;
			 * am.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0);
			 * } show(3000); } }); ll.addView(muteDownButton);
			 * 
			 * Button searchButton = new Button(context);
			 * searchButton.setText(R.string.exit);
			 * searchButton.setOnClickListener(new OnClickListener() {
			 * 
			 * @Override public void onClick(View v) { finish(); } });
			 * ll.addView(searchButton);
			 * 
			 * FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
			 * LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			 * params.gravity = Gravity.LEFT; addView(ll, params);
			 */

		}

	}

}
