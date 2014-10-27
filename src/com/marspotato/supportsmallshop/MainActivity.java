package com.marspotato.supportsmallshop;


import org.joda.time.DateTime;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.marspotato.supportsmallshop.util.Config;


import android.os.Bundle;
import android.app.Activity;

import android.content.Intent;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity {

	private DateTime lastClickTime;//Just for avoiding double-click problem, no need to persistence

	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

	private void checkPlayServices() {
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
				GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
			else {
		        Toast.makeText(this, this.getString(R.string.google_play_service_error_message), Toast.LENGTH_LONG).show();
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		checkPlayServices();
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
		startActivity(intent);
	}
	public void gotoReviewShopChangeAction(View view) {
		if (lastClickTime != null && lastClickTime.plusMillis(Config.AVOID_DOUBLE_CLICK_PERIOD).isAfterNow())
			return;
		lastClickTime = DateTime.now();
		
		//TODO: implement it
		//Intent intent = new Intent(this, ShopListActivity.class);
		//startActivity(intent);
	}
	
	
	
	public void aboutUsAction(View view) {
		if (lastClickTime != null && lastClickTime.plusMillis(Config.AVOID_DOUBLE_CLICK_PERIOD).isAfterNow())
			return;
		lastClickTime = DateTime.now();
		//TODO:
		Intent intent = new Intent(this, AboutUsActivity.class);
		startActivity(intent);
	}
}
