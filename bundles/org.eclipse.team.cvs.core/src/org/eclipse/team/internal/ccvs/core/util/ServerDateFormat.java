package org.eclipse.team.internal.ccvs.core.util;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Does convertion beetween this timeformats:<br>
 * <ul>
 * <li> "18 Oct 2001 20:21:13 -0350"
 * <li> a long messuring the milliseconds after 1970
 * </ul>
 */
public class ServerDateFormat extends SimpleDateFormat {
	
	public static final String FORMAT = "dd MMM yyyy HH:mm:ss";
	
	public ServerDateFormat() {
		super(FORMAT);
	}

	/**
	 * "18 Oct 2001 20:21:13 -0350" => long since 1970
	 */
	public long parseMill(String text) throws ParseException {

		// FIXME this cuts the timezone which we do not 
		//       want
		if (text.indexOf("-") != -1) {
			text = text.substring(0,text.indexOf("-"));
		}
		
		return parse(text).getTime();
	}

	/**
	 * long since 1970 => "18 Oct 2001 20:21:13 -0350"
	 */	
	public String formatMill(long millSec) {
		return format(new Date(millSec));
	}
		
}

