package com.example.weather;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.TextView;

public class MainActivity extends Activity {
	private static final String DEBUG_TAG = "Motion"; 
	
	private GestureDetectorCompat mDetector;
	Context context;
	int screenHeight, screenWidth;
	
	//Variables to set time
	boolean inHours = false;
	int timeChosen;
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
		context = this;
		mDetector = new GestureDetectorCompat(this, new MyGestureListener());
		
		//Finding current time
		Time now = new Time();
		now.setToNow();
		TextView mainText = (TextView) findViewById(R.id.mainText);
		if(now.hour>12){
			now.hour-=12;
			amPm = "pm";
		}
		mainText.setText("Long press to choose a time\n\nCurrent time is: "+now.hour+":"+now.minute+amPm);
		timeChosen = now.hour;
		
		
		state = State.IDLE;
		previousState = State.IDLE;

		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		screenWidth = metrics.widthPixels;
		screenHeight = metrics.heightPixels;
		
		Log.v("state", "Screen width: " + screenWidth + " Screen height: " + screenHeight);

		//JSON Testing
		//currently using wunderground's api!!!!

		
		JSONParser parser = new JSONParser();
		Log.v("http", "parser attempting");
		ArrayList<String> testArray;
		
		JSONObject obj;
		try {
			obj = parser.execute("safsfs").get();
			testArray = parser.parse(obj);

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			testArray = new ArrayList<String>();
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			testArray = new ArrayList<String>();
			e.printStackTrace();
		}

		for(int i = 0; i< testArray.size(); i++) {
			Log.v("http", testArray.get(i));
		}

		/*OKAY GAUTAM READ

		Currently, the data is being held in an arraylist and I am only parsing 		some data.
		Just let me know if you need more. Its currently stored based on indices
			-> I know this is retarded
		I will use a hash or key value pair soon
		
		index 0 = observation_time
		index 1 = cloud cover
		index 2 = current pressure
		index 3 = temperature in string format
		
		*/

		
		
		//TESTING DATABASE
		CommentsDataSource datasource = new CommentsDataSource(this);
		datasource.open();
		
		Log.v("db", "Inserting...");
		
		datasource.createComment("TEST");
		datasource.createComment("TEST1");
		datasource.createComment("TEST2");


		
		
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
				if((!inHours)&&(amPmCount == 0)){
					if(amPm.equals("am")){
						amPm = "pm";
					}
					else if(amPm.equals("pm")){
						amPm = "am";
					}
					
					TextView mainText = (TextView) findViewById(R.id.mainText);
					mainText.setText("You chose "+Integer.toString(timeChosen)+":00"+amPm);
					amPmCount = 1;
				}
			
						
			} else if (previousState == State.Q4 && state == State.Q1) {
				//((TextView) findViewById(R.id.text)).setText("Right Up");

				Log.v("state", "Right pull up");
			}
			
			if(inHours==true){
				int angle = (int) Math.toDegrees(Math.atan2(x1 - screenWidth / 2, y1 - screenHeight/2));
			    if(angle < 0){
			        angle += 360;
			    }
			    
			    TextView t1 = (TextView) findViewById(R.id.chosendate);
			    timeChosen = calculateClockAngle(angle);
			    t1.setText(Integer.toString(timeChosen)+":00"); 		
				   
			}

			return true;
		case (MotionEvent.ACTION_UP):
			// Log.d(DEBUG_TAG,"Action was UP");
			if(inHours){
				inHours = false;
				setContentView(R.layout.activity_main);
				
				TextView mainText = (TextView) findViewById(R.id.mainText);
				mainText.setText("You chose "+Integer.toString(timeChosen)+":00"+amPm);
				
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
		timeChosen+=change;
		if(timeChosen>12){
			timeChosen-=12;
			if(amPm.equals("am")){
				amPm = "pm";
			}
			else if(amPm.equals("pm")){
				amPm = "am";
			}
		}
		else if(timeChosen<1){
			timeChosen+=12;
			if(amPm.equals("am")){
				amPm = "pm";
			}
			else if(amPm.equals("pm")){
				amPm = "am";
			}
		}
		
		timeChangeCount = 1;
		
		TextView mainText = (TextView) findViewById(R.id.mainText);
		mainText.setText("You chose "+Integer.toString(timeChosen)+":00"+amPm);
	}
	private int calculateClockAngle(int angle) {
		int upperLimit = 15;
		int hour = 6;
		while(upperLimit<365){
			if(angle<upperLimit){
				Log.d(DEBUG_TAG, "Hour: "+ hour +" Angle: " + angle); 
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
            inHours = true;
        }
        
    }
	 
}

