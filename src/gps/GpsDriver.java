package gps;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.TooManyListenersException;

public class GpsDriver {

	private SerialPort port;
	private BufferedReader input;

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
		// FIXME incluir el checksum
		
		String [] fields = inputLine.substring(1).split(",");
		if (fields[0].equals("GPGSA")) {
			System.err.println("GPGSA");
			if (fields[2].equals("1")) {
				System.err.println("Fix is not available");
			} else if (fields[2].equals("2")) {
				System.out.println("2D");
			} else if (fields[2].equals("3")) {
				System.out.println("3D");
			}
		} else if (fields[0].equals("GPRMC")) {
			System.err.println("GPRMC (Recommended Minimum)");
			System.out.println("Time: " + fields[1] + " (UTC)");
			System.out.println("Valid? " + (fields[2].equals("A") ? "Yes" : "No"));
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
