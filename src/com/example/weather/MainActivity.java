package com.example.weather;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.slidingmenu.lib.SlidingMenu;
import com.slidingmenu.lib.SlidingMenu.CanvasTransformer;
import com.slidingmenu.lib.app.SlidingActivity;


public class MainActivity extends SlidingActivity implements LocationListener{
	private static final String DEBUG_TAG = "Motion"; 
	
	private GestureDetectorCompat mDetector;
	Context context;
	int screenHeight, screenWidth;
	private CanvasTransformer mTransformer;
	
	//for gps
	int lng, lat;
	
	//Variables to set time
	boolean inHours = false;
	Time timeChosen;
	String amPm = "am";
	int amPmCount = 0;
	int timeChangeCount = 0;
	
	
	public enum State {
		Q1, Q2, Q3, Q4, IDLE;
	}
	State state;
	State previousState;
	
	// Called when the activity is first created.
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setBehindContentView(R.layout.menu);
		context = this;
		mDetector = new GestureDetectorCompat(this, new MyGestureListener());
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
		

		
		
		ListView days = (ListView)findViewById(R.id.days);
		String[] stringArray = new String[] {"Tomorrow", "Day After" };
		ArrayAdapter<String> modeAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, android.R.id.text1, stringArray);
		days.setAdapter(modeAdapter);
		
		
		//Finding current time
		Time now = new Time();
		now.setToNow();
		TextView mainText = (TextView) findViewById(R.id.mainText);
		if(now.hour>12){
			now.hour-=12;
			amPm = "pm";
		}

		timeChosen = now;
		if(timeChosen.minute<10){
    		mainText.setText("Current time is: "+timeChosen.hour+":0"+timeChosen.minute+amPm); 
    	}
    	else{
    		mainText.setText("Current time is: "+timeChosen.hour+":"+timeChosen.minute+amPm); 
    	}
		
		
		state = State.IDLE;
		previousState = State.IDLE;

		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		screenWidth = metrics.widthPixels;
		screenHeight = metrics.heightPixels;
		
		Log.v("state", "Screen width: " + screenWidth + " Screen height: " + screenHeight);

		//JSON Testing
		//currently using wunderground's api!!!!
		String[] testArray;
		
		JSONParser parser = new JSONParser();
		Log.v("http", "parser attempting");
		//ArrayList<String> testArray;
		
		JSONObject obj;
		try {
			obj = parser.execute("safsfs").get();
			ArrayList<String[]> current = parser.parse(obj);
			
			for(int i = 0; i< 7; i++) {
				Log.v("http", current.get(0)[i]);
			}

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			testArray = new String[6];
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			testArray = new String[6];
			e.printStackTrace();
		}



		/*OKAY GAUTAM READ

		Currently, the data is being held in an arraylist and I am only parsing 		some data.
		Just let me know if you need more. Its currently stored based on indices
			-> I know this is retarded
		I will use a hash or key value pair soon
		

		
		*/

		
		
		//TESTING DATABASE
		CommentsDataSource datasource = new CommentsDataSource(this);
		datasource.open();
		
		Log.v("db", "Inserting...");
		
		datasource.createComment("TEST");
		datasource.createComment("TEST1");
		datasource.createComment("TEST2");


		
		
		//GPS TESTING
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
		
		if(enabled) {
			  Log.v("gps", "GPS is enabled");

		    // Get the location manager
		    LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		    // Define the criteria how to select the location provider -> use
		    // default
		    Criteria criteria = new Criteria();
		    String provider = locationManager.getBestProvider(criteria, false);
		    Log.v("gps", "provider: " + provider);
		    
		    if(locationManager.isProviderEnabled(provider)) {
		    	Log.v("gps", provider + " is enabled");
		    } else {
		    	Log.v("gps", provider + " is NOT enabled");
		    }
		    
		    
		    Location location = locationManager.getLastKnownLocation(provider);
		    //Log.v("gps", "location: " + location.toString());
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
	
		
		Log.v("db", "Reading...");
		List<Comment> comments = datasource.getAllComments();
		
		Log.v("db", "Printing...");
		Log.v("db", comments.size() + "");
		for(int i = 0; i < comments.size(); i++) {
			String log = "Name: " + comments.get(i).toString();
			Log.v("db", log);
		}
		
	}
	
	

	
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		this.mDetector.onTouchEvent(event);
		int action = MotionEventCompat.getActionMasked(event);

		switch (action) {
		case (MotionEvent.ACTION_DOWN):
			
			int x = (int) event.getX();
			int y = (int) event.getY();
			Log.d(DEBUG_TAG,"Action was DOWN: x -> " + x + " y -> " + y);

			if (x < screenWidth / 2 && y < screenHeight / 2) {
				previousState = State.Q2;

			} else if (x > screenWidth / 2 && y < screenHeight / 2) {
				previousState = State.Q1;
			} else if (x < screenWidth / 2 && y > screenHeight / 2) {
				previousState = State.Q3;
			} else if (x > screenWidth / 2 && y > screenHeight / 2) {
				previousState = State.Q4;
			}
			// Log.v("state", "previous state: " + previousState.toString());
			return true;

		case (MotionEvent.ACTION_MOVE):
			
			//Log.d(DEBUG_TAG,"Action was MOVE");
			int x1 = (int) event.getX();
			int y1 = (int) event.getY();
			if (x1 < screenWidth / 2 && y1 < screenHeight / 2) {
				state = State.Q2;
			} else if (x1 > screenWidth / 2 && y1 < screenHeight / 2) {
				state = State.Q1;
			} else if (x1 < screenWidth / 2 && y1 > screenHeight / 2) {
				state = State.Q3;
			} else if (x1 > screenWidth / 2 && y1 > screenHeight / 2) {
				state = State.Q4;
			}

			// now we have two states to handle...one is the state we are coming
			// from and the other is the state we currently
			// moved to...
			// now do the logic

			Log.v("state", "Previous state: " + previousState.toString()
					+ " Current state: " + state.toString());
			if (previousState == State.Q2 && state == State.Q3) {
				Log.v("state", "Left pull down");
				
				if((timeChangeCount == 0)&&(!inHours)){
					changeHour(-1);
				}
				
				
				//((TextView) findViewById(R.id.text)).setText("Left Down");
			} else if (previousState == State.Q3 && state == State.Q2) {
				//((TextView) findViewById(R.id.text)).setText("Left Up");
				Log.v("state", "Left pull up");
				
				if((timeChangeCount == 0)&&(!inHours)){
					changeHour(1);
				}
			} else if (previousState == State.Q1 && state == State.Q4) {
				//((TextView) findViewById(R.id.text)).setText("Right Down");
				Log.v("state", "Right pull down");
				if((timeChangeCount == 0)&&(!inHours)){
					changeHour(-6);
				}
			
						
			} else if (previousState == State.Q4 && state == State.Q1) {
				//((TextView) findViewById(R.id.text)).setText("Right Up");

				Log.v("state", "Right pull up");
				
				if((timeChangeCount == 0)&&(!inHours)){
					changeHour(6);
				}
			}
			
			if(inHours==true){
				TextView t1 = (TextView) findViewById(R.id.chosendate);
				
				int angle = (int) Math.toDegrees(Math.atan2(x1 - screenWidth / 2, y1 - screenHeight/2));
			    if(angle < 0){
			        angle += 360;
			    }
			    double dX= Math.pow((x1 - screenWidth / 2),2);
			    double dY= Math.pow((y1 - screenHeight/2),2);

			    double d = Math.sqrt(dX + dY);
			    Log.d(DEBUG_TAG, Double.toString(d));
			    
			    if(d>90){
			    	timeChosen.hour = calculateClockAngle(angle);
			    	timeChosen.minute = 0;
			    	t1.setText(timeChosen.hour+":00"+amPm);
			    }
			    else{
			    	Time now = new Time();
					now.setToNow();
			    	timeChosen = now;
			    	if(timeChosen.hour>12){
			    		timeChosen.hour-=12;
			    	}
			    	if(timeChosen.minute<10){
			    		t1.setText(timeChosen.hour+":0"+timeChosen.minute+amPm); 
			    	}
			    	else{
			    		t1.setText(timeChosen.hour+":"+timeChosen.minute+amPm); 
			    	}
			    }
			    		
				   
			}

			return true;
		case (MotionEvent.ACTION_UP):
			// Log.d(DEBUG_TAG,"Action was UP");
			if(inHours){
				inHours = false;
				setContentView(R.layout.activity_main);
				
				TextView mainText = (TextView) findViewById(R.id.mainText);
				if(timeChosen.minute<10){
		    		mainText.setText("You chose "+timeChosen.hour+":0"+timeChosen.minute+amPm); 
		    	}
		    	else{
		    		mainText.setText("You chose "+timeChosen.hour+":"+timeChosen.minute+amPm); 
		    	}
			}
			else if(!inHours){
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
	private void changeHour(int change) {
		timeChosen.hour+=change;
		if(timeChosen.hour>12){
			timeChosen.hour-=12;
			if(amPm.equals("am")){
				amPm = "pm";
			}
			else if(amPm.equals("pm")){
				amPm = "am";
			}
		}
		else if(timeChosen.hour<1){
			timeChosen.hour+=12;
			if(amPm.equals("am")){
				amPm = "pm";
			}
			else if(amPm.equals("pm")){
				amPm = "am";
			}
		}
		
		timeChangeCount = 1;
		
		TextView mainText = (TextView) findViewById(R.id.mainText);
		if(timeChosen.minute<10){
    		mainText.setText("You chose "+timeChosen.hour+":0"+timeChosen.minute+amPm); 
    	}
    	else{
    		mainText.setText("You chose "+timeChosen.hour+":"+timeChosen.minute+amPm); 
    	}
	}
	private int calculateClockAngle(int angle) {
		int upperLimit = 15;
		int hour = 6;
		while(upperLimit<365){
			if(angle<upperLimit){
				
				return hour;
				
			}
			else if(angle>=upperLimit){
				
				upperLimit+=30;
				hour--;
				if(hour == 0){
					hour = 12;
				}
			}
		}
		return 6;
	}
		
	class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final String DEBUG_TAG = "Motion"; 
        @Override
        public void onLongPress(MotionEvent event) {
            Log.d(DEBUG_TAG, "onLongPress: " + event.toString()); 
            setContentView(R.layout.hours);
            int x1 = (int) event.getX();
            if(x1<screenWidth/2){
            	amPm = "am";
            }
            else if(x1>screenWidth/2){
            	amPm = "pm";
            }
            inHours = true;
        }
        @Override
        public boolean onDoubleTap(MotionEvent event){
        	//Toast.makeText(context, "double tap",Toast.LENGTH_SHORT).show();
        	if((!inHours)&&(amPmCount == 0)){
        		//Change imageview on doubleTap
        		ImageView main = (ImageView)findViewById(R.id.weatherIcon);
        		TextView conditions = (TextView)findViewById(R.id.conditions);
        		
				if(amPm.equals("am")){
					amPm = "pm";
					main.setImageResource(R.drawable.clim_cloudmoon);
					conditions.setText("Currently 15 Degrees");
				}
				else if(amPm.equals("pm")){
					amPm = "am";
					main.setImageResource(R.drawable.clim_cloud);
					conditions.setText("Currently 75 Degrees");
				}
				
				TextView mainText = (TextView) findViewById(R.id.mainText);
				if(timeChosen.minute<10){
		    		mainText.setText("You chose "+timeChosen.hour+":0"+timeChosen.minute+amPm); 
		    	}
		    	else{
		    		mainText.setText("You chose "+timeChosen.hour+":"+timeChosen.minute+amPm); 
		    	}
				amPmCount = 1;
				
			}
        	return true;
        }
        
    }

	//GPS
	@Override
	  public void onLocationChanged(Location location) {
	    lat = (int) (location.getLatitude());
	    lng = (int) (location.getLongitude());
	    Log.v("gps", "Latitude: " + lat + " Longitude: " + lng);


	  }
	
	
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
	
	
}



