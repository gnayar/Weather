package com.example.weather;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.View;

public class CircleDraw extends View{
	float radius;
	int finishdegree=80;
	int startdegree = 0;
	int color = 0xFFFFFF;
	int paintWidth = 20;
	public CircleDraw(Context context) {

		super(context);

		// TODO Auto-generated constructor stub

	}
	public CircleDraw(Context context, float radius) {

		super(context);
		this.radius = radius;
		// TODO Auto-generated constructor stub

	}



	public CircleDraw(Context context, float radius, int finishdegree,
			int startdegree) {
		super(context);
		this.radius = radius;
		this.finishdegree = finishdegree;
		this.startdegree = startdegree;
	}
	
	public CircleDraw(Context context, float radius, int finishdegree,
			int startdegree, int color) {
		super(context);
		this.radius = radius;
		this.finishdegree = finishdegree;
		this.startdegree = startdegree;
		this.color = color;
	}
	
	@Override

	protected void onDraw(Canvas canvas) {

		// TODO Auto-generated method stub

		super.onDraw(canvas);



		

		float width = (float)getWidth();

		float height = (float)getHeight();

		

		if (width > height){

			radius = height/4;

		}else{

			radius = width/4;

		}
		

		Paint paint = new Paint();

		paint.setColor(color);

		paint.setStrokeWidth(paintWidth);	
		 paint.setAntiAlias(true); 
		 paint.setStrokeCap(Paint.Cap.ROUND);   




		float center_x, center_y;

		center_x = width/2;

		center_y = height/2;

		final RectF oval = new RectF();
		

		paint.setStyle(Paint.Style.STROKE);

		center_x = width/2;

		center_y = height * 2/4;

		oval.set(center_x - radius, 

				center_y - radius, 

				center_x + radius, 

				center_y + radius);

		canvas.drawArc(oval, startdegree, finishdegree, false, paint);

		

	}




}


