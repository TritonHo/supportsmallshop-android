package com.marspotato.supportsmallshop.util;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class RequestManager {

    private static RequestManager instance;
    private RequestQueue mRequestQueue;


    private RequestManager(Context context) {
    	mRequestQueue = Volley.newRequestQueue(context.getApplicationContext());
    }
    public RequestQueue getRequestQueue()
    {
    	return mRequestQueue;
    }

    // This method should be called first to do singleton initialization
    public static synchronized RequestManager getInstance(Context context) {
        if (instance == null) {
            instance = new RequestManager(context);
        }
        return instance;
    }

    public static synchronized RequestManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException(RequestManager.class.getSimpleName() +
                    " is not initialized, call getInstance(..) method first.");
        }
        return instance;
    }
}