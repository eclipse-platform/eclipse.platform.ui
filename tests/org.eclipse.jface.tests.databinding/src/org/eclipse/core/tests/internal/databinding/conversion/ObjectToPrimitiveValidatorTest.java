/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
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
 *******************************************************************************/

package org.eclipse.core.tests.internal.databinding.conversion;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.internal.databinding.validation.ObjectToPrimitiveValidator;
import org.eclipse.core.runtime.IStatus;
import org.junit.Before;
import org.junit.Test;

/**
 * @since 3.2
 *
 */
public class ObjectToPrimitiveValidatorTest {

	private ObjectToPrimitiveValidator objectToPrimitiveValidator;

	@Before
	public void setUp() throws Exception {
		this.objectToPrimitiveValidator = new ObjectToPrimitiveValidator(
				Integer.TYPE);
	}

	/**
	 * Test method for
	 * {@link org.eclipse.jface.internal.databinding.provisional.validation.ObjectToPrimitiveValidator#isValid(java.lang.Object)}.
	 */
	@Test
	public void testIsValid() {
		IStatus result = this.objectToPrimitiveValidator.validate(null);
		assertEquals("The wrong validation error was found.", result
				.getMessage(), this.objectToPrimitiveValidator.getNullHint());

		result = this.objectToPrimitiveValidator.validate(Integer.valueOf(1));
		assertTrue("No validation error should be found.", result.isOK());

		result = this.objectToPrimitiveValidator.validate(new Object());
		assertEquals("The wrong validation error was found.", result
				.getMessage(), this.objectToPrimitiveValidator.getClassHint());
	}

}
