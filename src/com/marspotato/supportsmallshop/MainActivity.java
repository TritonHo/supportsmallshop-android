package com.marspotato.supportsmallshop;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.joda.time.DateTime;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.marspotato.supportsmallshop.util.Config;
import com.marspotato.supportsmallshop.util.RequestManager;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity {

	private DateTime lastClickTime;//Just for avoiding double-click problem, no need to persistence

	private static final int CHILDREN_RESULT_CODE = 9000;
	private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	private static final String PROPERTY_REG_ID = "registrationId";
	private static final String PROPERTY_HELPER_ID = "helperId";
	private static final String PROPERTY_APP_VERSION = "appVersion";

	private String getField(String fieldName) {
		final SharedPreferences prefs = getSharedPreferences(MainActivity.class.getSimpleName(), Context.MODE_PRIVATE);
		return prefs.getString(fieldName, "");
	}

	private void storeField(String fieldName, String value) {
		final SharedPreferences prefs = getSharedPreferences(MainActivity.class.getSimpleName(), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(fieldName, value);
		editor.commit();
	}
	private static int getAppVersion(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			// should never happen
			throw new RuntimeException("Could not get package name: " + e);
		}
	}
	
	private void checkPlayServices() {
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
				GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
			else {
				//TODO: the message should show once only
		        Toast.makeText(this, this.getString(R.string.google_play_service_error_message), Toast.LENGTH_LONG).show();
			}
		}
	}
	private void processGCMId(String oldRegId, String newRegId)
	{
		String helperId = getField(PROPERTY_HELPER_ID);
		Log.d("newRegId", "newRegId = " + newRegId);
		storeField(PROPERTY_REG_ID, newRegId);
		storeField(PROPERTY_APP_VERSION, "" + getAppVersion(this.getApplicationContext()));
		
		if (oldRegId.isEmpty() != false && helperId.isEmpty() != false)
			return;//no further action is needed
				
		//update the helperId and regId mapping in server
		Response.Listener<String> listener = new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				//nothing to process
			}
		};
		Response.ErrorListener errorListener = new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				//if the PUT request failed, it matter little to the end-user
				//Also, the chance that successfully get the GCM, but failed to communicate with the error is small
				//thus no need to handle the error
			}
		};
		String url = "";
		try {
			url = Config.HOST_URL + "/Helper?deviceType=" + Config.deviceType 
					+ "&id=" + URLEncoder.encode(helperId, "UTF-8")
					+ "&oldRegId=" + URLEncoder.encode(oldRegId, "UTF-8")
					+ "&newRegId=" + URLEncoder.encode(newRegId, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// should never reach this line
			e.printStackTrace();
		}
		StringRequest request = new StringRequest(Request.Method.PUT, url, listener, errorListener);
		request.setRetryPolicy(new DefaultRetryPolicy(Config.DEFAULT_HTTP_TIMEOUT, Config.DEFAULT_HTTP_MAX_RETRY, 1.5f));
		RequestManager.getInstance().getRequestQueue().add(request);
	}
	private void registerGCM() {
		//TODO: add checking if Google Play Service exists
		final String originalRegId = getField(PROPERTY_REG_ID);
		int registeredVersion = Integer.MIN_VALUE;
		try {
			registeredVersion = Integer.parseInt(getField(PROPERTY_APP_VERSION));
		} catch (Exception ex) {
			// no need to handle it;
		}
		Log.d("originalRegId", "originalRegId = " + originalRegId);
		
		if (originalRegId.isEmpty() == false && registeredVersion == getAppVersion(this.getApplicationContext()))
			return;//valid GCM registration ID, no need for further action
		
		//get a new registration ID
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				try {
					GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(MainActivity.this.getApplicationContext());
					String newRegid = gcm.register(Config.GCM_SENDER_ID);

					processGCMId(originalRegId, newRegid);
				} catch (IOException ex) {
					//TODO: think about the error handling
				}
				return null;
			}

		}.execute(null, null, null);
	}
	
	

	@Override
	protected void onResume() {
		super.onResume();

		checkPlayServices();
		registerGCM();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		checkPlayServices();
	}

	public void gotoShopListAction(View view) {
		if (lastClickTime != null && lastClickTime.plusMillis(Config.AVOID_DOUBLE_CLICK_PERIOD).isAfterNow())
			return;
		lastClickTime = DateTime.now();
		Intent intent = new Intent(this, ShopListActivity.class);
		startActivity(intent);
	}
	public void gotoCreateShopAction(View view) {
		if (lastClickTime != null && lastClickTime.plusMillis(Config.AVOID_DOUBLE_CLICK_PERIOD).isAfterNow())
			return;
		lastClickTime = DateTime.now();
		
		Intent intent = new Intent(this, CreateShopActivity.class);
		intent.putExtra("regId", getField(PROPERTY_REG_ID));
		startActivityForResult(intent, CHILDREN_RESULT_CODE);
	}
	public void gotoReviewShopChangeAction(View view) {
		if (lastClickTime != null && lastClickTime.plusMillis(Config.AVOID_DOUBLE_CLICK_PERIOD).isAfterNow())
			return;
		lastClickTime = DateTime.now();
	}
	
	public void aboutUsAction(View view) {
		if (lastClickTime != null && lastClickTime.plusMillis(Config.AVOID_DOUBLE_CLICK_PERIOD).isAfterNow())
			return;
		lastClickTime = DateTime.now();
		//TODO:
		Intent intent = new Intent(this, AboutUsActivity.class);
		startActivity(intent);
	}
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		String helperId = data.getStringExtra("helperId");
		if (helperId != null)
			this.storeField(PROPERTY_HELPER_ID, helperId);
	}
}
