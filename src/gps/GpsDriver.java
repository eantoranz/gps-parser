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

public class GpsDriver implements Runnable {

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
		new Thread(this).start();
	}

	public void run() {
		while (true) {
			try {
				System.out.println(input.readLine());
			} catch (IOException e) {
				e.printStackTrace();
			}
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
