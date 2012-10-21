package gps;

/*
 * Copyright 2012 Edmundo Carmona Antoranz <eantoranz@gmail.com>
 * All rights reserved
 */
import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class GpsAnalyzer {

	private final BufferedReader input;

	private HashMap<Integer, Satellite> satellites = new HashMap<Integer, Satellite>();
	/**
	 * Used when refreshing information about satellites to be able to tell
	 * which are new and which are out of sight
	 */
	private ArrayList<Satellite> satellitesInView = new ArrayList<Satellite>();
	
	public GpsAnalyzer(InputStream theInput) {
		this(new InputStreamReader(theInput));
	}
	
	public GpsAnalyzer(InputStreamReader theInput) {
		this(new BufferedReader(theInput));
	}

	public GpsAnalyzer(BufferedReader theInput) {
		this.input = theInput;
		new Thread(new Runnable() {

			public void run() {
				while (true) {
					try {
						processInputLine(input.readLine());
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}

	private void processInputLine(String inputLine) {
		if (inputLine.length() == 0) {
			System.err.println("Empty input line");
			return;
		}
		if (inputLine.charAt(0) != '$') {
			System.err.println("invalid input. Doesn't start with $ ("
					+ inputLine + ")");
			return;
		}
		int checksumPos = inputLine.indexOf('*');
		if (checksumPos != -1) {
			// FIXME has checksum, let's check it.... but, for now, let's just
			// strip it
			inputLine = inputLine.substring(0, checksumPos);
		}

		String[] fields = inputLine.substring(1).split(",");
		if (fields[0].equals("GPGGA")) {
			// 3d location and accuracy data
			System.out.println("GPGGA Fix information");
		} else if (fields[0].equals("GPGLL")) {
			System.out.println("GPGLL Geographic Latitude and Longitude");
		} else if (fields[0].equals("GPGSA")) {
			System.out
					.println("GPGSA (Dillution of precision / Active satellites)");
			if (fields[2].equals("1")) {
				System.err.println("\tFix is not available");
			} else if (fields[2].equals("2")) {
				System.out.println("\t2D");
			} else if (fields[2].equals("3")) {
				System.out.println("\t3D");
			}
		} else if (fields[0].equals("GPGSV")) {
			System.out.println("GPGSV (Satellites in view)");
			System.err.println(inputLine);
			System.out.println("\tLine " + fields[2] + " of " + fields[1]);
			if (fields[2].equals("1")) {
				// it's the first line, let's clear/update satellitesInView
				satellitesInView.clear();
			}
			System.out.println("\tSatellites in view: " + fields[3]);
			// how many satellites are in the line?
			int numberOfSatellitesInLine = (fields.length
					+ (inputLine.endsWith(",") ? 1 : 0) - 4) / 4;
			System.out.println("\tThis line includes "
					+ numberOfSatellitesInLine + " satellites");
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
				System.out.println("\tSatellite PRN: " + prn);
				System.out.println("\t\tElevation: " + elevation + "°");
				System.out.println("\t\tAzimuth: " + azimuth + "°");
				System.out.println("\t\tSNR: "
						+ (snr == -1 ? "-" : (snr + " db")));

				// satellite is obviously <b>in view</b>, right?
				// if it's a new satellite, let's put it in the satellite map
				// right away
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

			// if it's the very last line, let's set satellites not in sight to
			// (not in sight)
			if (fields[2].equals(fields[1])) {
				// satellites that are set as "inview" but are not in
				// satellitesInView
				// will be set as not in view
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
			System.out.println("GPVTG Vestor track and speed over ground");
		} else if (fields[0].equals("GPRMC")) {
			System.out.println("GPRMC (Recommended Minimum)");
			System.out.println("\tTime: " + fields[1] + " (UTC)");
			System.out.println("\tValid? "
					+ (fields[2].equals("A") ? "Yes" : "No"));
		} else {
			System.err.println("Unknown input: " + inputLine);
		}
	}

	public static void main(String[] args) {
		String device = "/dev/ttyUSB0";
		int speed = 4800;
		try {
			CommPortIdentifier portId = CommPortIdentifier
					.getPortIdentifier(device);

			// open and wait for max 2 seconds for port
			CommPort theCommPort = portId.open(GpsAnalyzer.class.getName(), 2000);
			// is it serial?
			if (!(theCommPort instanceof SerialPort)) {
				theCommPort.close();
				throw new IOException("Device " + device + " is not a serial port");
			}

			SerialPort port = (SerialPort) theCommPort;
			// now, let's set speed
			port.setSerialPortParams(speed, 8, 1, SerialPort.PARITY_NONE);
			new GpsAnalyzer(port.getInputStream());
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

}
