package gps.event.records;

import org.apache.log4j.Logger;

/*
 * Copyright 2012 Edmundo Carmona Antoranz
 * All rights reserved
 * Released under the terms of Mozilla Public License 2.0
 */

public class GPGLL extends GpsInfoRecord {

	private static Logger log = Logger.getLogger(GPGLL.class);

	protected GPGLL(String[] fields) {
		super(fields);
		log.debug("GPGLL Geographic Latitude and Longitude");
	}

}
