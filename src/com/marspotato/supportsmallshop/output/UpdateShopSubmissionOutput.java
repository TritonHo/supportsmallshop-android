package com.marspotato.supportsmallshop.output;

import java.io.Serializable;

import com.google.gson.annotations.Expose;
import com.marspotato.supportsmallshop.BO.Shop;
import com.marspotato.supportsmallshop.BO.UpdateShopSubmission;

public class UpdateShopSubmissionOutput implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Expose
	public UpdateShopSubmission s;
	@Expose
	public Shop shop;
	@Expose
	public boolean isCreator;
	@Expose
	public boolean isReviewer;
}
