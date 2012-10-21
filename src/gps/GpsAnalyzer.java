package gps;

/*
 * Copyright 2012 Edmundo Carmona Antoranz <eantoranz@gmail.com>
 * All rights reserved
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TimeZone;

import org.apache.log4j.Logger;

public class GpsAnalyzer {

	private static Logger log = Logger.getLogger(GpsAnalyzer.class);

	private static SimpleDateFormat dateFormat = new SimpleDateFormat(
			"ddMMyy HHmmss.SSS");
	static {
		// set the timezone for analysis of dates on UTC
		dateFormat
				.setCalendar(Calendar.getInstance(TimeZone.getTimeZone("UTC")));
	}

	private final BufferedReader input;

	private HashMap<Integer, Satellite> satellites = new HashMap<Integer, Satellite>();
	/**
	 * Used when refreshing information about satellites to be able to tell
	 * which are new and which are out of sight
	 */
	private ArrayList<Satellite> satellitesInView = new ArrayList<Satellite>();

	private LatLongReading lastValidReading;
	private boolean gettingValidReadings = false;
	private GpsThread gpsThread;

	public GpsAnalyzer(InputStream theInput) {
		this(new InputStreamReader(theInput));
	}

	public GpsAnalyzer(InputStreamReader theInput) {
		this(new BufferedReader(theInput));
	}

	public GpsAnalyzer(BufferedReader theInput) {
		this.input = theInput;
		this.gpsThread = new GpsThread(this, input);
		this.gpsThread.start();
	}

	private void processInputLine(String inputLine) {
		if (inputLine.length() == 0) {
			log.warn("Empty input line");
			return;
		}
		if (inputLine.charAt(0) != '$') {
			log.warn("invalid input. Doesn't start with $ (" + inputLine + ")");
			return;
		}
		int checksumPos = inputLine.indexOf('*');
		if (checksumPos != -1) {
			/*
			 * FIXME has checksum, let's check it.... but, for now, let's just
			 * strip it
			 */
			inputLine = inputLine.substring(0, checksumPos);
		}

		String[] fields = inputLine.substring(1).split(",");
		if (fields[0].equals("GPGGA")) {
			// 3d location and accuracy data
			log.info("GPGGA Fix information");
		} else if (fields[0].equals("GPGLL")) {
			log.info("GPGLL Geographic Latitude and Longitude");
		} else if (fields[0].equals("GPGSA")) {
			log.info("GPGSA (Dillution of precision / Active satellites)");
			if (fields[2].equals("1")) {
				log.debug("\tFix is not available");
			} else if (fields[2].equals("2")) {
				log.debug("\t2D");
			} else if (fields[2].equals("3")) {
				log.debug("\t3D");
			}
		} else if (fields[0].equals("GPGSV")) {
			log.info("GPGSV (Satellites in view)");
			log.debug("\tLine " + fields[2] + " of " + fields[1]);
			if (fields[2].equals("1")) {
				// it's the first line, let's clear/update satellitesInView
				satellitesInView.clear();
			}
			log.debug("\tSatellites in view: " + fields[3]);
			// how many satellites are in the line?
			int numberOfSatellitesInLine = (fields.length
					+ (inputLine.endsWith(",") ? 1 : 0) - 4) / 4;
			log.debug("\tThis line includes " + numberOfSatellitesInLine
					+ " satellites");
			Satellite satellite = null;
			for (int i = 0; i < numberOfSatellitesInLine; i++) {
				int prn = -1;
				int elevation = -1;
				int azimuth = -1;
				int snr = -1; // no signal
				satellite = null;
				int fieldIndex = 3 + (i * 4) + 1;
				prn = Integer.parseInt(fields[fieldIndex++]);
				elevation = fields[fieldIndex].length() == 0 ? -1 : Integer
						.parseInt(fields[fieldIndex]);
				fieldIndex++;
				azimuth = fields[fieldIndex].length() == 0 ? -1 : Integer
						.parseInt(fields[fieldIndex]);
				fieldIndex++;
				if (fieldIndex >= fields.length) {
					// probably the line ends up in ,
					snr = -1;
				} else {
					snr = fields[fieldIndex].length() == 0 ? -1 : Integer
							.parseInt(fields[fieldIndex]);
				}
				log.debug("\tSatellite PRN: " + prn);
				log.debug("\t\tElevation: " + elevation + "°");
				log.debug("\t\tAzimuth: " + azimuth + "°");
				log.debug("\t\tSNR: " + (snr == -1 ? "-" : (snr + " db")));

				/*
				 * satellite is obviously <b>in view</b>, right? if it's a new
				 * satellite, let's put it in the satellite map right away
				 */
				satellite = satellites.get(prn);
				if (satellite == null) {
					satellite = new Satellite(prn, elevation, azimuth, snr,
							true);
					satellites.put(prn, satellite);
				} else {
					// let's update it
					satellite.refresh(elevation, azimuth, snr, true);
				}

				satellitesInView.add(satellite);
			}

			/*
			 * if it's the very last line, let's set satellites not in sight to
			 * (not in sight)
			 */
			if (fields[2].equals(fields[1])) {
				/*
				 * satellites that are set as "inview" but are not in
				 * satellitesInView will be set as not in view
				 */
				Iterator<Satellite> sats = satellites.values().iterator();
				while (sats.hasNext()) {
					satellite = sats.next();
					if (satellite.isInView()) {
						// is it really in view?
						if (!satellitesInView.contains(satellite)) {
							// nope, it's not
							satellite.setNotInView();
						}
					}
				}
				// we are through
				satellitesInView.clear();
			}
		} else if (fields[0].equals("GPVTG")) {
			log.info("GPVTG Vector track and speed over ground");
		} else if (fields[0].equals("GPRMC")) {
			log.info("GPRMC (Recommended Minimum)");
			log.debug("\tTime: " + fields[1] + " (UTC)");
			boolean isValid = fields[2].equals("A");
			this.gettingValidReadings = isValid;
			log.debug("\tValid? " + (isValid ? "Yes" : "No"));
			if (isValid) {

				String rawLat = fields[3];
				String rawLong = fields[5];
				if (rawLat.length() > 0 && rawLong.length() > 0) {
					// we have a winner
					int degrees = Integer.parseInt(rawLat.substring(0, 2));
					double minutes = Double.parseDouble(rawLat.substring(2));
					double latitude = degrees + minutes / 60;
					if (fields[4].equals("S")) {
						latitude *= -1;
					}
					degrees = Integer.parseInt(rawLong.substring(0, 3));
					minutes = Double.parseDouble(rawLong.substring(3));
					double longitude = degrees + minutes / 60;
					if (fields[6].equals("W")) {
						longitude *= -1;
					}
					Date readingDate = null;
					try {
						readingDate = dateFormat.parse(fields[9] + " "
								+ fields[1]);
					} catch (ParseException e) {
						log.error("Couldn't parse reading date", e);
						log.debug("Will assume _now_ as reading date");
						readingDate = new Date();
					}
					lastValidReading = new LatLongReading(latitude, longitude,
							readingDate);
					log.debug("\t" + lastValidReading);
				}
			}
		} else {
			log.warn("Unknown input: " + inputLine);
		}
	}

	/**
	 * Last reading that was reported as valid from GPS device (from RMC)
	 * 
	 * @return last valid reading
	 */
	public LatLongReading getLastValidReading() {
		return this.lastValidReading;
	}

	/**
	 * Indicate if we are getting valid readings at the moment (from RMC)
	 * 
	 * @return
	 */
	public boolean gettingValidReadings() {
		return this.gettingValidReadings;
	}

	public boolean isFinished() {
		return !this.gpsThread.isReading();
	}

	private static class GpsThread extends Thread {

		private GpsAnalyzer analyzer;
		private BufferedReader reader;
		private boolean reading;

		public GpsThread(GpsAnalyzer analyzer, BufferedReader reader) {
			this.analyzer = analyzer;
			this.reader = reader;
			this.reading = true;
		}

		public void run() {
			String inputLine = null;
			while (true) {
				try {
					inputLine = reader.readLine();
					if (inputLine == null) {
						log.info("Reached EOS. Quitting analysis");
						break;
					}
					analyzer.processInputLine(inputLine);
				} catch (IOException e) {
					log.error("Error reading from input stream", e);
				}
			}
			reading = false;
		}

		public boolean isReading() {
			return reading;
		}
	}

}
