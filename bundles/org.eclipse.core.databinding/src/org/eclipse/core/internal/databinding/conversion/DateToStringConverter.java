/*
 * Copyright (C) 2005, 2007 db4objects Inc.  http://www.db4o.com
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     db4objects - Initial API and implementation
 */
package org.eclipse.core.internal.databinding.conversion;

import java.util.Date;

import org.eclipse.core.databinding.conversion.IConverter;


/**
 * Converts a Java.util.Date to a String using the current locale.  Null date
 * values are converted to an empty string.
 * 
 * @since 1.0
 */
public class DateToStringConverter extends DateConversionSupport implements IConverter {	
	public Object convert(Object source) {
		if (source != null)
			return format((Date)source);
		return ""; //$NON-NLS-1$
	}

	public Object getFromType() {
		return Date.class;
	}

	public Object getToType() {
		return String.class;
	}	
}
