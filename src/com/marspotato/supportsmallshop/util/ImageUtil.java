package com.marspotato.supportsmallshop.util;

import android.content.res.Resources;

public class ImageUtil {

	public static boolean isSw600dp()
	{
		//density * dp = pixel
		//dp = pixel / density
		double dpWidth = Resources.getSystem().getDisplayMetrics().widthPixels / Resources.getSystem().getDisplayMetrics().density;
		return dpWidth > 600;
	}
	
}
