package gps;

import java.util.Date;

/*
 * Copyright 2012 Edmundo Carmona Antoranz <eantoranz@gmail.com>
 * All rights reserved
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

	/**
	 * If time is null, a new {@link java.util.Date} instance will be used
	 * instead (in other words, <b>now</b> will be assumed as the reading time)
	 * 
	 * @param latitude
	 * @param longitude
	 * @param time
	 */
	public LatLongReading(double latitude, double longitude, Date time) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.time = time == null ? new Date() : time;
	}

	public double getLatitude() {
		return this.latitude;
	}

	public double getLongitude() {
		return this.longitude;
	}

	public String toString() {
		return "Lat: " + Math.abs(latitude) + (latitude > 0 ? 'N' : 'S')
				+ " Lon: " + Math.abs(longitude) + (longitude > 0 ? 'E' : 'W')
				+ " (" + time + ")";
	}

}
