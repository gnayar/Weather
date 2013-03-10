package com.example.weather;
//how to get updateQueue() working
//how to get tableNumber and tablePasscode
//both issues stem from that the fact that I can't access Main
import java.util.ArrayList;

import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
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


public class ForecastAdapter extends ArrayAdapter<Temperature> {
	
	private int resource;
	private LayoutInflater inflater;
	private Context context;
	
	public ForecastAdapter(Context ctx, int textViewResourceId,
			ArrayList<Temperature> temps) {
		super(ctx, textViewResourceId, temps);
		
		
		this.resource = textViewResourceId;
		this.inflater = LayoutInflater.from(ctx);
		context = ctx;
	}
	@Override
	public View getView (final int position, View convertView, ViewGroup parent){
		
		convertView = (RelativeLayout) inflater.inflate(resource,  null);
		final Temperature temp = getItem(position);
		ImageView condition = (ImageView) convertView.findViewById(R.id.condition_pic);
		condition.setBackgroundResource(temp.imageResourceID);
		TextView title = (TextView) convertView.findViewById(R.id.day_forecast);
		title.setText(temp.day);
		TextView album = (TextView) convertView.findViewById(R.id.high_low);
		if(!temp.celsius){
			album.setText("High" + temp.tempFHigh + " / Low:" + temp.tempFLow);
		}
		else{
			album.setText("High" + temp.tempCHigh + " / Low:" + temp.tempCLow);
		}
		TextView artist = (TextView) convertView.findViewById(R.id.precip_forecast);
		artist.setText(String.valueOf(temp.precip + "%"));
		
		return convertView;
	}
		
//    public void imageButtonClick(View v) {
//    	Log.v("imagebutton", "it worked!");
//    	//this is where request to upvote should go
//
//    	
//    }
	
	
	
	public void refreshSongs(ArrayList<Temperature> inputTemps) {
		this.clear();
		for(int i =0; i < inputTemps.size(); i++) {
			this.add(inputTemps.get(i));
		}
	}
	
	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
		Log.v("Return", "notifyDataSetChanged");
	}
	
		

}
