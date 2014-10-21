package com.marspotato.supportsmallshop;


import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;
import android.widget.TextView;

public class ShowGenericErrorActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setBackgroundDrawable(new ColorDrawable(0));
		super.onCreate(savedInstanceState);
		setContentView(R.layout.error);
		
		if (savedInstanceState == null) {
			Intent intent = getIntent();
			String message = intent.getStringExtra("message");
			
			TextView errorMessage = (TextView) this.findViewById(R.id.error_message);
			errorMessage.setText(message);
		}
	}
}
