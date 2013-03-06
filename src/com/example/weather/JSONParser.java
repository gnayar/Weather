package com.example.weather;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;


// arraylist of string[] arrays 
// string[] -> tempf, tempc, chance of rain, wind speed, wind direction, condition
// just parseInt when you want the int values

//arraylist of those strings will be indexed appropriately to the hour



public class JSONParser extends AsyncTask<String, Integer, JSONObject> {
	
	private final String API_KEY = "6421665c1fee1f47";
	//my private generated key to access the weather api
	//will be a part of the url to send/receive json requests
	
	public JSONParser() {
		//always 34 because 24 hours weather and then 24 + n for n number of days (10)
	}
	
	
	public JSONObject doInBackground(String... params) {
		StringBuilder builder = new StringBuilder();
		HttpClient client = new DefaultHttpClient();
		
		//HttpGet http = new HttpGet("http://api.wunderground.com/api/6421665c1fee1f47/conditions/q/CA/San_Francisco.json");
		HttpGet http = new HttpGet("http://api.wunderground.com/api/6421665c1fee1f47/hourly/q/CA/San_Francisco.json");
		
		//will probably need to use a stringbuilder to generate the true url based on request
		try {
			HttpResponse response = client.execute(http);
			HttpEntity entity = response.getEntity();
			InputStream content = entity.getContent();
			BufferedReader reader = new BufferedReader(new InputStreamReader(content));
			String data;
			while((data = reader.readLine()) != null) {
				builder.append(data);
			}
			data = builder.toString();
			//Log.v("http", data);
			JSONObject obj = new JSONObject(data);
			return obj;
			
			
		} catch (Exception e) {
			e.printStackTrace();
			Log.v("http", "Failed to download JSON");
			return null;
		}
		
	}
	
	
	public ArrayList<String[]> parse(JSONObject obj) {
		String[] data = new String[6]; //will always contain six values
		ArrayList<String[]> conditions = new ArrayList<String[]>(24);

		//first need to create a jsonobject out of the string
		try {
			//Log.v("http", jsonString);
			//JSONObject obj = new JSONObject(jsonString);
			Log.v("http", "created json object");
			JSONArray allHours = obj.getJSONArray("hourly_forecast");
			Log.v("http", "array made");
			Log.v("http", "size of jsonarray is: " + allHours.length());
			//from here on will surely be iterative
			for(int i = 0; i < 24; i++) { //for 24 hours
				JSONObject time = allHours.getJSONObject(i);
			
				//Log.v("http", time.toString());

				//***NOTE
				//NEED TO PARSE THE ACTUAL INT VALUES FROM THIS. I KNOW I AM JUST DUPLICATING
			
				JSONObject temporary_temperature = time.getJSONObject("temp");
				JSONObject temp_wind = time.getJSONObject("wspd");
				JSONObject temp_winddir = time.getJSONObject("wdir");
				
				data[0] = String.valueOf(temporary_temperature.getInt("english"));
				data[1] = String.valueOf(temporary_temperature.getInt("metric"));
				data[2] = String.valueOf(time.getInt("pop"));
				data[3] = String.valueOf(temp_wind.getInt("english")); //LET ME KNOW IF YOU WANT METRIC -> CHANGE KEY TO metric
				data[4] = String.valueOf(temp_winddir.getInt("degrees")); //in degrees so easier to bound and choose icon
				data[5] = time.getString("wx");
				
				conditions.add(data);
			
			}

			
			
		} catch (Exception e) {
			e.printStackTrace();
			Log.v("http", "failed to parse");
		}
		
		
		
		return conditions;
	}
	
	
}
