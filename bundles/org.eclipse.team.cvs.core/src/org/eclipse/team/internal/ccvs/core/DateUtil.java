package org.eclipse.team.internal.ccvs.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.text.ParseException;import java.text.SimpleDateFormat;import java.util.Date;import java.util.Locale;import java.util.TimeZone;

/**
 * Utilities to handle time stamps in a cvs client.
 */
public class DateUtil {

	private static final String ENTRY_TIMESTAMP_FORMAT= "EEE MMM dd HH:mm:ss yyyy";
	private static final String ENTRY_TIMESTAMP_TIME_ZONE= "GMT";
	private static final Locale ENTRY_TIMESTAMP_LOCALE= Locale.US;
	
	private static final String MODTIME_TIMESTAMP_FORMAT= "dd MMM yyyy HH:mm:ss zz";
	private static final Locale MODTIME_TIMESTAMP_LOCALE= Locale.US;
	
	private static final String LOG_TIMESTAMP_FORMAT= "yyyy/MM/dd HH:mm:ss zzz";
	private static final Locale LOG_TIMESTAMP_LOCALE= Locale.US;
	
	private static final String HISTORY_TIMESTAMP_FORMAT= "yyyy-MM-dd HH:mm zzzz";
	private static final Locale HISTORY_TIMESTAMP_LOCALE= Locale.US;
	
	/**
	 * Converts a time stamp as sent from a cvs server for a "log" command into a
	 * <code>Date</code>.
	 */
	public static Date convertFromLogTime(String modTime) {
		SimpleDateFormat format= new SimpleDateFormat(LOG_TIMESTAMP_FORMAT, 
			LOG_TIMESTAMP_LOCALE);
		try {
			return format.parse(modTime);
		} catch (ParseException e) {
			// fallback is to return null
			return null;
		}
	}
	/**
	 * Converts a modifcation time stamp as send from a cvs server into a
	 * <code>Date</code>. The format of the modification time stamp is defined
	 * in the document CVS Client/Server for CVS 1.11 section 5.6 Dates
	 */
	public static Date convertFromModTime(String modTime) {
		SimpleDateFormat format= new SimpleDateFormat(MODTIME_TIMESTAMP_FORMAT, 
			MODTIME_TIMESTAMP_LOCALE);
		try {
			return format.parse(modTime);
		} catch (ParseException e) {
			// fallback is to return null
			return null;
		}
	}
	/**
	 * Converts a history time stamp as sent from a cvs server into a
	 * <code>Date</code>.
	 */
	public static Date convertFromHistoryTime(String historyTime) {
		SimpleDateFormat format= new SimpleDateFormat(HISTORY_TIMESTAMP_FORMAT, 
			HISTORY_TIMESTAMP_LOCALE);
		try {
			return format.parse(historyTime);
		} catch (ParseException e) {
			// fallback is to return null
			return null;
		}
	}
	/**
	 * Converts a date into an entry time format as specified in the document
	 * Version Management with CVS for CVS 1.10.6 page 14. Note that the
	 * time format is always in GMT also not specified in the document.
	 */
	public static String toEntryFormat(Date date) {
		SimpleDateFormat format= new SimpleDateFormat(ENTRY_TIMESTAMP_FORMAT,
			ENTRY_TIMESTAMP_LOCALE);
		format.setTimeZone(TimeZone.getTimeZone(ENTRY_TIMESTAMP_TIME_ZONE));
		return format.format(date);
	}
}
