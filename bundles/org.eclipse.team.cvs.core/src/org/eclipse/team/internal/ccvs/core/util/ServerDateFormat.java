package org.eclipse.team.internal.ccvs.core.util;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Does convertion beetween this timeformats:<br>
 * <ul>
 * <li> "18 Oct 2001 20:21:13 -0350"
 * <li> a long messuring the milliseconds after 1970
 * </ul>
 */
public class ServerDateFormat extends SimpleDateFormat {
	
	public static final String FORMAT = "dd MMM yyyy HH:mm:ss";//$NON-NLS-1$
	
	public ServerDateFormat() {
		super(FORMAT, Locale.US);
		setTimeZone(TimeZone.getTimeZone("GMT"));//$NON-NLS-1$
	}

	/**
	 * "18 Oct 2001 20:21:13 -0350" => long since 1970
	 */
	public Date toDate(String text) throws ParseException {
		// FIXME this cuts the timezone which we do not want
		if (text.indexOf("-") != -1) {//$NON-NLS-1$
			text = text.substring(0,text.indexOf("-"));//$NON-NLS-1$
		}
		return parse(text);
	}

	/**
	 * long since 1970 => "18 Oct 2001 20:21:13 -0350"
	 */	
	public String formatDate(Date date) {
		return format(date);
	}
		
}

