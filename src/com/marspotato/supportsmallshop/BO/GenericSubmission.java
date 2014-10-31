package com.marspotato.supportsmallshop.BO;

import com.google.gson.annotations.Expose;

public class GenericSubmission {
	public static final int CREATE_TYPE = 1;
	public static final int UPDATE_TYPE = 2;
	public static final int DELETE_TYPE = 3;
	
	@Expose
	public int submissionType;
	@Expose
	public String submissionId;//null for CreateShopSubmission
	@Expose
	public String shopName;
	@Expose
	public String shopShortDesc;	
}
