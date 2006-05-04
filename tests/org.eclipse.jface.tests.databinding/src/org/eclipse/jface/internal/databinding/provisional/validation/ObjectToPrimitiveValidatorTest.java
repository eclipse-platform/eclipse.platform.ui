/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.provisional.validation;

import junit.framework.TestCase;

/**
 * @since 3.2
 *
 */
public class ObjectToPrimitiveValidatorTest extends TestCase {

	private ObjectToPrimativeValidator objectToPrimativeValidator;
	
	protected void setUp() throws Exception {
		this.objectToPrimativeValidator = new ObjectToPrimativeValidator(Integer.TYPE);
	}

	/**
	 * Test method for {@link org.eclipse.jface.internal.databinding.provisional.validation.ObjectToPrimativeValidator#isPartiallyValid(java.lang.Object)}.
	 */
	public void testIsPartiallyValid() {
		ValidationError result = this.objectToPrimativeValidator.isPartiallyValid(null);
		assertEquals("The wrong validation error was found.", result.message, this.objectToPrimativeValidator.getNullHint());

		result = this.objectToPrimativeValidator.isPartiallyValid(new Integer(1));
		assertNull("No validation error should be found.", result);
		
		result = this.objectToPrimativeValidator.isPartiallyValid(new Object());
		assertEquals("The wrong validation error was found.", result.message, this.objectToPrimativeValidator.getClassHint());
	}

	/**
	 * Test method for {@link org.eclipse.jface.internal.databinding.provisional.validation.ObjectToPrimativeValidator#isValid(java.lang.Object)}.
	 */
	public void testIsValid() {
		ValidationError result = this.objectToPrimativeValidator.isValid(null);
		assertEquals("The wrong validation error was found.", result.message, this.objectToPrimativeValidator.getNullHint());

		result = this.objectToPrimativeValidator.isValid(new Integer(1));
		assertNull("No validation error should be found.", result);
		
		result = this.objectToPrimativeValidator.isValid(new Object());
		assertEquals("The wrong validation error was found.", result.message, this.objectToPrimativeValidator.getClassHint());
	}

}
