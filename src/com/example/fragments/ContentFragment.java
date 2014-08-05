package com.example.fragments;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.Secure;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

import com.androidquery.AQuery;
import com.androidquery.auth.FacebookHandle;
import com.androidquery.callback.AjaxStatus;
import com.example.Objects.Menu;
import com.example.Objects.Screen;
import com.example.Objects.Tab;
import com.example.dialogs.RefreshDialog;
import com.example.dialogs.TargetPopup;
import com.example.tntapp.Gps;
import com.example.tntapp.InputActivity;
import com.example.tntapp.MainActivity;
import com.example.tntapp.PopupActivity;
import com.example.tntapp.R;
import com.example.tntapp.VideoPlayerActivity;
import com.example.tntapp.R.drawable;
import com.example.tntapp.R.id;
import com.example.tntapp.R.layout;
import com.example.tntapp.R.string;
import com.example.utils.ConnectionDetector;
import com.example.widget.AnimatedLineLayout;
import com.handmark.pulltorefresh.library.PullToRefreshWebView;
import com.myextend.widgets.ObservableWebView;
import com.myextend.widgets.ObservableWebView.OnNetworkDisable;
import com.myextend.widgets.ObservableWebView.OnScrollChangedCallback;

public class ContentFragment extends Fragment implements OnClickListener {
	private Tab tab;
	private Screen src;
	private EditText input;
	private RelativeLayout tabs_lay;
	private InputMethodManager imm;
	private ObservableWebView viewer;
	private final static String APP = "app";
	final String LOG_TAG = "myLogs";
	private static boolean isReady = false;
	private final int mVisible = View.VISIBLE, mGone = View.GONE,
			mInvisible = View.INVISIBLE, mBackId = R.id.back,
			mTitleId = R.id.title, mShare = R.id.share, mShowInput = R.id.btnShowInput;
	private static int TIMEOUT = 3000;
	private boolean mInputShow = false, mGps = false, mRefreshable;
	private AnimatedLineLayout header;
	private AnimatedLineLayout linearLayoutPopup;
	private AQuery aq;
	private FacebookHandle handle;
	private ProgressBar mProgress;
	private String mTitle;
	private final static String APP_ID = "300089453493860";
	private final static String PERMISSIONS = "read_stream,read_friendlists,manage_friendlists,manage_notifications,publish_stream,publish_checkins,offline_access,user_photos,user_likes,user_groups,friends_photos";
	ImageView myImageView;
	private PullToRefreshWebView mPullRefreshWebView;
	public static final String EXTRA_TAB_ID = "fm.finch.tab_id";
	FragmentManager mFm;
	RefreshDialog connectionErrorDialog;

	public static ContentFragment newInstance(int tabId) {
		Bundle args = new Bundle();
		args.putInt(EXTRA_TAB_ID, tabId);
		ContentFragment fragment = new ContentFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		src = Screen.getInstance(getActivity());
		int tabId = getArguments().getInt(EXTRA_TAB_ID);
		tab = src.getTabById(tabId);
		mFm = getFragmentManager();
		handle = new FacebookHandle(getActivity(), APP_ID, PERMISSIONS);
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	@SuppressLint("SetJavaScriptEnabled")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		log("OnCreateView");
		View v = inflater.inflate(R.layout.fragment_content, null);
		aq = new AQuery(getActivity(), v);
		aq.id(R.id.title_layout).clicked(this);
		isReady = false;
		aq.id(mBackId).clicked(this);
		aq.id(mTitleId).clicked(this);
		aq.id(mShare).clicked(this);
		aq.id(mShowInput).clicked(this);
		mPullRefreshWebView = (PullToRefreshWebView) v
				.findViewById(R.id.pull_refresh_webview);
		viewer = (ObservableWebView) mPullRefreshWebView.getRefreshableView();
		viewer.setOnNetworkDisable(new OnNetworkDisable() {

			@Override
			public void onConnectionError() {
				log("================there is no connection");
				viewer.stopLoading();
				connectionErrorDialog = RefreshDialog
						.newInstance("Ошибка интернет соединения");
				connectionErrorDialog.setCancelable(false);
				connectionErrorDialog.show(getFragmentManager(),
						"network_error");
			}
		});
		mProgress = (ProgressBar) v.findViewById(R.id.progressLoading);
		myImageView = (ImageView) v.findViewById(R.id.imageView1);
		input = (EditText) getActivity().findViewById(R.id.input);
		tabs_lay = (RelativeLayout) getActivity().findViewById(R.id.tabs);
		header = (AnimatedLineLayout) v.findViewById(R.id.header);
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

		return v;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		viewer.setOnScrollChangedCallback(new OnScrollChangedCallback() {

			@Override
			public void onScroll(int l, int t) {
				hideMenu();
				if (t < oldT) {
					header.show();
				} else if (t > oldT) {
					header.hide();
				}
				oldT = t;
			}
		});
		super.onViewCreated(view, savedInstanceState);
	}

