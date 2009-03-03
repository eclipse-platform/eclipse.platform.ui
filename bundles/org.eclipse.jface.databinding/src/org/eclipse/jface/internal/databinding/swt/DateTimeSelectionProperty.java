/*******************************************************************************
 * Copyright (c) 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 169876)
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.swt;

import java.util.Calendar;
import java.util.Date;

import org.eclipse.jface.databinding.swt.WidgetValueProperty;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DateTime;

/**
 * @since 3.2
 * 
 */
public class DateTimeSelectionProperty extends WidgetValueProperty {
	/**
	 * 
	 */
	public DateTimeSelectionProperty() {
		super(SWT.Selection);
	}

	public Object getValueType() {
		return Date.class;
	}

	// One calendar per thread to preserve thread-safety
	private static final ThreadLocal calendar = new ThreadLocal() {
		protected Object initialValue() {
			return Calendar.getInstance();
		}
	};

	protected Object doGetValue(Object source) {
		DateTime dateTime = (DateTime) source;

		Calendar cal = (Calendar) calendar.get();
		cal.clear();
		if ((dateTime.getStyle() & SWT.TIME) != 0) {
			cal.set(Calendar.HOUR_OF_DAY, dateTime.getHours());
			cal.set(Calendar.MINUTE, dateTime.getMinutes());
			cal.set(Calendar.SECOND, dateTime.getSeconds());
		} else {
			cal.set(Calendar.YEAR, dateTime.getYear());
			cal.set(Calendar.MONTH, dateTime.getMonth());
			cal.set(Calendar.DAY_OF_MONTH, dateTime.getDay());
		}
		return cal.getTime();
	}

	protected void doSetValue(Object source, Object value) {
		DateTime dateTime = (DateTime) source;

		Calendar cal = (Calendar) calendar.get();
		cal.setTime((Date) value);
		if ((dateTime.getStyle() & SWT.TIME) != 0) {
			dateTime.setTime(cal.get(Calendar.HOUR_OF_DAY), cal
					.get(Calendar.MINUTE), cal.get(Calendar.SECOND));
		} else {
			dateTime.setDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
					cal.get(Calendar.DAY_OF_MONTH));
		}
	}
}
