package com.marspotato.supportsmallshop;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.joda.time.DateTime;

import com.marspotato.supportsmallshop.BO.Submission;
import com.marspotato.supportsmallshop.util.Config;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;


public class CreateShopActivity extends Activity {
	private static final String DRAFT_SUBMISSION = "draftSubmission";

	private DateTime lastClickTime;//Just for avoiding double-click problem, no need to persistence
	
	
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
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_shop);
		
		Spinner spinner = (Spinner) findViewById(R.id.shop_type_spinner);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.shop_type_display_values , android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		
		String json = getField(DRAFT_SUBMISSION);
		if (json.isEmpty() == false)
		{
			Submission s = Config.defaultGSON.fromJson(json, Submission.class);
			fillInputWithSubmission(s);
		}
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
		Submission s = buildSubmissionFromInput();
		storeField(DRAFT_SUBMISSION, Config.defaultGSON.toJson(s));
		super.finish();
	} 
	
	public void confirmAction(View view) {
		if (lastClickTime != null && lastClickTime.plusMillis(Config.AVOID_DOUBLE_CLICK_PERIOD).isAfterNow())
			return;
		lastClickTime = DateTime.now();
		//erase any saved draft
		storeField(DRAFT_SUBMISSION, "");
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
}
