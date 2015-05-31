package eu.hellek.gba.shared;

import java.io.Serializable;

public class UserFavouritePositionProxy implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private double lat;
	
	private double lon;
	
	private String name;
	
	private String key;
	
	public UserFavouritePositionProxy() { }
	
	public UserFavouritePositionProxy(String name, double lat, double lon, String key) {
		this.name = name;
		this.lat = lat;
		this.lon = lon;
		this.key = key;
	}
	
	public UserFavouritePositionProxy(String name, double lat, double lon) {
		this.name = name;
		this.lat = lat;
		this.lon = lon;
		this.key = null;
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getKey() {
		return key;
	}

}