	public void reload() {
		logi("Reload");
		viewer.reload();
		// loadPage(viewer.getUrl());
	}

	private void chooseLoad() {
		if (tab.getSizeOfUrl() > 0) {
			loadPage(tab.getLastUrl());
		} else if (tab.getSecondUrl() != null) {
			loadPage(tab.getSecondUrl());
			mTitle = tab.getCurrentTitle();
			aq.id(mTitleId).text(mTitle);
		} else {
			loadPage(tab.getUrl());
			mTitle = tab.getTitle();
			aq.id(mTitleId).text(resizeTitle(mTitle));
		}
	}

	private int oldT;

	public void auth_facebook() {
		String url = "https://graph.facebook.com/me";
		aq.auth(handle).ajax(url, JSONObject.class, this, "facebookCb");

	}

	public void facebookCb(String url, JSONObject json, AjaxStatus status) {

		if (json != null) {
			Log.d(LOG_TAG, json.toString());
			showVideo(video);
		} else {
		}

	}

	String video;

	public void showInput(final Uri uri) {
		if (mInputShow)
			return;
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
					imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
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

	private void showInputWin() {
		Intent intent = new Intent(getActivity(), InputActivity.class);
		startActivity(intent);
	}
	
	OnTouchListener mToucherSetOn = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			return false;
		}
	};
	OnTouchListener mToucherSetOff = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			return true;
		}
	};

	private void localPush(Uri uri) {
		NotificationManager mNotificationManager = (NotificationManager) getActivity()
				.getSystemService(Context.NOTIFICATION_SERVICE);
		Uri alarmSound = RingtoneManager
				.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

		PendingIntent contentIntent = PendingIntent.getActivity(getActivity(),
				0, new Intent(getActivity(), MainActivity.class), 0);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				getActivity())
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle("Self notifiction")
				.setStyle(
						new NotificationCompat.BigTextStyle().bigText(uri
								.getQueryParameter("text")))
				.setSound(alarmSound)
				.setContentText(uri.getQueryParameter("text"));

		mBuilder.setContentIntent(contentIntent);
		mNotificationManager.notify(0, mBuilder.build());
	}

	public void executeJS(Uri command) {
		if (command.getHost().equals("video")) {
			/*
			 * if (!handle.authenticated()) auth_facebook(); else
			 */
			showVideo(command.getQueryParameter("v"));
			video = command.getQueryParameter("v");
		}

		if (command.getHost().equals("buy")) {
			buy(command);
		}
		if (command.getHost().equals("showInputWin")) {
			showInputWin();
		}
		if (command.getHost().equals("showInputBtn")) {
			aq.id(mShowInput).visible();
		}
		if (command.getHost().equals("hideInputBtn")) {
			aq.id(mShowInput).invisible();
		}
		if (command.getHost().equals("push")) {
			localPush(command);
		}
		if (command.getHost().equals("popupResolve")) {
			performJS(command, "popup");
			if (command.getQueryParameter("_resolve") != null)
				Toast.makeText(getActivity(),
						"resolve=" + command.getQueryParameter("_resolve"),
						Toast.LENGTH_SHORT).show();
		}

		if (command.getHost().equals("getDeviceId")) {
			getDeviceId(command);
		}
		if (command.getHost().equals("getGeolocation")) {
			getGeolocation(command);
		}
		if (command.getHost().equals("ready")) {
			aq.id(mShare).visible();
			mProgress.setVisibility(mInvisible);
			if (handle != null)
				handler.removeCallbacks(r);
			if (mRefreshable)
				mPullRefreshWebView.setOnTouchListener(mToucherSetOn);
			viewer.setOnTouchListener(mToucherSetOn);
			isReady = true;
		}
	}
	

	Handler handler;
	RefreshDialog dialog;
	Runnable r = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (!isReady) {
				dialog = RefreshDialog.newInstance("Ошибка загрузки");
				dialog.setCancelable(false);
				if (dialog != null)
					dialog.show(mFm, "LONG_LOAD");
				mProgress.setVisibility(mInvisible);
			}
		}
	};

	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		removeHandler();
	};

	public void logi(String s) {
		Log.d("twice", s);
	}

	private void removeHandler() {
		if (handler != null)
			handler.removeCallbacks(r);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// TODO неправильно обрабатывается
		if (requestCode == 222)
			if (requestCode == Activity.RESULT_OK) {
				executeJS(Uri.parse(data.getStringExtra(PopupActivity.EXTRA_RESOLVE)));
			}
	}

	public void onDetach() {
		removeHandler();
		super.onDetach();
	};

	class MyWebViewClient extends WebViewClient {

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
			aq.id(R.id.btnShowInput).invisible();
			ConnectionDetector cd = new ConnectionDetector(getActivity());
			logi("Start page: " + url);
			if (!cd.isConnectingToInternet()) {
				connectionErrorDialog = RefreshDialog
						.newInstance("Ошибка интернет соединения");
				connectionErrorDialog.setCancelable(false);
				connectionErrorDialog.show(getFragmentManager(),
						"network_error");
				viewer.stopLoading();
				return;
			}
			aq.id(mShare).invisible();
			removeHandler();
			handler = new Handler();
			handler.postDelayed(r, TIMEOUT);
			log("Start loading: " + url);
			mProgress.setVisibility(mVisible);
			mPullRefreshWebView.setOnTouchListener(mToucherSetOff);
			view.setOnTouchListener(mToucherSetOff);
			Uri uri = Uri.parse(url);
			if (uri.getQueryParameter("_title") != null) {
				mTitle = uri.getQueryParameter("_title");
				aq.id(mTitleId).text(resizeTitle(mTitle));
			}
			if (uri.getQueryParameter("_refreshable") != null)
				mRefreshable = true;
			else
				mRefreshable = false;
			hideMenu();
			isReady = false;
			if (uri.getQueryParameter("_target") != null) {
				Tab t = src.findTabByName(uri.getQueryParameter("_target"));
				if ((t != null) && (t.getId() != tab.getId())) {
					return;
				}
			}

		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			logi("Should overrideUrl: " + url);
			Uri uri = Uri.parse(url);
			Log.d(LOG_TAG, uri.getHost() + " " + uri.getScheme() + " " + url);
			if (uri.getScheme().equals(APP)) {
				executeJS(uri);
				return true;
			}
			// if (tab.getDomain().contains(uri.getHost())) {
			if (uri.getQueryParameter("_target") != null) {
				if (uri.getQueryParameter("_target").equals("popup")) {
					Intent popup = new Intent(getActivity(),
							PopupActivity.class);
					popup.putExtra(PopupActivity.EXTRA_URL, url
							+ "&_resolve=popupResolve");
					startActivityForResult(popup, 222);
					return true;
				}
				Tab t = src.findTabByName(uri.getQueryParameter("_target"));
				if (t != null) {
					t.setSecondUrl(uri.toString());
					changeTabListener.changeTab(t);
					t.removeStack();
					Log.d(LOG_TAG, t.getSecondUrl());
					return true;
				}

			}
			tab.addUrl(url);
			return false;
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			if (!url.contains(src.getDomain()))
				executeJS(Uri.parse("app://ready"));
			logi("Page Finish: " + url);
			log("Page is loaded");
			Uri uri = Uri.parse(url);
			if (src.getSelectTab().getSizeOfUrl() == 0) {
				aq.id(mBackId).invisible();
				aq.id(R.id.imageView1).visible();
			} else {
				aq.id(mBackId).visible();
				aq.id(R.id.imageView1).invisible();
			}
			if (tab.getDomain().equals(uri.getHost()))
				if (uri.getQueryParameter("_title") != null) {
					mTitle = uri.getQueryParameter("_title");
					aq.id(mTitleId).text(resizeTitle(mTitle));
				}

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
		float f = ((TextView) aq.id(mTitleId).getView()).getTextSize();
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
			aq.id(R.id.imageView1).image(R.drawable.arrow_down);
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
					aq.id(R.id.imageView1).image(R.drawable.arrow_down);
			}
			return;
		case R.id.share:
			/*
			 * Intent sendIntent = new Intent();
			 * sendIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			 * sendIntent.setAction(Intent.ACTION_SEND);
			 * sendIntent.putExtra(Intent.EXTRA_TEXT, viewer.getUrl());
			 * sendIntent.setType("text/plain");
			 * startActivity(Intent.createChooser(sendIntent,
			 * getResources().getText(R.string.send_to)));
			 */
			onShareClick();
			break;
		case R.id.btnShowInput:
			showInputWin();
			break;
		default:
			break;
		}
		linearLayoutPopup.hide();
	}

	private void onShareClick() {
		Intent emailIntent = new Intent();
		emailIntent.setAction(Intent.ACTION_SEND);
		// Native email client doesn't currently support HTML, but it doesn't
		// hurt to try in case they fix it
		emailIntent.putExtra(Intent.EXTRA_TEXT, "Mesage");
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject");
		emailIntent.setType("message/rfc822");

		PackageManager pm = getActivity().getPackageManager();
		Intent sendIntent = new Intent(Intent.ACTION_SEND);
		sendIntent.setType("text/plain");

		Intent openInChooser = Intent
				.createChooser(emailIntent, getResources().getString(R.string.send_to));

		List<ResolveInfo> resInfo = pm.queryIntentActivities(sendIntent, 0);
		List<LabeledIntent> intentList = new ArrayList<LabeledIntent>();
		for (int i = 0; i < resInfo.size(); i++) {
			// Extract the label, append it, and repackage it in a LabeledIntent
			ResolveInfo ri = resInfo.get(i);
			String packageName = ri.activityInfo.packageName;
			if (packageName.contains("android.email")) {
				emailIntent.setPackage(packageName);
			} else if (packageName.contains("twitter")
					|| packageName.contains("facebook")
					|| packageName.contains("sms")
					|| packageName.contains("android.gm")
					|| packageName.contains("vkontakte")) {
				Intent intent = new Intent();
				intent.setComponent(new ComponentName(packageName,
						ri.activityInfo.name));
				intent.setAction(Intent.ACTION_SEND);
				intent.setType("text/plain");
				if (packageName.contains("twitter")) {
					intent.putExtra(Intent.EXTRA_TEXT, viewer.getUrl());
				} else if (packageName.contains("facebook")) {
					// Warning: Facebook IGNORES our text. They say
					// "These fields are intended for users to express themselves. Pre-filling these fields erodes the authenticity of the user voice."
					// One workaround is to use the Facebook SDK to post, but
					// that doesn't allow the user to choose how they want to
					// share. We can also make a custom landing page, and the
					// link
					// will show the <meta content ="..."> text from that page
					// with our link in Facebook.
					intent.putExtra(Intent.EXTRA_TEXT, viewer.getUrl());
				} else if (packageName.contains("vkontakte")) {
					intent.putExtra(Intent.EXTRA_TEXT, viewer.getUrl());
				} else if (packageName.contains("sms")) {
					intent.putExtra(Intent.EXTRA_TEXT, viewer.getUrl());
				} else if (packageName.contains("android.gm")) {
					intent.putExtra(Intent.EXTRA_TEXT, viewer.getUrl());
					intent.putExtra(Intent.EXTRA_SUBJECT, "Subject");
					intent.setType("message/rfc822");
				}

				intentList.add(new LabeledIntent(intent, packageName, ri
						.loadLabel(pm), ri.icon));
			}
		}

		// convert intentList to array
		LabeledIntent[] extraIntents = intentList
				.toArray(new LabeledIntent[intentList.size()]);

		openInChooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, extraIntents);
		startActivity(openInChooser);
	}

	public void showHideHeader() {
		header.show();
	}

	public void changeTitleAfterChangeConfig() {
		aq.id(mTitleId).text(resizeTitle(mTitle));
	}

	public void hideMenu() {
		aq.id(R.id.imageView1).image(R.drawable.arrow_down);
		linearLayoutPopup.hide();
	}

	private void loadPage(String s) {
		if (viewer == null)
			return;
		if (!s.contains("http"))
			s = src.getDomain() + s;
		else if (!s.contains(src.getDomain())) {
			Uri uri = Uri.parse(s);
			Intent i = new Intent(Intent.ACTION_VIEW, uri);
			startActivity(i);
			return;
		}
		viewer.loadUrl(s);
	}

	private void ConstructMenu(ArrayList<Menu> menus) {
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
					if (m.getUrl().contains("http")) {
						aq.id(R.id.imageView1).image(R.drawable.arrow_down);
						linearLayoutPopup.hide();
						loadPage(m.getUrl());
						return;
					}
					linearLayoutPopup.repaintChild(src.getTabBarBgColor());
					text.setBackgroundColor(src.getTabBarSelectionColor());
					src.getSelectTab().setSecondUrl(m.getUrl());
					src.getSelectTab().setCurrentTitle(m.getTitle());
					tab.removeStack();
					loadPage(m.getUrl());
					mTitle = m.getTitle();
					aq.id(mTitleId).text(resizeTitle(mTitle));
					aq.id(R.id.imageView1).image(R.drawable.arrow_down);
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

}
