package gps;

import java.util.Date;

/*
 * Copyright 2012 Edmundo Carmona Antoranz <eantoranz@gmail.com>
 * All rights reserved
 */

/**
 * Information about a satellite
 * 
 * @author antoranz
 * 
 */
public class Satellite {

	private int prn;
	private int elevation;
	private int azimuth;
	private int snr;
	private boolean inView;
	private Date lastUpdate;

	/**
	 * 
	 * @param pnr
	 * @param elevation
	 * @param azimuth
	 * @param snr
	 *            Signal-to-Noise reduction. Values between 0-100. -1 means
	 *            there's not signal
	 */
	public Satellite(int prn, int elevation, int azimuth, int snr, boolean inView) {
		System.err.println("Creating satellite " + prn);
		this.prn = prn;
		this.elevation = elevation;
		this.azimuth = azimuth;
		this.snr = snr;
		this.inView = inView;
		this.lastUpdate = new Date();
	}
	
	public void refresh(int elevation, int azimuth, int snr, boolean inView) {
		System.out.println("Updating satellite " + prn);
		this.elevation = elevation;
		this.azimuth = azimuth;
		this.snr = snr;
		this.inView = inView;
		this.lastUpdate = new Date();
	}
	
	public boolean isInView() {
		return inView;
	}

	/**
	 * set the satellite to not in view. Don't refresh last update date
	 */
	public void setNotInView() {
		System.err.println("Setting satellite " + prn + " as not in view");
		this.inView = false;
	}

}
