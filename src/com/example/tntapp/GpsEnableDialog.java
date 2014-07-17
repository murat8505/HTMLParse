package com.example.tntapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View.OnClickListener;

public class GpsEnableDialog extends DialogFragment implements android.content.DialogInterface.OnClickListener {
	
	public Dialog onCreateDialog(Bundle savedInstanceState) {
	    AlertDialog.Builder adb = new AlertDialog.Builder(getActivity())
	        .setTitle("GPS выключен").setPositiveButton(R.string.yes, this)
	        .setNegativeButton(R.string.no, this)
	        .setMessage(R.string.message_text);
	    return adb.create();
	  }

	  public void onClick(DialogInterface dialog, int which) {
	    int i = 0;
	    switch (which) {
	    case Dialog.BUTTON_POSITIVE:
	      i = R.string.yes;
	      break;
	    case Dialog.BUTTON_NEGATIVE:
	      i = R.string.no;
	      break;
	    }
	  }

	  public void onDismiss(DialogInterface dialog) {
	    super.onDismiss(dialog);
	  }

	  public void onCancel(DialogInterface dialog) {
	    super.onCancel(dialog);
	  }
}
