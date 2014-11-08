package com.marspotato.supportsmallshop;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;

import org.joda.time.DateTime;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.marspotato.supportsmallshop.BO.CreateShopSubmission;
import com.marspotato.supportsmallshop.BO.Shop;
import com.marspotato.supportsmallshop.BO.UpdateShopSubmission;
import com.marspotato.supportsmallshop.gcm.GcmIntentService;
import com.marspotato.supportsmallshop.util.AuthCodeRequester;
import com.marspotato.supportsmallshop.util.AuthCodeUtil;
import com.marspotato.supportsmallshop.util.Config;
import com.marspotato.supportsmallshop.util.MiscUtil;
import com.marspotato.supportsmallshop.util.RequestManager;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


public class UpdateShopActivity extends Activity implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener, AuthCodeRequester  
{	
	private BroadcastReceiver authCodeIntentReceiver;
	private String regId;
	private String helperId;
	private Shop shop;
	private UpdateShopSubmission submission;

	private LocationClient mLocationClient;
	private DateTime lastClickTime;//Just for avoiding double-click problem, no need to persistence
	
	/*
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */
    @Override
    public void onConnected(Bundle dataBundle) {
    	//on purposely show nothing, to avoid confusion to end user.
    }
    /*
     * Called by Location Services if the connection to the
     * location client drops because of an error.
     */
    @Override
    public void onDisconnected() {
    	//on purposely show nothing, to avoid confusion to end user.
    }
    /*
     * Called by Location Services if the attempt to
     * Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    	//NICE: make this part better
        Toast.makeText(this, this.getString(R.string.location_service_error_message), Toast.LENGTH_LONG).show();
    }
	

	@Override
    protected void onStart() {
        super.onStart();
        mLocationClient.connect();
    }
    @Override
    protected void onStop() {
        mLocationClient.disconnect();
        super.onStop();
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
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putString("regId", regId);
		savedInstanceState.putString("helperId", helperId);
		savedInstanceState.putSerializable("shop", shop);
		savedInstanceState.putSerializable("submission", submission);
		
		//save the visibility status
		savedInstanceState.putInt("new_address_block", 		findViewById(R.id.new_address_block).getVisibility() );
		savedInstanceState.putInt("new_full_desc_block", 	findViewById(R.id.new_full_desc_block).getVisibility() );
		savedInstanceState.putInt("new_open_hours_block", 	findViewById(R.id.new_open_hours_block).getVisibility() );
		savedInstanceState.putInt("new_search_tags_block", 	findViewById(R.id.new_search_tags_block).getVisibility() );
		
		savedInstanceState.putInt("new_phone_block", 		findViewById(R.id.new_phone_block).getVisibility() );
		savedInstanceState.putInt("new_short_desc_block", 	findViewById(R.id.new_short_desc_block).getVisibility() );
		savedInstanceState.putInt("new_shop_name_block", 	findViewById(R.id.new_shop_name_block).getVisibility() );
		savedInstanceState.putInt("new_shop_type_block", 	findViewById(R.id.new_shop_type_block).getVisibility() );
		savedInstanceState.putInt("new_district_block", 	findViewById(R.id.new_district_block).getVisibility() );
		savedInstanceState.putInt("new_coordinates_block", 	findViewById(R.id.new_coordinates_block).getVisibility() );
		
	}
	private void controlNewLocationIconVisibilty()
	{
		EditText newLongitude = (EditText) findViewById(R.id.new_longitude);
		EditText newLatitude = (EditText) findViewById(R.id.new_latitude);

		if (newLongitude.getText().toString().isEmpty() == false && newLatitude.getText().toString().isEmpty() == false)
			findViewById(R.id.new_location_icon).setVisibility(View.VISIBLE);
		else
			findViewById(R.id.new_location_icon).setVisibility(View.INVISIBLE);
	}
	private void setupBlock(int fieldId, String value)
	{
		TextView field = (TextView) findViewById(fieldId);

		if (value != null && value.isEmpty() == false )
			field.setText(value);
		else
			field.setText(R.string.no_information);
	}
	public void newLocationAction(View view) {
		if (lastClickTime != null && lastClickTime.plusMillis(Config.AVOID_DOUBLE_CLICK_PERIOD).isAfterNow())
			return;
		lastClickTime = DateTime.now();
		
		EditText longitude = (EditText) findViewById(R.id.new_longitude);
		EditText latitude = (EditText) findViewById(R.id.new_latitude);
		
		String lng = longitude.getText().toString();
		String lat = latitude.getText().toString();
		
		double lngDouble = Double.parseDouble(lng) * 1000000;
		double latDouble = Double.parseDouble(lat) * 1000000;
		
		if ( latDouble > Config.HK_NORTH_LAT1000000 || latDouble < Config.HK_SOUTH_LAT1000000
				|| lngDouble > Config.HK_EAST_LNG1000000 || lngDouble < Config.HK_WEST_LNG1000000 ) 
		
		{
			Toast.makeText(this, getString(R.string.coordinate_error_message), Toast.LENGTH_LONG).show();
			return;
		}

		String uri = "http://maps.google.com/maps?q=loc:" + lat + "," + lng;
		
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
		intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
		startActivity(intent);
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

		TextView shopName = (TextView) findViewById(R.id.shop_title);
		shopName.setText(shop.name);
		
		setupBlock(R.id.shop_type, shop.shopType);
		setupBlock(R.id.district,  MiscUtil.getDistrictName(this, shop.district));
		setupBlock(R.id.address, shop.address);

		setupBlock(R.id.phone, shop.phone);
		setupBlock(R.id.short_desc, shop.shortDescription);
		setupBlock(R.id.full_desc, shop.fullDescription);
		setupBlock(R.id.open_hours, shop.openHours);
		setupBlock(R.id.search_tags, shop.searchTags);
		
		setupLocationIcon(R.id.location_icon, shop.longitude1000000, shop.latitude1000000);
		if (shop.latitude1000000 == 0 && shop.longitude1000000 == 0)
		{
			findViewById(R.id.no_oordinates_information).setVisibility(View.VISIBLE);

			//disable fields
			findViewById(R.id.location_icon).setVisibility(View.GONE);
			findViewById(R.id.lat).setVisibility(View.GONE);
			findViewById(R.id.lng).setVisibility(View.GONE);
			findViewById(R.id.comma).setVisibility(View.GONE);
		}
		else
		{
			findViewById(R.id.no_oordinates_information).setVisibility(View.GONE);
			
			TextView lat = (TextView) findViewById(R.id.lat);
			TextView lng = (TextView) findViewById(R.id.lng);
			lat.setText(MiscUtil.getLatLngString(shop.latitude1000000));
			lng.setText(MiscUtil.getLatLngString(shop.longitude1000000));
		}

		//setup phone icon
		if (shop.phone != null && shop.phone.isEmpty() == false)
		{
			ImageView phoneIcon = (ImageView) findViewById(R.id.phone_icon);
			phoneIcon.setVisibility(View.VISIBLE);
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
		else
			findViewById(R.id.phone_icon).setVisibility(View.GONE);
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.update_shop);

		mLocationClient = new LocationClient(this, this, this);
		
		Spinner spinner = (Spinner) findViewById(R.id.new_shop_type_spinner);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.shop_type_display_values , android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		
		Intent intent = getIntent();
		if (savedInstanceState != null)
		{
			regId = savedInstanceState.getString("regId");
			helperId = savedInstanceState.getString("helperId");	
			shop = (Shop) savedInstanceState.getSerializable("shop");
			submission = (UpdateShopSubmission) savedInstanceState.getSerializable("submission");
			
			//restore the visibility
			findViewById(R.id.new_address_block).setVisibility( savedInstanceState.getInt("new_address_block", View.GONE) );
			findViewById(R.id.new_full_desc_block).setVisibility( savedInstanceState.getInt("new_full_desc_block", View.GONE) );
			findViewById(R.id.new_open_hours_block).setVisibility( savedInstanceState.getInt("new_open_hours_block", View.GONE) );
			findViewById(R.id.new_search_tags_block).setVisibility( savedInstanceState.getInt("new_search_tags_block", View.GONE) );
			findViewById(R.id.new_phone_block).setVisibility( savedInstanceState.getInt("new_phone_block", View.GONE) );
			findViewById(R.id.new_short_desc_block).setVisibility( savedInstanceState.getInt("new_short_desc_block", View.GONE) );
			findViewById(R.id.new_shop_name_block).setVisibility( savedInstanceState.getInt("new_shop_name_block", View.GONE) );
			findViewById(R.id.new_shop_type_block).setVisibility( savedInstanceState.getInt("new_shop_type_block", View.GONE) );
			findViewById(R.id.new_district_block).setVisibility( savedInstanceState.getInt("new_district_block", View.GONE) );
			findViewById(R.id.new_coordinates_block).setVisibility( savedInstanceState.getInt("new_coordinates_block", View.GONE) );
			controlNewLocationIconVisibilty();
		}
		else
		{
			regId = intent.getStringExtra("regId");
			helperId = intent.getStringExtra("helperId");
			shop = (Shop) intent.getExtras().getSerializable("shop");
			submission = null;//TODO: imeplement it
		}
		displayData();
		

		TextWatcher textWatcher = new TextWatcher(){
			@Override
			public void afterTextChanged(Editable arg0) {
				//nothing to do
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				//nothing to do
			}

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				 controlNewLocationIconVisibilty();	
			}
		};
		EditText newLongitude = (EditText) findViewById(R.id.new_longitude);
		newLongitude.addTextChangedListener(textWatcher);
		EditText newLatitude = (EditText) findViewById(R.id.new_latitude);
		newLatitude.addTextChangedListener(textWatcher);

	}
	private String getValueFromEditTextView(int viewId, int maxLength)
	{
		EditText t = (EditText) this.findViewById(viewId);
		String s = t.getText().toString();
		
		if (s.length() < maxLength)
			return s;
		else
			return s.substring(0, maxLength);
	}
	private void setEditTextView(int viewId, String value)
	{
		EditText t = (EditText) this.findViewById(viewId);
		t.setText(value);
	}
	private CreateShopSubmission buildSubmissionFromInput()
	{
		CreateShopSubmission s = new CreateShopSubmission();
		
		s.name 				= getValueFromEditTextView(R.id.name, Config.NAME_MAX_LENGTH);
		s.shortDescription 	= getValueFromEditTextView(R.id.short_desc, Config.SHORT_DESCRIPTION_MAX_LENGTH);
		s.fullDescription 	= getValueFromEditTextView(R.id.full_desc, Config.FULL_DESCRIPTION_MAX_LENGTH);
		s.openHours 		= getValueFromEditTextView(R.id.open_hours, Config.OPEN_HOURS_MAX_LENGTH);
		s.searchTags 		= getValueFromEditTextView(R.id.search_tags, Config.SEARCH_TAGS_MAX_LENGTH);
		s.address 			= getValueFromEditTextView(R.id.address, Config.ADDRESS_MAX_LENGTH);
		s.phone 			= getValueFromEditTextView(R.id.phone, Config.PHONE_MAX_LENGTH);
		
		Spinner spinner = (Spinner) findViewById(R.id.shop_type_spinner);
		int selectedType = spinner.getSelectedItemPosition();
		if (selectedType != Spinner.INVALID_POSITION && selectedType > 0)
			s.shopType = Config.shopTypes[selectedType-1];
		else
			s.shopType = "";

		//default value
		s.longitude1000000 = 0;
		s.latitude1000000 = 0;
		try
		{
			String longitude = getValueFromEditTextView(R.id.longitude, 20);
			String latitude = getValueFromEditTextView(R.id.latitude, 20);
			
			if (longitude.isEmpty() == false && latitude.isEmpty() == false)
			{
				BigDecimal lng = new BigDecimal(longitude);
				BigDecimal lat = new BigDecimal(latitude);
				lng = lng.movePointRight(6).setScale(0, RoundingMode.HALF_UP);
				lat = lat.movePointRight(6).setScale(0, RoundingMode.HALF_UP);
				s.longitude1000000 = lng.intValueExact();
				s.latitude1000000 = lat.intValueExact();
			}
		}
		catch(Exception ex)
		{
			s.longitude1000000 = 0;
			s.latitude1000000 = 0;
		}
		
		RadioGroup districtRadio = (RadioGroup) findViewById(R.id.district_radio_group);
		switch (districtRadio.getCheckedRadioButtonId()) 
		{
			case R.id.hk_island:
				s.district = Config.HK_ISLAND;
				break;
			case R.id.kowloon:
				s.district = Config.KOWL0ON;
				break;
			case R.id.new_territories:
				s.district = Config.NEW_TERRITORIES;
				break;
			default:
				s.district = Config.WHOLE_HK;
				break;
		}
		return s;
	}

	//return value: true if error occurs
	private boolean checkMandatoryField(String value, int fieldNameId)
	{
		if (value.isEmpty() == false)
			return false;
		String errorMessage = getString(R.string.mandatory_field_error_message);
		errorMessage = errorMessage.replace("<field_name>", getString(fieldNameId) );
		showErrorMessage(errorMessage);
		return true;
	}
	private void showErrorMessage(String messge)
	{
		Intent intent = new Intent(this, ShowGenericErrorActivity.class);
		intent.putExtra("message", messge);
		startActivity(intent);
	}
    private void receiveAuthCode(String authCode)
    {
    	Response.Listener<String> listener = new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				try {
					findViewById(R.id.progress_bar).setVisibility(View.GONE);
					CreateShopSubmission s = Config.defaultGSON.fromJson(response, CreateShopSubmission.class);
					UpdateShopActivity.this.helperId = s.helperId;
					
					UpdateShopActivity.this.resetAction();
			        Toast.makeText(UpdateShopActivity.this, getString(R.string.success_create_shop), Toast.LENGTH_LONG).show();
			        UpdateShopActivity.this.finish();
				} catch (Exception ex) {
					UpdateShopActivity.this.onSendAuthCodeRequestError(Config.WIFI_ERROR);
				}
			}
		};

		Response.ErrorListener errorListener = new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				if ((error instanceof NetworkError) || (error instanceof NoConnectionError) || (error instanceof TimeoutError)) {
					UpdateShopActivity.this.onSendAuthCodeRequestError(Config.NETWORK_ERROR);
				}
				else
					UpdateShopActivity.this.onSendAuthCodeRequestError(Config.OTHERS_ERROR);
			}
		};

		CreateShopSubmission s = this.buildSubmissionFromInput();
		String url = "";
		try {
			url = Config.HOST_URL + "/CreateShopSubmission?code=" + URLEncoder.encode(authCode, "UTF-8") 
					+ "&name=" + URLEncoder.encode(s.name, "UTF-8")
					+ "&shopType=" + URLEncoder.encode(s.shopType, "UTF-8")
					+ "&shortDescription=" + URLEncoder.encode(s.shortDescription, "UTF-8")
					+ "&fullDescription=" + URLEncoder.encode(s.fullDescription, "UTF-8")
					+ "&district=" + s.district
					+ "&address=" + URLEncoder.encode(s.address, "UTF-8")
					+ "&phone=" + URLEncoder.encode(s.phone, "UTF-8")
					+ "&openHours=" + URLEncoder.encode(s.openHours, "UTF-8")
					+ "&searchTags=" + URLEncoder.encode(s.searchTags, "UTF-8")
					+ "&latitude1000000=" + s.latitude1000000
					+ "&longitude1000000=" + s.longitude1000000;
		} catch (UnsupportedEncodingException e) {
			// should never reach this line
			e.printStackTrace();
		}
		StringRequest request = new StringRequest(Request.Method.POST, url, listener, errorListener);
		request.setRetryPolicy(new DefaultRetryPolicy(Config.DEFAULT_HTTP_TIMEOUT, Config.DEFAULT_HTTP_MAX_RETRY, 1.5f));
		RequestManager.getInstance().getRequestQueue().add(request);
    	
    	
    }
	public void confirmAction(View view) {
		if (lastClickTime != null && lastClickTime.plusMillis(Config.AVOID_DOUBLE_CLICK_PERIOD).isAfterNow())
			return;
		lastClickTime = DateTime.now();
		CreateShopSubmission s = buildSubmissionFromInput();
		if (checkMandatoryField(s.name, R.string.shop_name) )
			return;
		if (checkMandatoryField(s.shortDescription, R.string.short_desc) )
			return;
		if (checkMandatoryField(s.fullDescription, R.string.full_desc) )
			return;
		if (checkMandatoryField(s.address, R.string.shop_name) )
			return;
		
		if (s.shopType.isEmpty() == true)
		{
			showErrorMessage( getString(R.string.shop_type_error_message) );
			return;
		}
		if (s.district == Config.WHOLE_HK)
		{
			showErrorMessage( getString(R.string.district_error_message) );
			return;
		}
				
		if (s.latitude1000000 != 0 && s.longitude1000000 != 0 )
			if ( s.latitude1000000 > Config.HK_NORTH_LAT1000000 || s.latitude1000000 < Config.HK_SOUTH_LAT1000000
					|| s.longitude1000000 > Config.HK_EAST_LNG1000000 || s.longitude1000000 < Config.HK_WEST_LNG1000000 )
			{
				showErrorMessage( getString(R.string.coordinate_error_message) );
				return;
			}
		findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);
		AuthCodeUtil.sendAuthCodeRequest(this, regId);
	}
	private void resetAction()
	{
		//TODO: implement it
		/*
		//erase any saved draft
		storeField(DRAFT_SUBMISSION, "");
		
		//initial state
		CreateShopSubmission s = new CreateShopSubmission();
		s.name = "";
		s.shortDescription = "";
		s.fullDescription = "";
		s.openHours = "";
		s.searchTags = "";
		s.address = "";
		s.phone = "";
		
		s.shopType = "";
		s.longitude1000000 = 0;
		s.latitude1000000 = 0;
		s.district = Config.WHOLE_HK;
		fillInputWithSubmission(s);
		*/
	}
	public void locationAction(View view) {
		if (lastClickTime != null && lastClickTime.plusMillis(Config.AVOID_DOUBLE_CLICK_PERIOD).isAfterNow())
			return;
		lastClickTime = DateTime.now();
		
		EditText longitude = (EditText) findViewById(R.id.longitude);
		EditText latitude = (EditText) findViewById(R.id.latitude);
		
		String lng = longitude.getText().toString();
		String lat = latitude.getText().toString();
		
		double lngDouble = Double.parseDouble(lng) * 1000000;
		double latDouble = Double.parseDouble(lat) * 1000000;
		
		if ( latDouble > Config.HK_NORTH_LAT1000000 || latDouble < Config.HK_SOUTH_LAT1000000
				|| lngDouble > Config.HK_EAST_LNG1000000 || lngDouble < Config.HK_WEST_LNG1000000 ) 
		
		{
			Toast.makeText(this, getString(R.string.coordinate_error_message), Toast.LENGTH_LONG).show();
			return;
		}

		String uri = "http://maps.google.com/maps?q=loc:" + lat + "," + lng;
		
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
		intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
		startActivity(intent);
	}
	private void flipVisibility(int triggerViewId, int editIconId, int blockId, int fieldId)
	{
		if (triggerViewId != editIconId)
			return;
		
		View block = findViewById(blockId);
		if (block.getVisibility() == View.GONE )
			block.setVisibility(View.VISIBLE);
		else
		{
			block.setVisibility(View.GONE);
			TextView t = (TextView) findViewById(fieldId);
			t.setText("");
		}
	}
	public void editAction(View view) {
		if (lastClickTime != null && lastClickTime.plusMillis(Config.AVOID_DOUBLE_CLICK_PERIOD).isAfterNow())
			return;
		lastClickTime = DateTime.now();
		
		flipVisibility(view.getId(), R.id.address_edit_icon, 		R.id.new_address_block, 	R.id.new_address);
		flipVisibility(view.getId(), R.id.full_desc_edit_icon, 		R.id.new_full_desc_block, 	R.id.new_full_desc);
		flipVisibility(view.getId(), R.id.open_hours_edit_icon, 	R.id.new_open_hours_block, 	R.id.new_open_hours);
		flipVisibility(view.getId(), R.id.search_tags_edit_icon, 	R.id.new_search_tags_block, R.id.new_search_tags);
		flipVisibility(view.getId(), R.id.phone_edit_icon, 			R.id.new_phone_block, 		R.id.new_phone);
		flipVisibility(view.getId(), R.id.short_desc_edit_icon, 	R.id.new_short_desc_block, 	R.id.new_short_desc);
		flipVisibility(view.getId(), R.id.shop_name_edit_icon, 		R.id.new_shop_name_block, 	R.id.new_shop_name);
		
		//for shop_type
		if (view.getId() == R.id.shop_type_edit_icon)
		{
			View block = findViewById(R.id.new_shop_type_block);
			if (block.getVisibility() == View.GONE )
				block.setVisibility(View.VISIBLE);
			else
			{
				block.setVisibility(View.GONE);
				Spinner spinner = (Spinner) findViewById(R.id.new_shop_type_spinner);
				spinner.setSelection(0);
			}
		}

		//for district
		if (view.getId() == R.id.district_edit_icon)
		{
			View block = findViewById(R.id.new_district_block);
			if (block.getVisibility() == View.GONE )
				block.setVisibility(View.VISIBLE);
			else
			{
				block.setVisibility(View.GONE);
                RadioGroup rg = (RadioGroup) findViewById(R.id.new_district_radio_group);
                rg.clearCheck();
			}
		}
		
		//for coordinates
		if (view.getId() == R.id.coordinates_edit_icon)
		{
			View block = findViewById(R.id.new_coordinates_block);
			if (block.getVisibility() == View.GONE )
				block.setVisibility(View.VISIBLE);
			else
			{
				block.setVisibility(View.GONE);
				TextView newLat = (TextView) findViewById(R.id.new_latitude);
				TextView newLong = (TextView) findViewById(R.id.new_longitude);
				newLat.setText("");
				newLong.setText("");
			}
		}
	}
	
	public void resetAction(View view) {
		if (lastClickTime != null && lastClickTime.plusMillis(Config.AVOID_DOUBLE_CLICK_PERIOD).isAfterNow())
			return;
		lastClickTime = DateTime.now();
		
		resetAction();
	}
	public void fillGPSAction(View view) {
		if (lastClickTime != null && lastClickTime.plusMillis(Config.AVOID_DOUBLE_CLICK_PERIOD).isAfterNow())
			return;
		lastClickTime = DateTime.now();
		
	    Location location = mLocationClient.getLastLocation();
	    if (location == null)
	    {
	    	//NICE: make this part better
			Intent intent = new Intent(this, ShowGenericErrorActivity.class);
			intent.putExtra("message", getString(R.string.location_error_message));
			startActivity(intent);
	    }
	    else
	    {
	    	setEditTextView(R.id.new_longitude,String.format("%.6f", location.getLongitude()));
	    	setEditTextView(R.id.new_latitude,String.format("%.6f", location.getLatitude()));
	    }
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
