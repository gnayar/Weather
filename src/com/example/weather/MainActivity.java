package com.example.weather;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
	
	boolean listViewUp = false;
	
	// Called when the activity is first created.
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		stacker = (RelativeLayout)findViewById(R.id.main);
		context = this;
		

		

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






		mDetector = new GestureDetectorCompat(this, new MyGestureListener());
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		this.mDetector.onTouchEvent(event);
		
		int action = MotionEventCompat.getActionMasked(event);
        
	    switch(action) {
	        case (MotionEvent.ACTION_DOWN) :
	            Log.d(DEBUG_TAG,"Action was DOWN");
	            return true;
	        case (MotionEvent.ACTION_MOVE) :
	        	int x = (int) event.getX();
	        	int y = (int) event.getY();
	            Log.d(DEBUG_TAG,"Action was MOVE: " + x +", "+y);
	            return true;
	        case (MotionEvent.ACTION_UP) :
	            Log.d(DEBUG_TAG,"Action was UP");
		        if(listViewUp == true){
		        	surface = (RelativeLayout)findViewById(R.id.surface);
	        		stacker.bringChildToFront(surface);
	        		Log.d(DEBUG_TAG,"UP with remove"); 
	        		listViewUp = false;
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
//	        		((TextView)findViewById(R.id.text)).setText("Left Down");
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

