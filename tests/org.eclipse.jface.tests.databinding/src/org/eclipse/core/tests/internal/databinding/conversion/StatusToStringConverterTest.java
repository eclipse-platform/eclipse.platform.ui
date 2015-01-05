/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.core.tests.internal.databinding.conversion;

import junit.framework.TestCase;

import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.internal.databinding.conversion.StatusToStringConverter;
import org.eclipse.core.runtime.IStatus;

/**
 * @since 1.1
 */
public class StatusToStringConverterTest extends TestCase {
	private StatusToStringConverter converter;

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();

		converter = new StatusToStringConverter();
	}

	public void testConvertedValueIsMessageOfStatus() throws Exception {
		String message = "this is my message";
		IStatus status = ValidationStatus.error(message);
		assertEquals(message, converter.convert(status));
	}

	public void testFromTypeIsIStatus() throws Exception {
		assertEquals(IStatus.class, converter.getFromType());
	}

	public void testToTypeIsString() throws Exception {
		assertEquals(String.class, converter.getToType());
	}

	public void testIllegalArgumentExceptionIsThrownWithNullInput() throws Exception {
		try {
			converter.convert(null);
			fail();
		} catch (IllegalArgumentException e) {
		}
	}
}
