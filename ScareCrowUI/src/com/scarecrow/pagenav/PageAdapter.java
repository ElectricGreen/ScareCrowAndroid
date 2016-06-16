package com.scarecrow.pagenav;

import java.io.File;

import com.aes.pid.R;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
 
public class PageAdapter extends ArrayAdapter<String> {
	private final Context context;
	private final String[] values;
 
	public PageAdapter(Context context, String[] values) {
		super(context, R.layout.nav_page, values);
		this.context = context;
		this.values = values;
	}
 
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
			.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
		View rowView = inflater.inflate(R.layout.nav_page, parent, false);
		TextView textView = (TextView) rowView.findViewById(R.id.label);
		ImageView imageView = (ImageView) rowView.findViewById(R.id.thumbnail_image);
		textView.setText(values[position]);
 
		imageView.setImageURI(Uri.fromFile(new File(Environment.getExternalStorageDirectory().toString() + "/" + "page"+position)));
 
		return rowView;
	}
}