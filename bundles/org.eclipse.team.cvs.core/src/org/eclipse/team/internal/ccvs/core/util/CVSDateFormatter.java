/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.core.util;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;

/**
 * Utility class for converting timestamps used in Entry file lines. The format
 * required in the Entry file is ISO C asctime() function (Sun Apr  7 01:29:26 1996).
 * <p>
 * To be compatible with asctime(), the day field in the entryline format is
 * padded with a space and not a zero. Most other CVS clients use string comparison 
 * for timestamps based on the result of the C function asctime().
 * </p>
 */
public class CVSDateFormatter {
	
	private static final String ENTRYLINE_FORMAT = "E MMM dd HH:mm:ss yyyy"; //$NON-NLS-1$
	private static final String DATE_TAG_NAME_FORMAT = "dd MMM yyyy HH:mm:ss Z";//$NON-NLS-1$
	private static final String REPO_VIEW_LONG_FORAMT = "dd MMM yyyy HH:mm:ss";
	private static final String REPO_VIEW_SHORT_FORMAT = "dd MMM yyyy";
	private static final String TIME_ONLY_DOT_FORMAT = "HH.mm.ss";
	private static final String TIME_ONLY_COLUMN_FORMAT = "HH:mm:ss";
	private static final String DATE_TAG_INTERNAL_NAME_FORMAT = "yyyy.MM.dd.HH.mm.ss";
	private static String DACORATOR_FORMAT = "yyyy/MM/dd HH:mm:ss";
	private static final int ENTRYLINE_TENS_DAY_OFFSET = 8;
	
	private static final SimpleDateFormat serverFormat = new SimpleDateFormat(DATE_TAG_NAME_FORMAT, Locale.US);
	private static SimpleDateFormat entryLineFormat = new SimpleDateFormat(ENTRYLINE_FORMAT, Locale.US);
	private static SimpleDateFormat localLongFormat = new SimpleDateFormat(REPO_VIEW_LONG_FORAMT,Locale.getDefault());
	private static SimpleDateFormat localShortFormat = new SimpleDateFormat(REPO_VIEW_SHORT_FORMAT,Locale.getDefault());
	private static SimpleDateFormat timeColumnFormat = new SimpleDateFormat(TIME_ONLY_COLUMN_FORMAT, Locale.getDefault());
	private static SimpleDateFormat tagNameInternalFormat = new SimpleDateFormat(DATE_TAG_INTERNAL_NAME_FORMAT, Locale.getDefault());
	private static SimpleDateFormat decorateFormatter = new SimpleDateFormat(DACORATOR_FORMAT, Locale.getDefault());
	
	static {
		entryLineFormat.setTimeZone(TimeZone.getTimeZone("GMT")); //$NON-NLS-1$
	}
	static synchronized public Date serverStampToDate(String text) throws ParseException {
		serverFormat.setTimeZone(getTimeZone(text));
		Date date = serverFormat.parse(text);
		return date;
	}
	
	static synchronized public String repoViewTimeStamp(Date date){
		String localTime = timeColumnFormat.format(date);
		timeColumnFormat.setTimeZone(TimeZone.getDefault());
		if(localTime.equals("00:00:00")){
			return localShortFormat.format(date);
		}
		return localLongFormat.format(date);
	}
	static synchronized public String decoratorTimeStamp(Date date){
		return decorateFormatter.format(date);
	}
	static synchronized public String dateToTagName(Date date){
		serverFormat.setTimeZone(TimeZone.getTimeZone("GMT"));//$NON-NLS-1$
		return serverFormat.format(date); //$NON-NLS-1$
	}
	static public Date tagNameToDate(String name){
		if (name == null) return null;		
		try {
			return serverFormat.parse(name);
		} catch (ParseException e) {
			CVSProviderPlugin.log(wrapException(name, DATE_TAG_NAME_FORMAT, e));
			return null;
		}
	}
	static synchronized public Date entryLineToDate(String text) throws ParseException {
		try {
			if (text.charAt(ENTRYLINE_TENS_DAY_OFFSET) == ' ') {
				StringBuffer buf = new StringBuffer(text);
				buf.setCharAt(ENTRYLINE_TENS_DAY_OFFSET, '0');
				text = buf.toString();
			}
		} catch (StringIndexOutOfBoundsException e) {
			throw new ParseException(e.getMessage(), ENTRYLINE_TENS_DAY_OFFSET);
		}
		return entryLineFormat.parse(text);
	}
	static synchronized public Date parseEntryLineName(String text){
		try {
			 return tagNameInternalFormat.parse(text);
		} catch (ParseException e) {
			CVSProviderPlugin.log(wrapException(text, DATE_TAG_INTERNAL_NAME_FORMAT, e));
			return null;
		}		
	}
	static synchronized public String dateToEntryLine(Date date) {
		if (date == null) return ""; //$NON-NLS-1$
		String passOne = entryLineFormat.format(date);
		if (passOne.charAt(ENTRYLINE_TENS_DAY_OFFSET) != '0') return passOne;
		StringBuffer passTwo = new StringBuffer(passOne);
		passTwo.setCharAt(ENTRYLINE_TENS_DAY_OFFSET, ' ');
		return passTwo.toString();
	}

	static synchronized public String tagNametoInternalName(String text){
		if(text.length() == DATE_TAG_INTERNAL_NAME_FORMAT.length())return text;
		Date date = null;
		try {
			date = serverFormat.parse(text);
		} catch (ParseException e) {
			CVSProviderPlugin.log(wrapException(text, DATE_TAG_NAME_FORMAT, e));
			return null;
		}
		tagNameInternalFormat.setTimeZone(TimeZone.getDefault());
		return tagNameInternalFormat.format(date);
	}
	static synchronized public String dateToNotifyServer(Date date) {
		serverFormat.setTimeZone(TimeZone.getTimeZone("GMT"));//$NON-NLS-1$
		return serverFormat.format(date) + " GMT"; //$NON-NLS-1$
	}
	
	/*
	 * Converts timezone text from date string from CVS server and
	 * returns a timezone representing the received timezone.
	 * Timezone string is of the following format: [-|+]MMSS
	 */
	static private TimeZone getTimeZone(String dateFromServer) {
		String tz = null;
		StringBuffer resultTz = new StringBuffer("GMT");//$NON-NLS-1$
		if (dateFromServer.indexOf("-") != -1) {//$NON-NLS-1$
			resultTz.append("-");//$NON-NLS-1$
			tz = dateFromServer.substring(dateFromServer.indexOf("-"));//$NON-NLS-1$
		} else if (dateFromServer.indexOf("+") != -1) {//$NON-NLS-1$
			resultTz.append('+');
			tz = dateFromServer.substring(dateFromServer.indexOf("+"));//$NON-NLS-1$
		}
		try {
			if(tz!=null) {
				resultTz.append(tz.substring(1, 3) /*hours*/ + ":" + tz.substring(3, 5) /*minutes*/);//$NON-NLS-1$
				return TimeZone.getTimeZone(resultTz.toString());
			}
		} catch(IndexOutOfBoundsException e) {
			return TimeZone.getTimeZone("GMT");//$NON-NLS-1$
		}
		return TimeZone.getTimeZone("GMT");//$NON-NLS-1$
	}
	
	static private CVSException wrapException(String string, String format, Throwable t){
		return new CVSException("Cannot parse " + string 
					+ ", format " + format 
					+ "is expected\n", t);
	}
	
}
