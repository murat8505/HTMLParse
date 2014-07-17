package com.example.tntapp;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;

public class AlertMessage {
	Builder ad;
	public AlertMessage(Context context){
		ad = new AlertDialog.Builder(context);
	    ad.setMessage("Сервер не отвечает, попробуйте позже"); 
	    ad.setPositiveButton("OK", null);
	    ad.show();
	}
	
	public AlertMessage(Context mContext, String message){
		ad = new AlertDialog.Builder(mContext);
	    ad.setMessage(message); 
	    ad.setPositiveButton("OK", null);
	    ad.show();
	}
	
	public AlertMessage(Context mContext, String title, String message){
		ad = new AlertDialog.Builder(mContext);
		ad.setTitle(title); 
	    ad.setMessage(message); 
	    ad.setPositiveButton("OK", null);
	    ad.show();
	}
	
	
	public AlertMessage(String title, String message,
			OnClickListener onClickPositive, OnClickListener onClickNegative) {
		ad = new AlertDialog.Builder(MainActivity.mActivity);
		ad.setTitle(title); 
	    ad.setMessage(message); 
	    ad.setPositiveButton("Ok", onClickPositive);
	    ad.setNegativeButton("Cancel", onClickNegative);
	    ad.show();
	}
}
