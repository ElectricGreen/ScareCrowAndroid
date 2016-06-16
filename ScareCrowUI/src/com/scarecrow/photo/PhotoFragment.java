package com.scarecrow.photo;


import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import com.aes.pid.R;
import com.scarecrow.activities.ScareCrowActivity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class PhotoFragment extends Fragment {

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	String URL = ScareCrowActivity.WEB_URL+"Photo/index.php";
	public void setURL(String URL){
		this.URL = ScareCrowActivity.WEB_URL+URL;
		Log.d("photofrag", this.URL);
	}
	public PhotoFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

//		if (getArguments().containsKey(ARG_ITEM_ID)) {
//			// Load the dummy content specified by the fragment
//			// arguments. In a real-world scenario, use a Loader
//			// to load content from a content provider.
//			mItem = DummyContent.ITEM_MAP.get(getArguments().getString(
//					ARG_ITEM_ID));
//		}
		
		

	}
	
	WebView photoWebView;
	TextView photoWebLocation;
	
	@Override 
	public void onResume() {
		super.onResume();
		
		photoWebView = (WebView) this.getActivity().findViewById(R.id.photo_web_view);
		photoWebLocation = (TextView) this.getActivity().findViewById(R.id.photo_web_location);
		
		photoWebView.loadUrl(URL);
		photoWebLocation.setText(URL);
		
		photoWebView.getSettings().setBuiltInZoomControls(true);
		photoWebView.getSettings().setSupportZoom(true); 
		photoWebView.getSettings().setUseWideViewPort(true);
		photoWebView.getSettings().setLoadWithOverviewMode(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_photo,
				container, false);

		// Show the dummy content as text in a TextView.
//		if (mItem != null) {
//			((TextView) rootView.findViewById(R.id.item_detail))
//					.setText(mItem.content);
//		}

		return rootView;
	}

	public static void updateUI(){
		
	}

	public static void ipDiag() {
		// TODO Auto-generated method stub
		
	}

	
}
