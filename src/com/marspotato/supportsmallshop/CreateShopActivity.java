package com.marspotato.supportsmallshop;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.joda.time.DateTime;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.marspotato.supportsmallshop.BO.Submission;
import com.marspotato.supportsmallshop.util.Config;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;


public class CreateShopActivity extends Activity implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener  
{
	private static final String DRAFT_SUBMISSION = "draftSubmission";
	private String regId;
	private String helperId;
	
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
	
	
	private String getField(String fieldName) {
		final SharedPreferences prefs = getSharedPreferences(this.getClass().getSimpleName(), Context.MODE_PRIVATE);
		return prefs.getString(fieldName, "");
	}

	private void storeField(String fieldName, String value) {
		final SharedPreferences prefs = getSharedPreferences(this.getClass().getSimpleName(), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(fieldName, value);
		editor.commit();
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
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putString("regId", regId);
		savedInstanceState.putString("helperId", helperId);
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_shop);

		Intent intent = getIntent();
		if (savedInstanceState != null)
		{
			regId = savedInstanceState.getString("regId");
			helperId = savedInstanceState.getString("helperId");
		}
		else
		{
			regId = intent.getStringExtra("regId");
			helperId = null;
			
			String json = getField(DRAFT_SUBMISSION);
			if (json.isEmpty() == false)
			{
				Submission s = Config.defaultGSON.fromJson(json, Submission.class);
				fillInputWithSubmission(s);
			}
		}

		mLocationClient = new LocationClient(this, this, this);
		
		Spinner spinner = (Spinner) findViewById(R.id.shop_type_spinner);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.shop_type_display_values , android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
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
	private void fillInputWithSubmission(Submission s)
	{
		setEditTextView(R.id.name,			 s.name 			);
		setEditTextView(R.id.short_desc,	 s.shortDescription );
		setEditTextView(R.id.full_desc,	 	 s.fullDescription 	);
		setEditTextView(R.id.open_hours,	 s.openHours 		);
		setEditTextView(R.id.search_tags,	 s.searchTags 		);
		setEditTextView(R.id.address,		 s.address 			);
		setEditTextView(R.id.phone,		 	 s.phone 			);
		
		Spinner spinner = (Spinner) findViewById(R.id.shop_type_spinner);
		if (s.shopType.isEmpty() == false)
		{
			for (int i = 0; i < Config.shopTypes.length; i++)
				if (s.shopType.equals(Config.shopTypes[i]))
					spinner.setSelection(i+1);
		}
		else
			spinner.setSelection(0);
		if (s.longitude1000000 > 0 && s.latitude1000000 > 0)
		{
			int t1 = s.longitude1000000 / 1000000;
			int t2 = s.longitude1000000 % 1000000;
			String t = t2 > 0?""+t1+"."+String.format("%06d", t2) :""+t1; 
			setEditTextView(R.id.longitude, t);
			
			t1 = s.latitude1000000 / 1000000;
			t2 = s.latitude1000000 % 1000000;
			t = t2 > 0?""+t1+"."+t2:""+t1;
			setEditTextView(R.id.latitude, t);
		}
		else	
		{
			setEditTextView(R.id.longitude, "");
			setEditTextView(R.id.latitude, "");
		}
		RadioGroup districtRadio = (RadioGroup) findViewById(R.id.district_radio_group);
		switch (s.district) 
		{
			case Config.HK_ISLAND:
				districtRadio.check(R.id.hk_island);
				break;
			case Config.KOWL0ON:
				districtRadio.check(R.id.kowloon);
				break;
			case Config.NEW_TERRITORIES:
				districtRadio.check(R.id.new_territories);
				break;
			default:
				districtRadio.check(-1);
				s.district = -1;
				break;
		}
	}
	private Submission buildSubmissionFromInput()
	{
		Submission s = new Submission();
		
		s.name 				= getValueFromEditTextView(R.id.name, Submission.NAME_MAX_LENGTH);
		s.shortDescription 	= getValueFromEditTextView(R.id.short_desc, Submission.SHORT_DESCRIPTION_MAX_LENGTH);
		s.fullDescription 	= getValueFromEditTextView(R.id.full_desc, Submission.FULL_DESCRIPTION_MAX_LENGTH);
		s.openHours 		= getValueFromEditTextView(R.id.open_hours, Submission.OPEN_HOURS_MAX_LENGTH);
		s.searchTags 		= getValueFromEditTextView(R.id.search_tags, Submission.SEARCH_TAGS_MAX_LENGTH);
		s.address 			= getValueFromEditTextView(R.id.address, Submission.ADDRESS_MAX_LENGTH);
		s.phone 			= getValueFromEditTextView(R.id.phone, Submission.PHONE_MAX_LENGTH);
		
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
	@Override
	public void finish() {
		//save the draft
		Submission s = buildSubmissionFromInput();
		storeField(DRAFT_SUBMISSION, Config.defaultGSON.toJson(s));
		
		//pass back the helperId back to Main
		Intent intent = new Intent();
		intent.putExtra("helperId", helperId);
		setResult(RESULT_OK, intent);
		super.finish();
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
	public void confirmAction(View view) {
		if (lastClickTime != null && lastClickTime.plusMillis(Config.AVOID_DOUBLE_CLICK_PERIOD).isAfterNow())
			return;
		lastClickTime = DateTime.now();
		Submission s = buildSubmissionFromInput();
		if (checkMandatoryField(s.name, R.string.shop_name) )
			return;
		if (checkMandatoryField(s.shortDescription, R.string.description) )
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

		Log.d("debugpoint", "debugpoint");
	}
	public void resetAction(View view) {
		if (lastClickTime != null && lastClickTime.plusMillis(Config.AVOID_DOUBLE_CLICK_PERIOD).isAfterNow())
			return;
		lastClickTime = DateTime.now();
		
		//erase any saved draft
		storeField(DRAFT_SUBMISSION, "");
		
		//initial state
		Submission s = new Submission();
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
	    	setEditTextView(R.id.longitude,String.format("%.6f", location.getLongitude()));
	    	setEditTextView(R.id.latitude,String.format("%.6f", location.getLatitude()));
	    }
	}
}
