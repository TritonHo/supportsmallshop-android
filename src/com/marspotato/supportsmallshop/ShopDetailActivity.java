package com.marspotato.supportsmallshop;

import org.joda.time.DateTime;

import com.marspotato.supportsmallshop.BO.Shop;
import com.marspotato.supportsmallshop.util.Config;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class ShopDetailActivity extends Activity {
	private static final int CHILDREN_RESULT_CODE = 0;
	private String regId;
	private String helperId;
	private Shop shop;

	private DateTime lastClickTime;//Just for avoiding double-click problem, no need to persistence
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putString("regId", regId);
		savedInstanceState.putString("helperId", helperId);
		savedInstanceState.putSerializable("shop", shop);
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
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		String helperId = data.getStringExtra("helperId");
		if (helperId != null)
			this.helperId = helperId;
	}
	@Override
	public void finish() {

		//pass back the helperId back to Main
		Intent intent = new Intent();
		intent.putExtra("helperId", helperId);
		setResult(RESULT_OK, intent);
		super.finish();
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shop_detail);

		Intent intent = getIntent();
		if (savedInstanceState != null)
		{
			regId = savedInstanceState.getString("regId");
			helperId = savedInstanceState.getString("helperId");	
			shop = (Shop) savedInstanceState.getSerializable("shop");
		}
		else
		{
			regId = intent.getStringExtra("regId");
			helperId = intent.getStringExtra("helperId");
			shop = (Shop) intent.getExtras().getSerializable("shop");
		}
		ImageView icon = (ImageView) findViewById(R.id.shop_icon);
		DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisk(true).build();
		ImageLoader il = ImageLoader.getInstance();
		if (!shop.photoUrl.isEmpty())
			il.displayImage(shop.photoUrl, icon, options);

		TextView title = (TextView) findViewById(R.id.shop_title);
		title.setText(shop.name);
		
		setupBlock(R.id.full_desc, R.id.full_desc_block, shop.fullDescription);
		setupBlock(R.id.address, R.id.address_block, shop.address);
		setupBlock(R.id.phone, R.id.phone_block, shop.phone);
		setupBlock(R.id.open_hours, R.id.open_hours_block, shop.openHours);		

		
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
		//set up the address icon
		if (shop.address != null && shop.address.isEmpty() == false)
		{
			ImageView locationIcon = (ImageView) findViewById(R.id.location_icon);
			locationIcon.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (lastClickTime != null && lastClickTime.plusMillis(Config.AVOID_DOUBLE_CLICK_PERIOD).isAfterNow())
						return;
					lastClickTime = DateTime.now();
					String uri = null;
					if (shop.latitude1000000 != 0 && shop.longitude1000000 != 0)
					{
						String longitude = "" + (shop.longitude1000000 / 1000000) + "." + (shop.longitude1000000 % 1000000);
						String latitude = "" + (shop.latitude1000000 / 1000000) + "." + (shop.latitude1000000 % 1000000);			
						uri = "http://maps.google.com/maps?q=loc:" + latitude + "," + longitude + " (" + shop.name + ")";
					}
					else
						uri = "http://maps.google.com/maps?q=" + shop.address;
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
					intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
					startActivity(intent);
				}
			});
		}
	}
	
	public void changeAction(View view) {
		if (lastClickTime != null && lastClickTime.plusMillis(Config.AVOID_DOUBLE_CLICK_PERIOD).isAfterNow())
			return;
		lastClickTime = DateTime.now();

		if (view.getId() == R.id.edit_button)
		{
			//TODO: implement it
			Intent intent = new Intent(ShopDetailActivity.this, UpdateShopActivity.class);
			intent.putExtra("shopId", shop.id);
			intent.putExtra("regId", regId);
			intent.putExtra("helperId", helperId);
			startActivityForResult(intent, CHILDREN_RESULT_CODE);
		}
		if (view.getId() == R.id.delete_button)
		{
			//TODO: implement it in next version
		}
	}
}
