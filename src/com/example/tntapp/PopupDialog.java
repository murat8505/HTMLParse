package com.example.tntapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

@SuppressLint("ValidFragment") public class PopupDialog extends DialogFragment {
	String splashUrl;
	
	/*@SuppressLint("ValidFragment") public PopupDialog(String url) {
		splashUrl = url;
	}*/
	
	public PopupDialog() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.dialog_popup, null);
		ImageView img = (ImageView) v.findViewById(R.id.image);
		getDialog().getWindow().getAttributes().windowAnimations=R.style.DialogAnimation;
		img.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
		Picasso.with(getActivity()).load(Uri.parse(getArguments().getString("splash"))).into(img);
		return v;
	}
	
	
	public void onDismiss(DialogInterface dialog) {
	    super.onDismiss(dialog);
	  }

	  public void onCancel(DialogInterface dialog) {
	    super.onCancel(dialog);
	  }
}
