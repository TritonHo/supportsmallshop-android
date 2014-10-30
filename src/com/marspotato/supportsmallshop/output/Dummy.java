package com.marspotato.supportsmallshop.output;

import com.google.gson.annotations.Expose;

public class Dummy {
	//a silly but working method to check if the network redirected by WIFI.
	//remarks: it can check WIFI redirect only, is not used for defends man-in-middle
	
	@Expose
	public int intColumn;
	@Expose
	public String stringColumn;
	
	public void checkValid() throws Exception
	{	
		if (intColumn != 123 || "456".equals(stringColumn) == false)
			throw new Exception("Wrong dummyJson" );
	}
}
