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
import org.json.JSONException;
import org.json.JSONObject;
 
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
 
 
// arraylist of string[] arrays 
// string[] -> tempf, tempc, chance of rain, wind speed, wind direction, condition, current hour
// just parseInt when you want the int values
 
//arraylist of those strings will be indexed appropriately to the hour
 
 
 
public class JSONParser extends AsyncTask<String, Integer, JSONObject> {
	int type = 0;
	private final String API_KEY = "6421665c1fee1f47";
	//my private generated key to access the weather api
	//will be a part of the url to send/receive json requests
	
	public JSONParser() {
		//always 34 because 24 hours weather and then 24 + n for n number of days (10)
	}
	
	
	public JSONObject doInBackground(String... params) {
		type = Integer.valueOf(params[0]);
		String place = params[1];
		String state = params[2];
		StringBuilder builder = new StringBuilder();
		HttpClient client = new DefaultHttpClient();
		HttpGet http;
		if(type == 0){
			http = new HttpGet("http://api.wunderground.com/api/6421665c1fee1f47/hourly/q/"+state+"/"+place+".json");
		}
		else{
			http =  new HttpGet("http://api.wunderground.com/api/6421665c1fee1f47/forecast10day/q/"+state+"/"+place+".json");
		}
		
		//will probably need to use a stringbuilder to generate the true url based on request
		try {
			
			//append the first set of strings
			HttpResponse response = client.execute(http);
			HttpEntity entity = response.getEntity();
			InputStream content = entity.getContent();
			BufferedReader reader = new BufferedReader(new InputStreamReader(content));
			String data;
			while((data = reader.readLine()) != null){
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
		String[] data = new String[7]; //will always contain 7 values
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
				JSONObject fcttime = time.getJSONObject("FCTTIME");
						
				data[0] = String.valueOf(temporary_temperature.getInt("english"));
				data[1] = String.valueOf(temporary_temperature.getInt("metric"));
				data[2] = String.valueOf(time.getInt("pop"));
				data[3] = String.valueOf(temp_wind.getInt("english")); //LET ME KNOW IF YOU WANT METRIC -> CHANGE KEY TO metric
				data[4] = String.valueOf(temp_winddir.getInt("degrees")); //in degrees so easier to bound and choose icon
				data[5] = time.getString("condition");
				data[6] = String.valueOf(fcttime.getInt("hour"));
				
				conditions.add((String[])data.clone());
			
				
			}
 
 
			
			
		} catch (Exception e) {
			e.printStackTrace();
			Log.v("http", "failed to parse");
		}
		
		
		
		return conditions;
	}
	
	
	
	
	public ArrayList<String[]> futureForecast(JSONObject obj) {
		ArrayList<String[]> forecast = new ArrayList<String[]>(); //10 days
		String[] data;
		
		//so we have our 24 hour and want to retreive the 10 day forecast
		
		try {
			Log.v("http", "beginning forecast json parsing");
			JSONObject JSONforecast = obj.getJSONObject("forecast");
			//Log.v("http", JSONforecast.toString());
			JSONObject txtforecastday = JSONforecast.getJSONObject("simpleforecast");
			//Log.v("http", txtforecastday.toString());
			
			//JSONObject simple = txtforecastday.getJSONObject("simpleforecast");
			
			JSONArray forecasts = txtforecastday.getJSONArray("forecastday");
			Log.v("http", "size of JSON forecasts: " + forecasts.length());
			
			for(int i = 0; i < forecasts.length(); i++) {
				JSONObject current = forecasts.getJSONObject(i);
				// string[] -> tempfH, tempfL, tempcH, tempcH, chance of rain, wind speed, wind direction, condition, current day, am/pm
				data = new String[10];
				
				JSONObject low = current.getJSONObject("low");
				JSONObject high = current.getJSONObject("high");
				JSONObject wind = current.getJSONObject("avewind");
				JSONObject date = current.getJSONObject("date");
				//Log.v("http", date.toString());
				
				//highs and lows
				data[0] = high.getString("fahrenheit");
				data[1] = low.getString("fahrenheit");
				data[2] = high.getString("celsius");
				data[3] = low.getString("celsius");
				//chance of rain
				data[4] = String.valueOf(current.getInt("pop"));
				//wind and windw dir
				data[5] = String.valueOf(wind.getInt("mph")); //need to add km/h 
				data[6] = String.valueOf(wind.getInt("degrees")); //degree format for direction
				//conditions
				data[7] = current.getString("conditions");
				//current day
				data[8] = date.getString("weekday");
				data[9] = date.getString("ampm");
				Log.v("http", data[7]);
				
				forecast.add((String[])data.clone());
				
				
			}
			return forecast;
 
		} catch (JSONException e) {
			Log.v("http", "Failed to parse forecast obj");
			e.printStackTrace();
			return forecast;
 
		}
		
		
	}
	
}