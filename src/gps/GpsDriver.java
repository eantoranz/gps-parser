package gps;

/*
 * Copyright 2012 Edmundo Carmona Antoranz <eantoranz@gmail.com>
 * All rights reserved
 */
import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.TooManyListenersException;

public class GpsDriver {

	private SerialPort port;
	private BufferedReader input;

	private HashMap<Integer, Satellite> satellites = new HashMap<Integer, Satellite>();
	/**
	 * Used when refreshing information about satellites to be able to tell
	 * which are new and which are out of sight
	 */
	private HashMap<Integer, Boolean> satellitesInView = new HashMap<Integer, Boolean>();

	public GpsDriver(String device, int speed) throws NoSuchPortException,
			PortInUseException, IOException, UnsupportedCommOperationException,
			TooManyListenersException {
		CommPortIdentifier portId = CommPortIdentifier
				.getPortIdentifier(device);

		// open and wait for max 2 seconds for port
		CommPort theCommPort = portId.open(this.getClass().getName(), 2000);
		// is it serial?
		if (!(theCommPort instanceof SerialPort)) {
			theCommPort.close();
			throw new IOException("Device " + device + " is not a serial port");
		}

		port = (SerialPort) theCommPort;
		// now, let's set speed
		port.setSerialPortParams(speed, 8, 1, SerialPort.PARITY_NONE);
		input = new BufferedReader(new InputStreamReader(port.getInputStream()));
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
		}
		if (inputLine.charAt(0) != '$') {
			System.err.println("invalid input. Doesn't start with $ ("
					+ inputLine + ")");
		}
		int checksumPos = inputLine.indexOf('*');
		if (checksumPos != -1) {
			// FIXME has checksum, let's check it.... but, for now, let's just
			// strip it
			inputLine = inputLine.substring(0, checksumPos);
		}

		String[] fields = inputLine.substring(1).split(",");
		if (fields[0].equals("GPGSA")) {
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
				// it's the first line, let's update satellitesInSight
				satellitesInView.clear();
			}
			System.out.println("\tSatellites in view: " + fields[3]);
			// how many satellites are in the line?
			int numberOfSatellites = (fields.length - 4) / 4;
			System.out.println("\tThis line includes " + numberOfSatellites
					+ " satellites");
			int prn = -1;
			int elevation = -1;
			int azimuth = -1;
			int snr = -1; // no signal
			for (int i = 0; i < numberOfSatellites; i++) {
				int fieldIndex = 3 + (i * 4) + 1;
				prn = Integer.parseInt(fields[fieldIndex++]);
				elevation = Integer.parseInt(fields[fieldIndex++]);
				azimuth = Integer.parseInt(fields[fieldIndex++]);
				snr = fields[fieldIndex].length() == 0 ? -1 : Integer
						.parseInt(fields[fieldIndex]);
				System.out.println("\tSatellite PRN: " + prn);
				System.out.println("\t\tElevation: " + elevation + "°");
				System.out.println("\t\tAzimuth: " + azimuth + "°");
				System.out.println("\t\tSNR: "
						+ (snr == -1 ? "-" : (snr + " db")));
			}
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
		try {
			new GpsDriver("/dev/ttyUSB0", 4800);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

}
