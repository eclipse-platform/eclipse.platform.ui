/*******************************************************************************
 * Copyright (c) 2010 Ovidio Mallo and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ovidio Mallo - initial API and implementation (bug 306611)
 ******************************************************************************/

package org.eclipse.core.tests.internal.databinding;

import org.eclipse.core.databinding.BindingProperties;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.core.internal.databinding.ConverterValueProperty;
import org.eclipse.core.internal.databinding.conversion.ObjectToStringConverter;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;

/**
 * Tests for the {@link ConverterValueProperty} class.
 */
public class ConverterValuePropertyTest extends AbstractDefaultRealmTestCase {

	private IConverter converter;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		converter = new ObjectToStringConverter(Integer.class);
	}

	public void testGetValue() {
		IValueProperty property = BindingProperties.convertedValue(converter);

		assertEquals("123", property.getValue(new Integer(123)));
	}

	public void testGetValueForNullSource() {
		// The converter converts null to "".
		IValueProperty property = BindingProperties.convertedValue(converter);

		// null should also be converted rather than simply returning null.
		assertEquals("", property.getValue(null));
	}

	public void testSetValue() {
		IValueProperty property = BindingProperties.convertedValue(converter);

		try {
			property.setValue(new Integer(123), "123");
			fail("setting a value should trigger an exception!");
		} catch (UnsupportedOperationException e) {
			// expected exception
		}
	}

	public void testGetValueType() {
		IValueProperty property = BindingProperties.convertedValue(converter);

		assertEquals(converter.getToType(), property.getValueType());
	}
}
