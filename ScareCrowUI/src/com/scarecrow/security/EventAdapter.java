package com.scarecrow.security;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import com.aes.pid.R;
import com.scarecrow.activities.ScareCrowActivity;
import com.scarecrow.photo.PhotoFragment;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

public class EventAdapter extends ArrayAdapter<Event> {
	private final Context context;
	  
	  static ArrayList<Event> events = new ArrayList<Event>();

	  public EventAdapter(Context context) {
		    super(context, R.layout.event_entry, events);
	    this.context = context;
	  }
	  
	  public void addEvent(Event e){
		  events.add(e);
	  }
	  

	  @Override
	  public View getView(int position, View convertView, ViewGroup parent) {
	    LayoutInflater inflater = (LayoutInflater) context
	        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    View rowView = inflater.inflate(R.layout.event_entry, parent, false);
	    TextView eventNumber = (TextView) rowView.findViewById(R.id.value_event_num);
	    TextView eventTime = (TextView) rowView.findViewById(R.id.value_event_time);
	    TextView eventDate = (TextView) rowView.findViewById(R.id.value_event_date);
	    TextView eventType = (TextView) rowView.findViewById(R.id.value_event_type);
	    TextView eventLocation = (TextView) rowView.findViewById(R.id.value_event_location);
	    Button eventPIC = (Button) rowView.findViewById(R.id.btn_open_pic);
	    
	   
	    final Event event = events.get(position);
	    eventPIC.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				ScareCrowActivity.fragPhoto.setURL(event.url);
			}
	    	
	    });
	    eventNumber.setText(Integer.toString(event.id));
	    Date date = new Date(event.time);
	    java.text.DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(context);
	    java.text.DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(context);
	    eventDate.setText(dateFormat.format(date));
	    eventTime.setText(timeFormat.format(date));
	    switch(event.type){
	    case Event.EVENT_SECURITY:
	    	eventType.setText("Security");
	    	break;
	    case Event.EVENT_USER:
		    eventType.setText("User Photo");
	    	break;
	    case Event.EVENT_WATER_ON:
	    	eventType.setText("Water ON");
	    	break;
	    case Event.EVENT_WATER_OFF:
	    	eventType.setText("Water OFF");
	    	break;
	    }
	    switch(event.direction){
	    case 1:
		    eventLocation.setText("NW");
	    	break;
	    case 2:
		    eventLocation.setText("N");
	    	break;
	    case 3:
	    	eventLocation.setText("NE");
	    	break;
	    case 4:
	    	eventLocation.setText("E");
	    	break;
	    case 5:
	    	eventLocation.setText("SE");
	    	break;
	    case 6:
	    	eventLocation.setText("S");
	    	break;
	    case 7:
	    	eventLocation.setText("SW");
	    	break;
	    case 8:
	    	eventLocation.setText("W");
	    	break;
	    }
	    
	    return rowView;
	  }
}