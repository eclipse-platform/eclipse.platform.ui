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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.internal.databinding.validation.NumberToNumberValidator;
import org.eclipse.core.runtime.IStatus;
import org.junit.Test;

/**
 * @since 1.1
 */
public abstract class NumberToNumberValidatorTestHarness {
	protected abstract NumberToNumberValidator doGetToPrimitiveValidator(Class<?> fromType);

	protected abstract NumberToNumberValidator doGetToBoxedTypeValidator(Class<?> fromType);
	protected abstract Number doGetOutOfRangeNumber();

	@Test
	public void testValidateNullForBoxedTypeIsOK() throws Exception {
		IStatus status = doGetToBoxedTypeValidator(Integer.class).validate(null);
		assertTrue(status.isOK());
	}

	@Test
	public void testValidateNullForPrimitiveThrowsIllegalArgumentException()
			throws Exception {
		IValidator<Object> validator = doGetToPrimitiveValidator(Integer.class);

		if (validator == null) {
			//return if a primitive validator does not exist (e.g. BigInteger, BigDecimal, etc.)
			return;
		}

		try {
			doGetToPrimitiveValidator(Integer.class).validate(null);

			fail("exception should have been thrown");
		} catch (IllegalArgumentException e) {
		}
	}

	@Test
	public void testValidReturnsOK() throws Exception {
		assertTrue(doGetToBoxedTypeValidator(Integer.class).validate(Integer.valueOf(1)).isOK());
	}

	@Test
	public void testOutOfRangeReturnsError() throws Exception {
		Number number = doGetOutOfRangeNumber();

		if (number == null) {
			//return if there is no value out of range (e.g. BigInteger, BigDecimal, etc.)
			return;
		}

		IStatus status = doGetToBoxedTypeValidator(Integer.class).validate(number);

		assertEquals(IStatus.ERROR, status.getSeverity());
		assertTrue(status.getMessage() != null);
	}

	@Test
	public void testValidateIncorrectTypeThrowsIllegalArgumentException() throws Exception {
		try {
			doGetToBoxedTypeValidator(Integer.class).validate("");
			fail("exception should have been thrown");
		} catch (IllegalArgumentException e) {
		}
	}
}
