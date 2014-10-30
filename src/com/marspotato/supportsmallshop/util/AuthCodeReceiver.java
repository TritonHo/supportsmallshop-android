package com.marspotato.supportsmallshop.util;

public interface AuthCodeReceiver {
	
	public void onSendAuthCodeRequestSuccess();
	public void onSendAuthCodeRequestError(int errorCode);
	
	public void receiveAuthCode(String regId);
}
