package com.marspotato.supportsmallshop.gcm;

import java.util.Set;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

public class GcmIntentService extends IntentService {
	public static final String GCM_AUTH_CODE = "authCode";
	
	public GcmIntentService() {
		super("GcmIntentService");
	}

	private void processAuthCode(String authCode) {
		// relay the message to activity
		Intent i = new Intent(GCM_AUTH_CODE).putExtra("authCode", authCode);
		LocalBroadcastManager.getInstance(this).sendBroadcast(i);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
		// The getMessageType() intent parameter must be the intent you received
		// in your BroadcastReceiver.
		String messageType = gcm.getMessageType(intent);

		if (!extras.isEmpty()) { // has effect of unparcelling Bundle
			// no need to care other GCM message type
			if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
				Set<String> keys = extras.keySet();

				if (keys.contains("data") == true)
				{
					//this project will have only one type of message, 
					//thus no need to determine the message type 
					processAuthCode(extras.getString("data"));
				}
			}
		}
		// Release the wake lock provided by the WakefulBroadcastReceiver.
		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}


}