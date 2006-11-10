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

import org.eclipse.core.databinding.validation.ObjectToPrimitiveValidator;
import org.eclipse.core.databinding.validation.ValidationError;

/**
 * @since 3.2
 *
 */
public class ObjectToPrimitiveValidatorTest extends TestCase {

	private ObjectToPrimitiveValidator objectToPrimitiveValidator;
	
	protected void setUp() throws Exception {
		this.objectToPrimitiveValidator = new ObjectToPrimitiveValidator(Integer.TYPE);
	}

	/**
	 * Test method for {@link org.eclipse.jface.internal.databinding.provisional.validation.ObjectToPrimitiveValidator#isPartiallyValid(java.lang.Object)}.
	 */
	public void testIsPartiallyValid() {
		ValidationError result = this.objectToPrimitiveValidator.isPartiallyValid(null);
		assertEquals("The wrong validation error was found.", result.message, this.objectToPrimitiveValidator.getNullHint());

		result = this.objectToPrimitiveValidator.isPartiallyValid(new Integer(1));
		assertNull("No validation error should be found.", result);
		
		result = this.objectToPrimitiveValidator.isPartiallyValid(new Object());
		assertEquals("The wrong validation error was found.", result.message, this.objectToPrimitiveValidator.getClassHint());
	}

	/**
	 * Test method for {@link org.eclipse.jface.internal.databinding.provisional.validation.ObjectToPrimitiveValidator#isValid(java.lang.Object)}.
	 */
	public void testIsValid() {
		ValidationError result = this.objectToPrimitiveValidator.isValid(null);
		assertEquals("The wrong validation error was found.", result.message, this.objectToPrimitiveValidator.getNullHint());

		result = this.objectToPrimitiveValidator.isValid(new Integer(1));
		assertNull("No validation error should be found.", result);
		
		result = this.objectToPrimitiveValidator.isValid(new Object());
		assertEquals("The wrong validation error was found.", result.message, this.objectToPrimitiveValidator.getClassHint());
	}

}
