package com.marspotato.supportsmallshop.util;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Config {	
	
	public final static String licenseUrl = "http://www.gnu.org/licenses/gpl-3.0.txt";
	public final static String[] shopTypes = new String[]{"食肆", "零售（食物）","零售（其他）", "服務"}; 
	
	public final static int DEFAULT_SEARCH_RANGE = 100;
	
	public final static int WHOLE_HK = 0;
	public final static int HK_ISLAND = 1;
	public final static int KOWL0ON = 2;	
	public final static int NEW_TERRITORIES = 3;
	
	public static final String HOST_URL = "http://192.168.0.102:8080/supportsmallshop";
	//public static final String HOST_URL = "http://supportsmallshop.marspotato.com/supportsmallshop";
	
	//the default http timeout
	public static final int DEFAULT_HTTP_TIMEOUT = 10000;//10000ms = 10 seconds
	
	//after the initial fail, the number of retry of http call
	public static final int DEFAULT_HTTP_MAX_RETRY = 1;
	
	//max number of image to be cached in disk
	public static final int MAX_DISK_CACHE_IMAGE = 100;
	
	public static DateTimeFormatter defaultDisplayDateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");

	public static DateTimeFormatter defaultDateTimeFormatter = ISODateTimeFormat.basicDateTimeNoMillis();
	
	public static Gson defaultGSON = new GsonBuilder().registerTypeAdapter(DateTime.class, new DateTimeTypeConverter()).setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();
	
	//time for a button to be disabled to avoid double action
	public static final int AVOID_DOUBLE_CLICK_PERIOD = 500;//500ms
	
	//THE time(in ms) that the progress bar will be delayed to show
	public static final int MS_GAME_PROGRESS_BAR_DELAY = 500; 
	
}
