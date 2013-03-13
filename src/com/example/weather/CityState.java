package com.example.weather;

public class CityState {
	String city;
	String state;
	double lat;
	double lon;
	public CityState(String city, String state, int lat, int lon) {
		super();
		this.city = city;
		this.state = state;
		this.lat = lat;
		this.lon = lon;
	}
	public CityState(String city, String state) {
		super();
		this.city = city;
		this.state = state;
	}
	public double getLat() {
		return lat;
	}
	public void setLat(double lat) {
		this.lat = lat;
	}
	public double getLon() {
		return lon;
	}
	public void setLon(double lon) {
		this.lon = lon;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
}
