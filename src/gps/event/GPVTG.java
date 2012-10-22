package gps.event;

import org.apache.log4j.Logger;

/*
 * Copyright 2012 Edmundo Carmona Antoranz
 * All rights reserved
 * Released under the terms of Affero GPLv3
 */
public class GPVTG extends GpsEvent {
	
	private static Logger log = Logger.getLogger(GPVTG.class);
	
	protected GPVTG(String [] fields) {
		super(fields);
		log.info("GPVTG Vector track and speed over ground");
	}

}
