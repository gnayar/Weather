package com.example.weather;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.MotionEventCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
	private static final String DEBUG_TAG = "Motion"; 
	int sum = 0;
	int color = 0;
	
	ViewGroup stacker;
	RelativeLayout touchlayer;
	RelativeLayout weather;
	TextView statusbox;
	ListView hours;
	Context context;
	int screenHeight, screenWidth;
	
	
	
	public enum State {
		Q1, Q2, Q3, Q4, IDLE;
	}
	State state;
	State previousState;
	
	boolean listViewUp = false;
	
	// Called when the activity is first created.
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		stacker = (RelativeLayout)findViewById(R.id.main);
		touchlayer = new RelativeLayout(this);
		stacker.addView(touchlayer);
		stacker.bringChildToFront(touchlayer);
		touchlayer.setEnabled(true);
		
		context = this;
		
		constructBaseLevel();
		
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

			Log.d(DEBUG_TAG,"Action was MOVE");
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
				if (constructListView()) {
					Log.v("state", "List view returned true");
				} else
					Log.v("state", "List view returned false");

				//((TextView) findViewById(R.id.text)).setText("Left Down");
			} else if (previousState == State.Q3 && state == State.Q2) {
				//((TextView) findViewById(R.id.text)).setText("Left Up");
				Log.v("state", "Left pull up");
			} else if (previousState == State.Q1 && state == State.Q4) {
				//((TextView) findViewById(R.id.text)).setText("Right Down");
				Log.v("state", "Right pull down");
			} else if (previousState == State.Q4 && state == State.Q1) {
				//((TextView) findViewById(R.id.text)).setText("Right Up");

				Log.v("state", "Right pull up");
			}

			return true;
		case (MotionEvent.ACTION_UP):
			// Log.d(DEBUG_TAG,"Action was UP");
			if (listViewUp == true) {
				stacker.bringChildToFront(weather);
				stacker.bringChildToFront(touchlayer);
				touchlayer.setEnabled(true);
				Log.d(DEBUG_TAG, "UP with remove");
				listViewUp = false;
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
			return super.onTouchEvent(event);
		}

	}

	public boolean constructListView() {
		//main.setBackgroundColor(Color.LTGRAY);
		if(listViewUp==false){
			hours = new ListView(context);
			
			String[] stringArray = new String[] { "1", "2","3","4","5","6","7","8","9","10","11","12" };
			ArrayAdapter<String> modeAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, android.R.id.text1, stringArray);
			hours.setAdapter(modeAdapter);
			stacker.addView(hours);
			
			hours.setEnabled(false);
			hours.setBackgroundColor(0xFF0099cc);
			listViewUp = true;
			stacker.bringChildToFront(hours);
			stacker.bringChildToFront(touchlayer);
			touchlayer.setEnabled(true);
			return true;
		}
		return false;
	}
	
	public boolean constructBaseLevel(){
		
		statusbox = new TextView(context);
		statusbox.setLayoutParams(new LayoutParams(
	            LayoutParams.FILL_PARENT,
	            LayoutParams.WRAP_CONTENT));
		statusbox.setText("Swipper");
		
		weather = new RelativeLayout(context);
		weather.setLayoutParams(new LayoutParams(
	            LayoutParams.FILL_PARENT,
	            LayoutParams.FILL_PARENT));
		weather.setBackgroundColor(0xFFff8800);
		
		weather.addView(statusbox);
		weather.bringChildToFront(statusbox);
		
		stacker.addView(weather);
		stacker.bringChildToFront(weather);
		stacker.bringChildToFront(touchlayer);
		touchlayer.setEnabled(true);
		
		
		return true;
	}
	
	
	 
}

