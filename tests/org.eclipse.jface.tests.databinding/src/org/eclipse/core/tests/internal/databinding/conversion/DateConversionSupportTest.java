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

import java.util.Date;

import org.eclipse.core.internal.databinding.BindingMessages;
import org.eclipse.core.internal.databinding.conversion.DateConversionSupport;
import org.junit.Test;

import com.ibm.icu.text.DateFormat;
import com.ibm.icu.text.SimpleDateFormat;

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
		SimpleDateFormat dateFormat = (SimpleDateFormat) stub.getDateFormat(0);
		assertEquals(format, dateFormat.toPattern());
	}

	@Test
	public void testTimePatternIsExternalized() throws Exception {
		StubConverter stub = new StubConverter();
		String key = "DateFormat_Time";
		String format = BindingMessages.getString(key);

		assertFalse("format is defined", key.equals(format));
		SimpleDateFormat dateFormat = (SimpleDateFormat) stub.getDateFormat(1);
		assertEquals(format, dateFormat.toPattern());
	}

	@Test
	public void testFormat_NullDate() {
		StubConverter stub = new StubConverter();
		assertNull(stub.format(null));
	}

	static class StubConverter extends DateConversionSupport {
		@Override
		protected DateFormat getDateFormat(int index) {
			return super.getDateFormat(index);
		}

		@Override
		protected String format(Date date) {
			return super.format(date);
		}
	}
}
