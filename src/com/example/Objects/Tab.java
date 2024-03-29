package com.example.Objects;

import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;
import java.util.UUID;

import com.example.fragments.ContentFragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.net.Uri;



@SuppressLint("NewApi") public class Tab {
	String url;
	String title, currentTitle;
	String icon;
	String name;
	String domain;
	String secondUrl = null;
	int id;
	Stack<String> links;
	ArrayList<Menu> menu;
	
	public String getMenuTittleByUrl (String url) {
		for (Menu m : menu) {
			if (url.equals(domain+m.getUrl())) 
				return m.getTitle();
		}
		return "";
			
	}

	public String getSecondUrl() {
		return secondUrl;
	}



	public void setSecondUrl(String secondUrl) {
		this.secondUrl = secondUrl;
	}



	public String getCurrentTitle() {
		/*Uri uri;
		if (getSizeOfUrl()>0) {
			uri = Uri.parse(getLastUrl());
			uri.getQueryParameter("")
		}*/
		return currentTitle;
	}

	public void setCurrentTitle(String currentTitle) {
		this.currentTitle = currentTitle;
	}

	public void popUrl() {
		links.pop();
	}
	
	public void removeStack(){
		links.clear();
	}
	
	public void addUrl(String url) {
		links.push(url);
	}
	
	public String getLastUrl() {
		return links.get(links.size()-1);
	}
	
	public int getSizeOfUrl() {
		return links.size();
	}
	
	
	public void setId(int _id) {
		id = _id;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public void setIcon(String icon) {
		this.icon = icon;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setMenu(ArrayList<Menu> menu) {
		this.menu = menu;
	}
	
	public String getUrl() {
		return this.url;
	}
	
	public String getTitle() {
		return this.title;
	}

	
	public String getIcon() {
		return this.icon;
	}
	
	public ArrayList<Menu> getMenu() {
		return this.menu;
	}
	
	public int getId() {
		return id;
	}
	
	public String getDomain() {
		return domain;
	}
	
	public String getName() {
		return name;
	}
	
	
	
	public Tab(String _domain) {
		domain = _domain;
		id = Math.abs(new Random().nextInt());
		links = new Stack<String>();
	}




	
}
