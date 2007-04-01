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

import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.runtime.IStatus;

import com.ibm.icu.text.NumberFormat;

/**
 * A test harness for testing string to number validators.
 * 
 * @since 1.1
 */
public abstract class StringToNumberValidatorTestHarness extends TestCase {
	private NumberFormat numberFormat;
	private IValidator validator;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();

		numberFormat = setupNumberFormat();
		validator = setupValidator(numberFormat);
	}
	
	/**
	 * Invoked during setup to instantiate the number format.
	 * 
	 * @return number format
	 */
	protected abstract NumberFormat setupNumberFormat();
	
	/**
	 * Invoked during setup to instantiate the validator.
	 * 
	 * @param numberFormat
	 * @return validator
	 */
	protected abstract IValidator setupValidator(NumberFormat numberFormat);

	/**
	 * Returns a string value that will not parse.
	 * 
	 * @return string
	 */
	protected abstract String getInvalidString();

	/**
	 * Returns a number value that is out of range for the validator.
	 * 
	 * @return number
	 */
	protected abstract Number getOutOfRangeNumber();
	
	/**
	 * Returns a number that is in range for the validator.
	 * 
	 * @return number
	 */
	protected abstract Number getInRangeNumber();
	
	public void testInvalidValueReturnsError() throws Exception {
		IStatus status = validator.validate(getInvalidString());
		assertEquals("error severify", IStatus.ERROR, status.getSeverity());
		assertNotNull("message not null", status.getMessage());
	}
	
	public void testOutOfRangeValueReturnsError() throws Exception {
		String string = numberFormat.format(getOutOfRangeNumber());
		IStatus status = validator.validate(string);
		assertEquals(IStatus.ERROR, status.getSeverity());
		assertNotNull(status.getMessage());
	}

	public void testValidateValidValue() throws Exception {
		String string = numberFormat.format(getInRangeNumber());
		assertTrue(validator.validate(string).isOK());
	}
}
