/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.core.tests.internal.databinding.conversion;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.internal.databinding.conversion.StatusToStringConverter;
import org.eclipse.core.runtime.IStatus;
import org.junit.Before;
import org.junit.Test;

/**
 * @since 1.1
 */
public class StatusToStringConverterTest {
	private StatusToStringConverter converter;

	@Before
	public void setUp() throws Exception {
		converter = new StatusToStringConverter();
	}

	@Test
	public void testConvertedValueIsMessageOfStatus() throws Exception {
		String message = "this is my message";
		IStatus status = ValidationStatus.error(message);
		assertEquals(message, converter.convert(status));
	}

	@Test
	public void testFromTypeIsIStatus() throws Exception {
		assertEquals(IStatus.class, converter.getFromType());
	}

	@Test
	public void testToTypeIsString() throws Exception {
		assertEquals(String.class, converter.getToType());
	}

	@Test
	public void testIllegalArgumentExceptionIsThrownWithNullInput() throws Exception {
		try {
			converter.convert(null);
			fail();
		} catch (IllegalArgumentException e) {
		}
	}
}
