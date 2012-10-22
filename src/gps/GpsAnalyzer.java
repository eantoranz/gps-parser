package gps;

/*
 * Copyright 2012 Edmundo Carmona Antoranz <eantoranz@gmail.com>
 * All rights reserved
 */
import gps.event.AbstractEvent;
import gps.event.GPGSV;
import gps.event.GPRMC;
import gps.event.InvalidInputException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;

public class GpsAnalyzer {

	private static Logger log = Logger.getLogger(GpsAnalyzer.class);

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

	private void processInputLine(String inputLine) throws InvalidInputException {
		AbstractEvent event = AbstractEvent.createEvent(inputLine);
		
		if (event instanceof GPGSV) {
			GPGSV gpgsv = (GPGSV) event;
			if (gpgsv.getThisLine() == 1) {
				// it's the first line, let's clear/update satellitesInView
				satellitesInView.clear();
			}
			
			Satellite satellite = null;
			Iterator<Satellite> sats = gpgsv.getSatellites();
			while (sats.hasNext()) {
				satellite = sats.next();
				satellites.put(satellite.getPRN(), satellite);
				satellitesInView.add(satellite);
			}

			/*
			 * if it's the very last line, let's set satellites not in sight to
			 * (not in sight)
			 */
			if (gpgsv.getThisLine() == gpgsv.getTotalLines()) {
				/*
				 * satellites that are set as "inview" but are not in
				 * satellitesInView will be set as not in view
				 */
				sats = satellites.values().iterator();
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
		} else if (event instanceof GPRMC) {
			GPRMC gprmc = (GPRMC) event;
			if (gprmc.isValid()) {
					lastValidReading = new LatLongReading(gprmc.getLatitude(), gprmc.getLongitude(),
							gprmc.getReadingDate());
					log.debug("\t" + lastValidReading);
			}
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
