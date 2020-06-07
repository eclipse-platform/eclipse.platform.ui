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

package org.eclipse.core.tests.databinding.observable.value;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.LocalDateTimeObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;
import org.junit.Before;
import org.junit.Test;

public class LocalDateTimeObservableValueTest extends AbstractDefaultRealmTestCase {
	private IObservableValue<LocalDate> date;
	private IObservableValue<LocalTime> time;
	private IObservableValue<LocalDateTime> dateAndTime;

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();

		date = WritableValue.withValueType(LocalDate.class);
		time = WritableValue.withValueType(LocalTime.class);

		dateAndTime = new LocalDateTimeObservableValue(date, time);
	}

	@Test
	public void testGetValue_NullDateNullResult() {
		date.setValue(null);
		time.setValue(LocalTime.of(12, 27, 17));

		assertNull(dateAndTime.getValue());
	}

	@Test
	public void testGetValue_NullTimeClearsTime() {
		date.setValue(LocalDate.of(2009, 3, 3));
		time.setValue(null);

		assertEquals(LocalDateTime.of(2009, 3, 3, 0, 0, 0), dateAndTime.getValue());
	}

	@Test
	public void testGetValue() {
		date.setValue(LocalDate.of(2009, 3, 3));
		time.setValue(LocalTime.of(23, 59, 59));

		assertEquals(LocalDateTime.of(2009, 3, 3, 23, 59, 59), dateAndTime.getValue());
	}

	@Test
	public void testSetValue() {
		date.setValue(LocalDate.of(2009, 3, 3));
		time.setValue(LocalTime.of(12, 32, 55));

		dateAndTime.setValue(LocalDateTime.of(2010, 1, 1, 2, 3, 5));

		assertEquals(LocalDate.of(2010, 1, 1), date.getValue());
		assertEquals(LocalTime.of(2, 3, 5), time.getValue());
	}

	@Test
	public void testSetValue_NullNullsDateClearsTime() {
		date.setValue(LocalDate.of(2009, 3, 3));
		time.setValue(LocalTime.of(12, 25, 34));

		dateAndTime.setValue(null);

		assertEquals(null, date.getValue());
		assertEquals(LocalTime.of(0, 0, 0), time.getValue());
	}

	@Test
	public void testSetValue_PreserveTimeOfDateAndDateOfTime() {
		date.setValue(LocalDate.of(2009, 3, 3));
		time.setValue(LocalTime.of(12, 32, 55));

		dateAndTime.setValue(LocalDateTime.of(2010, 1, 1, 2, 3, 5));

		assertEquals(LocalDate.of(2010, 1, 1), date.getValue());
		assertEquals(LocalTime.of(2, 3, 5), time.getValue());
	}
}
