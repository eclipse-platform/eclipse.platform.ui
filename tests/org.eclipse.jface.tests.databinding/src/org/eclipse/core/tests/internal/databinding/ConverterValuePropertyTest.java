/*******************************************************************************
 * Copyright (c) 2010 Ovidio Mallo and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ovidio Mallo - initial API and implementation (bug 306611)
 ******************************************************************************/

package org.eclipse.core.tests.internal.databinding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.eclipse.core.databinding.BindingProperties;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.core.internal.databinding.conversion.ObjectToStringConverter;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link BindingProperties#convertedValue}.
 */
public class ConverterValuePropertyTest extends AbstractDefaultRealmTestCase {

	private IConverter<Object, String> converter;

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();

		converter = new ObjectToStringConverter(Integer.class);
	}

	@Test
	public void testGetValue() {
		IValueProperty<Object, String> property = BindingProperties.convertedValue(converter);

		assertEquals("123", property.getValue(123));
	}

	@Test
	public void testGetValueForNullSource() {
		// The converter converts null to "".
		IValueProperty<Object, String> property = BindingProperties.convertedValue(converter);

		// null should also be converted rather than simply returning null.
		assertEquals("", property.getValue(null));
	}

	@Test
	public void testSetValue() {
		IValueProperty<Object, String> property = BindingProperties.convertedValue(converter);

		assertThrows("setting a value should trigger an exception!", UnsupportedOperationException.class,
				() -> property.setValue(123, "123"));
	}

	@Test
	public void testGetValueType() {
		IValueProperty<Object, String> property = BindingProperties.convertedValue(converter);

		assertEquals(converter.getToType(), property.getValueType());
	}
}
