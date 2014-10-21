package com.marspotato.supportsmallshop;


import com.marspotato.supportsmallshop.util.Config;
import com.marspotato.supportsmallshop.util.RequestManager;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import android.app.Application;

public class MyApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

		// RequestManager initialization
		RequestManager.getInstance(getApplicationContext());

		// Create global configuration and initialize ImageLoader with this configuration
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
			.memoryCacheSizePercentage(5) 		//only use 5 percent of memory for image cache
			.diskCacheFileCount(Config.MAX_DISK_CACHE_IMAGE)			//limit the disc to store at most 100 image only
			.build();
		ImageLoader.getInstance().init(config);
	}
}