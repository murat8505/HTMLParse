package com.example.tntapp;

import java.util.ArrayList;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.Objects.Screen;
import com.example.Objects.Tab;
import com.example.tntapp.ContentFragment.onChangeTab;
import com.squareup.picasso.Picasso;


public class MainActivity extends ActionBarActivity implements OnClickListener, onChangeTab {
	Screen source;
	public static Activity 		mActivity;
	int 						matchParent = LinearLayout.LayoutParams.MATCH_PARENT,
								wrapContent = LinearLayout.LayoutParams.WRAP_CONTENT;
	FragmentTransaction 		fTrans;
	LinearLayout 				ll;
	DialogFragment 				menuDialog;
	ConnectionDetector 			cd;
	
	private boolean checkNetwork(final Bundle savedInstanceState) {
		if (!cd.isConnectingToInternet()) {
            // Internet Connection is not present
        	setContentView(R.layout.activity_connect_error);
        	Button b = (Button)findViewById(R.id.refresh_ntwrk);
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
	
	private void networkOn(Bundle savedInstanceState) {
		setContentView(R.layout.activity_main);
        source = Screen.getInstance(this);
        mActivity = this;
        menuDialog = new MenuFragment();
        ll = (LinearLayout) findViewById(R.id.lilLayout);
        createTabs();
        if (savedInstanceState== null) {
			if (source.getSplashUrl() != null) {
				DialogFragment popup = new PopupDialog();
				popup.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
				Bundle b = new Bundle();
				b.putString("splash", source.getSplashUrl());
				popup.setArguments(b);
				popup.show(getSupportFragmentManager(), "PopupDialog");
			}
	        fTrans = getSupportFragmentManager().beginTransaction();
			fTrans.add(R.id.container, source.getSelectTab().getFragment());
			fTrans.commit();
        }
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        cd = new ConnectionDetector(getApplicationContext());
        if (!checkNetwork(savedInstanceState))
        	return;
        networkOn(savedInstanceState);
        // Check if Internet present
        
    }
    


	void createTabs() {
		ArrayList<Tab> tabs = source.getTabs();
		if (tabs.size() != 0) {
			for (Tab tab : tabs) {
				createTab(tab);
			}
		}
		rePaintTabs();
	}
	
	void createTab(final Tab tab) {
		LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(
				matchParent, matchParent);
		LinearLayout.LayoutParams lParamsImg = new LinearLayout.LayoutParams(
				matchParent, wrapContent);
		lParams.weight = 1;
		lParamsImg.gravity = Gravity.CENTER;
		final LinearLayout newll = new LinearLayout(this);
		newll.setOrientation(LinearLayout.VERTICAL);
		newll.setPadding(20, 20, 20, 20);
		newll.setBackgroundColor(source.getTabBarBgColor());
		int id = tab.getId();
		newll.setId(id);
		tab.setId(id);
		/*
		 * int[] pressedState = { android.R.attr.state_pressed }; int[]
		 * defaultState = { android.R.attr.state_enabled }; ColorDrawable
		 * colorPressed = new ColorDrawable(Color.parseColor("#" +
		 * src.getTabBarSelectionColor().toUpperCase())); ColorDrawable
		 * colorDefault = new ColorDrawable(Color.parseColor("#" +
		 * src.getTabBarBgColor().toUpperCase()));
		 * 
		 * StateListDrawable drawable = new StateListDrawable();
		 * drawable.addState(pressedState, colorPressed);
		 * drawable.addState(defaultState, colorDefault);
		 * newll.setBackground(drawable); Очень конечно круто, что я нашел как
		 * программно сделать селектор, но нужно то что бы было по другому -
		 * намного проще, надеюсь понадобится кога нибудь. Програмная реализация
		 * селектора цвета для лэйаут файла
		 */
		ll.addView(newll, lParams);
		newll.setOnClickListener(this);
		ImageView imgView = new ImageView(this);
		Picasso.with(this).load(tab.getDomain() + tab.getIcon())
				//.resize(metrics.heightPixels / 15, metrics.heightPixels / 15)
				.into(imgView);
		newll.addView(imgView, lParamsImg);
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
	}

    /**
     * A placeholder fragment containing a simple view.
     */
    /*public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }*/


	@Override
	public void onClick(View v) {
		source.setSelectTab(v.getId());
		ContentFragment currentFrag = (ContentFragment) getSupportFragmentManager().findFragmentById(R.id.container);
		ContentFragment fr = source.getSelectTab().getFragment();
		fTrans = getSupportFragmentManager().beginTransaction();
		if (currentFrag==source.getSelectTab().getFragment()) {
			currentFrag.showHideHeader();
			if (source.getSelectTab().getSecondUrl()!=null) {
				source.getSelectTab().removeStack();
				source.getSelectTab().setSecondUrl(null);
				fTrans.detach(currentFrag);
				fTrans.attach(currentFrag);
				
			}
		} else {
			WebView viewer = (WebView)currentFrag.getView().findViewById(R.id.viewer);
			viewer.stopLoading();
			fTrans.replace(R.id.container, source.getSelectTab().getFragment());
		}
		fTrans.commit();
		rePaintTabs();
	}

	
	@Override
	public void changeTab(Tab tab) {
		onClick(findViewById(tab.getId()));
	}


	@Override
	public void showMenu() {
		Bundle b = new Bundle();
		b.putInt("id", R.id.container);
		menuDialog.setArguments(b);
		menuDialog.show(getSupportFragmentManager(), "menuDialog");
	}
	

}
