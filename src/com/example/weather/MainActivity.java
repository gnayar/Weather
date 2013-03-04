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
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ListView;
import android.widget.RelativeLayout;

public class MainActivity extends Activity {
	private static final String DEBUG_TAG = "Motion"; 
	private GestureDetectorCompat mDetector;
	private ArrayList<Integer> buffer = new ArrayList<Integer>();
	int sum = 0;
	int color = 0;
	
	RelativeLayout stacker;
	RelativeLayout surface;
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
		context = this;
		
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
		
		Comment comment = null;
		comment = datasource.createComment("TEST");
		comment = datasource.createComment("TEST1");
		comment = datasource.createComment("TEST2");


		
		
		Log.v("db", "Reading...");
		List<Comment> comments = datasource.getAllComments();
		
		Log.v("db", "Printing...");
		Log.v("db", comments.size() + "");
		for(int i = 0; i < comments.size(); i++) {
			String log = "Name: " + comments.get(i).toString();
			Log.v("db", log);
		}




		mDetector = new GestureDetectorCompat(this, new MyGestureListener());
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		this.mDetector.onTouchEvent(event);

		int action = MotionEventCompat.getActionMasked(event);
        
	    switch(action) {
	        case (MotionEvent.ACTION_DOWN) :
	        	int x = (int) event.getX();
	        	int y = (int) event.getY();
	            //Log.v("state","Action was DOWN: x -> " + x + " y -> " + y);

		        if(x < screenWidth/2 && y < screenHeight/2) {     
		        	previousState = State.Q2;
	            
	            } else if (x > screenWidth/2 && y < screenHeight/2) {
	            	previousState = State.Q1;
	            } else if (x < screenWidth/2 && y > screenHeight/2) {
	            	previousState = State.Q3;
	            } else if (x > screenWidth/2 && y > screenHeight/2) {
	            	previousState = State.Q4;
	            }
		       // Log.v("state", "previous state: " + previousState.toString());
		        return true;
	        
	        case (MotionEvent.ACTION_MOVE) :

	            //Log.d(DEBUG_TAG,"Action was MOVE");
	                        
	            
	            return true;
	        case (MotionEvent.ACTION_UP) :
	            //Log.d(DEBUG_TAG,"Action was UP");
	        
        		int x1 = (int) event.getX();
        		int y1 = (int) event.getY();
		        if(x1 < screenWidth/2 && y1 < screenHeight/2) {
	            	state = State.Q2;            
	            } else if (x1 > screenWidth/2 && y1 < screenHeight/2) {
	            	state = State.Q1;
	            } else if (x1 < screenWidth/2 && y1 > screenHeight/2) {
	            	state = State.Q3;
	            } else if (x1 > screenWidth/2 && y1 > screenHeight/2) {
	            	state = State.Q4;
	            }

		        
	            //now we have two states to handle...one is the state we are coming from and the other is the state we currently
	            //moved to...
	            //now do the logic 
	            
	        
		        
	            Log.v("state", "Previous state: " + previousState.toString() + " Current state: " + state.toString());
	            if(previousState == State.Q2 && state == State.Q3) {
	            	Log.v("state", "Left pull down");
	        		//((TextView)findViewById(R.id.text)).setText("Left Down");
	            } else if (previousState == State.Q3 && state == State.Q3) {
	            	Log.v("state", "Left pull up");
	            } else if (previousState == State.Q1 && state == State.Q4) {
	            	Log.v("state", "Right pull down");
	            } else if (previousState == State.Q4 && state == State.Q1) {
	            	Log.v("state", "Right pull up");
	            }
	        
		        if(listViewUp == true){
		        	surface = (RelativeLayout)findViewById(R.id.surface);
	        		stacker.bringChildToFront(surface);
	        		Log.d(DEBUG_TAG,"UP with remove"); 
	        		//listViewUp = false;
	        	} 
            	return true;
	        case (MotionEvent.ACTION_CANCEL) :
	            Log.d(DEBUG_TAG,"Action was CANCEL");
	            return true;
	        case (MotionEvent.ACTION_OUTSIDE) :
	            Log.d(DEBUG_TAG,"Movement occurred outside bounds " +
	                    "of current screen element");
	            return true;      
	        default : 
	            return super.onTouchEvent(event);
	    }      
		
	}

	
	
	
	 class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
	        
	        
//	        @Override
//	        public boolean onDown(MotionEvent event) { 
//	            Log.d(DEBUG_TAG,"onDown: "); 
//	        	buffer.clear();
//	            return true;
//	        }
//	        
//
//	        @Override
//	        public boolean onScroll(MotionEvent event1, MotionEvent event2, 
//	                float distanceX, float distanceY) {
//	        	
//	        	int middle = (((RelativeLayout)findViewById(R.id.main)).getRight())/2;
//	        	
//	        	buffer.add((int)distanceY);
//	        	if(buffer.size()>20){
//	        		buffer.remove(0);
//	        	}
//	        	sum = 0;
//	        	for(int i = 0;i<buffer.size();i++){
//	        		sum+= buffer.get(i);
//	        	}
//	        	boolean swiped = false;
//	        	 
//        		RelativeLayout main = (RelativeLayout)findViewById(R.id.surface);
//	        	if((event1.getRawX()<middle)&&(sum>400)){
//	        		swiped = true;
//	        		main.setBackgroundColor(Color.GRAY);
//	        		((TextView)findViewById(R.id.text)).setText("Left Up");
//	        	}
//	        	else if((event1.getRawX()>middle)&&(sum>400)){
//	        		swiped = true;
//	        		main.setBackgroundColor(Color.GRAY);
//	        		((TextView)findViewById(R.id.text)).setText("Right Up");
//	        	}
//	        	else if((event1.getRawX()<middle)&&(sum<-400)){
//	        		swiped = true;
//	        		main.setBackgroundColor(Color.LTGRAY);
//	        		
//	        		hours = new ListView(context);
//	        		
//	        		String[] stringArray = new String[] { "1", "2","3","4","5","6","7","8","9","10","11","12" };
//	        		ArrayAdapter<String> modeAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, android.R.id.text1, stringArray);
//	        		hours.setAdapter(modeAdapter);
//	        		stacker.addView(hours);
//	        		
//	        		listViewUp = true;
//	        		stacker.bringChildToFront(hours);
//	        	}
//	        	else if((event1.getRawX()>middle)&&(sum<-400)){
//	        		swiped = true;
//	        		main.setBackgroundColor(Color.LTGRAY);
//	        		((TextView)findViewById(R.id.text)).setText("Right Down");
//	        	}
//	        	
//	           // Log.d(DEBUG_TAG, "onScroll: "+sum);
//	            return true;
//	        }
	    }
}

