package com.marspotato.supportsmallshop;

import org.joda.time.DateTime;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.JsonSyntaxException;
import com.marspotato.supportsmallshop.BO.CreateUpdateShopResponseType;
import com.marspotato.supportsmallshop.BO.Submission;
import com.marspotato.supportsmallshop.util.Config;
import com.marspotato.supportsmallshop.util.RequestManager;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class ReviewCreateShopActivity extends Activity {

	private Submission submission;
	private CreateUpdateShopResponseType[] responseType;
	private String regId;
	private String helperId;

	
	private DateTime lastClickTime;//Just for avoiding double-click problem, no need to persistence
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putSerializable("submission", submission);
		savedInstanceState.putString("responseTypeJSON", Config.defaultGSON.toJson(responseType));
		savedInstanceState.putString("regId", regId);
		savedInstanceState.putString("helperId", helperId);
	}
	private void setupBlock(int fieldId, int blockId, String value)
	{
		if (value != null && value.isEmpty() == false )
		{
			TextView field = (TextView) findViewById(fieldId);
			field.setText(value);
		}
		else
			findViewById(blockId).setVisibility(View.GONE); 
	}
	private void displayData()
	{
		TextView title = (TextView) findViewById(R.id.shop_title);
		title.setText(submission.name);
		
		setupBlock(R.id.description, R.id.description_block, submission.fullDescription);
		setupBlock(R.id.address, R.id.address_block, submission.address);
		setupBlock(R.id.phone, R.id.phone_block, submission.phone);
		setupBlock(R.id.open_hours, R.id.open_hours_block, submission.openHours);		

		
		//setup phone icon
		if (submission.phone != null && submission.phone.isEmpty() == false)
		{
			ImageView phoneIcon = (ImageView) findViewById(R.id.phone_icon);
			phoneIcon.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (lastClickTime != null && lastClickTime.plusMillis(Config.AVOID_DOUBLE_CLICK_PERIOD).isAfterNow())
						return;
					lastClickTime = DateTime.now();
					Intent intent = new Intent(Intent.ACTION_DIAL);
					intent.setData(Uri.parse("tel:" + submission.phone));
					startActivity(intent);
				}
			});
		}
		//set up the address icon
		if (submission.address != null && submission.address.isEmpty() == false)
		{
			ImageView locationIcon = (ImageView) findViewById(R.id.location_icon);
			locationIcon.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (lastClickTime != null && lastClickTime.plusMillis(Config.AVOID_DOUBLE_CLICK_PERIOD).isAfterNow())
						return;
					lastClickTime = DateTime.now();
					String uri = null;
					if (submission.latitude1000000 != 0 && submission.longitude1000000 != 0)
					{
						String longitude = "" + (submission.longitude1000000 / 1000000) + "." + (submission.longitude1000000 % 1000000);
						String latitude = "" + (submission.latitude1000000 / 1000000) + "." + (submission.latitude1000000 % 1000000);			
						uri = "http://maps.google.com/maps?q=loc:" + latitude + "," + longitude + " (" + submission.name + ")";
					}
					else
						uri = "http://maps.google.com/maps?q=" + submission.address;
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
					intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
					startActivity(intent);
				}
			});
		}
	}
	@Override
	public void finish() {
		Intent intent = new Intent();
		intent.putExtra("helperId", helperId);
		setResult(RESULT_OK, intent);
		super.finish();
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.review_create_shop);

		Intent intent = getIntent();
		if (savedInstanceState != null)
		{
			submission = (Submission) savedInstanceState.getSerializable("submission");
			String responseTypeJSON = savedInstanceState.getString("responseTypeJSON");
			responseType = Config.defaultGSON.fromJson(responseTypeJSON, CreateUpdateShopResponseType[].class);
			
			regId = savedInstanceState.getString("regId");
			helperId = savedInstanceState.getString("helperId");
			displayData();
		}
		else
		{
			regId = intent.getStringExtra("regId");
			helperId = null;
			String submissionId = intent.getStringExtra("submissionId");
			
			// onPreExecute
			findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);
			findViewById(R.id.main_layout).setVisibility(View.GONE);
			getCreateShopSubmission(submissionId);
			getResponseType();
		}

	}
	private void getResponseType() {
		Response.Listener<String> listener = new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				try {
					CreateUpdateShopResponseType[] result = Config.defaultGSON.fromJson(response, CreateUpdateShopResponseType[].class);
					ReviewCreateShopActivity.this.responseType = result;
					if (ReviewCreateShopActivity.this.submission != null && ReviewCreateShopActivity.this.responseType != null)
					{
						findViewById(R.id.progress_bar).setVisibility(View.GONE);
						findViewById(R.id.main_layout).setVisibility(View.VISIBLE);
					}
				} catch (JsonSyntaxException ex) {
					// failed json parsing means the network is already hijacked
					Intent intent = new Intent(ReviewCreateShopActivity.this, ShowGenericErrorActivity.class);
					intent.putExtra("message", getString(R.string.network_redirection_error_message));
					startActivity(intent);
				}
			}
		};

		Response.ErrorListener errorListener = new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				if ((error instanceof NetworkError) || (error instanceof NoConnectionError) || (error instanceof TimeoutError)) {
					Intent intent = new Intent(ReviewCreateShopActivity.this, ShowGenericErrorActivity.class);
					intent.putExtra("message", getString(R.string.network_connection_error_message));
					startActivity(intent);
					return;
				}
			}
		};

		String url = Config.HOST_URL + "/CreateUpdateShopResponseType";
		StringRequest request = new StringRequest(Request.Method.GET, url, listener, errorListener);
		request.setRetryPolicy(new DefaultRetryPolicy(Config.DEFAULT_HTTP_TIMEOUT, Config.DEFAULT_HTTP_MAX_RETRY, 1.5f));
		RequestManager.getInstance().getRequestQueue().add(request);
	}
	
	private void getCreateShopSubmission(String submissionId) {
		Response.Listener<String> listener = new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				try {
					Submission s = Config.defaultGSON.fromJson(response, Submission.class);
					ReviewCreateShopActivity.this.submission = s;
					ReviewCreateShopActivity.this.displayData();
					if (ReviewCreateShopActivity.this.submission != null && ReviewCreateShopActivity.this.responseType != null)
					{
						findViewById(R.id.progress_bar).setVisibility(View.GONE);
						findViewById(R.id.main_layout).setVisibility(View.VISIBLE);
					}
				} catch (JsonSyntaxException ex) {
					// failed json parsing means the network is already hijacked
					Intent intent = new Intent(ReviewCreateShopActivity.this, ShowGenericErrorActivity.class);
					intent.putExtra("message", getString(R.string.network_redirection_error_message));
					startActivity(intent);
				}
			}
		};

		Response.ErrorListener errorListener = new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				if ((error instanceof NetworkError) || (error instanceof NoConnectionError) || (error instanceof TimeoutError)) {
					Intent intent = new Intent(ReviewCreateShopActivity.this, ShowGenericErrorActivity.class);
					intent.putExtra("message", getString(R.string.network_connection_error_message));
					startActivity(intent);
					return;
				}
			}
		};

		String url = Config.HOST_URL + "/CreateShopSubmission?submissionId=" + submissionId;
		StringRequest request = new StringRequest(Request.Method.GET, url, listener, errorListener);
		request.setRetryPolicy(new DefaultRetryPolicy(Config.DEFAULT_HTTP_TIMEOUT, Config.DEFAULT_HTTP_MAX_RETRY, 1.5f));
		RequestManager.getInstance().getRequestQueue().add(request);
	}
	
}
