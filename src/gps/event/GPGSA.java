package gps.event;

import org.apache.log4j.Logger;

/*
 * Copyright 2012 Edmundo Carmona Antoranz
 * All rights reserved
 * Released under the terms of Mozilla Public License 2.0
 */
public class GPGSA extends GpsEvent {
	
	private static Logger log = Logger.getLogger(GPGSA.class);
	
	protected GPGSA(String [] fields) {
		super(fields);
		log.info("GPGSA (Dillution of precision / Active satellites)");
		if (fields[2].equals("1")) {
			log.debug("\tFix is not available");
		} else if (fields[2].equals("2")) {
			log.debug("\t2D");
		} else if (fields[2].equals("3")) {
			log.debug("\t3D");
		}
	}

}
