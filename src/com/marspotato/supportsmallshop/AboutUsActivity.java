package com.marspotato.supportsmallshop;


import android.os.Bundle;
import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.text.Html;
import android.view.Window;
import android.widget.TextView;

public class AboutUsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setBackgroundDrawable(new ColorDrawable(0));
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about_us);
		
		TextView message = (TextView) this.findViewById(R.id.about_us_message);
		message.setText( Html.fromHtml(getString(R.string.about_us_message)));
	}
}
