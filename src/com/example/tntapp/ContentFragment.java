package com.example.tntapp;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings.Secure;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.example.Objects.Menu;
import com.example.Objects.Screen;
import com.example.Objects.Tab;

public class ContentFragment extends Fragment implements OnClickListener,
		AnimationListener {
	Tab tab;
	Screen src;
	EditText input;
	RelativeLayout tabs_lay;
	String newTitle;
	DialogFragment progress;
	InputMethodManager imm;
	Context mContext;
	private TextView title;
	private TextView back, menu_btn;
	private WebView viewer;
	private final static String APP = "app";
	final String LOG_TAG = "myLogs";
	static boolean isReady = false;
	int mVisible = View.VISIBLE;
	static int TIMEOUT = 3000;
	int mGone = View.GONE;
	SwipeRefreshLayout swipeLayout;
	boolean isMenuOpen = false, mInputShow = false;

	private Animation popupShow;
	private Animation popupHide;

	private LinearLayout linearLayoutPopup;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		src = Screen.getInstance(getActivity());
		tab = src.getSelectTab();
		mContext = getActivity().getApplicationContext();
		progress = MyProgressDialog.newInstnce();
		super.onCreate(savedInstanceState);
		//setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_content, null);
		isReady = false;
		title = (TextView) v.findViewById(R.id.title);
		back = (TextView) v.findViewById(R.id.back);
		viewer = (WebView) v.findViewById(R.id.viewer);
		menu_btn = (TextView) v.findViewById(R.id.menu);
		input = (EditText) getActivity().findViewById(R.id.input);
		tabs_lay = (RelativeLayout) getActivity().findViewById(R.id.tabs);
		LinearLayout header = (LinearLayout) v.findViewById(R.id.header);
		linearLayoutPopup = (LinearLayout) v.findViewById(R.id.popup_menu);
		linearLayoutPopup.setVisibility(View.INVISIBLE); // убираем с экрана
		linearLayoutPopup.setBackgroundColor(src.getTabBarBgColor());
		ConstructMenu(src.getSelectTab().getMenu());

		popupShow = AnimationUtils.loadAnimation(getActivity(),
				R.anim.menu_show);
		popupShow.setAnimationListener(this);
		popupHide = AnimationUtils.loadAnimation(getActivity(),
				R.anim.menu_hide);
		popupHide.setAnimationListener(this);
		isMenuOpen = false;
		header.setBackgroundColor(src.getHeaderColor());
		header.setOnClickListener(this);
		viewer.setOnClickListener(this);
		back.setOnClickListener(this);
		menu_btn.setOnClickListener(this);
		title.setOnClickListener(this);
		viewer.setWebViewClient(new MyWebViewClient());

		if (tab.getSizeOfUrl() > 0) {
			viewer.loadUrl(tab.getLastUrl());
			showBack();
		} else if (tab.getSecondUrl() != null) {
			viewer.loadUrl(tab.getSecondUrl());
			title.setText(tab.getCurrentTitle());
		} else {
			viewer.loadUrl(tab.getDomain() + tab.getUrl());
			title.setText(resizeTitle(tab.getTitle()));
		}

		viewer.getSettings().setJavaScriptEnabled(true);

		imm = (InputMethodManager) getActivity().getSystemService(
				Context.INPUT_METHOD_SERVICE);
		swipeLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe_container);
        swipeLayout.setColorScheme(android.R.color.holo_blue_bright, 
                android.R.color.holo_green_light, 
                android.R.color.holo_orange_light, 
                android.R.color.holo_red_light);
		// title.setText(viewer.getTitle());
		return v;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		swipeLayout.setOnRefreshListener(new OnRefreshListener() {
			
			@Override
			public void onRefresh() {
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						swipeLayout.setRefreshing(false);
						swipeLayout.setEnabled(false);
					}
				}, 4000);
				
			}
		});
		super.onViewCreated(view, savedInstanceState);
	}

	public int getTabId() {
		return tab.getId();
	}

	void showBack() {
		back.setVisibility(View.VISIBLE);
	}

	void hideBack() {
		back.setVisibility(View.INVISIBLE);
	}

	public void executeJS(Uri command) {
		if (command.getHost().equals("video")) {
			showVideo(command.getQueryParameter("v"));
		}

		if (command.getHost().equals("buy")) {
			buy(command);
		}
		if (command.getHost().equals("getDeviceId")) {
			getDeviceId(command);
		}
		if (command.getHost().equals("getGeolocation")) {
			getGeolocation(command);
		}
		if (command.getHost().equals("ready")) {
			/*
			 * if (progress.isShowing()) progress.dismiss();
			 */
			isReady = true;
		}
		if (command.getHost().equals("showInput")) {
			showInput(command);
		}

	}

	public void showInput(final Uri uri) {
		input.setVisibility(mVisible);
		tabs_lay.setVisibility(mGone);
		// getActivity().getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
		input.requestFocus();
		input.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					input.setText("");
					input.setVisibility(mGone);
					tabs_lay.setVisibility(mVisible);
					imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
					mInputShow = !mInputShow;
				}

			}
		});
		input.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				Log.d("logs", "action");
				String function = "javascript:"
						+ uri.getQueryParameter("_btn1_callback") + "('"
						+ input.getText() + "');";
				viewer.loadUrl(function);
				input.setText("");
				input.setVisibility(mGone);
				tabs_lay.setVisibility(mVisible);
				return false;
			}
		});
	}

	public void getGeolocation(Uri uri) {
		Gps.getInstance(getActivity());
		String function = null;
		if (uri.getQueryParameter("_reject") != null) {
			function = "javascript:" + uri.getQueryParameter("_reject") + "('"
					+ Gps.getLocation() + "');";
			viewer.loadUrl(function);
		}
		if (uri.getQueryParameter("_resolve") != null) {
			function = "javascript:" + uri.getQueryParameter("_resolve") + "('"
					+ Gps.getLocation() + "');";
			viewer.loadUrl(function);
		}
	}

	public void getDeviceId(Uri uri) {
		String function = null;
		String android_id = Secure.getString(
				getActivity().getContentResolver(), Secure.ANDROID_ID);
		if (uri.getQueryParameter("_reject") != null) {
			function = "javascript:" + uri.getQueryParameter("_reject") + "('"
					+ android_id + "');";
			viewer.loadUrl(function);
		}
		if (uri.getQueryParameter("_resolve") != null) {
			function = "javascript:" + uri.getQueryParameter("_resolve") + "('"
					+ android_id + "');";
			viewer.loadUrl(function);
		}
	}

	public void buy(Uri uri) {
		String function = null;
		if (uri.getQueryParameter("_reject") != null) {
			function = "javascript:" + uri.getQueryParameter("_reject") + "('"
					+ uri.getQueryParameter("id") + "');";
			viewer.loadUrl(function);
		}
		if (uri.getQueryParameter("_resolve") != null) {
			function = "javascript:" + uri.getQueryParameter("_resolve") + "('"
					+ uri.getQueryParameter("id") + "');";
			viewer.loadUrl(function);
		}
	}

	public void showVideo(String path) {
		// path = path.substring(0, path.indexOf(","));
		//this.path = path;
		Intent intent = new Intent(getActivity(), VideoPlayerActivity.class);
		intent.putExtra("data", path);
		startActivity(intent);
	}

	class MyWebViewClient extends WebViewClient {
		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			// TODO Auto-generated method stub
			Uri uri = Uri.parse(url);
			if (isMenuOpen)
				hideMenu();
			isReady = false;
			if (uri.getQueryParameter("_target") != null) {
				// TODO Change tab
				Tab t = src.findTabByName(uri.getQueryParameter("_target"));
				if ((t != null) && (t.getId() != tab.getId())) {
					return;
				}
			}
			progress.setCancelable(false);
			// progress.show(getFragmentManager(), "progress");
			super.onPageStarted(view, url, favicon);
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			Uri uri = Uri.parse(url);
			Log.d(LOG_TAG, uri.getHost() + " " + uri.getScheme() + " " + url);
			if (uri.getScheme().contains(APP)) {
				executeJS(uri);
				return true;
			}
			if (tab.getDomain().contains(uri.getHost())) {
				if (uri.getQueryParameter("_target") != null) {
					// TODO Change tab
					Tab t = src.findTabByName(uri.getQueryParameter("_target"));
					if (t != null) {
						t.setSecondUrl(uri.toString());
						changeTabListener.changeTab(t);
						t.removeStack();
						Log.d(LOG_TAG, t.getSecondUrl());
						return false;
					}

				}
				tab.addUrl(url);
				showBack();
				return false;
			} else {
				Intent i = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(i);
				return true;
			}

		}

		@Override
		public void onPageFinished(WebView view, String url) {
			// TODO Auto-generated method stub
			Uri uri = Uri.parse(url);
			if (uri.getQueryParameter("_title") != null)
				title.setText(resizeTitle(uri.getQueryParameter("_title")));
			super.onPageFinished(view, url);
		}

	}

	public interface onChangeTab {
		public void changeTab(Tab tab);

		public void showMenu();
	}

	onChangeTab changeTabListener;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			changeTabListener = (onChangeTab) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement onSomeEventListener");
		}
	}

	private String resizeTitle(String s) {
		float f = title.getTextSize();
		DisplayMetrics metrics = new DisplayMetrics();
		MainActivity.mActivity.getWindowManager().getDefaultDisplay()
				.getMetrics(metrics);
		int count = (int) ((metrics.widthPixels - f * 10) / (f / 2));
		if (s.length() > count) {
			s = s.substring(0, count);
			s = s.concat("...");
		}
		return s;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			tab.popUrl();
			if (tab.getSizeOfUrl() != 0) {
				viewer.loadUrl(tab.getLastUrl());
				title.setText(resizeTitle(viewer.getTitle()));
			} else {
				if (tab.getSecondUrl() == null) {
					viewer.loadUrl(tab.getDomain() + tab.getUrl());
					title.setText(resizeTitle(tab.getTitle()));
				} else {
					viewer.loadUrl(tab.getSecondUrl());
					title.setText(tab.getMenuTittleByUrl(tab.getSecondUrl()));
				}
				hideBack();
			}
			break;
		case R.id.menu:
			// changeTabListener.showMenu();

			return;
		case R.id.title:
			Log.d("logs", "Click title");
			// viewer.scrollTo(0, 0);
			//if (showHideMenu())
			showHideMenu();
				return;
			//break;
		default:
			break;
		}
		hideMenu();
		// TODO Auto-generated method stub
	}
	
	public boolean showHideMenu() {
		if (linearLayoutPopup.getVisibility() == View.INVISIBLE) {
			linearLayoutPopup.startAnimation(popupShow); // показываем
			isMenuOpen = true;
		} else {
			linearLayoutPopup.startAnimation(popupHide); // пр€чем
			isMenuOpen = false;
		}
		return isMenuOpen;
	}

	public void hideMenu() {
		if (isMenuOpen)
			title.performClick();
	}

	public void ConstructMenu(ArrayList<Menu> menus) {
		Log.d("logs", "construct menu");
		for (Menu menu : menus) {
			final Menu m = menu;
			TextView text = new TextView(getActivity().getApplicationContext());
			text.setText(menu.getTitle());
			text.setTextSize((float) 20.0);
			text.setPadding(0, 20, 0, 20);
			text.setTextColor(Color.WHITE);
			text.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.WRAP_CONTENT));
			text.setGravity(Gravity.CENTER);
			text.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					src.getSelectTab().setSecondUrl(
							src.getDomain() + m.getUrl());
					src.getSelectTab().setCurrentTitle(m.getTitle());
					tab.removeStack();
					viewer.loadUrl(src.getDomain() + m.getUrl());
					title.setText(m.getTitle());
					//title.performClick();
					showHideMenu();
				}
			});
			linearLayoutPopup.addView(text);
			View devider = new View(getActivity().getApplicationContext());
			devider.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
					1));
			devider.setBackgroundColor(Color.BLACK);
			linearLayoutPopup.addView(devider);
		}
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAnimationRepeat(Animation animation) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAnimationStart(Animation animation) {
		// TODO Auto-generated method stub
		Log.d("logs", "start anim");
		if (animation.equals(popupShow)) {
			linearLayoutPopup.setVisibility(View.VISIBLE);
			for(int i =0; i < linearLayoutPopup.getChildCount(); i++)
				linearLayoutPopup.getChildAt(i).setClickable(true);
			title.setText(title.getText() + " v");
		} else if (animation.equals(popupHide)) {
			linearLayoutPopup.setVisibility(View.INVISIBLE);
			for(int i =0; i < linearLayoutPopup.getChildCount(); i++)
				linearLayoutPopup.getChildAt(i).setClickable(false);
			title.setText(title.getText().subSequence(0,
					title.getText().length() - 2));
		}
	}

	public static class MyProgressDialog extends DialogFragment implements
			OnClickListener {
		Button btn;
		TextView loadInfo;
		ProgressBar progr;
		Handler handler;
		Runnable mTimeOutRunnable = new Runnable() {

			@Override
			public void run() {
				if (!isReady) {
					btn.setVisibility(View.VISIBLE);
					loadInfo.setText(R.string.loading_info_bad);
					progr.setVisibility(View.INVISIBLE);
				} else
					getDialog().dismiss();

			}
		};

		public static MyProgressDialog newInstnce() {
			MyProgressDialog fragment = new MyProgressDialog();
			return fragment;
		}

		public MyProgressDialog() {

		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			// TODO Auto-generated method stub
			getDialog().setTitle(R.string.loading);
			View v = inflater.inflate(R.layout.dialog_loader, container);
			btn = (Button) v.findViewById(R.id.playstop);
			btn.setOnClickListener(this);
			loadInfo = (TextView) v.findViewById(R.id.info);
			progr = (ProgressBar) v.findViewById(R.id.progressBar1);
			handler = new Handler();
			return v;
		}

		private void LoadError() {
			handler.postDelayed(mTimeOutRunnable, TIMEOUT);
		}

		@Override
		public void onDestroyView() {
			// TODO Auto-generated method stub
			super.onDestroyView();
			handler.removeCallbacks(mTimeOutRunnable);
		}

		@Override
		public void onResume() {
			// TODO Auto-generated method stub
			LoadError();
			super.onResume();
		}

		@Override
		public void onClick(View v) {
			btn.setVisibility(View.INVISIBLE);
			loadInfo.setText(R.string.loading);
			progr.setVisibility(View.VISIBLE);
			LoadError();
		}

	}
}
