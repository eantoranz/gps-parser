package gps.event;

import org.apache.log4j.Logger;

/*
 * Copyright 2012 Edmundo Carmona Antoranz
 * All rights reserved
 */

public class GPGGA extends AbstractEvent {

	private static Logger log = Logger.getLogger(GPGGA.class);

	protected GPGGA(String[] fields) {
		super(fields);
		log.info("GPGGA Fix information (3d location and accuracy data)");
	}

}
