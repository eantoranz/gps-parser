package gps;

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

	private int pnr;
	private int elevation;
	private int azimuth;
	private int snr;
	private boolean inView;

	/**
	 * 
	 * @param pnr
	 * @param elevation
	 * @param azimuth
	 * @param snr
	 *            Signal-to-Noise reduction. Values between 0-100. -1 means
	 *            there's not signal
	 */
	public Satellite(int pnr, int elevation, int azimuth, int snr, boolean inView) {
		this.pnr = pnr;
		this.elevation = elevation;
		this.azimuth = azimuth;
		this.snr = snr;
		this.inView = inView;
	}

}
