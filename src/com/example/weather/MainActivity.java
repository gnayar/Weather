package com.example.weather;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.slidingmenu.lib.SlidingMenu;
import com.slidingmenu.lib.app.SlidingActivity;

public class MainActivity extends SlidingActivity implements LocationListener, OnItemClickListener {
	private static final String DEBUG_TAG = "Motion";
	private static final String WHERE_TAG = "Where";
	public static final String PREFS_NAME = "LocationPrefs";
	
	//autocomplete api
	private static final String LOG_TAG = "Autocomplete API";
	private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
	private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
	private static final String OUT_JSON = "/json";
	private static final String API_KEY = "AIzaSyC99uGV72z9HkApLazz-yux3yyzGKuhC9E";
	
	
	String locationProvider = LocationManager.NETWORK_PROVIDER;

	private GestureDetectorCompat mDetector;
	Context context;

	Boolean gottenWeather = false;

	int screenHeight, screenWidth;
	public int colorSet;

	ArrayList<Temperature> current = new ArrayList<Temperature>();
	ArrayList<String[]> future = new ArrayList<String[]>();
	Map<String, Integer> conditionPicMatcher;
	Map<String, Integer> forecastPicMatcher;
	ArrayList<CityState> places = new ArrayList<CityState>(); // plug from
																// database into
																// here
	LocationAdapter adapter;
	String currentCity;
	String currentStateCode;
	// for gps
	int lng = 0;
	int lat = 0;
	int oldLat;
	int oldLong;

	// Variables to set time
	boolean inHours = false;
	Time now = new Time();// current moment
	Time internalTime = new Time();// chosen time in internal format [0- 47] =
									// [now - 47 hours later]
	Time displayTime = new Time();// time meant for display [0-12]
	boolean am = true;// am or pm status for displayTime
	boolean today = true;// today or tomorrow status for displayTime

	int amPmCount = 0;
	int timeChangeCount = 0;

	LocationManager locationManager;
	Criteria criteria;
	String provider;

	public enum State {
		Q1, Q2, Q3, Q4, IDLE;
	}

	State state;
	State previousState;
	int touchdownX = 0;
	int touchdownY = 0;
	boolean increasingY = false;
	int hoursAdded = 0;

	boolean celsius = false;
	boolean gps = false;

	// Views
	TextView clock;// says what time it is, exists in two layouts
	TextView dayView;// says what time it is, exists in two layouts
	LinearLayout main;// root layout of main screen
	RelativeLayout menu;// root layout of clock screen
	CircleDraw clockDrawn;// overlay for clock to follow finger

	// Called when the activity is first created.
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setBehindContentView(R.layout.menu);
		context = this;
		
		loadInitialSharedPrefs();
		
		setUpLayoutVars();

		setUpSlidingMenu();

		setUpMap();

		// Finding current time
		now.setToNow();

		Log.d(DEBUG_TAG, "onLongPress: " + now.hour);
		internalTime.setToNow();
		internalToDisplayTime();
		showTime(0);

		setUpMotion();

		// fetchData();

		// setUpForecast();

		// setUpDataBase();
		if (gps) {
			setUpGPS();
		}
		setUpLocationList();
		
