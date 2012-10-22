package gps;

import java.io.FileReader;

import gps.event.GPGLL;
import gps.event.GPRMC;
import gps.event.GpsEvent;
import gps.event.GpsEventListener;

/*
 * Will create one analyzer and will use two listeners. One listener will
 * listen to all events and the other will listen to GPRMC and GPGLL
 */
public class FileAnalyzerTwoListeners {

	public static void main(String[] args) {
		GpsEventListener listenerAll = new ListenerAll();
		GpsEventListener listenerSome = new ListenerSome();
		try {
			GpsAnalyzer analyzer = new GpsAnalyzer(new FileReader(
					"/home/antoranz/gps2.txt"));
			analyzer.addGpsEventListener(GpsEvent.class, listenerAll);
			analyzer.addGpsEventListener(GPRMC.class, listenerSome);
			analyzer.addGpsEventListener(GPGLL.class, listenerSome);
			analyzer.startAnalyzing();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

class ListenerAll implements GpsEventListener {

	public void eventFound(GpsEvent event) {
		System.out.println("All events listener got an event notification: "
				+ event);
	}

}

class ListenerSome implements GpsEventListener {

	public void eventFound(GpsEvent event) {
		System.out.println("Some events listener got an event notification: "
				+ event);
	}

}
