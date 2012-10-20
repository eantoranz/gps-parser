package gps;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.util.TooManyListenersException;

public class GpsDriver implements SerialPortEventListener {

	private SerialPort port;

	public GpsDriver(String device, int speed) throws NoSuchPortException,
			PortInUseException, IOException, UnsupportedCommOperationException, TooManyListenersException {
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
		port.notifyOnDataAvailable(true);
		port.addEventListener(this);
	}
	

	public static void main(String[] args) {
		try {
			new GpsDriver("/dev/ttyUSB0", 4800);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}


	public void serialEvent(SerialPortEvent event) {
		// there was an event at the serial port
		byte chars [] = new byte[1024];
		
		if (event.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			// there's some data available at the port
			try {
				int bytesRead = port.getInputStream().read(chars, 0, 1024);
				System.out.println("Read " + new String(chars));
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			System.out.print("Some kind of port event: " + event);
		}
	}

}
