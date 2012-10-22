package gps.event;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.log4j.Logger;

/*
 * Copyright 2012 Edmundo Carmona Antoranz
 * All rights reserved
 */
public class GPRMC extends GpsEvent {

	private static Logger log = Logger.getLogger(GPRMC.class);

	private static SimpleDateFormat dateFormat = new SimpleDateFormat(
			"ddMMyy HHmmss.SSS");
	static {
		// set the timezone for analysis of dates on UTC
		dateFormat
				.setCalendar(Calendar.getInstance(TimeZone.getTimeZone("UTC")));
	}

	private boolean isValid;
	private double latitude;
	private double longitude;
	private Date readingDate;

	protected GPRMC(String[] fields) {
		super(fields);

		log.info("GPRMC (Recommended Minimum)");
		log.debug("\tTime: " + fields[1] + " (UTC)");
		this.isValid = fields[2].equals("A");
		log.debug("\tValid? " + (isValid ? "Yes" : "No"));
		if (this.isValid) {

			String rawLat = fields[3];
			String rawLong = fields[5];
			if (rawLat.length() > 0 && rawLong.length() > 0) {
				// we have a winner
				int degrees = Integer.parseInt(rawLat.substring(0, 2));
				double minutes = Double.parseDouble(rawLat.substring(2));
				this.latitude = degrees + minutes / 60;
				if (fields[4].equals("S")) {
					this.latitude *= -1;
				}
				degrees = Integer.parseInt(rawLong.substring(0, 3));
				minutes = Double.parseDouble(rawLong.substring(3));
				this.longitude = degrees + minutes / 60;
				if (fields[6].equals("W")) {
					this.longitude *= -1;
				}
				this.readingDate = null;
				try {
					this.readingDate = dateFormat.parse(fields[9] + " "
							+ fields[1]);
				} catch (ParseException e) {
					log.error("Couldn't parse reading date", e);
					log.debug("Will assume _now_ as reading date");
					this.readingDate = new Date();
				}
			}
		}

	}

	public boolean isValid() {
		return isValid;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public Date getReadingDate() {
		return readingDate;
	}

}
