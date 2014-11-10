package com.marspotato.supportsmallshop;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Vector;

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
import com.marspotato.supportsmallshop.BO.CreateUpdateShopResponse;
import com.marspotato.supportsmallshop.BO.CreateUpdateShopResponseType;
import com.marspotato.supportsmallshop.BO.CreateShopSubmission;
import com.marspotato.supportsmallshop.gcm.GcmIntentService;
import com.marspotato.supportsmallshop.output.CreateShopSubmissionOutput;
import com.marspotato.supportsmallshop.util.AuthCodeRequester;
import com.marspotato.supportsmallshop.util.AuthCodeUtil;
import com.marspotato.supportsmallshop.util.Config;
import com.marspotato.supportsmallshop.util.ImageUtil;
import com.marspotato.supportsmallshop.util.MiscUtil;
import com.marspotato.supportsmallshop.util.RequestManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class ReviewCreateShopActivity extends Activity implements AuthCodeRequester {
	private BroadcastReceiver authCodeIntentReceiver;
	
	private CreateShopSubmissionOutput submissionOutput;
	private CreateUpdateShopResponseType[] responseTypes;
	private String regId;
	private String helperId;
	private CreateUpdateShopResponseType selectedResponse;
	

	
	private DateTime lastClickTime;//Just for avoiding double-click problem, no need to persistence
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putSerializable("submissionOutput", submissionOutput);
		savedInstanceState.putString("responseTypesJSON", Config.defaultGSON.toJson(responseTypes));
		savedInstanceState.putString("regId", regId);
		savedInstanceState.putString("helperId", helperId);
		savedInstanceState.putSerializable("selectedResponse", selectedResponse);
		
	}
	private void setupBlock(int fieldId, int blockId, String value)
	{
		if (value != null && value.isEmpty() == false )
		{
			findViewById(blockId).setVisibility(View.VISIBLE); 
			TextView field = (TextView) findViewById(fieldId);
			field.setText(value);
		}
		else
			findViewById(blockId).setVisibility(View.GONE); 
	}
	
	private void setupLocationIcon(int viewId, final int longitude1000000, final int latitude1000000)
	{
		ImageView locationIcon = (ImageView) findViewById(viewId);
		if (longitude1000000 == 0 && latitude1000000 == 0)
		{
			locationIcon.setVisibility(View.GONE);
			return;
		}
		locationIcon.setVisibility(View.VISIBLE);
		locationIcon.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (lastClickTime != null && lastClickTime.plusMillis(Config.AVOID_DOUBLE_CLICK_PERIOD).isAfterNow())
					return;
				lastClickTime = DateTime.now();
				String uri = null;		
					uri = "http://maps.google.com/maps?q=loc:" + MiscUtil.getLatLngString(latitude1000000) + "," + MiscUtil.getLatLngString(longitude1000000);

				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
				intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
				startActivity(intent);
			}
		});
	}
	private void displayData()
	{
		final CreateShopSubmission submission = submissionOutput.s;
		TextView title = (TextView) findViewById(R.id.shop_title);
		title.setText(submission.name);
		
		setupBlock(R.id.shop_type, R.id.shop_type_block, submission.shopType);
		setupBlock(R.id.district, R.id.district_block,  MiscUtil.getDistrictName(this, submission.district));
		setupBlock(R.id.address, R.id.address_block, submission.address);

		setupBlock(R.id.phone, R.id.phone_block, submission.phone);
		setupBlock(R.id.short_desc, R.id.short_desc_block, submission.shortDescription);
		setupBlock(R.id.full_desc, R.id.full_desc_block, submission.fullDescription);
		setupBlock(R.id.open_hours, R.id.open_hours_block, submission.openHours);		
		setupBlock(R.id.search_tags, R.id.search_tags_block, submission.searchTags);		

		if (submission.latitude1000000 == 0 && submission.longitude1000000 == 0)
			findViewById(R.id.coordinates_block).setVisibility(View.GONE);
		else
		{
			findViewById(R.id.coordinates_block).setVisibility(View.VISIBLE);
			setupLocationIcon(R.id.location_icon, submission.longitude1000000, submission.latitude1000000);
			
			TextView lat = (TextView) findViewById(R.id.lat);
			TextView lng = (TextView) findViewById(R.id.lng);
			lat.setText(MiscUtil.getLatLngString(submission.latitude1000000));
			lng.setText(MiscUtil.getLatLngString(submission.longitude1000000));
		}

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
		
		Button acceptButton = (Button) findViewById(R.id.accept_button);
		Button rejectButton = (Button) findViewById(R.id.reject_button);
		if (submissionOutput.isCreator == true || submissionOutput.isReviewer == true)
		{
			acceptButton.setBackgroundResource(R.drawable.button_4w_dimmed);
			rejectButton.setBackgroundResource(R.drawable.button_4w_dimmed);
			acceptButton.setTextColor(getResources().getColor(R.color.button_dim_text_color));
			rejectButton.setTextColor(getResources().getColor(R.color.button_dim_text_color));
		}
		else
		{
			acceptButton.setBackgroundResource(R.drawable.button_4w);
			rejectButton.setBackgroundResource(R.drawable.button_4w);
			acceptButton.setTextColor(Color.WHITE);
			rejectButton.setTextColor(Color.WHITE);
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
    protected void onPause() {
        super.onPause();
		LocalBroadcastManager.getInstance(this).unregisterReceiver(authCodeIntentReceiver);
    }
    @Override
    protected void onResume() {
        super.onResume();
        
        authCodeIntentReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if ( findViewById(R.id.progress_bar).getVisibility() != View.VISIBLE)
					return;//it is not submitting data, thus simply ignore the authCode
				String authCode = intent.getStringExtra("authCode");
				receiveAuthCode(authCode);
			}
		};
		LocalBroadcastManager.getInstance(this).registerReceiver(authCodeIntentReceiver, new IntentFilter(GcmIntentService.GCM_AUTH_CODE));
    }
	public boolean onOptionsItemSelected(MenuItem item) {
	    if (item.getItemId() == android.R.id.home) {
	        finish();
	        return true;
	    } else {
	        return super.onOptionsItemSelected(item);
	    }
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.review_create_shop);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		Intent intent = getIntent();
		if (savedInstanceState != null)
		{
			submissionOutput = (CreateShopSubmissionOutput) savedInstanceState.getSerializable("submissionOutput");
			String responseTypeJSON = savedInstanceState.getString("responseTypesJSON");
			responseTypes = Config.defaultGSON.fromJson(responseTypeJSON, CreateUpdateShopResponseType[].class);
			
			regId = savedInstanceState.getString("regId");
			helperId = savedInstanceState.getString("helperId");
			selectedResponse = (CreateUpdateShopResponseType) savedInstanceState.getSerializable("selectedResponse");
			
			displayData();
		}
		else
		{
			regId = intent.getStringExtra("regId");
			helperId = intent.getStringExtra("helperId");
			String submissionId = intent.getStringExtra("submissionId");
			selectedResponse = null;
			
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
					ReviewCreateShopActivity.this.responseTypes = result;
					if (ReviewCreateShopActivity.this.submissionOutput != null && ReviewCreateShopActivity.this.responseTypes != null)
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
					CreateShopSubmissionOutput submissionOutput = Config.defaultGSON.fromJson(response, CreateShopSubmissionOutput.class);
					ReviewCreateShopActivity.this.submissionOutput = submissionOutput;
					ReviewCreateShopActivity.this.displayData();
					if (ReviewCreateShopActivity.this.submissionOutput != null && ReviewCreateShopActivity.this.responseTypes != null)
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

		String url = Config.HOST_URL + "/CreateShopSubmission?submissionId=" + submissionId + "&helperId=" + helperId; 
		
		StringRequest request = new StringRequest(Request.Method.GET, url, listener, errorListener);
		request.setRetryPolicy(new DefaultRetryPolicy(Config.DEFAULT_HTTP_TIMEOUT, Config.DEFAULT_HTTP_MAX_RETRY, 1.5f));
		RequestManager.getInstance().getRequestQueue().add(request);
	}
	private void receiveAuthCode(String authCode)
    {
    	Response.Listener<String> listener = new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				try {
					findViewById(R.id.progress_bar).setVisibility(View.GONE);
					CreateUpdateShopResponse cusr = Config.defaultGSON.fromJson(response, CreateUpdateShopResponse.class);
					ReviewCreateShopActivity.this.helperId = cusr.helperId;
					
			        Toast.makeText(ReviewCreateShopActivity.this, getString(R.string.success_review), Toast.LENGTH_LONG).show();
			        ReviewCreateShopActivity.this.finish();
				} catch (Exception ex) {
					ReviewCreateShopActivity.this.onSendAuthCodeRequestError(Config.WIFI_ERROR);
				}
			}
		};

		Response.ErrorListener errorListener = new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				if ((error instanceof NetworkError) || (error instanceof NoConnectionError) || (error instanceof TimeoutError)) {
					ReviewCreateShopActivity.this.onSendAuthCodeRequestError(Config.NETWORK_ERROR);
				}
				else
					ReviewCreateShopActivity.this.onSendAuthCodeRequestError(Config.OTHERS_ERROR);
			}
		};

		String url = "";
		try {
			url = Config.HOST_URL + "/CreateUpdateShopResponse?submissionType=create&code=" + URLEncoder.encode(authCode, "UTF-8") 
					+ "&submissionId=" + URLEncoder.encode(submissionOutput.s.id , "UTF-8")
					+ "&responseTypeId=" + selectedResponse.id;
		} catch (UnsupportedEncodingException e) {
			// should never reach this line
			e.printStackTrace();
		}
		StringRequest request = new StringRequest(Request.Method.POST, url, listener, errorListener);
		request.setRetryPolicy(new DefaultRetryPolicy(Config.DEFAULT_HTTP_TIMEOUT, Config.DEFAULT_HTTP_MAX_RETRY, 1.5f));
		RequestManager.getInstance().getRequestQueue().add(request);
    }
	
	public void showResponseDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(true);
		
		final Vector<CreateUpdateShopResponseType> rejectTypes = new Vector<CreateUpdateShopResponseType>();

		LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View dialoglayout = inflater.inflate(R.layout.select_response, null);
		builder.setView(dialoglayout);
		
		RadioGroup radioGroup = (RadioGroup) dialoglayout.findViewById(R.id.reject_reason_radio_group);
		radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(RadioGroup arg0, int arg1) {
				Button confirmButton = (Button) dialoglayout.findViewById(R.id.confirm_button); 
				confirmButton.setBackgroundResource(R.drawable.button_4w);
				confirmButton.setTextColor(Color.WHITE);
				confirmButton.setEnabled(true);
			}} );
		for(int i = 0; i < responseTypes.length; i++)
			if (responseTypes[i].isReject == true || responseTypes[i].isSeriousReject == true)
			{
				rejectTypes.add(responseTypes[i]);
				RadioButton rb = new RadioButton(this);
		        rb.setText(responseTypes[i].message);
		        radioGroup.addView(rb);
			}
		
		final AlertDialog dialog = builder.create();

		Button confirmButton = (Button) dialoglayout.findViewById(R.id.confirm_button);
		confirmButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				RadioGroup radioGroup = (RadioGroup) dialoglayout.findViewById(R.id.reject_reason_radio_group);
				int index = radioGroup.indexOfChild(dialoglayout.findViewById(radioGroup.getCheckedRadioButtonId()));
				selectedResponse = rejectTypes.get(index);
				
				ReviewCreateShopActivity.this.findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);
				AuthCodeUtil.sendAuthCodeRequest(ReviewCreateShopActivity.this, regId);
				dialog.dismiss();
			}
		});
		
		dialog.show();

		// work-around for very strange behaviour(bug??) in alertDialog
		float factor = ImageUtil.isSw600dp()?2:1; 
		int width_px = (int) (Resources.getSystem().getDisplayMetrics().density * 280 * factor);
		int height_px = (int) (Resources.getSystem().getDisplayMetrics().density * 420 * factor);
		dialog.getWindow().setLayout(width_px, height_px);
	}
	
	public void reviewAction(View view) {
		if (lastClickTime != null && lastClickTime.plusMillis(Config.AVOID_DOUBLE_CLICK_PERIOD).isAfterNow())
			return;
		lastClickTime = DateTime.now();

		if (submissionOutput.isCreator)
		{
	        Toast.makeText(this, this.getString(R.string.self_review_error_message), Toast.LENGTH_LONG).show();
			return;
		}
		if (submissionOutput.isReviewer)
		{
	        Toast.makeText(this, this.getString(R.string.double_review_error_message), Toast.LENGTH_LONG).show();
			return;
		}
		if (view.getId() == R.id.accept_button)
		{
			for (int i = 0; i < this.responseTypes.length; i++)
				if (this.responseTypes[i].isAccept)
					selectedResponse = this.responseTypes[i];
			
			findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);
			AuthCodeUtil.sendAuthCodeRequest(this, regId);
		}
		if (view.getId() == R.id.reject_button)
			showResponseDialog();
	}
	@Override
	public void onSendAuthCodeRequestError(int errorCode) {
		findViewById(R.id.progress_bar).setVisibility(View.GONE);
		
		String errorMessage = null;
		if (errorCode == Config.WIFI_ERROR)
			errorMessage = getString(R.string.network_redirection_error_message);
		if (errorCode == Config.NETWORK_ERROR)
			errorMessage = getString(R.string.network_connection_error_message);
		if (errorCode == Config.OTHERS_ERROR)
			errorMessage = getString(R.string.network_other_error_message);

		Intent intent = new Intent(this, ShowGenericErrorActivity.class);
		intent.putExtra("message", errorMessage);
		startActivity(intent);
	}
}
