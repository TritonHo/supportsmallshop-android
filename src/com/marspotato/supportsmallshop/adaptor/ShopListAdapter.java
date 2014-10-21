package com.marspotato.supportsmallshop.adaptor;

import com.marspotato.supportsmallshop.R;
import com.marspotato.supportsmallshop.BO.Shop;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ShopListAdapter extends BaseAdapter {
	private Activity activity;
	private static LayoutInflater inflater = null;
	private Shop[] shopArray;


	public ShopListAdapter(Activity a, Shop[] shopArray) {
		activity = a;
		this.shopArray = shopArray;

		inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public int getCount() {
		if (shopArray != null)
			return shopArray.length;
		else
			return 0;
	}

	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View vi = inflater.inflate(R.layout.shop_list_item, null);
		TextView title = (TextView) vi.findViewById(R.id.shop_title);
		TextView shortDesc = (TextView) vi.findViewById(R.id.shop_short_desc);

		Shop s = shopArray[position];
		title.setText(s.name);
		shortDesc.setText(s.shortDescription);

		ImageView icon = (ImageView) vi.findViewById(R.id.shop_icon);
		DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisk(true).build();
		ImageLoader il = ImageLoader.getInstance();
		if (!s.photoUrl.isEmpty())
			il.displayImage(s.photoUrl, icon, options);

		vi.findViewById(R.id.separator).setVisibility(position == shopArray.length - 1 ? View.GONE : View.VISIBLE);
		return vi;
	}

}
