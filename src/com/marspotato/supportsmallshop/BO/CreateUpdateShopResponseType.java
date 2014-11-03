package com.marspotato.supportsmallshop.BO;

import java.io.Serializable;

import com.google.gson.annotations.Expose;


public class CreateUpdateShopResponseType implements Serializable {
	private static final long serialVersionUID = 1L;
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
