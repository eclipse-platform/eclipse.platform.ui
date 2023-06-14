/*******************************************************************************
 * Copyright (c) 2007, 2009 IBM Corporation and others.
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
 *     Matthew Hall - bug 121110
 ******************************************************************************/

package org.eclipse.core.tests.internal.databinding.conversion;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.core.internal.databinding.BindingMessages;
import org.eclipse.core.internal.databinding.conversion.DateConversionSupport;
import org.junit.Test;

/**
 * @since 1.1
 */
public class DateConversionSupportTest {
	@Test
	public void testDatePatternIsExternalized() throws Exception {
		StubConverter stub = new StubConverter();
		String key = "DateFormat_DateTime";
		String format = BindingMessages.getString(key);
		assertFalse("format is defined", key.equals(format));
		Date date = new Date();
		assertEquals(new SimpleDateFormat(format).format(date), stub.format(date, 0));
	}

	@Test
	public void testTimePatternIsExternalized() throws Exception {
		StubConverter stub = new StubConverter();
		String key = "DateFormat_Time";
		String format = BindingMessages.getString(key);

		assertFalse("format is defined", key.equals(format));
		Date date = new Date();
		assertEquals(new SimpleDateFormat(format).format(date), stub.format(date, 1));
	}

	@Test
	public void testFormat_NullDate() {
		StubConverter stub = new StubConverter();
		assertNull(stub.format(null));
	}

	@Test
	public void testParse() {
		StubConverter stub = new StubConverter();
		Date date = new Date(11111111111111L);
		// for example "05.02.2322 20:45:11.111 +0100" or
		// "05.02.2322 20:45:11.111 +0000" depending on timezone:
		String expected = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS Z").format(date);
		String actual = stub.format(date, 0);
		assertEquals(expected, actual);
		assertEquals(date, stub.parse(actual, 0));

		assertEquals("20:45:11.111", stub.format(date, 1));
		assertEquals(71111111, stub.parse("20:45:11.111", 1).getTime());

		assertEquals("05.02.22, 20:45", stub.format(date, 2));
		assertEquals(1644090300000L, stub.parse("05.02.22, 20:45", 2).getTime());

		assertEquals("05.02.22", stub.format(date, 3));
		assertEquals(1644015600000L, stub.parse("05.02.22", 3).getTime());

		assertEquals("20:45", stub.format(date, 4));
		assertEquals(71100000L, stub.parse("20:45", 4).getTime());

		assertEquals("05.02.22, 20:45:11", stub.format(date, 5));
		assertEquals(1644090311000L, stub.parse("05.02.22, 20:45:11", 5).getTime());

		assertEquals("20:45:11", stub.format(date, 6));
		assertEquals(71111000L, stub.parse("20:45:11", 6).getTime());
	}

	static class StubConverter extends DateConversionSupport {
		// make public
		@Override
		public String format(Date date, int formatterIdx) {
			return super.format(date, formatterIdx);
		}

		@Override
		public String format(Date date) {
			return super.format(date);
		}

		@Override
		public Date parse(String str, int formatterIdx) {
			return super.parse(str, formatterIdx);
		}

	}
}
