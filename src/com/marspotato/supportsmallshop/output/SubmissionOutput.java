package com.marspotato.supportsmallshop.output;

import java.io.Serializable;

import com.google.gson.annotations.Expose;
import com.marspotato.supportsmallshop.BO.Submission;

public class SubmissionOutput implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Expose
	public Submission s;
	@Expose
	public boolean isCreator;
	@Expose
	public boolean isReviewer;
}
