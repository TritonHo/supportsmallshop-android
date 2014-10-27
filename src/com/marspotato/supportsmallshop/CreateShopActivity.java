package com.marspotato.supportsmallshop;

import org.joda.time.DateTime;

import android.app.Activity;
import android.os.Bundle;


public class CreateShopActivity extends Activity {


	private DateTime lastClickTime;//Just for avoiding double-click problem, no need to persistence
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_shop);
	}
}
