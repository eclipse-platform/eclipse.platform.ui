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

package org.eclipse.core.tests.internal.databinding.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.tests.databinding.BindingTestSetup;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.ibm.icu.text.NumberFormat;

/**
 * A test harness for testing string to number validators.
 *
 * @since 1.1
 */
public abstract class StringToNumberValidatorTestHarness {
	private NumberFormat numberFormat;
	private IValidator<Object> validator;

	@Rule
	public BindingTestSetup testSetup = new BindingTestSetup();

	@Before
	public void setUp() throws Exception {

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
	protected abstract IValidator<Object> setupValidator(NumberFormat numberFormat);

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

	@Test
	public void testInvalidValueReturnsError() throws Exception {
		IStatus status = validator.validate(getInvalidString());
		assertEquals("error severity", IStatus.ERROR, status.getSeverity());
		assertNotNull("message not null", status.getMessage());
	}

	@Test
	public void testOutOfRangeValueReturnsError() throws Exception {
		String string = numberFormat.format(getOutOfRangeNumber());
		IStatus status = validator.validate(string);
		assertEquals(IStatus.ERROR, status.getSeverity());
		assertNotNull(status.getMessage());
	}

	@Test
	public void testValidateValidValue() throws Exception {
		String string = numberFormat.format(getInRangeNumber());
		assertTrue(validator.validate(string).isOK());
	}
}
