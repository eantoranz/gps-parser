package gps.event;

import org.apache.log4j.Logger;

/*
 * Copyright 2012 Edmundo Carmona Antoranz
 * All rights reserved
 * Released under the terms of Affero GPLv3
 */

public class GPGLL extends GpsEvent {

	private static Logger log = Logger.getLogger(GPGLL.class);

	protected GPGLL(String[] fields) {
		super(fields);
		log.info("GPGLL Geographic Latitude and Longitude");
	}

}
