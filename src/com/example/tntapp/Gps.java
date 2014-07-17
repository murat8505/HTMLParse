package com.example.tntapp;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.widget.Toast;

public class Gps {

	private static Gps instance = null;
	static Context context;
	static LocationManager manager;
	static Location location;
	private static LocationListener mLocationListener;
	private static final long MINIMUM_DISTANCE_FOR_UPDATES = 1; // в метрах
	private static final long MINIMUM_TIME_BETWEEN_UPDATES = 1000; // в мс

	public static Gps getInstance(Context _context) {
		context = _context.getApplicationContext();
		if (instance == null)
			instance = new Gps();
		else
			checkEnableGPS();
		return instance;
	}

	public static String getLocation() {
		String provider = Settings.Secure.getString(
				context.getContentResolver(),
				Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
		if (!provider.equals("")) {
			// GPS Enabled
			int i = 0;
			while ((location == null) || (i < 100)) {
				if (provider.contains("network")) {
					i = i + 10;
					manager.requestLocationUpdates(
							LocationManager.NETWORK_PROVIDER,
							MINIMUM_TIME_BETWEEN_UPDATES,
							MINIMUM_DISTANCE_FOR_UPDATES, mLocationListener);
					location = manager
							.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
				} else if (provider.contains("gps")) {
					i++;
					manager.requestLocationUpdates(
							LocationManager.GPS_PROVIDER,
							MINIMUM_TIME_BETWEEN_UPDATES,
							MINIMUM_DISTANCE_FOR_UPDATES, mLocationListener);
					location = manager
							.getLastKnownLocation(LocationManager.GPS_PROVIDER);
				}
			}
		}
		if (location == null) {
			return "GPS выключен, повторите попытку";
		}
		String s = String.valueOf(location.getLatitude()) + ","
				+ String.valueOf(location.getLongitude());
		if (s == null)
			s = "";
		return s;
	}

	private Gps() {
		PackageManager pm = context.getPackageManager();
		mLocationListener = new MyLocationListener();
		manager = (LocationManager) MainActivity.mActivity
				.getSystemService(Context.LOCATION_SERVICE);
		/*
		 * String gps_provider = LocationManager.GPS_PROVIDER; String
		 * network_provider = LocationManager.NETWORK_PROVIDER; LocationProvider
		 * gpsProvider; gpsProvider = manager.getProvider(gps_provider);
		 */

		if (pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)) {
			checkEnableGPS();
		}
		Toast.makeText(
				context,
				pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS) ? "Есть GPS"
						: "Нет GPS", Toast.LENGTH_LONG).show();
	}

	private static void checkEnableGPS() {
		String provider = Settings.Secure.getString(
				context.getContentResolver(),
				Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
		if (!provider.equals("")) {
			// GPS Enabled
			if (provider.contains("gps")) {
				manager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
						MINIMUM_TIME_BETWEEN_UPDATES,
						MINIMUM_DISTANCE_FOR_UPDATES, mLocationListener);
			} else if (provider.equals("network")) {
				manager.requestLocationUpdates(
						LocationManager.NETWORK_PROVIDER,
						MINIMUM_TIME_BETWEEN_UPDATES,
						MINIMUM_DISTANCE_FOR_UPDATES, mLocationListener);
			}

		} else {
			new AlertMessage("GPS выключен", "Включить GPS?",
					new OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							MainActivity.mActivity
									.startActivity(new Intent(
											android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
						}
					}, new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							dialog.cancel();
						}
					});
			Toast.makeText(context, "GPS disabled", Toast.LENGTH_LONG).show();
			// tvInfo.setText("GPS выключен");
		}
	}

	private class MyLocationListener implements LocationListener {

		public void onLocationChanged(Location _location) {
			/*
			 * String message = String.format(
			 * "Новое местоположение \n Долгота: %1 \n Ширина: %2",
			 * location.getLongitude(), location.getLatitude());
			 */
			location = _location;
			// showCurrentLocation();

		}

		public void onStatusChanged(String s, int i, Bundle b) {
		}

		public void onProviderDisabled(String s) {
		}

		public void onProviderEnabled(String s) {
		}
	}
}