		 AutoCompleteTextView autoCompView = (AutoCompleteTextView) findViewById(R.id.autocomplete);
		 autoCompView.setAdapter(new PlacesAutoCompleteAdapter(this, R.layout.autocomplete_list_item));
		 autoCompView.setOnItemClickListener(this);

	}

	private void loadInitialSharedPrefs() {
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(this);
		celsius = sharedPref.getBoolean("temp_scale", false);
		gps = sharedPref.getBoolean("gps", true);
		mDetector = new GestureDetectorCompat(this, new MyGestureListener());
		
	}

	public void setUpClock() {
		GradientDrawable g = new GradientDrawable(Orientation.TL_BR, new int[] {
				Color.BLACK, Color.GRAY, 0xFF666666 });
		g.setGradientType(GradientDrawable.RADIAL_GRADIENT);
		g.setGradientRadius(3000.0f);
		g.setGradientCenter(0.5f, 0.5f);
		TextView colorBox = (TextView) findViewById(R.id.color_box);
		try {
			colorBox.setBackgroundDrawable(g);
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		// set up background groove on hour
		RelativeLayout clockHolder = (RelativeLayout) findViewById(R.id.draw_image_here);
		CircleDraw groove = new CircleDraw(context, 600.0f, 360, 0, 0xFF666666);
		clockHolder.addView(groove);
		clockHolder.bringChildToFront(groove);

		clockDrawn = new CircleDraw(context, 600.0f, -90, -90, colorSet);
		clockHolder.addView(clockDrawn);
		clockHolder.bringChildToFront(clockDrawn);

		now.setToNow();
		Time inputTime = now;
		inputTime.hour += 6;
		inputClockSetup(inputTime, 1);

		inputTime.hour += 6;
		inputClockSetup(inputTime, 2);

		inputTime.hour += 6;
		inputClockSetup(inputTime, 3);

	}

	private void setUpLayoutVars() {
		main = (LinearLayout) findViewById(R.id.main);
		menu = (RelativeLayout) findViewById(R.id.menu);
		
	}

	private void writeLocSharedPref(int latitude, int longitude) {
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt("lat", latitude);
		editor.putInt("long", longitude);

		// Commit the edits!
		editor.commit();
	}

	private void writeLocSharedPref() {
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("currentCity", currentCity);
		editor.putString("currentStateCode", currentStateCode);
		Log.d(WHERE_TAG, "Writing in  " + currentCity);
		Log.d(WHERE_TAG, "Writing in  " + currentStateCode);
		// Commit the edits!
		editor.commit();
	}

	private void getLatLongSharedPref() {
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		oldLat = settings.getInt("lat", 0);
		oldLong = settings.getInt("long", 0);
		currentCity = settings.getString("currentCity", " ");
		currentStateCode = settings.getString("currentStateCode", " ");
		CityState temp = new CityState(currentCity, currentStateCode, oldLat, oldLong);
		if(!places.contains(temp)){
			places.add(temp);
		}
		Log.d(WHERE_TAG, "Reading  " + currentCity);
		Log.d(WHERE_TAG, "Reading  " + currentStateCode);

	}

	private void setUpGPS() {
		// GPS TESTING
		LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
		boolean enabled = service
				.isProviderEnabled(LocationManager.GPS_PROVIDER);
		String locationProvider = LocationManager.NETWORK_PROVIDER;

		// Check if enabled and if not send user to the GSP settings
		// Better solution would be to display a dialog and suggesting to
		// go to the settings
		if (!enabled) {
			Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			startActivity(intent);
			Log.v("gps", "GPS is disabled");
		}

		if (enabled) {
			Log.v("gps", "GPS is enabled");

			// Get the location manager
			locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			// Define the criteria how to select the location provider -> use
			// default
			criteria = new Criteria();
			provider = locationManager.getBestProvider(criteria, false);
			Log.v("gps", "provider: " + provider);

			if (locationManager.isProviderEnabled(provider)) {
				Log.v("gps", provider + " is enabled");
			} else {
				Log.v("gps", provider + " is NOT enabled");
			}

			Location location = locationManager.getLastKnownLocation(provider);
			// Log.v("gps", "location: " + location.toString());
			// Initialize the location fields
			if (location != null) {
				Log.v("gps", "Found a location");
				Log.v("gps", "Provider " + provider + " has been selected.");
				onLocationChanged(location);

			} else {
				lat = 0;
				lng = 0;
				Log.v("gps", "Latitude: " + lat + " Longitude: " + lng);

			}
		}

	}

	private void setUpDataBase() {
		// testing forecast json obj

		// String[] test = future.get(4);
		// for(int i = 0; i < 10; i++) {
		// Log.v("http", test[i]);
		// }

		/*
		 * OKAY GAUTAM READ
		 * 
		 * Currently, the data is being held in an arraylist and I am only
		 * parsing some data. Just let me know if you need more. Its currently
		 * stored based on indices -> I know this is retarded I will use a hash
		 * or key value pair soon
		 */

		// TESTING DATABASE
		CommentsDataSource datasource = new CommentsDataSource(this);
		datasource.open();

		Log.v("db", "Inserting...");

		String[] test0 = new String[7];
		test0[0] = "100";
		test0[1] = "101";
		test0[2] = "102";
		test0[3] = "103";
		test0[4] = "104";
		test0[5] = "105";
		test0[6] = "106";

		// String[7] test1 = { "200", "201", "202", "203", "204", "205", "206"};

		// datasource.addWeather(current.get(0));
		Log.v("db", "Found weather data");
		// datasource.createComment("TEST");
		// datasource.createComment("TEST1");
		// datasource.createComment("TEST2");
		Log.v("db", "Reading...");
		List<String[]> test = datasource.getAllWeather();

		Log.v("db", "Printing...");
		Log.v("db", test.size() + "");
		for (int i = 0; i < test.size(); i++) {
			Log.v("db", "TEMPF: " + test.get(i)[0]);
			Log.v("db", "TEMPC1: " + test.get(i)[1]);
			Log.v("db", "TEMPC2: " + test.get(i)[2]);
			Log.v("db", "TEMPC3: " + test.get(i)[3]);
			Log.v("db", "TEMPC4: " + test.get(i)[4]);
			Log.v("db", "TEMPC5: " + test.get(i)[5]);
			Log.v("db", "TEMPC6: " + test.get(i)[6]);
			Log.v("db", "TEMPC7: " + test.get(i)[7]);

		}
	}

	private void fetchData() {
		JSONParser parser = new JSONParser();
		JSONParser parser2 = new JSONParser();
		parser.setContext(context);
		parser2.setContext(context);
		Log.v("http", "parser attempting");
		// ArrayList<String> testArray;
		CityState temp = places.get(0);
		try {//sent in type, lat/state,long/place
			parser.execute("0", ""+temp.lat,""+temp.lon);
			parser2.execute("1", temp.state, temp.city);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

	private void setUpMotion() {
		state = State.IDLE;
		previousState = State.IDLE;

		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		screenWidth = metrics.widthPixels;
		screenHeight = metrics.heightPixels;

	}

	private void setUpLocationList() {
		ListView locs = (ListView) findViewById(R.id.locations);
		adapter = new LocationAdapter(this, R.layout.location_row_item, places);
		locs.setAdapter(adapter);
		locs.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> arg0, View v,
                    int index, long arg3) {
  			  CityState chosen = places.get(index);
  			  Toast.makeText(context, chosen.city, Toast.LENGTH_SHORT).show();
  			  places.remove(index);
  			  adapter.notifyDataSetChanged();
  			  places.add(0, chosen);
  			  adapter.notifyDataSetChanged();
  			  fetchData();
              
                 
                return true;
            }
}); 

	}

	public void setUpForecast() {
		ArrayList<Forecast> temps = new ArrayList<Forecast>();
		for (int i = 0; i < future.size(); i++) {
			String[] item = future.get(i);
			Forecast temp;
			Log.d(DEBUG_TAG, "Looping: " + (item[7]));
			Log.d(DEBUG_TAG, "Looping: " + forecastPicMatcher.get(item[7]));
			if (forecastPicMatcher.get(item[7]) == null) {
				if (conditionPicMatcher.get(item[7]) != null) {// if cant find
																// in forecast
																// map, check
																// conditions
																// map (more
																// extensive)
					temp = new Forecast(Integer.parseInt(item[0]),
							Integer.parseInt(item[1]),
							Integer.parseInt(item[3]),
							Integer.parseInt(item[2]),
							Integer.parseInt(item[4]),
							Integer.parseInt(item[5]), item[7], item[8],
							conditionPicMatcher.get(item[7]), celsius);
				} else {// if in neither map, display default cloud
					temp = new Forecast(Integer.parseInt(item[0]),
							Integer.parseInt(item[1]),
							Integer.parseInt(item[3]),
							Integer.parseInt(item[2]),
							Integer.parseInt(item[4]),
							Integer.parseInt(item[5]), item[7], item[8],
							conditionPicMatcher.get("Cloudy"), celsius);
				}
			} else {// display if in forecast map
				temp = new Forecast(Integer.parseInt(item[0]),
						Integer.parseInt(item[1]), Integer.parseInt(item[3]),
						Integer.parseInt(item[2]), Integer.parseInt(item[4]),
						Integer.parseInt(item[5]), item[7], item[8],
						forecastPicMatcher.get(item[7]), celsius);
			}
			temps.add(temp);
		}
		ListView days = (ListView) findViewById(R.id.days);
		days.setCacheColorHint(Color.TRANSPARENT);
		days.setFastScrollEnabled(true);
		days.setScrollingCacheEnabled(true);
		ForecastAdapter adapter = new ForecastAdapter(this,
				R.layout.forecast_row_item, temps);
		days.setAdapter(adapter);

	}

	private void setUpSlidingMenu() {

		SlidingMenu menu = getSlidingMenu();
		menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		menu.setFadeEnabled(true);
		menu.setFadeDegree(0.35f);
		menu.setMode(SlidingMenu.LEFT_RIGHT);
		menu.setSecondaryMenu(R.layout.menu2);
		menu.setAboveOffset(20);
		menu.setShadowWidthRes(R.dimen.shadow_width);
		menu.setShadowDrawable(R.drawable.shadow);
		menu.setSecondaryShadowDrawable(R.drawable.shadow2);
		menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		SlidingMenu menu = getSlidingMenu();

		if (menu.getContent() == menu.getSecondaryMenu()) {

			return true;
		}

		this.mDetector.onTouchEvent(event);
		int action = MotionEventCompat.getActionMasked(event);

		int x1 = (int) event.getX();
		int y1 = (int) event.getY();

		switch (action) {
		case (MotionEvent.ACTION_DOWN):
			// point where contact initiates
			touchdownX = (int) event.getX();
			touchdownY = (int) event.getY();
			// State of entry
			if (touchdownX < screenWidth / 2 && touchdownY < screenHeight / 2) {
				previousState = State.Q2;

			} else if (touchdownX > screenWidth / 2
					&& touchdownY < screenHeight / 2) {
				previousState = State.Q1;
			} else if (touchdownX < screenWidth / 2
					&& touchdownY > screenHeight / 2) {
				previousState = State.Q3;
			} else if (touchdownX > screenWidth / 2
					&& touchdownY > screenHeight / 2) {
				previousState = State.Q4;
			}

			Log.d(DEBUG_TAG, "onLongPress: " + event.toString());

			LinearLayout view = (LinearLayout) findViewById(R.id.main);
			View hours = (View) findViewById(R.layout.hours);
			Animation a1 = AnimationUtils.loadAnimation(context,
					android.R.anim.fade_out);
			a1.setDuration(200);
			view.startAnimation(a1);
			setContentView(R.layout.hours);

			setUpClock();
			inHours = true;

			return true;

		case (MotionEvent.ACTION_MOVE):
			// generate swiping time boundary
			now.setToNow();
			int lowerLimit = now.hour;
			int upperLimit = lowerLimit + 23;
			// Log.d(DEBUG_TAG,"Action was MOVE");
			int distX = (int) Math.pow((x1 - touchdownX), 2);
			int distY = (int) Math.pow((y1 - touchdownY), 2);
			int distance = (int) Math.sqrt(distX + distY);

			// Moving up or down?
			if (y1 > touchdownY) {
				increasingY = false;// moving towards bottom of screen
			} else {
				increasingY = true;// moving up the screen from bottom
			}

			// Current State
			// if (x1 < screenWidth / 2 && y1 < screenHeight / 2) {
			// state = State.Q2;
			// } else if (x1 > screenWidth / 2 && y1 < screenHeight / 2) {
			// state = State.Q1;
			// } else if (x1 < screenWidth / 2 && y1 > screenHeight / 2) {
			// state = State.Q3;
			// } else if (x1 > screenWidth / 2 && y1 > screenHeight / 2) {
			// state = State.Q4;
			// }
			//
			// // now we have two states to handle...one is the state we are
			// coming
			// // from and the other is the state we currently
			// // moved to...
			// // now do the logic
			//
			// if (previousState == State.Q2 && distance > 100 && !increasingY)
			// {
			// if ((internalTime.hour > lowerLimit) && !inHours) {
			// Log.v("state", "Left pull down");
			// clockSwipeTransition.setText("AN HOUR EARLIER");
			// clockSwipeTransition.setTextColor(0x50CCCCFF);
			// if (distance > 200) {
			// clockSwipeTransition.setTextColor(0xFFFFFFFF);
			// if (timeChangeCount == 0) {
			// internalTime.hour -= 1;
			// internalToDisplayTime();
			// showTime(0);
			// weatherAtTime(0);
			// timeChangeCount = 1;
			// }
			// }
			// }
			//
			// } else if (previousState == State.Q3 && distance > 100
			// && increasingY && !inHours) {
			// ;
			// Log.v("state", "Left pull up");
			// if ((internalTime.hour < upperLimit) && !inHours) {
			// clockSwipeTransition.setText("AN HOUR LATER");
			// clockSwipeTransition.setTextColor(0x50CCCCFF);
			// if (distance > 200) {
			// clockSwipeTransition.setTextColor(0xFFFFFFFF);
			// if (timeChangeCount == 0) {
			// internalTime.hour += 1;
			// internalToDisplayTime();
			// showTime(0);
			// weatherAtTime(0);
			// timeChangeCount = 1;
			// }
			// }
			// }
			// } else if (previousState == State.Q1 && distance > 100
			// && !increasingY && !inHours) {
			// if ((internalTime.hour > lowerLimit) && !inHours) {
			// Log.v("state", "Right pull down");
			// clockSwipeTransition.setText("AN HOUR EARLIER");
			// clockSwipeTransition.setTextColor(0x50CCCCFF);
			// if (distance > 200) {
			// clockSwipeTransition.setTextColor(0xFFFFFFFF);
			// if (timeChangeCount == 0) {
			// internalTime.hour -= 1;
			// internalToDisplayTime();
			// showTime(0);
			// weatherAtTime(0);
			// timeChangeCount = 1;
			// }
			// }
			//
			// }
			//
			// } else if (previousState == State.Q4 && distance > 100
			// && increasingY && !inHours) {
			//
			// Log.v("state", "Right pull up");
			// if ((internalTime.hour < upperLimit) && !inHours) {
			// clockSwipeTransition.setText("AN HOUR LATER");
			// clockSwipeTransition.setTextColor(0x50CCCCFF);
			// if (distance > 200) {
			// clockSwipeTransition.setTextColor(0xFFFFFFFF);
			// if (timeChangeCount == 0) {
			// internalTime.hour += 1;
			// internalToDisplayTime();
			// showTime(0);
			// weatherAtTime(0);
			// timeChangeCount = 1;
			// }
			// }
			// }
			// }
			if (inHours) {
				int angle = (int) Math.toDegrees(Math.atan2(x1 - screenWidth
						/ 2, y1 - screenHeight / 2));
				if (angle < 0) {
					angle += 360;
				}

				moveHourOverlay(angle);
				double dX = Math.pow((x1 - screenWidth / 2), 2);
				double dY = Math.pow((y1 - screenHeight / 2), 2);

				double d = Math.sqrt(dX + dY);
				// Log.d(DEBUG_TAG, Double.toString(d));
				TextView hoursTillShown = (TextView) findViewById(R.id.how_many_hours);
				if (d > 90) {
					int hoursAdded = calculateClockAngle(angle);
					
					hoursTillShown.setText(hoursAdded + " Hours Later");
					if (hoursAdded == 1) {
						hoursTillShown.setText(hoursAdded + " Hour Later");
					}
					clockToInternalTime(hoursAdded);
					internalTime.minute = 0;
				} else {
					internalTime.setToNow();
					hoursTillShown.setText("Current");

				}
				internalToDisplayTime();
				showTime(1);
				weatherAtTime(1);
			}

			return true;
		case (MotionEvent.ACTION_UP):
			// Log.d(DEBUG_TAG,"Action was UP");
			if (inHours) {
				inHours = false;

				setContentView(R.layout.activity_main);
				view = (LinearLayout) findViewById(R.id.main);

				Animation a2 = AnimationUtils.loadAnimation(context,
						android.R.anim.fade_in);
				a2.setDuration(200);
				view.startAnimation(a2);

				int angle = (int) Math.toDegrees(Math.atan2(x1 - screenWidth
						/ 2, y1 - screenHeight / 2));
				if (angle < 0) {
					angle += 360;
				}
				double dX = Math.pow((x1 - screenWidth / 2), 2);
				double dY = Math.pow((y1 - screenHeight / 2), 2);

				double d = Math.sqrt(dX + dY);
				// Log.d(DEBUG_TAG, Double.toString(d));

				if (d > 90) {
					int hoursAdded = calculateClockAngle(angle);
					clockToInternalTime(hoursAdded);
					internalTime.minute = 0;
				} else {
					internalTime.setToNow();

				}
				internalToDisplayTime();
				showTime(0);
				weatherAtTime(0);

			} else if (!inHours) {
				amPmCount = 0;
				timeChangeCount = 0;

			}
			return true;
		case (MotionEvent.ACTION_CANCEL):
			Log.d(DEBUG_TAG, "Action was CANCEL");
			return true;
		case (MotionEvent.ACTION_OUTSIDE):
			Log.d(DEBUG_TAG, "Movement occurred outside bounds "
					+ "of current screen element");
			return true;
		default:
			return true;
		}

	}

	private void moveHourOverlay(int angle) {
		int angleDrawn = (540 - angle) % 360;
		Log.d(DEBUG_TAG, String.valueOf(angleDrawn));
		clockDrawn.finishdegree = angleDrawn;
		clockDrawn.invalidate();

	}

	private void clockToInternalTime(int hoursAdded) {
		internalTime.setToNow();
		internalTime.hour += hoursAdded;
	}

	public void internalToDisplayTime() {// modifies internal time to an
											// appropriate version for display
		int hour = internalTime.hour;
		if (hour < 24) {
			today = true;
		}
		if (hour >= 24) {
			today = false;
			hour -= 24;
		}

		if (hour > 12) {
			am = false;
			hour -= 12;
		} else if (hour <= 12) {
			am = true;
		}
		displayTime.hour = hour;
		displayTime.minute = internalTime.minute;
	}

	public void inputClockSetup(Time input, int digit) {// helps generate
														// timedates for marking
														// around clock [3 = 6
														// oclock pos, 9 = 9 o
														// clock pos...]
		int inputHour = input.hour;
		Time outputTime = new Time();
		boolean inputToday = true;
		boolean inputAm = true;
		if (inputHour < 24) {
			inputToday = true;
		}
		if (inputHour >= 24) {
			inputToday = false;
			inputHour -= 24;
		}

		if (inputHour > 12) {
			inputAm = false;
			inputHour -= 12;
		} else if (inputHour <= 12) {
			inputAm = true;
		}
		outputTime.hour = inputHour;
		outputTime.minute = input.minute;
		String output;
		output = String.valueOf(outputTime.hour);

		if (inputAm) {
			output += "am";
		} else {
			output += "pm";
		}
		// output+= "\n"+findDay(inputToday);
		if (input.hour == 0 || input.hour == 24) {

			output = "Midnight";
		} else if (input.hour == 12 || input.hour == 36) {

			output = "Noon";
		}
		Log.d(DEBUG_TAG, output);
		// if (digit == 3) {
		// TextView left = (TextView) findViewById(R.id.left);
		// left.setText(output);
		// } else if (digit == 2) {
		// TextView bottom = (TextView) findViewById(R.id.bottom);
		// bottom.setText(output);
		// } else if (digit == 1) {
		// TextView right = (TextView) findViewById(R.id.right);
		// right.setText(output);
		// }

	}

	public int closestHourToNow() {
		now.setToNow();
		int closest = 0;
		if (now.minute < 30) {
			closest = now.hour;
		} else {
			closest = now.hour + 1;
		}
		if (closest == 25) {
			closest = 1;
		}
		return closest;
	}

	public void showTime(int type) {
		now.setToNow();
		if (type == 0) {
			clock = (TextView) findViewById(R.id.time);
			dayView = (TextView) findViewById(R.id.day);
		} else {
			clock = (TextView) findViewById(R.id.choosertime);
			dayView = (TextView) findViewById(R.id.chooserdate);
		}

		dayView.setText(findDay(today));

		String amPm;
		if (am) {
			amPm = "am";
		} else {
			amPm = "pm";
		}

		if (internalTime.hour == 0 || internalTime.hour == 24) {

			clock.setText("Midnight");
		} else if (internalTime.hour == 12 || internalTime.hour == 36) {

			clock.setText("Noon");
		}

		else if (displayTime.minute < 10) {

			clock.setText(displayTime.hour + ":0" + displayTime.minute + amPm);

		} else {
			clock.setText(displayTime.hour + ":" + displayTime.minute + amPm);
		}
	}

	private String findDay(boolean inputToday) {
		int dayToday = now.weekDay;
		String day = "", tomorrowDay = "";
		switch (dayToday) {
		case 0:
			day = "Sunday";
			tomorrowDay = "Monday";
			break;
		case 1:
			day = "Monday";
			tomorrowDay = "Tuesday";
			break;
		case 2:
			day = "Tuesday";
			tomorrowDay = "Wednesday";
			break;
		case 3:
			day = "Wednesday";
			tomorrowDay = "Thursday";
			break;
		case 4:
			day = "Thursday";
			tomorrowDay = "Friday";
			break;
		case 5:
			day = "Friday";
			tomorrowDay = "Saturday";
			break;
		case 6:
			day = "Saturday";
			tomorrowDay = "Sunday";
			break;
		}
		if (inputToday) {
			return day;
		} else {
			return tomorrowDay;
		}
	}

	private int calculateClockAngle(int angle) {
		int upperLimit = 15;
		int hour = 12;
		while (upperLimit < 365) {
			if (angle < upperLimit) {

				return hour;

			} else if (angle >= upperLimit) {

				upperLimit += 15;
				hour--;
				if (hour == 0) {
					hour = 24;
				}
			}
		}
		return 6;
	}

	class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
		private static final String DEBUG_TAG = "Motion";

		@Override
		public void onLongPress(MotionEvent event) {

		}

		// @Override
		// public boolean onDoubleTap(MotionEvent event) {
		// // Toast.makeText(context, "double tap",Toast.LENGTH_SHORT).show();
		// if ((!inHours) && (amPmCount == 0)) {
		// // Change imageview on doubleTap
		//
		// if (amPm.equals("am")) {
		// amPm = "pm";
		// } else if (amPm.equals("pm")) {
		// amPm = "am";
		// }
		//
		// displayTime(0);
		// amPmCount = 1;
		// weatherAtTime(displayTime.hour, amPm, 0);
		//
		// }
		// return true;
		// }

	}

	// GPS

	/* Request updates at startup */
	@Override
	protected void onResume() {
		super.onResume();
		if (gps) {
			locationManager.requestLocationUpdates(provider, 400, 1000, this);
		}
		weatherAtTime(0);
		setUpForecast();
	}

	/* Remove the locationlistener updates when Activity is paused */
	@Override
	protected void onPause() {
		super.onPause();
		if (gps) {
			locationManager.removeUpdates(this);
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		lat = (int) (location.getLatitude());
		lng = (int) (location.getLongitude());
		Log.v("gps", "Latitude: " + lat + " Longitude: " + lng);
		getLatLongSharedPref();
		if (gottenWeather) {
			return;
		}
		if (lat == oldLat && lng == oldLong && !gottenWeather) {
			fetchData();
			boolean gottenWeather = true;
			return;
		} else {
			writeLocSharedPref(lat, lng);
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD
				&& Geocoder.isPresent()) {
			// Since the geocoding API is synchronous and may take a while. You
			// don't want to lock
			// up the UI thread. Invoking reverse geocoding in an AsyncTask.
			(new ReverseGeocodingTask(this))
					.execute(new Location[] { location });
		}
	};

	@Override
	public void onProviderDisabled(String provider) {
		Toast.makeText(this, "Disabled provider " + provider,
				Toast.LENGTH_SHORT).show();

	}

	@Override
	public void onProviderEnabled(String provider) {
		Toast.makeText(this, "Enabled new provider " + provider,
				Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	public void callPreferences(View view) {
		Intent intent = new Intent(this, SettingsActivity.class);
		startActivityForResult(intent, 1);
	}

	public void weatherAtTime(int type) {

		int lookupHour = internalTime.hour;
		if (lookupHour >= 24)
			lookupHour -= 24;
		for (int i = 0; i < current.size(); i++) {

			Temperature temp = current.get(i);
			int time = temp.toNormalTime(temp.dateTime).getHours();

			// Log.d(DEBUG_TAG, "Looping: " +time);
			if (time == lookupHour) {
				main = (LinearLayout) findViewById(R.id.main);
				int temperature = 0;
				if (celsius) {
					temperature = (int) ((temp.temperature - 32)/(1.8));

				} else if (!celsius) {
					temperature = (int)temp.temperature;

				}
				String wind = temp.windSpeed;
				String precip = temp.chancePrecip;
				String condition = temp.desc;
				if (type == 0) {
					// Log.d(DEBUG_TAG, "Found Info: "+time+"   Temp: " +
					// temperature + " condition: " + condition);
					TextView temperatureView = (TextView) findViewById(R.id.temperature);
					try {
						if (celsius) {
							temperatureView.setText(Integer
									.toString(temperature) + "C");
						} else {
							temperatureView.setText(Integer
									.toString(temperature) + "F");
						}
					} catch (NullPointerException e) {
						e.printStackTrace();
						Log.e(DEBUG_TAG,
								"View id " + Integer.valueOf(R.id.temperature));
						temperatureView.setText(Integer.toString(temperature)
								+ "F");
					}
					TextView windSpeed = (TextView) findViewById(R.id.windspeed);
					windSpeed.setText(wind + "mph");
					TextView precipChance = (TextView) findViewById(R.id.precip);
					precipChance.setText(precip + "%");
					TextView conditionView = (TextView) findViewById(R.id.condition);
					conditionView.setText(condition);
					ImageView weatherIcon = (ImageView) findViewById(R.id.weatherIcon);
					try {
						weatherIcon.setImageResource(conditionPicMatcher
								.get(condition));
					} catch (Exception E) {

					}

					if (temperature <= 50) {
						colorSet = craftColors(60, temperature, !celsius);
						main.setBackgroundColor(colorSet);
						// hours.setBackgroundColor(0xFF33B5E5);
					} else if (temperature > 50) {
						colorSet = craftColors(60, temperature, !celsius);
						main.setBackgroundColor(colorSet);
						// hours.setBackgroundColor(0xFFFF9900);
					}

					adapter.notifyDataSetChanged();
					return;
				} else if (type == 1) {
					menu = (RelativeLayout) findViewById(R.id.menu);
					TextView temperatureView = (TextView) findViewById(R.id.temp_chooser);
					temperatureView
							.setText(Integer.toString(temperature) + "F");
					if (celsius) {
						temperatureView.setText(Integer.toString(temperature)
								+ "C");
					}
					TextView precipChance = (TextView) findViewById(R.id.precip_chooser);
					precipChance.setText(precip + "%");
					int color = craftColors(60, temperature, !celsius);
					temperatureView.setTextColor(color);
					clockDrawn.color = color;
					clockDrawn.invalidate();

				}
			}

		}
	}

	public int craftColors(int thresholdTemp, int currentTemp,
			boolean fahrenheitFlag) {
		// where threshold is the user set temperature as to being cold
		// where current is the current temperature
		// where fahrenheitflag -> true if in fahrenheit and is false needs to
		// be in fahrenheit
		int current = currentTemp;
		int threshold = thresholdTemp;

		if (fahrenheitFlag) {
			Log.v("temp", "As fahrenheit: " + current);
			current = (int) ((current - 32) / 1.8f);
			Log.v("temp", "As celsius: " + current);

			threshold = (int) ((threshold - 32) / 1.8f);
			Log.v("temp", "Threshold as celsius: " + threshold);
			// convert celsius to fahrenheit
		}

		if (current >= threshold) {
			// here we are greater than the threshold so will be a red color
			int increment = 255 / (40 - threshold);
			int red = 255;
			int blue = 0;

			int green = 255 - (current * increment);

			String RED = Integer.toHexString(red);
			String GREEN = Integer.toHexString(green);
			String BLUE = Integer.toHexString(blue);

			StringBuilder builder = new StringBuilder();
			builder.append("#");
			builder.append(RED);
			builder.append(GREEN);
			builder.append(BLUE);
			builder.append("0");
			int color;
			try {
				color = Color.parseColor(builder.toString());
			} catch (IllegalArgumentException e) {
				color = (0xFF33B5E5);
			}
			return color;

		} else if (current < threshold) {
			int increment = (255 / (threshold + 10));
			Log.v("temp", "Increment: " + increment);
			StringBuilder builder = new StringBuilder();

			// r,g are always 0 and blue is just changing
			// start at 0 and will gain to 255

			int blue = 255 - (current * increment);
			Log.v("temp", "Blue is: " + blue);

			builder.append("#0000");
			builder.append(Integer.toHexString(blue));
			Log.v("temp", "builder string: " + builder.toString());
			int color;	
			try{
				color = Color.parseColor(builder.toString());
			}catch (IllegalArgumentException e) {
				color = (0xFF33B5E5);
			}
			
			return color;

		} else {
			return 0;
		}

	}

	// Maps stuff
	public void setUpMap() {//NOTICE: NEED TO ADD NEW VARIABLES FROM NEW API
		conditionPicMatcher = new HashMap<String, Integer>();
		forecastPicMatcher = new HashMap<String, Integer>();

		forecastPicMatcher.put("Chance of Freezing Rain",
				R.drawable.small_cloudsnow);
		forecastPicMatcher
				.put("Chance of Flurries", R.drawable.small_cloudsnow);
		forecastPicMatcher.put("Chance of Rain", R.drawable.small_cloudrain);
		forecastPicMatcher
				.put("Chance of Sleet", R.drawable.small_cloudhailalt);
		forecastPicMatcher.put("Chance of Snow", R.drawable.small_cloudsnowalt);
		forecastPicMatcher.put("Chance of Thunderstorms",
				R.drawable.small_cloudlightning);
		forecastPicMatcher.put("Chance of a Thunderstorm",
				R.drawable.small_cloudlightning);
		forecastPicMatcher.put("Cloudy", R.drawable.small_cloud);
		forecastPicMatcher.put("Clear", R.drawable.small_sun);
		forecastPicMatcher.put("Flurries", R.drawable.small_cloudsnow);
		forecastPicMatcher.put("Fog", R.drawable.small_cloudfog);
		forecastPicMatcher.put("Haze", R.drawable.small_cloudfog);
		forecastPicMatcher.put("Partly Cloudy", R.drawable.small_cloud);
		forecastPicMatcher.put("Mostly Cloudy", R.drawable.small_cloud);
		forecastPicMatcher.put("Partly Sunny", R.drawable.small_cloudsun);
		forecastPicMatcher.put("Mostly Sunny", R.drawable.small_cloudsun);
		forecastPicMatcher.put("Freezing Rain", R.drawable.small_cloudsnow);
		forecastPicMatcher.put("Rain", R.drawable.small_cloudrain);
		forecastPicMatcher.put("Sleet", R.drawable.small_cloudhailalt);
		forecastPicMatcher.put("Snow", R.drawable.small_cloudsnowalt);
		forecastPicMatcher.put("Overcast", R.drawable.small_cloud);
		forecastPicMatcher.put("Sunny", R.drawable.small_sun);
		forecastPicMatcher.put("Scattered Clouds", R.drawable.small_cloud);
		forecastPicMatcher.put("Unknown", R.drawable.small_cloud);
		forecastPicMatcher
				.put("Thunderstorms", R.drawable.small_cloudlightning);
		forecastPicMatcher.put("Thunderstorm", R.drawable.small_cloudlightning);

		conditionPicMatcher.put("Light Drizzle",
				R.drawable.clim_clouddrizzlealt);
		conditionPicMatcher.put("Drizzle", R.drawable.clim_clouddrizzle);
		conditionPicMatcher.put("Heavy Drizzle", R.drawable.clim_clouddrizzle);
		conditionPicMatcher.put("Light Rain", R.drawable.clim_cloudrainalt);
		conditionPicMatcher.put("Rain", R.drawable.clim_cloudrain);
		conditionPicMatcher.put("Heavy Rain", R.drawable.clim_cloudrain);
		conditionPicMatcher.put("Light Rain Showers",
				R.drawable.clim_cloudrainalt);
		conditionPicMatcher.put("Rain Showers", R.drawable.clim_cloudrain);
		conditionPicMatcher
				.put("Heavy Rain Showers", R.drawable.clim_cloudrain);
		conditionPicMatcher.put("Chance of Rain", R.drawable.clim_cloudrain);

		conditionPicMatcher
				.put("Chance of Flurries", R.drawable.clim_cloudsnow);
		conditionPicMatcher.put("Flurries", R.drawable.clim_cloudsnow);
		conditionPicMatcher.put("Light Snow", R.drawable.clim_cloudsnow);
		conditionPicMatcher.put("Snow", R.drawable.clim_cloudsnowalt);
		conditionPicMatcher.put("Chance of Snow", R.drawable.clim_cloudsnowalt);
		conditionPicMatcher.put("Heavy Snow", R.drawable.clim_cloudsnowalt);
		conditionPicMatcher
				.put("Light Snow Showers", R.drawable.clim_cloudsnow);
		conditionPicMatcher.put("Snow Showers", R.drawable.clim_cloudsnowalt);
		conditionPicMatcher.put("Heavy Snow Showers",
				R.drawable.clim_cloudsnowalt);
		conditionPicMatcher.put("Light Snow Blowing Snow Mist",
				R.drawable.clim_cloudsnow);
		conditionPicMatcher.put("Snow Blowing Snow Mist",
				R.drawable.clim_cloudsnowalt);
		conditionPicMatcher.put("Heavy Snow Blowing Snow Mist",
				R.drawable.clim_cloudsnowalt);
		conditionPicMatcher.put("Light Snow Grains", R.drawable.clim_cloudsnow);
		conditionPicMatcher.put("Snow Grains", R.drawable.clim_cloudsnowalt);
		conditionPicMatcher.put("Heavy Snow Grains",
				R.drawable.clim_cloudsnowalt);
		conditionPicMatcher
				.put("Light Ice Crystals", R.drawable.clim_cloudsnow);
		conditionPicMatcher.put("Ice Crystals", R.drawable.clim_cloudsnowalt);
		conditionPicMatcher.put("Heavy Ice Crystals",
				R.drawable.clim_cloudsnowalt);
		conditionPicMatcher.put("Light Low Drifting Snow",
				R.drawable.clim_cloudsnow);
		conditionPicMatcher.put("Low Drifting Snow",
				R.drawable.clim_cloudsnowalt);
		conditionPicMatcher.put("Heavy Drifting Snow",
				R.drawable.clim_cloudsnowalt);
		conditionPicMatcher
				.put("Light Blowing Snow", R.drawable.clim_cloudsnow);
		conditionPicMatcher.put("Blowing Snow", R.drawable.clim_cloudsnowalt);
		conditionPicMatcher.put("Heavy Blowing Snow",
				R.drawable.clim_cloudsnowalt);

		conditionPicMatcher.put("Light Ice Pellets", R.drawable.clim_cloudhail);
		conditionPicMatcher.put("Ice Pellets", R.drawable.clim_cloudhailalt);
		conditionPicMatcher.put("Heavy Ice Pellets",
				R.drawable.clim_cloudhailalt);
		conditionPicMatcher.put("Light Hail", R.drawable.clim_cloudhail);
		conditionPicMatcher.put("Hail", R.drawable.clim_cloudhailalt);
		conditionPicMatcher.put("Heavy Hail", R.drawable.clim_cloudhailalt);

		conditionPicMatcher
				.put("Light Hail Showers", R.drawable.clim_cloudhail);
		conditionPicMatcher.put("Hail Showers", R.drawable.clim_cloudhailalt);
		conditionPicMatcher.put("Heavy Hail Showers",
				R.drawable.clim_cloudhailalt);
		conditionPicMatcher.put("Light Small Hail Showers",
				R.drawable.clim_cloudhail);
		conditionPicMatcher
				.put("Chance of Sleet", R.drawable.clim_cloudhailalt);
		conditionPicMatcher.put("Sleet", R.drawable.clim_cloudhailalt);
		conditionPicMatcher.put("Small Hail Showers",
				R.drawable.clim_cloudhailalt);
		conditionPicMatcher.put("Heavy Small Hail Showers",
				R.drawable.clim_cloudhailalt);
		conditionPicMatcher.put("Light Ice Pellet Showers",
				R.drawable.clim_cloudhail);
		conditionPicMatcher.put("Ice Pellet Showers",
				R.drawable.clim_cloudhailalt);
		conditionPicMatcher.put("Heavy Ice Pellet Showers",
				R.drawable.clim_cloudhailalt);

		conditionPicMatcher.put("Light Mist", R.drawable.clim_cloudfogalt);
		conditionPicMatcher.put("Mist", R.drawable.clim_cloudfog);
		conditionPicMatcher.put("Heavy Mist", R.drawable.clim_cloudfog);
		conditionPicMatcher.put("Light Fog", R.drawable.clim_cloudfogalt);
		conditionPicMatcher.put("Fog", R.drawable.clim_cloudfog);
		conditionPicMatcher.put("Heavy Fog", R.drawable.clim_cloudfog);
		conditionPicMatcher.put("Light Fog Patches",
				R.drawable.clim_cloudfogalt);
		conditionPicMatcher.put("Fog Patches", R.drawable.clim_cloudfog);
		conditionPicMatcher.put("Heavy Fog Patches", R.drawable.clim_cloudfog);
		conditionPicMatcher.put("Light Smoke", R.drawable.clim_cloudfogalt);
		conditionPicMatcher.put("Smoke", R.drawable.clim_cloudfog);
		conditionPicMatcher.put("Heavy Smoke", R.drawable.clim_cloudfog);
		conditionPicMatcher.put("Light Volcanic Ash",
				R.drawable.clim_cloudfogalt);
		conditionPicMatcher.put("Volcanic Ash", R.drawable.clim_cloudfog);
		conditionPicMatcher.put("Heavy Volcanic Ash", R.drawable.clim_cloudfog);
		conditionPicMatcher.put("Light Widespread Dust",
				R.drawable.clim_cloudfogalt);
		conditionPicMatcher.put("Widespread Dust", R.drawable.clim_cloudfog);
		conditionPicMatcher.put("Heavy Widespread Dust",
				R.drawable.clim_cloudfog);
		conditionPicMatcher.put("Light Sand", R.drawable.clim_cloudfogalt);
		conditionPicMatcher.put("Sand", R.drawable.clim_cloudfog);
		conditionPicMatcher.put("Heavy Sand", R.drawable.clim_cloudfog);
		conditionPicMatcher.put("Light Haze", R.drawable.clim_cloudfogalt);
		conditionPicMatcher.put("Haze", R.drawable.clim_cloudfog);
		conditionPicMatcher.put("Heavy Haze", R.drawable.clim_cloudfog);
		conditionPicMatcher.put("Light Dust Whirls",
				R.drawable.clim_cloudfogalt);
		conditionPicMatcher.put("Dust Whirls", R.drawable.clim_cloudfog);
		conditionPicMatcher.put("Heavy Dust Whirls", R.drawable.clim_cloudfog);
		conditionPicMatcher.put("Light Sandstorm", R.drawable.clim_cloudfogalt);
		conditionPicMatcher.put("Sandstorm", R.drawable.clim_cloudfog);
		conditionPicMatcher.put("Heavy Sandstorm", R.drawable.clim_cloudfog);
		conditionPicMatcher.put("Light Low Drifting Widespread Dust",
				R.drawable.clim_cloudfogalt);
		conditionPicMatcher.put("Low Drifting Widespread Dust",
				R.drawable.clim_cloudfog);
		conditionPicMatcher.put("Heavy Low Drifting Widespread Dust",
				R.drawable.clim_cloudfog);
		conditionPicMatcher.put("Light Low Drifting Sand",
				R.drawable.clim_cloudfogalt);
		conditionPicMatcher.put("Low Drifting Sand", R.drawable.clim_cloudfog);
		conditionPicMatcher.put("Heavy Low Drifting Sand",
				R.drawable.clim_cloudfog);
		conditionPicMatcher.put("Light Blowing Widespread Dust",
				R.drawable.clim_cloudfogalt);
		conditionPicMatcher.put("Blowing Widespread Dust",
				R.drawable.clim_cloudfog);
		conditionPicMatcher.put("Heavy Blowing Widespread Dust",
				R.drawable.clim_cloudfog);
		conditionPicMatcher.put("Light Blowing Sand",
				R.drawable.clim_cloudfogalt);
		conditionPicMatcher.put("Blowing Sand", R.drawable.clim_cloudfog);
		conditionPicMatcher.put("Heavy Blowing Sand", R.drawable.clim_cloudfog);
		conditionPicMatcher.put("Light Rain Mist", R.drawable.clim_cloudfogalt);
		conditionPicMatcher.put("Rain Mist", R.drawable.clim_cloudfog);
		conditionPicMatcher.put("Heavy Rain Mist", R.drawable.clim_cloudfog);
		conditionPicMatcher.put("Light Freezing Fog",
				R.drawable.clim_cloudfogalt);
		conditionPicMatcher.put("Freezing Fog", R.drawable.clim_cloudfog);
		conditionPicMatcher.put("Heavy Freezing Fog", R.drawable.clim_cloudfog);
		conditionPicMatcher.put("Patches of Fog", R.drawable.clim_cloudfogalt);
		conditionPicMatcher.put("Shallow Fog", R.drawable.clim_cloudfogalt);
		conditionPicMatcher.put("Partial Fog", R.drawable.clim_cloudfogalt);

		conditionPicMatcher.put("Chance of Thunderstorms",
				R.drawable.clim_cloudlightning);
		conditionPicMatcher.put("Chance of a Thunderstorm",
				R.drawable.clim_cloudlightning);
		conditionPicMatcher.put("Light Thunderstorm",
				R.drawable.clim_cloudlightning);
		conditionPicMatcher
				.put("Thunderstorms", R.drawable.clim_cloudlightning);
		conditionPicMatcher.put("Thunderstorm", R.drawable.clim_cloudlightning);
		conditionPicMatcher.put("Heavy Thunderstorms",
				R.drawable.clim_cloudlightning);
		conditionPicMatcher.put("Light Thunderstorms and Rain",
				R.drawable.clim_cloudlightning);
		conditionPicMatcher.put("Thunderstorms and Rain",
				R.drawable.clim_cloudlightning);
		conditionPicMatcher.put("Heavy Thunderstorms and Rain",
				R.drawable.clim_cloudlightning);
		conditionPicMatcher.put("Light Thunderstorms and Snow",
				R.drawable.clim_cloudlightning);
		conditionPicMatcher.put("Thunderstorms and Snow",
				R.drawable.clim_cloudlightning);
		conditionPicMatcher.put("Heavy Thunderstorms and Snow",
				R.drawable.clim_cloudlightning);
		conditionPicMatcher.put("Light Thunderstorms and Ice Pellets",
				R.drawable.clim_cloudlightning);
		conditionPicMatcher.put("Thunderstorms and Ice Pellets",
				R.drawable.clim_cloudlightning);
		conditionPicMatcher.put("Heavy Thunderstorms and Ice Pellets",
				R.drawable.clim_cloudlightning);
		conditionPicMatcher.put("Light Thunderstorms with Hail",
				R.drawable.clim_cloudlightning);
		conditionPicMatcher.put("Thunderstorms with Hail",
				R.drawable.clim_cloudlightning);
		conditionPicMatcher.put("Heavy Thunderstorms with Hail",
				R.drawable.clim_cloudlightning);
		conditionPicMatcher.put("Light Thunderstorms with Small Hail",
				R.drawable.clim_cloudlightning);
		conditionPicMatcher.put("Thunderstorms with Small Hail",
				R.drawable.clim_cloudlightning);
		conditionPicMatcher.put("Heavy Thunderstorms with Small Hail",
				R.drawable.clim_cloudlightning);

		conditionPicMatcher.put("Light Freezing Drizzle",
				R.drawable.clim_cloudsnow);
		conditionPicMatcher.put("Freezing Drizzle", R.drawable.clim_cloudsnow);
		conditionPicMatcher.put("Heavy Freezing Drizzle",
				R.drawable.clim_cloudsnow);
		conditionPicMatcher.put("Light Freezing Rain",
				R.drawable.clim_cloudsnow);
		conditionPicMatcher.put("Chance of Freezing Rain",
				R.drawable.clim_cloudsnow);
		conditionPicMatcher.put("Freezing Rain", R.drawable.clim_cloudsnow);
		conditionPicMatcher.put("Heavy Freezing Rain",
				R.drawable.clim_cloudsnow);

		conditionPicMatcher.put("Overcast", R.drawable.clim_cloud);
		conditionPicMatcher.put("Cloudy", R.drawable.clim_cloud);
		conditionPicMatcher.put("Partly Cloudy", R.drawable.clim_cloud);
		conditionPicMatcher.put("Mostly Cloudy", R.drawable.clim_cloud);
		conditionPicMatcher.put("Partly Sunny", R.drawable.clim_cloudsun);
		conditionPicMatcher.put("Mostly Sunny", R.drawable.clim_cloudsun);
		conditionPicMatcher.put("Sunny", R.drawable.clim_sun);
		conditionPicMatcher.put("Scattered Clouds", R.drawable.clim_cloud);
		conditionPicMatcher.put("Squals", R.drawable.clim_cloudwind);
		conditionPicMatcher.put("Funnel Cloud", R.drawable.clim_cloud);
		conditionPicMatcher.put("Unknown Precipitation", R.drawable.clim_cloud);
		conditionPicMatcher.put("Unknown", R.drawable.clim_cloud);
		conditionPicMatcher.put("Clear", R.drawable.clim_sun);

	}

	// AsyncTask encapsulating the reverse-geocoding API. Since the geocoder API
	// is blocked,
	// we do not want to invoke it from the UI thread.
	private class ReverseGeocodingTask extends
			AsyncTask<Location, Void, String> {
		Context mContext;

		public ReverseGeocodingTask(Context context) {
			super();
			mContext = context;
		}

		@Override
		protected String doInBackground(Location... params) {
			Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());

			Location loc = params[0];
			List<Address> addresses = null;
			try {
				// Call the synchronous getFromLocation() method by passing in
				// the lat/long values.
				addresses = geocoder.getFromLocation(loc.getLatitude(),
						loc.getLongitude(), 1);
			} catch (IOException e) {
				e.printStackTrace();
				// Update UI field with the exception.
				// Message.obtain(mHandler, UPDATE_ADDRESS,
				// e.toString()).sendToTarget();
			}
			if (addresses != null && addresses.size() > 0) {
				Address address = addresses.get(0);
				// Format the first line of address (if available), city, and
				// country name.
				String addressText = String.format("%s, %s",
						address.getLocality(), address.getAdminArea());
				// Update the UI via a message handler.
				// Message.obtain(mHandler, UPDATE_ADDRESS,
				// addressText).sendToTarget();
				return addressText;

			}
			return null;
		}

		@Override
		protected void onPostExecute(String s) {
			Log.v("gps", "s string is: " + s);
			String currentLocation = s;
			Log.v("gps", "currentlocation: " + currentLocation);
			String[] components = s.split(",");
			currentCity = components[0];
			String temp = components[1];
			String[] tempy = temp.split("\\s+");
			currentStateCode = tempy[1];

			Log.v("gps", "current city: " + currentCity);
			Log.v("gps", "current state code: " + currentStateCode);
			writeLocSharedPref();
			setUpLocationList();
			if (!gottenWeather) {
				fetchData();
				gottenWeather = true;
			}
			;

		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == 1) {

			if (resultCode == RESULT_OK) {
				boolean result = data.getBooleanExtra("result", false);
				SharedPreferences sharedPref = PreferenceManager
						.getDefaultSharedPreferences(this);
				celsius = sharedPref.getBoolean("temp_scale", false);
				gottenWeather = true;
				weatherAtTime(0);

			}
			if (resultCode == RESULT_CANCELED) {
				// Write your code on no result return
			}
		}

	}

	public int getColorSet() {
		return colorSet;
	}

	public void setColorSet(int colorSet) {
		this.colorSet = colorSet;
	}
	
	private ArrayList<String> autocomplete(String input) {
	    ArrayList<String> resultList = null;
	    
	    HttpURLConnection conn = null;
	    StringBuilder jsonResults = new StringBuilder();
	    try {
	        StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
	        sb.append("?sensor=false&key=" + API_KEY);
	        sb.append("&components=country:usa");
	        sb.append("&input=" + URLEncoder.encode(input, "utf8"));
	        
	        URL url = new URL(sb.toString());
	        conn = (HttpURLConnection) url.openConnection();
	        InputStreamReader in = new InputStreamReader(conn.getInputStream());
	        
	        // Load the results into a StringBuilder
	        int read;
	        char[] buff = new char[1024];
	        while ((read = in.read(buff)) != -1) {
	            jsonResults.append(buff, 0, read);
	        }
	    } catch (MalformedURLException e) {
	        Log.e(LOG_TAG, "Error processing Places API URL", e);
	        return resultList;
	    } catch (IOException e) {
	        Log.e(LOG_TAG, "Error connecting to Places API", e);
	        return resultList;
	    } finally {
	        if (conn != null) {
	            conn.disconnect();
	        }
	    }

	    try {
	        // Create a JSON object hierarchy from the results
	        JSONObject jsonObj = new JSONObject(jsonResults.toString());
	        JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");
	        
	        // Extract the Place descriptions from the results
	        resultList = new ArrayList<String>(predsJsonArray.length());
	        for (int i = 0; i < predsJsonArray.length(); i++) {
	            resultList.add(predsJsonArray.getJSONObject(i).getString("description"));
	        }
	    } catch (JSONException e) {
	        Log.e(LOG_TAG, "Cannot process JSON results", e);
	    }
	    
	    return resultList;
	}
	
	private class PlacesAutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {
	    private ArrayList<String> resultList;
	    
	    public PlacesAutoCompleteAdapter(Context context, int textViewResourceId) {
	        super(context, textViewResourceId);
	    }
	    
	    @Override
	    public int getCount() {
	        return resultList.size();
	    }

	    @Override
	    public String getItem(int index) {
	        return resultList.get(index);
	    }

	    @Override
	    public Filter getFilter() {
	        Filter filter = new Filter() {
	            @Override
	            protected FilterResults performFiltering(CharSequence constraint) {
	                FilterResults filterResults = new FilterResults();
	                if (constraint != null) {
	                    // Retrieve the autocomplete results.
	                    resultList = autocomplete(constraint.toString());
	                    
	                    // Assign the data to the FilterResults
	                    filterResults.values = resultList;
	                    filterResults.count = resultList.size();
	                }
	                return filterResults;
	            }

	            @Override
	            protected void publishResults(CharSequence constraint, FilterResults results) {
	                if (results != null && results.count > 0) {
	                    notifyDataSetChanged();
	                }
	                else {
	                    notifyDataSetInvalidated();
	                }
	            }};
	        return filter;
	    }
	}
	
	  public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
		
	        String str = (String) adapterView.getItemAtPosition(position);
	        String[] temp = str.split(",");
	        Toast.makeText(this, temp[1], Toast.LENGTH_SHORT).show();
	        CityState newLocation = new CityState(temp[0].trim(),temp[1].trim());
	        CityState addLocation = getLatAndLong(str, newLocation);
	        places.add(addLocation);
	        adapter.notifyDataSetChanged();
		  
	    }
	  
	  
	  public CityState getLatAndLong(String searchedAddress, CityState target){

		    Geocoder coder = new Geocoder(this);
		    List<Address> address;
		    try 
		    {
		        address = coder.getFromLocationName(searchedAddress,5);
		        if (address == null) {
		            Log.d("getCord", "############Address not correct #########");
		        }
		        Address location = address.get(0);

		        Log.d("getCord", "Address Latitude : "+ location.getLatitude() + "Address Longitude : "+ location.getLongitude());
		        target.lat = location.getLatitude();
		        target.lon = location.getLongitude();
		        return target;

		    }
		    catch(Exception e)
		    {
		        Log.d("getCord", "MY_ERROR : ############Address Not Found");
		        return null;
		    }
		}

	public void setSaveCurrent(ArrayList<Temperature> temps) {//JOE READ!!!  This is where we return the 24 hour temp array. SAVE DATABASE HERE!!!
		current = temps;
		
		File dbFile = new File("weather.db");
		//check to see if db exists and if so delete it

		if(dbFile.exists()) {
			Log.v("db", "database exists so deleting it");
			this.deleteDatabase("weather.db");
		} else {
			Log.v("db", "database doesn't exists so just continue");
		}
		
		CommentsDataSource datasource = new CommentsDataSource(this);
		datasource.open();

		
		Log.v("db", "Inserting...");
		
		
		for(int i = 0; i<temps.size();i++){//this is already in order of hours, starting at the current hour block aka 1:30 am -> starts at 1 am- 2am block
			Temperature t = current.get(i);
			String[] tempdb = new String[8];
			tempdb[0] = String.valueOf(i); //just an id
			tempdb[1] = String.valueOf(t.temperature);//temperature at time
			tempdb[2] = String.valueOf((t.temperature - 32)/1.8f); //celsius here
			tempdb[3] = String.valueOf(t.chancePrecip);//chance of precipitation - read in as a string or int, either way doesn't matter
			tempdb[4] = t.windSpeed;//current windspeed
			tempdb[5] = "default";
			tempdb[6] = t.desc;//current condition
			tempdb[7] = String.valueOf(t.dateTime);
			
			
			
			//if you end up doing time
			//int dateTime = t.dateTime;  NOTICE: If we don't do this, reminder to fix weatherAtTime method to not rely on datetime
			datasource.addWeather(tempdb);
			
			
		}
		
		Log.v("db", "Finished inserting");
		
	}
	
	public void getSavedTemperatures(){//JOE READ!! this is where we generate 24 hour temp array if pulling weather from database. LOAD DATABASE HERE
		//Function only called if weather not downloaded today. Otherwise, function won't be called from onCreate
		
		File dbFile = new File("weather.db");
		//check to see if db exists and if so delete it

		if(dbFile.exists()) {
			Log.v("db", "database exists so continue forward");

			
			
			CommentsDataSource datasource = new CommentsDataSource(this);
			datasource.open();
			
			
			ArrayList<String[]> allWeather = (ArrayList) datasource.getAllWeather();
			ArrayList<String[]> fin = new ArrayList<String[]>();
			
			for(int i = 0; i < allWeather.size(); i++) {
				String[] temp = new String[7]; //because i have a primary key in the db, make it so we have concurrent indices
				temp[0] = allWeather.get(i)[1];
				temp[1] = allWeather.get(i)[2];
				temp[2] = allWeather.get(i)[3];
				temp[3] = allWeather.get(i)[4];
				temp[4] = allWeather.get(i)[5];
				temp[5] = allWeather.get(i)[6];
				temp[6] = allWeather.get(i)[7];
				fin.add(temp);
			}
			
			//hence fin has the arraylist you want! return that maybe? or set to the most up-to-date arraylist
			
			
//			for(int i = 0;i<24;i++){
//	//			Temperature t = new Temperature();
//	//			t.chancePrecip = getPrecipFromDatabase;
//	//			t.desc = getConditionFromDatabase;
//	//			t.temperature = getTemperatureFromDatabase;
//	//			t.windSpeed = getWindSpeedFromDatabase;
//				//if you end up doing time
//				//t.dateTime  = getDateTimegFromDatabase
//			}
		} else {
			Log.v("db", "database does not exist and there is nothing to read");
		}
	}
}
