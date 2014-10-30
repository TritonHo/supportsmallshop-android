package com.marspotato.supportsmallshop.gcm;


import java.util.Set;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class GcmIntentService extends IntentService {

	public GcmIntentService() {
		super("GcmIntentService");
	}
/*
	private void removeCouponMessageHandler(RemoveCouponMessage message) {
		// Update the local DB
//		Coupon.deleteCouponFromCache(getApplicationContext(), message.code);

		// relay the message to foreground activity
		Intent i = new Intent("com.martianpotato.questionator.gcm.RemoveCouponMessage").putExtra("message", message);
		LocalBroadcastManager.getInstance(this).sendBroadcast(i);
	}
*/
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
				if (keys.contains("data"))
				{
					Log.d("gcm-received", extras.getString("data"));
					//TODO: fix me
				}
			}
		}
		// Release the wake lock provided by the WakefulBroadcastReceiver.
		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}


}