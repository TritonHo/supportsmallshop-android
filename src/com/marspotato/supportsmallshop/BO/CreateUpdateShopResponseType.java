package com.marspotato.supportsmallshop.BO;

import com.google.gson.annotations.Expose;


public class CreateUpdateShopResponseType {
	@Expose
	public int id;
	@Expose
	public String message;
	@Expose
	public boolean isReject;
	@Expose
	public boolean isSeriousReject;
	@Expose
	public boolean isAccept;
}
