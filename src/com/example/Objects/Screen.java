package com.example.Objects;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONObject;




import com.example.tntapp.R;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebView.FindListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class Screen implements Serializable {
	public static final String TAB_TITLE = "title";
	public static final String TAB_URL = "url";
	public static final String TAB_ICON = "icon";
	public static final String TAB_NAME = "name";
	public static final String TAB_MENU = "menu";

	public static final String MENU_TITLE = "title";
	public static final String MENU_URL = "url";

	public static final String SPLASH_URL = "splashUrl";
	public static final String DEFAULT_URL = "defaultUrl";
	public static final String DOMAIN = "domain";

	public static final String SKIN = "skin";
	public static final String BACK_COLOR = "backgroundColor";
	public static final String TAB_BACK_COLOR = "tabBarBgColor";
	public static final String TAB_SEL_BACK_COLOR = "tabBarSelectionColor";
	public static final String HEAD_COLOR = "headerColor";

	ArrayList<Tab> tabs;
	static Context context;
	String splashUrl = null;
	String defaultUrl;
	String domain;
	int backgroundColor;
	int tabBarBgColor;
	int tabBarSelectionColor;
	int headerColor;
	Tab selectTab, curTab;
	boolean secondClick;
	public static final String TAG = "Screen";
	private static Screen instance = null;

	public static Screen getInstance(Activity ctx) {
		context = ctx.getApplicationContext();
		if (instance == null) {
			instance = new Screen();
		}
		return instance;
	}

	public Tab getTabById(int _id) {
		for (Tab tab : tabs) {
			if (tab.getId() == _id)
				return tab;
		}
		return null;
	}

	public String getSplashUrl() {
		return splashUrl;
	}

	private Screen() {
		Config config = Config.getInstance(context);
		String result = null;
		result = config.getResult();
		init(result);
		selectTab = tabs.get(0);
	}

	public String getDomain() {
		return domain;
	}

	public void setTabs(ArrayList<Tab> tabs) {
		this.tabs = tabs;
	}
	
	public Tab findTabByName(String name) {
		for (Tab t : tabs)
			if (t.getName().contains(name))
				return t;
		return null;
		
	}

	public int getbackgroundColor() {
		return backgroundColor;
	}

	public Tab getSelectTab() {
		return selectTab;
	}
	
	public void setSelectTab(int id) {
		for (Tab t : tabs)
			if (t.getId()==id)
				selectTab = t;
	}




	private String resizeTitle(String s, float f) {
		DisplayMetrics metrics = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int count = (int) ((metrics.widthPixels - f * 10) / (f / 2));
		if (s.length() > count) {
			s = s.substring(0, count);
			s = s.concat("...");
		}
		return s;
	}

	public int getTabBarBgColor() {
		return tabBarBgColor;
	}

	public int getTabBarSelectionColor() {
		return tabBarSelectionColor;
	}

	public int getHeaderColor() {
		return headerColor;
	}

	public ArrayList<Tab> getTabs() {
		return tabs;
	}

	public void init(String source) {
		try {
			JSONObject resultObject = new JSONObject(source);
			splashUrl = resultObject.getString(SPLASH_URL);
			defaultUrl = resultObject.getString(DEFAULT_URL);
			domain = resultObject.getString(DOMAIN);
			JSONArray Array = resultObject.getJSONArray("tabs");
			ArrayList<Tab> tabs = new ArrayList<Tab>();
			for (int t = 0; t < Array.length(); t++) {
				Tab newTab = new Tab(domain);
				JSONObject TabObject = Array.getJSONObject(t);
				newTab.setIcon(TabObject.getString(TAB_ICON));
				newTab.setTitle(TabObject.getString(TAB_TITLE));
				newTab.setName(TabObject.getString(TAB_NAME));
				newTab.setUrl(TabObject.getString(TAB_URL));
				JSONArray MenuArray = TabObject.getJSONArray("menu");
				ArrayList<Menu> menus = new ArrayList<Menu>();
				for (int i = 0; i < MenuArray.length(); i++) {
					JSONObject MenuObject = MenuArray.getJSONObject(i);
					Menu newMenu = new Menu();
					newMenu.setTitle(MenuObject.getString(MENU_TITLE));
					newMenu.setUrl(MenuObject.getString(MENU_URL));
					menus.add(newMenu);
				}
				newTab.setMenu(menus);
				tabs.add(newTab);
			}
			setTabs(tabs);
			JSONObject colors = resultObject.getJSONObject(SKIN);
			backgroundColor = Color.parseColor("#"
					+colors.getString(BACK_COLOR).toUpperCase());
			tabBarBgColor = Color.parseColor("#"
					+ colors.getString(TAB_BACK_COLOR).toUpperCase());
			;
			tabBarSelectionColor = Color.parseColor("#"
					+ colors.getString(TAB_SEL_BACK_COLOR).toUpperCase());
			headerColor = Color.parseColor("#"
					+ colors.getString(HEAD_COLOR).toUpperCase());
		} catch (Exception e) {
			Log.d("logs", "Что-то пошло не так в обработке джейсона");
			Log.d("logs", e.getMessage());
			e.printStackTrace();
		}
	}
}
