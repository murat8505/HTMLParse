package com.example.tntapp;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import com.example.Objects.Screen;
import com.example.Objects.Tab;
import com.example.dialogs.PopupDialog;
import com.example.fragments.ContentFragment;
import com.example.fragments.SplashFragment;
import com.example.fragments.ContentFragment.onChangeTab;
import com.example.utils.ConnectionDetector;
import com.example.utils.ServerUtulities;
import com.handmark.pulltorefresh.library.PullToRefreshWebView;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.myextend.widgets.ObservableWebView.OnNetworkDisable;
import com.squareup.picasso.Picasso;

public class MainActivity extends ActionBarActivity implements OnClickListener,
		onChangeTab {
	private Screen source;
	private boolean isShowDialog = false;
	private String mError;
	public static Activity mActivity;
	private int matchParent = LinearLayout.LayoutParams.MATCH_PARENT,
			wrapContent = LinearLayout.LayoutParams.WRAP_CONTENT, childCount,
			width;
	private FragmentTransaction fTrans;
	private LinearLayout ll;
	private ConnectionDetector cd;
	private MixpanelAPI mMixpanel;
	private ImageView triangle;
	private Point size;
	private Display display;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		cd = new ConnectionDetector(getApplicationContext());
		// Для начала получим интернет соединение
		if (!checkNetwork(savedInstanceState))
			return;
		networkOn(savedInstanceState);
	}
	
	@Override
	protected void onActivityResult(int arg0, int arg1, Intent arg2) {
		// TODO Auto-generated method stub
		super.onActivityResult(arg0, arg1, arg2);
	}
	

	private boolean checkNetwork(final Bundle savedInstanceState) {
		if (!cd.isConnectingToInternet()) {
			setContentView(R.layout.activity_connect_error);
			Button b = (Button) findViewById(R.id.refresh_ntwrk);
			b.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (cd.isConnectingToInternet())
						networkOn(savedInstanceState);
				}
			});
			return false;
		}
		return true;
	}

	/*
	 * Получив доступ к интернету инициалзируем основную часть прилоения:
	 * mixpanel, ServerUtilities - отправка данных об устройстве на контроллер
	 * сервера, сразу подключим полученные куки, создадим табы, выберем первый и
	 * подключим его фрагмент, так же при первом запуске покажем попап
	 */
	private void networkOn(Bundle savedInstanceState) {
		setContentView(R.layout.activity_main);
		source = Screen.getInstance(this);
		mMixpanel = MixpanelAPI.getInstance(this,
				"2bde03449939b8d502548cf8923753b6");
		ServerUtulities sv = ServerUtulities
				.getInstance(getApplicationContext());
		mActivity = this;
		ll = (LinearLayout) findViewById(R.id.lilLayout);
		CookieSyncManager cookieSyncManager = CookieSyncManager
				.createInstance(this);
		CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.setAcceptCookie(true);
		cookieManager.removeSessionCookie();
		cookieManager.setCookie(source.getDomain(), "did="
				+ ServerUtulities.RegDid);
		cookieSyncManager.sync();
		createTabs();
		if (savedInstanceState == null) {
			if (source.getSplashUrl() != null) {
				fTrans = getSupportFragmentManager().beginTransaction();
				fTrans.add(R.id.container, SplashFragment.newInstance());
				fTrans.commit();
			} else {
				fTrans = getSupportFragmentManager().beginTransaction();
				fTrans.add(R.id.container, ContentFragment.newInstance(source
						.getSelectTab().getId()));
				fTrans.commit();
			}
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mMixpanel.flush();
	}

	void createTabs() {
		ArrayList<Tab> tabs = source.getTabs();
		if (tabs.size() != 0) {
			for (Tab tab : tabs) {
				createTab(tab);
			}
		}
		// rePaintTabs();
		childCount = ll.getChildCount();
		triangle = (ImageView) findViewById(R.id.button111);
		triangle.getDrawable().setColorFilter(source.getTabBarSelectionColor(),
				PorterDuff.Mode.MULTIPLY);
		display = getWindowManager().getDefaultDisplay();
		size = new Point();
		display.getSize(size);
		width = size.x / ll.getChildCount();
		setTrianglePosition(true);
	}

	/*
	 * Программное создание табов, на основе данных из конфига
	 */
	void createTab(final Tab tab) {
		LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(
				matchParent, matchParent);
		LinearLayout.LayoutParams lParamsImg = new LinearLayout.LayoutParams(
				matchParent, wrapContent);
		lParams.weight = 1;
		lParamsImg.gravity = Gravity.CENTER;
		final LinearLayout newll = new LinearLayout(this);
		newll.setOrientation(LinearLayout.VERTICAL);
		newll.setPadding(5, 10, 5, 10);
		newll.setBackgroundColor(source.getTabBarBgColor());
		int id = tab.getId();
		newll.setId(id);
		tab.setId(id);

		ll.addView(newll, lParams);
		newll.setOnClickListener(this);
		ImageView imgView = new ImageView(this);
		Picasso.with(this).load(tab.getDomain() + tab.getIcon())
		// .resize(metrics.heightPixels / 15, metrics.heightPixels / 15)
				.into(imgView);
		newll.addView(imgView, lParamsImg);
		Space sp = new Space(getApplicationContext());
		newll.addView(sp, lParams);
		TextView txtTitle = new TextView(this);
		txtTitle.setGravity(Gravity.CENTER);
		txtTitle.setTextSize(16);
		txtTitle.setText(tab.getTitle());
		txtTitle.setTextColor(Color.WHITE);
		newll.addView(txtTitle);
	}

	@SuppressWarnings("unused")
	private void rePaintTabs() {

		int childCount = ll.getChildCount();
		for (int k = 0; k < childCount; k++) {
			ll.getChildAt(k).setBackgroundColor(source.getTabBarBgColor());
		}
		View v = findViewById(source.getSelectTab().getId());
		v.setBackgroundColor(source.getTabBarSelectionColor());
		// setTrianglePosition(v);
	}

	public void log(String s) {
		Log.d("logs", s);
	}

	@Override
	public void onClick(View v) {

		try {
			final JSONObject properties = new JSONObject();
			properties.put("Create Activity", "now");
			mMixpanel.track("Switch Tab", properties);
		} catch (final JSONException e) {
			throw new RuntimeException(
					"Could not encode hour of the day in JSON");
		}
		fTrans = getSupportFragmentManager().beginTransaction();
		if ((source.getSelectTab() == null)
				|| (v.getId() != source.getSelectTab().getId())) {
			if (source.getSelectTab() != null) {
				ContentFragment currentFrag = (ContentFragment) getSupportFragmentManager()
						.findFragmentById(R.id.container);
				PullToRefreshWebView viewer = (PullToRefreshWebView) currentFrag
						.getView().findViewById(R.id.pull_refresh_webview);
				viewer.getRefreshableView().stopLoading();
			}
			source.setSelectTab(v.getId());
			setTrianglePosition(false);
			ContentFragment fr = ContentFragment.newInstance(source
					.getSelectTab().getId());
			fTrans.replace(R.id.container, fr);
		} else {
			ContentFragment currentFrag = (ContentFragment) getSupportFragmentManager()
					.findFragmentById(R.id.container);
			currentFrag.showHideHeader();
			if (source.getSelectTab().getSecondUrl() != null) {
				source.getSelectTab().removeStack();
				source.getSelectTab().setSecondUrl(null);
				fTrans.detach(currentFrag);
				fTrans.attach(currentFrag);

			}

		}
		fTrans.commit();
		rePaintTabs();
	}

	public void setTrianglePosition(boolean firstLaunch) {
		int l, w, pos = 0;
		if (!firstLaunch) {
			if (triangle.getVisibility() == View.INVISIBLE)
				triangle.setVisibility(View.VISIBLE);
			l = 0;
			w = width;
			for (int k = 0; k < childCount; k++) {
				if (ll.getChildAt(k).getId() == source.getSelectTab().getId()) {
					pos = k;
					break;
				}
			}
			l = pos * w;
		} else {
			triangle.setVisibility(View.INVISIBLE);
			return;
		}
		int height = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 30, getResources()
						.getDisplayMetrics());
		FrameLayout.LayoutParams lParamsImg = new FrameLayout.LayoutParams(
				height, height);
		lParamsImg.gravity = Gravity.BOTTOM;
		lParamsImg.setMargins(l - height / 2 + (w / 2), 0, 0, 0);
		triangle.setLayoutParams(lParamsImg);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		if (source.getSelectTab() == null)
			return;
		ContentFragment cf = (ContentFragment) getSupportFragmentManager()
				.findFragmentById(R.id.container);
		cf.changeTitleAfterChangeConfig();
		display = getWindowManager().getDefaultDisplay();
		size = new Point();
		display.getSize(size);
		width = size.x / ll.getChildCount();
		setTrianglePosition(false);
	}

	@Override
	public void changeTab(Tab tab) {
		onClick(findViewById(tab.getId()));
	}

	@Override
	public void showMenu() {

	}

}
