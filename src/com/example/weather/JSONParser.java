package com.example.weather;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class JSONParser extends AsyncTask<String, Integer, JSONObject> {
	int type = 0;
	Context context;

	public JSONParser() {
	}

	@Override
	public JSONObject doInBackground(String... params) {
		type = Integer.valueOf(params[0]);
		

		StringBuilder builder = new StringBuilder();
		HttpClient client = new DefaultHttpClient();
		HttpGet http;
		if (type == 0) {
			String lat = params[1];
			String lng = params[2];
			http = new HttpGet(
					
					"http://i.wxbug.net/REST/Direct/GetForecastHourly.ashx?la="
							+ lat+"&lo="+lng
							+ "&ht=t&ht=cp&ht=ws&ht=d&api_key=qhm5e4wcjz3zc4rzy6va5p9j");
		} else {
			String state = params[1];
			String place = params[2];
			http = new HttpGet(
					"http://api.wunderground.com/api/6421665c1fee1f47/forecast10day/q/"
							+ state + "/" + place + ".json");
			// http = new
			// HttpGet("http://api.wunderground.com/api/6421665c1fee1f47/forecast10day/q/FL/Gainesville.json");

		}

		try {

			// append the first set of strings
			HttpResponse response = client.execute(http);
			HttpEntity entity = response.getEntity();
			InputStream content = entity.getContent();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					content));
			String data;
			while ((data = reader.readLine()) != null) {
				builder.append(data);
			}

			data = builder.toString();
			// Log.v("http", data);
			JSONObject obj = new JSONObject(data);
			return obj;

		} catch (Exception e) {
			e.printStackTrace();
			Log.v("http", "Failed to download JSON");
			return null;
		}

	}

	public void setContext(Context temp) {
		this.context = temp;
	}

	@Override
	protected void onPostExecute(JSONObject Object) {
		if (type == 0) {
			ObjectMapper mapper = new ObjectMapper();
			ArrayList<Temperature> temps = new ArrayList<Temperature>();
			JSONArray allHours = new JSONArray();
			int k = 0;
			try {
				allHours = Object.getJSONArray("forecastHourlyList");
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			for (int i = 0; i < allHours.length(); i++) {

				try {
					JSONObject temperature = allHours.getJSONObject(i);
					String temperatureString = temperature.toString();
					Temperature temp = mapper.readValue(temperatureString,
							Temperature.class);
					Date dateOfTemp = temp.toNormalTime(temp.dateTime);
					Date now = new Date();
					boolean validTime = checkDates(now, dateOfTemp);
					if (validTime) {
						if (k < 24) {
							temps.add(temp);
							Log.d("jackson", dateOfTemp.getHours()+" day" +dateOfTemp.getDate());
							k++;
						}

					}

				} catch (JsonParseException e) {
					Log.d("jackson", "failed JsonParse");
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JsonMappingException e) {
					Log.d("jackson", "failed JsonMappng");
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					Log.d("jackson", "failedIO");
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			// Code for processing weather data
			((MainActivity)context).setSaveCurrent(temps);
	    	if(!((MainActivity)context).inHours){
	    		((MainActivity)context).weatherAtTime(0);
	    	}
			((MainActivity)context).getSavedTemperatures();

		} else if (type == 1) {
			((MainActivity) context).future = futureForecast(Object);
			((MainActivity) context).setUpForecast();

		}
	}

	private boolean checkDates(Date now, Date dateOfTemp) {
		if (dateOfTemp.after(now)) {
			return true;
		}
		if(dateOfTemp.getHours() == now.getHours()){
			return true;
		}
		return false;
	}

	public ArrayList<String[]> futureForecast(JSONObject obj) {
		ArrayList<String[]> forecast = new ArrayList<String[]>(); // 10 days
		String[] data;

		// so we have our 24 hour and want to retreive the 10 day forecast

		try {
			Log.v("http", "beginning forecast json parsing");
			JSONObject JSONforecast = obj.getJSONObject("forecast");
			// Log.v("http", JSONforecast.toString());
			JSONObject txtforecastday = JSONforecast
					.getJSONObject("simpleforecast");
			// Log.v("http", txtforecastday.toString());

			// JSONObject simple =
			// txtforecastday.getJSONObject("simpleforecast");

			JSONArray forecasts = txtforecastday.getJSONArray("forecastday");
			Log.v("http", "size of JSON forecasts: " + forecasts.length());

			for (int i = 0; i < forecasts.length(); i++) {
				JSONObject current = forecasts.getJSONObject(i);
				// string[] -> tempfH, tempfL, tempcH, tempcH, chance of rain,
				// wind speed, wind direction, condition, current day, am/pm
				data = new String[10];

				JSONObject low = current.getJSONObject("low");
				JSONObject high = current.getJSONObject("high");
				JSONObject wind = current.getJSONObject("avewind");
				JSONObject date = current.getJSONObject("date");
				// Log.v("http", date.toString());

				// highs and lows
				data[0] = high.getString("fahrenheit");
				data[1] = low.getString("fahrenheit");
				data[2] = high.getString("celsius");
				data[3] = low.getString("celsius");
				// chance of rain
				data[4] = String.valueOf(current.getInt("pop"));
				// wind and windw dir
				data[5] = String.valueOf(wind.getInt("mph")); // need to add
																// km/h
				data[6] = String.valueOf(wind.getInt("degrees")); // degree
																	// format
																	// for
																	// direction
				// conditions
				data[7] = current.getString("conditions");
				// current day
				data[8] = date.getString("weekday");
				data[9] = date.getString("ampm");
				Log.v("http", data[7]);

				forecast.add((String[]) data.clone());

			}
			return forecast;

		} catch (JSONException e) {
			Log.v("http", "Failed to parse forecast obj");
			e.printStackTrace();
			return forecast;

		}

	}

}
