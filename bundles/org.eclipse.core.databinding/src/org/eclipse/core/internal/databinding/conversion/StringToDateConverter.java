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
 * Convert a String to a java.util.Date, respecting the current locale
 *
 * @since 1.0
 */
public class StringToDateConverter extends DateConversionSupport implements IConverter<Object, Date> {
	@Override
	public Date convert(Object source) {
		return parse(source.toString());
	}

	@Override
	public Object getFromType() {
		return String.class;
	}

	@Override
	public Object getToType() {
		return Date.class;
	}
}
