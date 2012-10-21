package gps;

import java.util.Date;

/*
 * Copyright 2012 Edmundo Carmona Antoranz <eantoranz@gmail.com>
 */

/**
 * A GPS Location reading
 * 
 * @author antoranz
 *
 */
public class LatLongReading {
	
	private double latitude;
	private double longitude;
	private Date time;
	
	public LatLongReading(double latitude, double longitude) {
		this(latitude, longitude, new Date());
	}
	
	public LatLongReading(double latitude, double longitude, Date time) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.time = time;
	}
	
	public String toString() {
		return "Lat: " + Math.abs(latitude) + (latitude > 0 ? 'N' : 'S') + " Lon: " + Math.abs(longitude) + (longitude > 0 ? 'E' : 'W') + " (" + time + ")";
	}

}
