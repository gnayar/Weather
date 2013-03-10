package com.example.weather;
//how to get updateQueue() working
//how to get tableNumber and tablePasscode
//both issues stem from that the fact that I can't access Main
import java.util.ArrayList;

import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Application;


public class LocationAdapter extends ArrayAdapter<CityState> {
	
	private int resource;
	private LayoutInflater inflater;
	private Context context;
	
	public LocationAdapter(Context ctx, int textViewResourceId,
			ArrayList<CityState> locs) {
		super(ctx, textViewResourceId, locs);
		
		
		this.resource = textViewResourceId;
		this.inflater = LayoutInflater.from(ctx);
		context = ctx;
	}
	@Override
	public View getView (final int position, View convertView, ViewGroup parent){
		
		convertView = (RelativeLayout) inflater.inflate(resource,  null);
		final CityState place = getItem(position);
		TextView state = (TextView) convertView.findViewById(R.id.state);
		TextView city = (TextView) convertView.findViewById(R.id.city);
		if(position == 0){
			int color = ((MainActivity)context).colorSet;
			if(color == 0){
				 color = Color.GREEN;
			}
			city.setTextColor(color);
			state.setTextColor(color);
		}
		else{
			int color = Color.WHITE;
			city.setTextColor(color);
			state.setTextColor(color);
		}
		city.setText(place.city);
		
		state.setText(place.state);
		return convertView;
	}
		
//    public void imageButtonClick(View v) {
//    	Log.v("imagebutton", "it worked!");
//    	//this is where request to upvote should go
//
//    	
//    }
	
	
	
	public void refreshLocations(ArrayList<CityState> inputLocs) {
		this.clear();
		for(int i =0; i < inputLocs.size(); i++) {
			this.add(inputLocs.get(i));
		}
	}
	
	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
		Log.v("Return", "notifyDataSetChanged");
	}
	
		

}
