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


//NOT REALLY USING ASYNCTASK
public class JSONParser extends AsyncTask {
	
	private final String API_KEY = "6c983158db171325130303";
	//my private generated key to access the weather api
	//will be a part of the url to send/receive json requests
	
	public JSONParser() {
		//empty constructor
	}
	
	
	public String read() {
		StringBuilder builder = new StringBuilder();
		HttpClient client = new DefaultHttpClient();
		
		HttpGet http = new HttpGet("http://free.worldweatheronline.com/feed/weather.ashx?q=32601&format=json&num_of_days=2&key=6c983158db171325130303");
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
			return data;
			
			
		} catch (Exception e) {
			e.printStackTrace();
			Log.v("http", "Failed to download JSON");
			return null;
		}
		
	}


	@Override
	protected Object doInBackground(Object... params) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	public ArrayList<String> parse(String jsonString) {
		ArrayList<String> data = new ArrayList<String>();
		
		//first need to create a jsonobject out of the string
		try {
			//Log.v("http", jsonString);
			JSONObject obj = new JSONObject(jsonString);
			Log.v("http", "created json object");
			JSONObject dataJSON = obj.getJSONObject("data");
			Log.v("http", "created data object");
			JSONArray array = dataJSON.getJSONArray("current_condition");
			Log.v("http", "array made");
			JSONObject current = array.getJSONObject(0);
			
			//just getting a LITTLE BIT  of data for now but its all here 
			String observation_time = current.getString("observation_time");
			String cloudcover = current.getString("cloudcover");
			String pressure = current.getString("pressure");
			String temp_F = current.getString("temp_F");
			String temp_C = current.getString("temp_C");
			data.add(observation_time);
			data.add(cloudcover);
			data.add(pressure);
			data.add(temp_F);
			data.add(temp_C);
			
		} catch (Exception e) {
			e.printStackTrace();
			Log.v("http", "failed to parse");
		}
		
		
		
		return data;
	}
	
	
}
