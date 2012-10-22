package gps.event;

import gps.Satellite;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.log4j.Logger;

/*
 * Copyright 2012 Edmundo Carmona Antoranz
 * All rights reserved
 * Released under the terms of Mozilla Public License 2.0
 */
public class GPGSV extends GpsEvent {

	private static Logger log = Logger.getLogger(GPGSV.class);

	private int thisLine;
	private int totalLines;
	private int satellitesInView;
	private ArrayList<Satellite> satellites = new ArrayList<Satellite>();

	protected GPGSV(String[] fields, boolean endsWithComa) {
		super(fields);
		log.info("GPGSV (Satellites in view)");

		this.thisLine = Integer.parseInt(fields[2]);
		this.totalLines = Integer.parseInt(fields[1]);

		log.debug("\tLine " + this.thisLine + " of " + this.totalLines);

		this.satellitesInView = Integer.parseInt(fields[3]);
		log.debug("\tSatellites in view: " + this.satellitesInView);
		// how many satellites are in the line?
		int numberOfSatellitesInLine = (fields.length + (endsWithComa ? 1 : 0) - 4) / 4;
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
			satellite = new Satellite(prn, elevation, azimuth, snr, true);

			satellites.add(satellite);
		}

	}

	public int getTotalLines() {
		return this.totalLines;
	}

	public int getThisLine() {
		return this.thisLine;
	}

	/**
	 * Number of satellites that are supposed to be included in this record
	 * according to GPGSV record (GPGSV's 4th field)
	 * 
	 * @return
	 */
	public int getSatellitesinViewCount() {
		return this.satellitesInView;
	}

	public Iterator<Satellite> getSatellites() {
		return this.satellites.iterator();
	}

}
