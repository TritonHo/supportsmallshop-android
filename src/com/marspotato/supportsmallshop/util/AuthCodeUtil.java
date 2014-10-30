package com.marspotato.supportsmallshop.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.joda.time.DateTime;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.marspotato.supportsmallshop.output.Dummy;

public class AuthCodeUtil {
	public static int WIFI_ERROR = 0;
	public static int NETWORK_ERROR = 1;
	public static int OTHERS_ERROR = 2;
	
    public static void sendAuthCodeRequest(final AuthCodeReceiver receiver, String regId)
    {
    	Response.Listener<String> listener = new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				try {
					Dummy dummy = Config.defaultGSON.fromJson(response, Dummy.class);
					dummy.checkValid();
					receiver.onSendAuthCodeRequestSuccess();
				} catch (Exception ex) {
					receiver.onSendAuthCodeRequestError(WIFI_ERROR);
				}
			}
		};

		Response.ErrorListener errorListener = new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				if ((error instanceof NetworkError) || (error instanceof NoConnectionError) || (error instanceof TimeoutError)) {
					receiver.onSendAuthCodeRequestError(NETWORK_ERROR);
				}
				else
					receiver.onSendAuthCodeRequestError(OTHERS_ERROR);
			}
		};

		String url = "";
		try {
			url = Config.HOST_URL + "/AuthCode?deviceType=" + Config.deviceType
					+ "&regId=" + URLEncoder.encode(regId, "UTF-8")
					+ "&dt=" + URLEncoder.encode(Config.defaultDateTimeFormatter.print(DateTime.now()), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// should never reach this line
			e.printStackTrace();
		}
		StringRequest request = new StringRequest(Request.Method.POST, url, listener, errorListener);
		request.setRetryPolicy(new DefaultRetryPolicy(Config.DEFAULT_HTTP_TIMEOUT, Config.DEFAULT_HTTP_MAX_RETRY, 1.5f));
		RequestManager.getInstance().getRequestQueue().add(request);
    }

}
