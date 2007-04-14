/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.core.tests.internal.databinding.conversion;

import junit.framework.TestCase;

import org.eclipse.core.internal.databinding.validation.ObjectToPrimitiveValidator;
import org.eclipse.core.runtime.IStatus;

/**
 * @since 3.2
 * 
 */
public class ObjectToPrimitiveValidatorTest extends TestCase {

	private ObjectToPrimitiveValidator objectToPrimitiveValidator;

	protected void setUp() throws Exception {
		this.objectToPrimitiveValidator = new ObjectToPrimitiveValidator(
				Integer.TYPE);
	}

	/**
	 * Test method for
	 * {@link org.eclipse.jface.internal.databinding.provisional.validation.ObjectToPrimitiveValidator#isValid(java.lang.Object)}.
	 */
	public void testIsValid() {
		IStatus result = this.objectToPrimitiveValidator.validate(null);
		assertEquals("The wrong validation error was found.", result
				.getMessage(), this.objectToPrimitiveValidator.getNullHint());

		result = this.objectToPrimitiveValidator.validate(new Integer(1));
		assertTrue("No validation error should be found.", result.isOK());

		result = this.objectToPrimitiveValidator.validate(new Object());
		assertEquals("The wrong validation error was found.", result
				.getMessage(), this.objectToPrimitiveValidator.getClassHint());
	}

}
