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


public class JSONParser extends AsyncTask<String, Integer, JSONObject> {
	
	private final String API_KEY = "6421665c1fee1f47";
	//my private generated key to access the weather api
	//will be a part of the url to send/receive json requests
	
	public JSONParser() {
		//empty constructor
	}
	
	
	public JSONObject doInBackground(String... params) {
		StringBuilder builder = new StringBuilder();
		HttpClient client = new DefaultHttpClient();
		
		HttpGet http = new HttpGet("http://api.wunderground.com/api/6421665c1fee1f47/conditions/q/CA/San_Francisco.json");
		//currently just arbitrarily gainesville
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
	
	
	public ArrayList<String> parse(JSONObject obj) {
		ArrayList<String> data = new ArrayList<String>();
		
		//first need to create a jsonobject out of the string
		try {
			//Log.v("http", jsonString);
			//JSONObject obj = new JSONObject(jsonString);
			Log.v("http", "created json object");
			JSONObject dataJSON = obj.getJSONObject("current_observation");
			Log.v("http", "created data object");
			//Log.v("http", dataJSON.getString("display_location"));
			Log.v("http", "array made");

			data.add(dataJSON.getString("observation_time"));
			data.add(dataJSON.getString("weather"));
			data.add(dataJSON.getString("relative_humidity"));
			data.add(dataJSON.getString("temperature_string"));


			
			
		} catch (Exception e) {
			e.printStackTrace();
			Log.v("http", "failed to parse");
		}
		
		
		
		return data;
	}
	
	
}
