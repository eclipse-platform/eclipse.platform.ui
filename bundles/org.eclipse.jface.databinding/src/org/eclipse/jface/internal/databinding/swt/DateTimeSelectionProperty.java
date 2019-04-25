/*******************************************************************************
 * Copyright (c) 2009, 2016 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 169876)
 *     Matthew Hall - bug 271720
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 502228
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
public class DateTimeSelectionProperty extends WidgetValueProperty<DateTime, Date> {
	/**
	 *
	 */
	public DateTimeSelectionProperty() {
		super(SWT.Selection);
	}

	@Override
	public Object getValueType() {
		return Date.class;
	}

	// One calendar per thread to preserve thread-safety
	private static final ThreadLocal<Calendar> calendar = ThreadLocal.withInitial(() -> Calendar.getInstance());

	@Override
	protected Date doGetValue(DateTime source) {
		Calendar cal = calendar.get();
		cal.clear();
		if ((source.getStyle() & SWT.TIME) != 0) {
			cal.set(Calendar.HOUR_OF_DAY, source.getHours());
			cal.set(Calendar.MINUTE, source.getMinutes());
			cal.set(Calendar.SECOND, source.getSeconds());
		} else {
			cal.set(Calendar.YEAR, source.getYear());
			cal.set(Calendar.MONTH, source.getMonth());
			cal.set(Calendar.DAY_OF_MONTH, source.getDay());
		}
		return cal.getTime();
	}

	@Override
	protected void doSetValue(DateTime source, Date value) {
		if (value == null) {
			// ignore null, since it cannot be applied to the DateTimeWidget
			return;
		}

		Calendar cal = calendar.get();
		cal.setTime(value);
		if ((source.getStyle() & SWT.TIME) != 0) {
			source.setTime(cal.get(Calendar.HOUR_OF_DAY), cal
					.get(Calendar.MINUTE), cal.get(Calendar.SECOND));
		} else {
			source.setDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
					cal.get(Calendar.DAY_OF_MONTH));
		}
	}
}
