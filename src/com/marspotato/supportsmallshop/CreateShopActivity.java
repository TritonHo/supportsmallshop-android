package com.marspotato.supportsmallshop;

import org.joda.time.DateTime;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;


public class CreateShopActivity extends Activity {


	private DateTime lastClickTime;//Just for avoiding double-click problem, no need to persistence
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_shop);
		
		Spinner spinner = (Spinner) findViewById(R.id.shop_type_spinner);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.shop_type_display_values , android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
	}
}
