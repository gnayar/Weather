package com.example.weather;

import java.util.ArrayList;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
	private GestureDetectorCompat mDetector;
	private ArrayList<Integer> buffer = new ArrayList<Integer>();
	int sum = 0;
	int color = 0;
	// Called when the activity is first created.
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
				
		/*OKAY GAUTAM READ

		Currently, the data is being held in an arraylist and I am only parsing 		some data.
		Just let me know if you need more. Its currently stored based on indices
			-> I know this is retarded
		I will use a hash or key value pair soon
		
		index 0 = observation_time <- not sure what this means
		index 1 = cloud cover
		index 2 = current pressure
		index 3 = temperature in fahrenheit
		index 4 = temperature in Celsius
		
		*/






		mDetector = new GestureDetectorCompat(this, new MyGestureListener());
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		this.mDetector.onTouchEvent(event);
		return super.onTouchEvent(event);
	}
	
	 class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
	        private static final String DEBUG_TAG = "Motion"; 
	        
	        @Override
	        public boolean onDown(MotionEvent event) { 
	           // Log.d(DEBUG_TAG,"onDown: "); 
	        	buffer.clear();
	            return true;
	        }

	        @Override
	        public boolean onScroll(MotionEvent event1, MotionEvent event2, 
	                float distanceX, float distanceY) {
	        	
	        	int middle = (((RelativeLayout)findViewById(R.id.main)).getRight())/2;
	        	
	        	buffer.add((int)distanceY);
	        	if(buffer.size()>20){
	        		buffer.remove(0);
	        	}
	        	sum = 0;
	        	for(int i = 0;i<buffer.size();i++){
	        		sum+= buffer.get(i);
	        	}
	        	boolean swiped = false;

        		RelativeLayout main = (RelativeLayout)findViewById(R.id.main);
	        	if((event1.getRawX()<middle)&&(sum>400)){
	        		swiped = true;
	        		main.setBackgroundColor(Color.GRAY);
	        		((TextView)findViewById(R.id.text)).setText("Left Up");
	        	}
	        	else if((event1.getRawX()>middle)&&(sum>400)){
	        		swiped = true;
	        		main.setBackgroundColor(Color.GRAY);
	        		((TextView)findViewById(R.id.text)).setText("Right Up");
	        	}
	        	else if((event1.getRawX()<middle)&&(sum<-400)){
	        		swiped = true;
	        		main.setBackgroundColor(Color.LTGRAY);
	        		((TextView)findViewById(R.id.text)).setText("Left Down");
	        	}
	        	else if((event1.getRawX()>middle)&&(sum<-400)){
	        		swiped = true;
	        		main.setBackgroundColor(Color.LTGRAY);
	        		((TextView)findViewById(R.id.text)).setText("Right Down");
	        	}
	        	
	            Log.d(DEBUG_TAG, "onScroll: "+sum);
	            return true;
	        }
	    }
}

