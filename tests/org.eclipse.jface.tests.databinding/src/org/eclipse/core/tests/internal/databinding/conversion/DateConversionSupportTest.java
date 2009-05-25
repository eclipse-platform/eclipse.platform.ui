/*******************************************************************************
 * Copyright (c) 2007, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matthew Hall - bug 121110
 ******************************************************************************/

package org.eclipse.core.tests.internal.databinding.conversion;

import java.util.Date;

import junit.framework.TestCase;

import org.eclipse.core.internal.databinding.BindingMessages;
import org.eclipse.core.internal.databinding.conversion.DateConversionSupport;

import com.ibm.icu.text.DateFormat;
import com.ibm.icu.text.SimpleDateFormat;

/**
 * @since 1.1
 */
public class DateConversionSupportTest extends TestCase {
	public void testDatePatternIsExternalized() throws Exception {
		StubConverter stub = new StubConverter();
		String key = "DateFormat_DateTime";
		String format = BindingMessages.getString(key);
		
		assertFalse("format is defined", key.equals(format));
		SimpleDateFormat dateFormat = (SimpleDateFormat) stub.getDateFormat(0);
		assertEquals(format, dateFormat.toPattern());
	}
	
	public void testTimePatternIsExternalized() throws Exception {
		StubConverter stub = new StubConverter();
		String key = "DateFormat_Time";
		String format = BindingMessages.getString(key);
		
		assertFalse("format is defined", key.equals(format));
		SimpleDateFormat dateFormat = (SimpleDateFormat) stub.getDateFormat(1);
		assertEquals(format, dateFormat.toPattern());
	}
	
	public void testFormat_NullDate() {
		StubConverter stub = new StubConverter();
		assertNull(stub.format(null));
	}
	
	static class StubConverter extends DateConversionSupport {
		protected DateFormat getDateFormat(int index) {
			return super.getDateFormat(index);
		}
		
		protected String format(Date date) {
			return super.format(date);
		}
	}
}
