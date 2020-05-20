/*******************************************************************************
 * Copyright (c) 2020 Jens Lidestrom and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Jens Lidestrom - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.swt;

import java.time.LocalDate;

import org.eclipse.jface.databinding.swt.WidgetValueProperty;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DateTime;

/**
 */
public class LocalDateSelectionProperty extends WidgetValueProperty<DateTime, LocalDate> {
	/**
	 */
	public LocalDateSelectionProperty() {
		super(SWT.Selection);
	}

	@Override
	public Object getValueType() {
		return LocalDate.class;
	}

	@Override
	protected LocalDate doGetValue(DateTime source) {
		if ((source.getStyle() & SWT.TIME) != 0) {
			throw new IllegalStateException();
		}

		// Adjust for 0 based month in DateTime
		return LocalDate.of(source.getYear(), source.getMonth() + 1, source.getDay());
	}

	@Override
	protected void doSetValue(DateTime source, LocalDate value) {
		if (value == null) {
			// Ignore null, since it cannot be applied to the DateTimeWidget
			return;
		}

		if ((source.getStyle() & SWT.TIME) != 0) {
			throw new IllegalStateException();
		}

		// Adjust for 0 based month in DateTime
		source.setDate(value.getYear(), value.getMonthValue() - 1, value.getDayOfMonth());
	}
}
