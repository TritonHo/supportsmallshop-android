package com.marspotato.supportsmallshop.adaptor;

import com.marspotato.supportsmallshop.R;
import com.marspotato.supportsmallshop.BO.GenericSubmission;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class SubmissionListAdapter extends BaseAdapter {
	private Activity activity;
	private static LayoutInflater inflater = null;
	private GenericSubmission[] gsArray;


	public SubmissionListAdapter(Activity a, GenericSubmission[] gsArray) {
		activity = a;
		this.gsArray = gsArray;

		inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public int getCount() {
		if (gsArray != null)
			return gsArray.length;
		else
			return 0;
	}

	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View vi = inflater.inflate(R.layout.shop_list_item, null);
		
		TextView title = (TextView) vi.findViewById(R.id.shop_title);
		TextView shortDesc = (TextView) vi.findViewById(R.id.shop_short_desc);

		GenericSubmission s = gsArray[position];
		title.setText(s.shopName);
		shortDesc.setText(s.shopShortDesc);

		ImageView icon = (ImageView) vi.findViewById(R.id.shop_icon);
		if (s.submissionType == GenericSubmission.CREATE_TYPE)
			icon.setImageResource(R.drawable.create);
		if (s.submissionType == GenericSubmission.UPDATE_TYPE)
			icon.setImageResource(R.drawable.edit);
		if (s.submissionType == GenericSubmission.DELETE_TYPE)
			icon.setImageResource(R.drawable.delete);

		vi.findViewById(R.id.separator).setVisibility(position == gsArray.length - 1 ? View.GONE : View.VISIBLE);
		return vi;
	}

}
