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

package org.eclipse.core.tests.internal.databinding.validation;

import junit.framework.TestCase;

import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.conversion.StringToNumberConverter;
import org.eclipse.core.internal.databinding.validation.WrappedConverterValidator;
import org.eclipse.core.runtime.IStatus;

/**
 * @since 1.1
 */
public class WrappedConverterValidatorTest extends TestCase {
	StringToNumberConverter converter;
	ValidatorStub validator;
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		
		converter = StringToNumberConverter.toInteger(false);		
		validator = new ValidatorStub(converter);
	}
	
	public void testValidReturnsOK() throws Exception {	
		IStatus status = validator.validate("1");
		assertTrue(status.isOK());
	}
	
	public void testInvalidReturnsFailure() throws Exception {
		IStatus status = validator.validate("a");
		assertEquals(IStatus.ERROR, status.getSeverity());
	}
	
	public void testInvalidReturnMessage() throws Exception {
		String message = "error message";
		validator.message = message;
		IStatus status = validator.validate("a");
		assertEquals(message, status.getMessage());
	}
	
	static class ValidatorStub extends WrappedConverterValidator {
		String message = "";
		
		ValidatorStub(IConverter converter) {
			super(converter);
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.core.internal.databinding.validation.WrappedConverterValidator#getErrorMessage()
		 */
		protected String getErrorMessage() {
			return message;
		}		
	}
}
