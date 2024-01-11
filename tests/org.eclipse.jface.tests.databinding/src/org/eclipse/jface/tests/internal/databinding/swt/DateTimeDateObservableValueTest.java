/*******************************************************************************
 * Copyright (c) 2006, 2009 Brad Reynolds and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 *     Ashley Cambrell - bug 198904
 *     Matthew Hall - bug 194734, 195222
 *        (through ComboObservableValueTest.java)
 *     Matthew Hall - bug 169876
 ******************************************************************************/

package org.eclipse.jface.tests.internal.databinding.swt;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.Date;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.tests.databinding.AbstractSWTTestCase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DateTime;
import org.junit.Before;
import org.junit.Test;

/**
 * @since 3.2
 */
public class DateTimeDateObservableValueTest extends AbstractSWTTestCase {
	private DateTime dateTime;
	private IObservableValue dateObservable;

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();
		dateTime = new DateTime(getShell(), SWT.DATE);
		dateObservable = WidgetProperties.dateTimeSelection().observe(dateTime);
	}

	@Test
	public void testGetValue_ExcludesTimeComponent() {
		Calendar calendar = Calendar.getInstance();
		calendar.clear();
		int epochHour = calendar.get(Calendar.HOUR_OF_DAY);
		int epochMinute = calendar.get(Calendar.MINUTE);
		int epochSecond = calendar.get(Calendar.SECOND);
		int epochMillisecond = calendar.get(Calendar.MILLISECOND);

		calendar.set(2009, 3, 3, 12, 7, 21); // time of writing
		dateObservable.setValue(calendar.getTime());

		calendar.setTime((Date) dateObservable.getValue());

		assertEquals(2009, calendar.get(Calendar.YEAR));
		assertEquals(3, calendar.get(Calendar.MONTH));
		assertEquals(3, calendar.get(Calendar.DAY_OF_MONTH));

		assertEquals(epochHour, calendar.get(Calendar.HOUR_OF_DAY));
		assertEquals(epochMinute, calendar.get(Calendar.MINUTE));
		assertEquals(epochSecond, calendar.get(Calendar.SECOND));
		assertEquals(epochMillisecond, calendar.get(Calendar.MILLISECOND));
	}
}
