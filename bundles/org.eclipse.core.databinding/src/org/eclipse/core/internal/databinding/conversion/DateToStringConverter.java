/*
 * Copyright (C) 2005, 2015 db4objects Inc.  http://www.db4o.com
 *
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     db4objects - Initial API and implementation
 */
package org.eclipse.core.internal.databinding.conversion;

import java.util.Date;

import org.eclipse.core.databinding.conversion.IConverter;

/**
 * Converts a Java.util.Date to a String using the current locale. Null date
 * values are converted to an empty string.
 *
 * @since 1.0
 */
public class DateToStringConverter extends DateConversionSupport implements IConverter<Date, String> {
	@Override
	public String convert(Date source) {
		if (source != null)
			return format(source);
		return ""; //$NON-NLS-1$
	}

	@Override
	public Object getFromType() {
		return Date.class;
	}

	@Override
	public Object getToType() {
		return String.class;
	}
}
