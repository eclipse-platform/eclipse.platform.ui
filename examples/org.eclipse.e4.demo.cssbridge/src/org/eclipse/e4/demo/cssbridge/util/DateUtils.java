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

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
	private static final String UNKNOWN_DATE = "unknown";

	private static DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(
			"yyyy-MM-dd HH:mm", Locale.ENGLISH).withZone(ZoneId.systemDefault());

	public static Date parse(String dateAsString) {
		try {
			return Date.from(Instant.from(DATE_FORMATTER.parse(dateAsString)));
		} catch (DateTimeParseException exc) {
			return null;
		}
	}

	public static String toString(Date date) {
		return date == null ? UNKNOWN_DATE : DATE_FORMATTER.format(date.toInstant());
	}
}
