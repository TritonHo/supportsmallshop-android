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
import com.marspotato.supportsmallshop.BO.Shop;
import com.marspotato.supportsmallshop.BO.UpdateShopSubmission;
import com.marspotato.supportsmallshop.gcm.GcmIntentService;
import com.marspotato.supportsmallshop.output.UpdateShopSubmissionOutput;
import com.marspotato.supportsmallshop.util.AuthCodeRequester;
import com.marspotato.supportsmallshop.util.AuthCodeUtil;
import com.marspotato.supportsmallshop.util.Config;
import com.marspotato.supportsmallshop.util.ImageUtil;
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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;
/*

ordering
shop_name(done)
shop_type(done)
district(done)
address(done)
coordinates
phone(done)
short_desc(done)
full_desc(done)
open_hours(done)
search_tags(done)
 
 */
public class ReviewUpdateShopActivity extends Activity implements AuthCodeRequester {
	private BroadcastReceiver authCodeIntentReceiver;
	
	private UpdateShopSubmissionOutput submissionOutput;
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
	private void setupBlock(int originalFieldId, int oldFieldId, int newFieldId, int oldCaptionId, int changeBlockId, int originalBlockId, String oldValue, String newValue)
	{
		findViewById(changeBlockId).setVisibility(newValue != null?View.VISIBLE:View.GONE); 
		
		if (newValue != null)
		{
			findViewById(originalBlockId).setVisibility(View.GONE);
			TextView newField = (TextView) findViewById(newFieldId);
			newField.setText(newValue);
			
			TextView oldField = (TextView) findViewById(oldFieldId);

			if (oldValue != null && oldValue.isEmpty() == false )
				oldField.setText(oldValue);
			else
			{
				findViewById(oldCaptionId).setVisibility(View.GONE);
				oldField.setVisibility(View.GONE);
			}
		}
		else
		{
			if (oldValue != null && oldValue.isEmpty() == false )
			{
				findViewById(originalBlockId).setVisibility(View.VISIBLE); 
				TextView field = (TextView) findViewById(originalFieldId);
				field.setText(oldValue);
			}
			else
				findViewById(originalBlockId).setVisibility(View.GONE); 
		}
	}
	private void setupChangeBlockOnly(int oldFieldId, int newFieldId, int changeBlockId, String oldValue, String newValue)
	{
		findViewById(changeBlockId).setVisibility(newValue != null?View.VISIBLE:View.GONE);
		if (newValue != null)
		{
			TextView newField = (TextView) findViewById(newFieldId);
			TextView oldField = (TextView) findViewById(oldFieldId);
			newField.setText(newValue);
			oldField.setText(oldValue);
		}
	}
	private String getDistrictName(int districtId)
	{
		String output = null;
		if (districtId == Config.WHOLE_HK)
			output = this.getString(R.string.whole_city);
		if (districtId == Config.HK_ISLAND )
			output = this.getString(R.string.hk_island);
		if (districtId == Config.KOWL0ON)
			output = this.getString(R.string.kowloon);
		if (districtId == Config.NEW_TERRITORIES)
			output = this.getString(R.string.new_territories);
		return output;
	}
	private String getLatLngString(int value)
	{
		if (value == 0)
			return "/";
		int t1 = value / 1000000, t2 = value % 1000000;
		return "" + t1 + "." + String.format("%06d", t2);
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
					uri = "http://maps.google.com/maps?q=loc:" + getLatLngString(latitude1000000) + "," + getLatLngString(longitude1000000);

				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
				intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
				startActivity(intent);
			}
		});
	}
	private void displayData()
	{
		final Shop shop = submissionOutput.shop;
		UpdateShopSubmission s = submissionOutput.s;
		
		TextView title = (TextView) findViewById(R.id.shop_title);
		title.setText(shop.name);
		
		setupChangeBlockOnly(R.id.old_shop_name, R.id.new_shop_name, R.id.change_name_block, shop.name, s.name);
		if (s.updateShopType == true)
			setupChangeBlockOnly(R.id.old_shop_type, R.id.new_shop_type, R.id.change_shop_type_block, shop.shopType, s.shopType);
		else
			findViewById(R.id.change_shop_type_block).setVisibility(View.GONE);
		if (s.updateDistrict == true)
			setupChangeBlockOnly(R.id.old_district, R.id.new_district, R.id.change_district_block, getDistrictName(shop.district), getDistrictName(s.district));
		else
			findViewById(R.id.change_district_block).setVisibility(View.GONE);
		
		if (s.updateLocation == true)
		{
			if (shop.latitude1000000 == 0 && shop.longitude1000000 == 0)
				findViewById(R.id.old_coordinates_block).setVisibility(View.GONE);
			else
			{
				findViewById(R.id.old_coordinates_block).setVisibility(View.VISIBLE);
				setupLocationIcon(R.id.old_location_icon, shop.longitude1000000, shop.latitude1000000);
			}
			findViewById(R.id.new_coordinates_block).setVisibility(View.VISIBLE);
			setupLocationIcon(R.id.new_location_icon, s.longitude1000000, s.latitude1000000);
			
			TextView oldLat = (TextView) findViewById(R.id.old_lat);
			TextView newLat = (TextView) findViewById(R.id.new_lat);
			TextView oldLng = (TextView) findViewById(R.id.old_lng);
			TextView newLng = (TextView) findViewById(R.id.new_lng);
			oldLat.setText(getLatLngString(shop.latitude1000000));
			oldLng.setText(getLatLngString(shop.longitude1000000));
			newLat.setText(getLatLngString(s.latitude1000000));
			newLng.setText(getLatLngString(s.longitude1000000));
		}
		
		setupBlock(R.id.short_desc, 	R.id.old_short_desc, 	R.id.new_short_desc, 	R.id.old_short_desc_caption, 	R.id.change_short_desc_block, 	R.id.short_desc_block, 	shop.shortDescription, 	s.shortDescription);
		setupBlock(R.id.full_desc, 		R.id.old_full_desc, 	R.id.new_full_desc, 	R.id.old_full_desc_caption, 	R.id.change_full_desc_block, 	R.id.full_desc_block, 	shop.fullDescription, 	s.fullDescription);
		setupBlock(R.id.address, 		R.id.old_address, 		R.id.new_address, 		R.id.old_address_caption, 		R.id.change_address_block, 		R.id.address_block, 	shop.address, 			s.address);
		setupBlock(R.id.phone, 			R.id.old_phone, 		R.id.new_phone, 		R.id.old_phone_caption, 		R.id.change_phone_block, 		R.id.phone_block, 		shop.phone, 			s.phone);
		setupBlock(R.id.open_hours, 	R.id.old_open_hours, 	R.id.new_open_hours, 	R.id.old_open_hours_caption, 	R.id.change_open_hours_block, 	R.id.open_hours_block, 	shop.openHours, 		s.openHours);
		setupBlock(R.id.search_tags, 	R.id.old_search_tags, 	R.id.new_search_tags, 	R.id.old_search_tags_caption, 	R.id.change_search_tags_block, 	R.id.search_tags_block, shop.searchTags, 		s.searchTags);
		
		//setup phone icon
		if (shop.phone != null && shop.phone.isEmpty() == false)
		{
			ImageView phoneIcon = (ImageView) findViewById(R.id.phone_icon);
			phoneIcon.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (lastClickTime != null && lastClickTime.plusMillis(Config.AVOID_DOUBLE_CLICK_PERIOD).isAfterNow())
						return;
					lastClickTime = DateTime.now();
					Intent intent = new Intent(Intent.ACTION_DIAL);
					intent.setData(Uri.parse("tel:" + shop.phone));
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

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.review_update_shop);

		Intent intent = getIntent();
		if (savedInstanceState != null)
		{
			submissionOutput = (UpdateShopSubmissionOutput) savedInstanceState.getSerializable("submissionOutput");
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
			getUpdateShopSubmission(submissionId);
			getResponseType();
		}
	}
	private void getResponseType() {
		Response.Listener<String> listener = new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				try {
					CreateUpdateShopResponseType[] result = Config.defaultGSON.fromJson(response, CreateUpdateShopResponseType[].class);
					ReviewUpdateShopActivity.this.responseTypes = result;
					if (ReviewUpdateShopActivity.this.submissionOutput != null && ReviewUpdateShopActivity.this.responseTypes != null)
					{
						findViewById(R.id.progress_bar).setVisibility(View.GONE);
						findViewById(R.id.main_layout).setVisibility(View.VISIBLE);
					}
				} catch (JsonSyntaxException ex) {
					// failed json parsing means the network is already hijacked
					Intent intent = new Intent(ReviewUpdateShopActivity.this, ShowGenericErrorActivity.class);
					intent.putExtra("message", getString(R.string.network_redirection_error_message));
					startActivity(intent);
				}
			}
		};

		Response.ErrorListener errorListener = new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				if ((error instanceof NetworkError) || (error instanceof NoConnectionError) || (error instanceof TimeoutError)) {
					Intent intent = new Intent(ReviewUpdateShopActivity.this, ShowGenericErrorActivity.class);
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
	
	private void getUpdateShopSubmission(String submissionId) {
		Response.Listener<String> listener = new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				try {
					UpdateShopSubmissionOutput submissionOutput = Config.defaultGSON.fromJson(response, UpdateShopSubmissionOutput.class);
					ReviewUpdateShopActivity.this.submissionOutput = submissionOutput;
					ReviewUpdateShopActivity.this.displayData();
					if (ReviewUpdateShopActivity.this.submissionOutput != null && ReviewUpdateShopActivity.this.responseTypes != null)
					{
						findViewById(R.id.progress_bar).setVisibility(View.GONE);
						findViewById(R.id.main_layout).setVisibility(View.VISIBLE);
					}
				} catch (JsonSyntaxException ex) {
					// failed json parsing means the network is already hijacked
					Intent intent = new Intent(ReviewUpdateShopActivity.this, ShowGenericErrorActivity.class);
					intent.putExtra("message", getString(R.string.network_redirection_error_message));
					startActivity(intent);
				}
			}
		};

		Response.ErrorListener errorListener = new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				if ((error instanceof NetworkError) || (error instanceof NoConnectionError) || (error instanceof TimeoutError)) {
					Intent intent = new Intent(ReviewUpdateShopActivity.this, ShowGenericErrorActivity.class);
					intent.putExtra("message", getString(R.string.network_connection_error_message));
					startActivity(intent);
					return;
				}
			}
		};

		String url = Config.HOST_URL + "/UpdateShopSubmission?submissionId=" + submissionId + "&helperId=" + helperId; 
		
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
					ReviewUpdateShopActivity.this.helperId = cusr.helperId;
					
			        Toast.makeText(ReviewUpdateShopActivity.this, getString(R.string.success_review), Toast.LENGTH_LONG).show();
			        ReviewUpdateShopActivity.this.finish();
				} catch (Exception ex) {
					ReviewUpdateShopActivity.this.onSendAuthCodeRequestError(Config.WIFI_ERROR);
				}
			}
		};

		Response.ErrorListener errorListener = new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				if ((error instanceof NetworkError) || (error instanceof NoConnectionError) || (error instanceof TimeoutError)) {
					ReviewUpdateShopActivity.this.onSendAuthCodeRequestError(Config.NETWORK_ERROR);
				}
				else
					ReviewUpdateShopActivity.this.onSendAuthCodeRequestError(Config.OTHERS_ERROR);
			}
		};

		String url = "";
		try {
			url = Config.HOST_URL + "/CreateUpdateShopResponse?code=" + URLEncoder.encode(authCode, "UTF-8") 
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
				
				ReviewUpdateShopActivity.this.findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);
				AuthCodeUtil.sendAuthCodeRequest(ReviewUpdateShopActivity.this, regId);
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
