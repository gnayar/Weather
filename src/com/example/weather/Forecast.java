package com.example.weather;

import java.util.Map;

public class Forecast{
	int tempFHigh;
	int tempFLow;
	int tempCLow;
	int tempCHigh;
	int precip;
	int windSpeed;
	String condition;
	String day;
	int imageResourceID;
	boolean celsius;

	public Forecast(){
	}

	public Forecast(int tempFHigh, int tempFLow, int tempCLow,
			int tempCHigh, int precip, int windSpeed, String condition,
			String day, int resourceID, boolean celsius) {
		super();
		this.tempFHigh = tempFHigh;
		this.tempFLow = tempFLow;
		this.tempCLow = tempCLow;
		this.tempCHigh = tempCHigh;
		this.precip = precip;
		this.windSpeed = windSpeed;
		this.condition = condition;
		this.day = day;
		this.imageResourceID = resourceID;
		this.celsius = celsius;

	}

	public int getTempFHigh() {
		return tempFHigh;
	}

	public void setTempFHigh(int tempFHigh) {
		this.tempFHigh = tempFHigh;
	}

	public int getTempFLow() {
		return tempFLow;
	}

	public void setTempFLow(int tempFLow) {
		this.tempFLow = tempFLow;
	}

	public int getTempCLow() {
		return tempCLow;
	}

	public void setTempCLow(int tempCLow) {
		this.tempCLow = tempCLow;
	}

	public int getTempCHigh() {
		return tempCHigh;
	}

	public void setTempCHigh(int tempCHigh) {
		this.tempCHigh = tempCHigh;
	}

	public int getPrecip() {
		return precip;
	}

	public void setPrecip(int precip) {
		this.precip = precip;
	}

	public int getWindSpeed() {
		return windSpeed;
	}

	public void setWindSpeed(int windSpeed) {
		this.windSpeed = windSpeed;
	}

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public String getDay() {
		return day;
	}

	public void setDay(String day) {
		this.day = day;
	}
}