package org.eclipse.team.internal.ccvs.core.util;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Does convertion beetween this timeformats:<br>
 * <ul>
 * <li> "Thu Oct 18 20:21:13 2001"
 * <li> a long messuring the milliseconds after 1970
 * </ul>
 */
public class FileDateFormat extends SimpleDateFormat {
	
	public static final String FORMAT = "E MMM dd HH:mm:ss yyyy";
	
	public FileDateFormat() {
		super(FORMAT,new Locale("en","US"));
	}
	
	/**
	 * "Thu Oct 18 20:21:13 2001" => long since 1970
	 */
	public long parseMill(String text) throws ParseException {
		return parse(text).getTime();
	}

	/**
	 * long since 1970 => "Thu Oct 18 20:21:13 2001"
	 */	
	public String formatMill(long millSec) {
		return format(new Date(millSec));
	}		
}

