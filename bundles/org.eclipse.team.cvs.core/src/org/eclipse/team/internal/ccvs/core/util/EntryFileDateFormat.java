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
 * Utility class for converting timestamps used in Entry file lines. The format
 * required in the Entry file is ISO C asctime() function (Sun Apr 7 01:29:26 1996).
 */
public class EntryFileDateFormat extends SimpleDateFormat {
	
	public static final String FORMAT = "E MMM dd HH:mm:ss yyyy";
	
	public EntryFileDateFormat() {
		super(FORMAT, Locale.US);
		setTimeZone(TimeZone.getTimeZone("GMT"));
	}
	
	/**
	 * Returns a date representing the number of milliseconds since
	 * January 1, 1970, 00:00:00 GMT represented by this Entry file
	 * string date format.
	 */
	public Date toDate(String text) throws ParseException {
		return parse(text);
	}

	/**
	 * long since 1970 => "Thu Oct 18 20:21:13 2001"
	 */	
	public String formatDate(Date date) {
		return format(date);
	}
}