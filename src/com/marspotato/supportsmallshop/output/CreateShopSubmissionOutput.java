package com.marspotato.supportsmallshop.output;

import java.io.Serializable;

import com.google.gson.annotations.Expose;
import com.marspotato.supportsmallshop.BO.CreateShopSubmission;

public class CreateShopSubmissionOutput implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Expose
	public CreateShopSubmission s;
	@Expose
	public boolean isCreator;
	@Expose
	public boolean isReviewer;
}
