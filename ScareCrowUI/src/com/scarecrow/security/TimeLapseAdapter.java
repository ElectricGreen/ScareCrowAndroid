package com.scarecrow.security;

import com.aes.pid.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

public class TimeLapseAdapter extends ArrayAdapter<String> {
	private final Context context;
	  private final String[] values;

	  public TimeLapseAdapter(Context context, String[] values) {
	    super(context, R.layout.event_entry, values);
	    this.context = context;
	    this.values = values;
	  }

	  @Override
	  public View getView(int position, View convertView, ViewGroup parent) {
	    LayoutInflater inflater = (LayoutInflater) context
	        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    View rowView = inflater.inflate(R.layout.event_entry, parent, false);
	    TextView eventNumber = (TextView) rowView.findViewById(R.id.value_event_num);
	    TextView eventTime = (TextView) rowView.findViewById(R.id.value_event_time);
	    TextView eventLocation = (TextView) rowView.findViewById(R.id.value_event_location);
	    Button eventPIC = (Button) rowView.findViewById(R.id.btn_open_pic);
	    eventNumber.setText("1");
	    eventTime.setText("11/18/91 10:55:30");
	    eventLocation.setText("NW");
	    
//	    textView.setText(values[position]);
//	    // change the icon for Windows and iPhone
//	    String s = values[position];
//	    if (s.startsWith("iPhone")) {
//	      imageView.setImageResource(R.drawable.no);
//	    } else {
//	      imageView.setImageResource(R.drawable.ok);
//	    }

	    return rowView;
	  }
}