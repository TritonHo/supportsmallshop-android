package com.marspotato.supportsmallshop.gcm;

import java.util.Iterator;
import java.util.Set;

import org.joda.time.DateTime;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

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
				for (Iterator<String> it = keys.iterator(); it.hasNext();) {
					// TODO: handle the from key to strengthen the security
					String key = it.next();
					if ("data".equals(key)) {
						String[] temp = extras.getString(key).split(":", 2);
/*
						// TODO: seems hardcode, use other method
						int playerId = Integer.parseInt(getField(MainActivity.MARTIANPOTATO_PLAYER_ID));

						String action = temp[0];
						String message = temp[1];

						if ("UseCouponMessage".equals(action)) {
							UseCouponMessage obj = Config.defaultGSON.fromJson(message, UseCouponMessage.class);
							if (obj.playerId == playerId)
								useCouponMessageHandler(obj);
						}
*/
					}
				}
			}
		}
		// Release the wake lock provided by the WakefulBroadcastReceiver.
		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}


}