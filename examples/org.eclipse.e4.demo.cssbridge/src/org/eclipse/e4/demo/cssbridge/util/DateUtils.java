/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.demo.cssbridge.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
	private static final String UNKNOWN_DATE = "unknown";

	private static SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm", Locale.ENGLISH);

	public static Date parse(String dateAsString) {
		try {
			return DATE_FORMATTER.parse(dateAsString);
		} catch (ParseException exc) {
			return null;
		}
	}

	public static String toString(Date date) {
		return date == null ? UNKNOWN_DATE : DATE_FORMATTER.format(date);
	}
}
