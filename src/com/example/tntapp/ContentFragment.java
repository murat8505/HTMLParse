package com.example.tntapp;

import java.lang.reflect.Field;
import java.util.ArrayList;
import org.json.JSONObject;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
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
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import com.androidquery.AQuery;
import com.androidquery.auth.FacebookHandle;
import com.androidquery.callback.AjaxStatus;
import com.example.Objects.Menu;
import com.example.Objects.Screen;
import com.example.Objects.Tab;
import com.example.tntapp.ObservableWebView.OnScrollChangedCallback;
import com.example.widget.AnimatedLineLayout;

public class ContentFragment extends Fragment implements OnClickListener {
	private Tab tab;
	private Screen src;
	private EditText input;
	private RelativeLayout tabs_lay;
	private DialogFragment progress;
	private InputMethodManager imm;
	private TextView title;
	private ObservableWebView viewer;
	private final static String APP = "app";
	final String LOG_TAG = "myLogs";
	private static boolean isReady = false;
	private final int mVisible = View.VISIBLE, mGone = View.GONE,
			mBackId = R.id.back, mTitleId = R.id.title;
	private static int TIMEOUT = 1500;
	private SwipeRefreshLayout swipeLayout;
	private boolean mInputShow = false, mGps = false, mRefreshable;
	private AnimatedLineLayout header;
	private AnimatedLineLayout linearLayoutPopup;
	private AQuery aq;
	private FacebookHandle handle;
	private final static String APP_ID = "300089453493860";
	private final static String PERMISSIONS = "read_stream,read_friendlists,manage_friendlists,manage_notifications,publish_stream,publish_checkins,offline_access,user_photos,user_likes,user_groups,friends_photos";
	ImageView myImageView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		src = Screen.getInstance(getActivity());
		tab = src.getSelectTab();
		getActivity().getApplicationContext();
		progress = MyProgressDialog.newInstnce();
		handle = new FacebookHandle(getActivity(), APP_ID, PERMISSIONS);
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH) @SuppressLint("SetJavaScriptEnabled") @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_content, null);
		aq = new AQuery(getActivity(), v);
		aq.id(R.id.title_layout).clicked(this);
		isReady = false;
		title = (TextView) v.findViewById(R.id.title);
		aq.id(mBackId).clicked(this);
		aq.id(mTitleId).clicked(this);
		viewer = (ObservableWebView) v.findViewById(R.id.viewer);
		myImageView = (ImageView) v.findViewById(R.id.imageView1);
		input = (EditText) getActivity().findViewById(R.id.input);
		tabs_lay = (RelativeLayout) getActivity().findViewById(R.id.tabs);
		header = (AnimatedLineLayout) v.findViewById(R.id.header);
		swipeLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe_container);
		linearLayoutPopup = (AnimatedLineLayout) v
				.findViewById(R.id.popup_menu);
		linearLayoutPopup.setVisibility(View.INVISIBLE);
		linearLayoutPopup.setVisible(false);
		linearLayoutPopup.setBackgroundColor(src.getTabBarBgColor());
		ConstructMenu(src.getSelectTab().getMenu());
		header.setBackgroundColor(src.getHeaderColor());
		header.setOnClickListener(this);
		header.setHeader(true);
		viewer.setOnClickListener(this);
		viewer.setWebViewClient(new MyWebViewClient());

		chooseLoad();

		viewer.getSettings().setJavaScriptEnabled(true);

		imm = (InputMethodManager) getActivity().getSystemService(
				Context.INPUT_METHOD_SERVICE);

		int mProgressBarHeight =  105;

		try { // Set the internal trigger distance using reflection. Field
			Field field = SwipeRefreshLayout.class
					.getDeclaredField("mProgressBarHeight");
			field.setAccessible(true);
			field.setInt(swipeLayout, mProgressBarHeight);
		} catch (Exception e) {
			e.printStackTrace();
		}

		swipeLayout
				.setColorScheme(android.R.color.holo_blue_dark,
						android.R.color.holo_green_dark,
						android.R.color.holo_orange_dark,
						android.R.color.holo_red_dark);
		// title.setText(viewer.getTitle());
		return v;
	}

	private void chooseLoad() {
		if (tab.getSizeOfUrl() > 0) {
			loadPage(tab.getLastUrl());
		} else if (tab.getSecondUrl() != null) {
			loadPage(tab.getSecondUrl());
			aq.id(mTitleId).text(tab.getCurrentTitle());
		} else {
			loadPage(tab.getDomain() + tab.getUrl());
			aq.id(mTitleId).text(resizeTitle(tab.getTitle()));
		}
	}

	private int oldT;

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		viewer.setOnScrollChangedCallback(new OnScrollChangedCallback() {

			@Override
			public void onScroll(int l, int t) {
				hideMenu();
				if (t < oldT) {
					// showHideHeader(true);
					header.show();
				} else if (t > oldT) {
					// showHideHeader(false);
					if (!swipeLayout.isRefreshing())
						header.hide();
				}
				if (mRefreshable)
					swipeLayout.setEnabled((t > 0) ? false : true);
				oldT = t;
			}

		});
		swipeLayout.setOnRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefresh() {
				viewer.reload();
				/*
				 * new Handler().postDelayed(new Runnable() {
				 * 
				 * @Override public void run() {
				 * swipeLayout.setRefreshing(false); } }, 4000);
				 */

			}
		});
		super.onViewCreated(view, savedInstanceState);
	}

	public void toggleSwipe(String url) {
		Uri uri = Uri.parse(url);
		String refreshable = uri.getQueryParameter("_refreshable");
		if (refreshable == null) {
			mRefreshable = false;
			swipeLayout.setEnabled(false);
		} else if (refreshable.contains("true")) {
			mRefreshable = true;
			swipeLayout.setEnabled(true);
		}
	}

	public void auth_facebook() {
		String url = "https://graph.facebook.com/me";
		aq.auth(handle).ajax(url, JSONObject.class, this, "facebookCb");

	}

	public void facebookCb(String url, JSONObject json, AjaxStatus status) {

		if (json != null) {
			Log.d(LOG_TAG, json.toString());
			showVideo(video);
		} else {
			// ajax error
		}

	}

	String video;

	public void executeJS(Uri command) {
		if (command.getHost().equals("video")) {
			if (!handle.authenticated())
				auth_facebook();
			else
				showVideo(command.getQueryParameter("v"));
			video = command.getQueryParameter("v");
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
			if (swipeLayout.isRefreshing())
				swipeLayout.setRefreshing(false);
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
		/*
		 * if (mInputShow) return;
		 */
		input.setVisibility(mVisible);
		tabs_lay.setVisibility(mGone);
		mInputShow = true;
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
					imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY,
							0);
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
				mInputShow = false;
				return false;
			}
		});
	}

	private void performJS(Uri uri, String target) {
		String function = null;
		if (uri.getQueryParameter("_reject") != null) {
			function = "javascript:" + uri.getQueryParameter("_reject") + "('"
					+ target + "');";
			viewer.loadUrl(function);
		}
		if (uri.getQueryParameter("_resolve") != null) {
			function = "javascript:" + uri.getQueryParameter("_resolve") + "('"
					+ target + "');";
			viewer.loadUrl(function);
		}
	}

	public void getGeolocation(Uri uri) {
		mGps = true;
		if (Gps.getInstance(getActivity()) == null)
			return;
		performJS(uri, Gps.getLocation());
	}

	public void getDeviceId(Uri uri) {
		String android_id = Secure.getString(
				getActivity().getContentResolver(), Secure.ANDROID_ID);
		performJS(uri, android_id);
	}

	public void buy(Uri uri) {
		performJS(uri, uri.getQueryParameter("id"));
	}

	public void showVideo(String path) {
		Intent intent = new Intent(getActivity(), VideoPlayerActivity.class);
		intent.putExtra("data", path);
		startActivity(intent);
	}

	class MyWebViewClient extends WebViewClient {
		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			Uri uri = Uri.parse(url);
			hideMenu();
			isReady = false;
			if (uri.getQueryParameter("_target") != null) {
				Tab t = src.findTabByName(uri.getQueryParameter("_target"));
				if ((t != null) && (t.getId() != tab.getId())) {
					return;
				}
			}
			progress.setCancelable(false);
			progress.show(getFragmentManager(), "progress");
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
				return false;
			} else {
				Intent i = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(i);
				return true;
			}

		}

		@Override
		public void onPageFinished(WebView view, String url) {
			toggleSwipe(url);
			log("Page is loaded");
			Uri uri = Uri.parse(url);
			if (src.getSelectTab().getSizeOfUrl() == 0) {
				aq.id(mBackId).invisible();
				aq.id(R.id.imageView1).visible();
			} else {
				aq.id(mBackId).visible();
				aq.id(R.id.imageView1).invisible();
			}
			if (tab.getDomain().contains(uri.getHost()))
				if (uri.getQueryParameter("_title") != null)
					title.setText(resizeTitle(uri.getQueryParameter("_title")));
			super.onPageFinished(view, url);
		}

	}

	void log(String s) {
		Log.d("logs", s);
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
		if (mInputShow)
			viewer.requestFocus();
		switch (v.getId()) {
		case R.id.back:
			aq.id(mBackId).invisible();
			aq.id(R.id.imageView1).image(R.drawable.close);
			tab.popUrl();
			chooseLoad();
			break;
		case R.id.title_layout:
		case R.id.title:
			if (aq.id(mBackId).getImageView().getVisibility() != View.VISIBLE) {
				linearLayoutPopup.toggle();
				if (linearLayoutPopup.isVisible())
					aq.id(R.id.imageView1).image(R.drawable.open);
				else
					aq.id(R.id.imageView1).image(R.drawable.close);
			}
			return;
		default:
			break;
		}
		linearLayoutPopup.hide();
	}

	public void showHideHeader() {
		header.show();
	}

	public void hideMenu() {
		aq.id(R.id.imageView1).image(R.drawable.close);
		linearLayoutPopup.hide();
	}

	public void loadPage(String s) {
		if (viewer == null)
			return;
		viewer.loadUrl(s);
		toggleSwipe(s);
	}

	public void ConstructMenu(ArrayList<Menu> menus) {
		Log.d("logs", "construct menu");
		for (Menu menu : menus) {
			final Menu m = menu;
			final TextView text = new TextView(getActivity()
					.getApplicationContext());
			if (src.getSelectTab().getSecondUrl() != null)
				if (menu.getTitle().contains(
						src.getSelectTab().getMenuTittleByUrl(
								src.getSelectTab().getSecondUrl())))
					text.setBackgroundColor(src.getTabBarSelectionColor());
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
					linearLayoutPopup.repaintChild(src.getTabBarBgColor());
					text.setBackgroundColor(src.getTabBarSelectionColor());
					src.getSelectTab().setSecondUrl(
							src.getDomain() + m.getUrl());
					src.getSelectTab().setCurrentTitle(m.getTitle());
					tab.removeStack();
					loadPage(src.getDomain() + m.getUrl());
					title.setText(m.getTitle());
					aq.id(R.id.imageView1).image(R.drawable.close);
					linearLayoutPopup.hide();

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
