package com.example.weather;

import java.util.Date;

public class Temperature {
	int temperature;
	String desc;
	long dateTime;
	String chancePrecip;
	String icon;
	String windSpeed;
	String humidity;
	String skyCover;
	String feelsLike;
	String feelsLikeLabel;
	String windDir;
	String dewPoint;
	
	public Temperature(){
	}

	public int getTemperature() {
		return temperature;
	}

	public void setTemperature(int temperature) {
		this.temperature = temperature;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public long getDateTime() {
		return dateTime;
	}

	public void setDateTime(long dateTime) {
		this.dateTime = dateTime;
	}

	public String getChancePrecip() {
		return chancePrecip;
	}

	public void setChancePrecip(String chancePrecip) {
		this.chancePrecip = chancePrecip;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getWindSpeed() {
		return windSpeed;
	}

	public void setWindSpeed(String windSpeed) {
		this.windSpeed = windSpeed;
	}

	public String getHumidity() {
		return humidity;
	}

	public void setHumidity(String humidity) {
		this.humidity = humidity;
	}

	public String getSkyCover() {
		return skyCover;
	}

	public void setSkyCover(String skyCover) {
		this.skyCover = skyCover;
	}

	public String getFeelsLike() {
		return feelsLike;
	}

	public void setFeelsLike(String feelsLike) {
		this.feelsLike = feelsLike;
	}

	public String getFeelsLikeLabel() {
		return feelsLikeLabel;
	}

	public void setFeelsLikeLabel(String feelsLikeLabel) {
		this.feelsLikeLabel = feelsLikeLabel;
	}

	public String getWindDir() {
		return windDir;
	}

	public void setWindDir(String windDir) {
		this.windDir = windDir;
	}

	public String getDewPoint() {
		return dewPoint;
	}

	public void setDewPoint(String dewPoint) {
		this.dewPoint = dewPoint;
	}
	
	public Date toNormalTime(long unixTime){
		return new Date(unixTime);
		
	}
	
}