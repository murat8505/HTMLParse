package com.example.Objects;

public class Menu {
	String url;
	String title;
	String icon = null;
	
	public String getIcon() {
		return icon;
	}
	

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return title;
	}
	
	public String getUrl() {
		return url;
	}
	
	public String getTitle() {
		return title;
	}
	
	public Menu() {

	}
}
