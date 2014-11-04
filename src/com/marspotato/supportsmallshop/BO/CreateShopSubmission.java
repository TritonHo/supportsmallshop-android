package com.marspotato.supportsmallshop.BO;

import java.io.Serializable;

import com.google.gson.annotations.Expose;

public class CreateShopSubmission implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public static int NAME_MAX_LENGTH = 20;
	public static int SHORT_DESCRIPTION_MAX_LENGTH = 30;
	public static int FULL_DESCRIPTION_MAX_LENGTH = 200;
	public static int OPEN_HOURS_MAX_LENGTH = 100;
	public static int SEARCH_TAGS_MAX_LENGTH = 200;
	public static int ADDRESS_MAX_LENGTH = 150;
	public static int PHONE_MAX_LENGTH = 10;
	
	@Expose
	public String id;
	@Expose
	public String helperId;
	@Expose
	public String name;
	@Expose
	public String shortDescription;
	@Expose
	public String fullDescription;
	@Expose
	public String searchTags;
	
	@Expose
	public String shopType;
	@Expose
	public String openHours;
	@Expose
	public int district;
	@Expose
	public String address;
	@Expose
	public String phone;
	@Expose
	public int latitude1000000; /* the value of latitude * 1000000, the Accuracy is ~0.1m */
	@Expose
	public int longitude1000000; /* the value of longitude * 1000000, the Accuracy is ~0.1m */
	@Expose
	public String photoUrl;
}
