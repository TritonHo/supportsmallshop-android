package com.marspotato.supportsmallshop.util;

import android.app.Activity;

import com.marspotato.supportsmallshop.R;

public class MiscUtil {

	public static String getDistrictName(Activity a, int districtId)
	{
		String output = null;
		if (districtId == Config.WHOLE_HK)
			output = a.getString(R.string.whole_city);
		if (districtId == Config.HK_ISLAND )
			output = a.getString(R.string.hk_island);
		if (districtId == Config.KOWL0ON)
			output = a.getString(R.string.kowloon);
		if (districtId == Config.NEW_TERRITORIES)
			output = a.getString(R.string.new_territories);
		return output;
	}
	public static String getLatLngString(int value)
	{
		if (value == 0)
			return "/";
		int t1 = value / 1000000, t2 = value % 1000000;
		return "" + t1 + "." + String.format("%06d", t2);
	}
}
