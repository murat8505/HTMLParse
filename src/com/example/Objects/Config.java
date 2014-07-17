package com.example.Objects;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import com.example.Objects.*;
import com.example.tntapp.R;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class Config  {
	private String mResult;
	private static Config instance = null;
	
	
	public String getResult() {
		return mResult;
	}

	public static Config getInstance(Context context) {
		if (instance==null)
			instance = new Config(context);
		return instance;
	}
	
	private Config(Context context) {
		try {
			mResult = new MyAsync().execute(context.getString(R.string.config_link)).get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	
	public class MyAsync extends AsyncTask<String, String, String> {
		@Override
		protected String doInBackground(String... params) {
			StringBuilder responseBuilder = new StringBuilder();
			// создаем запрос на сервер
			HttpClient Client = new DefaultHttpClient();
			try {
				HttpGet Get = new HttpGet(params[0]);
				// получаем ответ от сервера
				HttpResponse Response = Client.execute(Get);
				StatusLine searchStatus = Response.getStatusLine();
				if (searchStatus.getStatusCode() == 200) {
					HttpEntity Entity = Response.getEntity();
					InputStream Content = Entity.getContent();
					InputStreamReader Input = new InputStreamReader(Content);
					BufferedReader Reader = new BufferedReader(Input);
					String lineIn;
					while ((lineIn = Reader.readLine()) != null) {
						responseBuilder.append(lineIn);
					}
				} else
					Log.d("logs",
							"„Что-то пошло не так при установки связи с сервом");
			} catch (Exception e) {
				e.printStackTrace();
				Log.d("logs", e.getMessage());
				return null;
			}

			String res = responseBuilder.toString();
			return res;
		}

		@Override
		protected void onPostExecute(String result) {
			
			// TODO Auto-generated method stub
			mResult = result;
			super.onPostExecute(result);
		}
	}

}
