/*
 * Copyright (C) 2005 db4objects Inc.  http://www.db4o.com
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     db4objects - Initial API and implementation
 */
package org.eclipse.jface.databinding.converterfunctions;

import java.text.*;
import java.util.*;

/**
 * Base support for date/string conversion handling according to the
 * default locale or in plain long milliseconds.
 * 
 * NOTE: parse(format(date)) will generally *not* be equal to date, since the
 * string representation may not cover the sub-second range, time-only string
 * representations will be counted from the beginning of the era, etc.
 */
public abstract class DateConversionSupport {
	private final static int DATE_FORMAT=DateFormat.SHORT;
	private final static int DEFAULT_FORMATTER_INDEX=0;

	private final static int NUM_VIRTUAL_FORMATTERS=1;
	
	/**
	 * Alternative formatters for date, time and date/time.
	 * Raw milliseconds are covered as a special case.
	 */
	// TODO: These could be shared, but would have to be synchronized.
	private DateFormat[] formatters={
			new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS Z"), //$NON-NLS-1$
            new SimpleDateFormat("HH:mm:ss.SSS"), //$NON-NLS-1$
			DateFormat.getDateTimeInstance(DATE_FORMAT,DateFormat.SHORT),
			DateFormat.getDateInstance(DATE_FORMAT),
			DateFormat.getTimeInstance(DateFormat.SHORT),
            DateFormat.getDateTimeInstance(DATE_FORMAT,DateFormat.MEDIUM),
            DateFormat.getTimeInstance(DateFormat.MEDIUM)
	};
	
	/**
	 * Tries all available formatters to parse the given string according to the
	 * default locale or as a raw millisecond value and returns the result of the
	 * first successful run.
	 * 
	 * @param str A string specifying a date according to the default locale or in raw milliseconds
	 * @return The parsed date, or null, if no available formatter could interpret the input string
	 */
	protected Date parse(String str) {
		for (int formatterIdx = 0; formatterIdx < formatters.length; formatterIdx++) {
			Date parsed=parse(str,formatterIdx);
			if(parsed!=null) {
				return parsed;
			}
		}
		return null;
	}

	protected Date parse(String str,int formatterIdx) {
		if(formatterIdx>=0) {
				ParsePosition pos=new ParsePosition(0);
				Date date=formatters[formatterIdx].parse(str,pos);
				if(pos.getErrorIndex()!=-1||pos.getIndex()!=str.length()) {
					return null;
				}
				return date;
		}
		try {
			long millisecs=Long.parseLong(str);
			return new Date(millisecs);
		}
		catch(NumberFormatException exc) {
		}
		return null;
	}
	
	/**
	 * Formats the given date with the default formatter according to the default locale.
	 */
	protected String format(Date date) {
		return format(date,DEFAULT_FORMATTER_INDEX);
	}

	protected String format(Date date,int formatterIdx) {
		if(formatterIdx>=0) {
			return formatters[formatterIdx].format(date);
		}
		return String.valueOf(date.getTime());
	}

	protected int numFormatters() {
		return formatters.length+NUM_VIRTUAL_FORMATTERS;
	}
}