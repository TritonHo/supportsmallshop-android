package com.marspotato.supportsmallshop;

import java.io.UnsupportedEncodingException;
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
import com.google.gson.JsonSyntaxException;
import com.marspotato.supportsmallshop.R;
import com.marspotato.supportsmallshop.BO.GenericSubmission;
import com.marspotato.supportsmallshop.BO.Submission;
import com.marspotato.supportsmallshop.adaptor.SubmissionListAdapter;
import com.marspotato.supportsmallshop.util.Config;
import com.marspotato.supportsmallshop.util.RequestManager;




import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;


public class SubmissionListActivity extends Activity 
{
	private GenericSubmission[] gsArray;
	private int selectedDistrict;
	private DateTime lastClickTime;//Just for avoiding double-click problem, no need to persistence
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putString("gsArrayJSON", Config.defaultGSON.toJson(gsArray) );
		savedInstanceState.putInt("selectedDistrict", selectedDistrict );
	}

	private void setupTabColor(int viewId, boolean isHighLighted) {
		TextView t = (TextView) findViewById(viewId);
		t.setBackgroundColor(getResources().getColor(isHighLighted ? R.color.title_pale_grey : R.color.title_dark_grey));
		t.setTextColor(isHighLighted ? Color.WHITE : getResources().getColor(R.color.tab_dim_text_color));
	}

	public void regionFilterAction(View v) {		
		setupTabColor(R.id.whole_city_tab, v.getId() == R.id.whole_city_tab);
		setupTabColor(R.id.hk_island_tab, v.getId() == R.id.hk_island_tab);
		setupTabColor(R.id.kowloon_tab, v.getId() == R.id.kowloon_tab);
		setupTabColor(R.id.new_territories_tab, v.getId() == R.id.new_territories_tab);
		
		switch (v.getId()) {
			case R.id.whole_city_tab:
				selectedDistrict = Config.WHOLE_HK;
				break;
			case R.id.hk_island_tab:
				selectedDistrict = Config.HK_ISLAND;
				break;
			case R.id.kowloon_tab:
				selectedDistrict = Config.KOWL0ON;
				break;
			case R.id.new_territories_tab:
				selectedDistrict = Config.NEW_TERRITORIES;
				break;
		}
		getSubmissionList();
	}

	@Override
	public void finish() {

		//pass back the helperId back to Main
		Intent intent = new Intent();
		//TODO: uncomment it
//		intent.putExtra("helperId", helperId);
		setResult(RESULT_OK, intent);
		super.finish();
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.submission_list);
		
		if (savedInstanceState != null) {
			selectedDistrict = savedInstanceState.getInt("selectedDistrict");
			String gsArrayJSON = savedInstanceState.getString("gsArrayJSON");
			gsArray = (GenericSubmission[]) Config.defaultGSON.fromJson(gsArrayJSON, GenericSubmission[].class);

			//resume the display
			ListView submissionListView = (ListView) findViewById(R.id.submission_list);
			submissionListView.setAdapter(new SubmissionListAdapter(this, gsArray));
			findViewById(R.id.submission_list).setVisibility(View.VISIBLE);
			findViewById(R.id.progress_bar).setVisibility(View.GONE);
		} else {
			//TODO: get regId
			selectedDistrict = Config.WHOLE_HK;
			getSubmissionList();
		}

		EditText edittext = (EditText) findViewById(R.id.search_word);
		edittext.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// If the event is a key-down event on the "enter" button
				if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
					SubmissionListActivity.this.getSubmissionList();
					return true;
				}
				return false;
			}
		});

		Spinner spinner = (Spinner) findViewById(R.id.shop_type_spinner);
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.shop_type_display_values , android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				SubmissionListActivity.this.getSubmissionList();
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				SubmissionListActivity.this.getSubmissionList();
			}} );

		// Click event for single list row
		ListView shopListView = (ListView) findViewById(R.id.submission_list);
		shopListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> a, View v, int pos, long id) {
				if (lastClickTime != null && lastClickTime.plusMillis(Config.AVOID_DOUBLE_CLICK_PERIOD).isAfterNow())
					return;
				lastClickTime = DateTime.now();
				//TODO: implement it
/*
				Intent intent = new Intent(SubmissionListActivity.this, ShopDetailActivity.class);
				intent.putExtra("shop", shopList[pos]);
				startActivity(intent);
*/
			}
		});
	}

	private void getSubmissionList() {
		// onPreExecute
		findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);
		findViewById(R.id.main_layout).setVisibility(View.GONE);

		Response.Listener<String> listener = new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				try {
					GenericSubmission[] result = Config.defaultGSON.fromJson(response, GenericSubmission[].class);
					SubmissionListActivity.this.gsArray = result;
					ListView submissionListView = (ListView) findViewById(R.id.submission_list);
					submissionListView.setAdapter(new SubmissionListAdapter(SubmissionListActivity.this, result));
				} catch (JsonSyntaxException ex) {
					// failed json parsing means the network is already hijacked
					Intent intent = new Intent(SubmissionListActivity.this, ShowGenericErrorActivity.class);
					intent.putExtra("message", getString(R.string.network_redirection_error_message));
					startActivity(intent);
				}
				findViewById(R.id.progress_bar).setVisibility(View.GONE);
				findViewById(R.id.main_layout).setVisibility(View.VISIBLE);
			}
		};

		Response.ErrorListener errorListener = new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				if ((error instanceof NetworkError) || (error instanceof NoConnectionError) || (error instanceof TimeoutError)) {
					Intent intent = new Intent(SubmissionListActivity.this, ShowGenericErrorActivity.class);
					intent.putExtra("message", getString(R.string.network_connection_error_message));
					startActivity(intent);
					return;
				}
			}
		};

		String url = "";
		try {
			url = Config.HOST_URL + "/GenericSubmission?district=" + selectedDistrict;
			//TODO: getLatest flag
			
			Spinner spinner = (Spinner) findViewById(R.id.shop_type_spinner);
			int selectedType = spinner.getSelectedItemPosition();
			if (selectedType != Spinner.INVALID_POSITION && selectedType > 0)
				url = url + "&shopType=" + URLEncoder.encode(Config.shopTypes[selectedType-1], "UTF-8");
			TextView t = (TextView) findViewById(R.id.search_word);
			String searchWord = t.getText().toString();
			if (!searchWord.isEmpty())
				url = url + "&searchWord=" + URLEncoder.encode(searchWord, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// should never reach this line
			e.printStackTrace();
		}
		StringRequest request = new StringRequest(Request.Method.GET, url, listener, errorListener);
		request.setRetryPolicy(new DefaultRetryPolicy(Config.DEFAULT_HTTP_TIMEOUT, Config.DEFAULT_HTTP_MAX_RETRY, 1.5f));
		RequestManager.getInstance().getRequestQueue().add(request);
	}
}
