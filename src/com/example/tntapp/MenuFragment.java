package com.example.tntapp;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import com.example.Objects.Menu;
import com.example.Objects.Screen;

public class MenuFragment extends DialogFragment {
	ListView menus;
	ArrayList<Menu> list;
	Screen src;
	int id;
	WebView view;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		src = Screen.getInstance(getActivity());
		getDialog().setTitle(src.getSelectTab().getTitle());
		id = getArguments().getInt("id");
		View v = inflater.inflate(R.layout.fragment_menu, null);
		list = src.getSelectTab().getMenu();
		menus = (ListView) v.findViewById(R.id.listView1);
		menus.setAdapter(new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, list));
		menus.setOnItemClickListener(new OnItemClickListener() {

			@SuppressLint("NewApi") @Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				view = (WebView) getFragmentManager().findFragmentById(id).getView().findViewById(R.id.viewer);
				src.getSelectTab().setSecondUrl(src.getDomain()+list.get(arg2).getUrl());
				src.getSelectTab().setCurrentTitle(list.get(arg2).getTitle());
				view.loadUrl(src.getDomain()+list.get(arg2).getUrl());
				TextView title = (TextView)getFragmentManager().findFragmentById(id).getView().findViewById(R.id.title);
				title.setText(list.get(arg2).getTitle());
				dismiss();
				
			}
		});
		return v;
	}
}