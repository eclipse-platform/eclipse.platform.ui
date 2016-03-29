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

import org.eclipse.core.databinding.conversion.IConverter;

/**
 * @since 1.1
 */
public abstract class NumberToNumberTestHarness extends TestCase {

	/**
	 * Invoked when a to primitive validator is needed.
	 *
	 * @param fromType
	 * @return validator, <code>null</code> if the type does not have a primitive form
	 */
	protected abstract IConverter doGetToPrimitiveValidator(Class fromType);

	/**
	 * Invoked when a to boxed validator is needed.
	 *
	 * @param fromType
	 * @return
	 */
	protected abstract IConverter doGetToBoxedTypeValidator(Class fromType);

	/**
	 * Invoked when the type is needed.
	 *
	 * @param primitive
	 * @return type, <code>null</code> if the type does not have a primitive form
	 */
	protected abstract Class doGetToType(boolean primitive);

	/**
	 * Invoked when an out of range number is needed to use for conversion.
	 *
	 * @return out of range number of <code>null</code> if the type has no bounds
	 */
	protected abstract Number doGetOutOfRangeNumber();

	public void testFromType() throws Exception {
		Class from = Integer.class;
		assertEquals(from, doGetToBoxedTypeValidator(from).getFromType());
	}

	public void testToTypeIsPrimitive() throws Exception {
		Class toType = doGetToType(true);

		if (toType == null) {
			//return if there is no primitive type
			return;
		}
		assertEquals("to type was not of the correct type", toType, doGetToPrimitiveValidator(Integer.class)
				.getToType());
		assertTrue("to type was not primitive", toType.isPrimitive());
	}

	public void testToTypeIsBoxedType() throws Exception {
		Class toType = doGetToType(false);
		assertEquals(toType, doGetToBoxedTypeValidator(Integer.class)
				.getToType());
		assertFalse(toType.isPrimitive());
	}

	public void testValidConversion() throws Exception {
		Integer value = Integer.valueOf(1);
		Number result = (Number) doGetToBoxedTypeValidator(Integer.class)
				.convert(value);

		assertNotNull("result was null", result);

		// regardless if the converter is for the primitive value the returned
		// value will be the boxed type
		assertEquals(doGetToType(false), result.getClass());
		assertEquals(value, Integer.valueOf(result.intValue()));
	}

	public void testOutOfRangeConversion() throws Exception {
		Number outOfRange = doGetOutOfRangeNumber();

		if (outOfRange == null) {
			//the number does not have bounds (e.g. BigInteger or BigDecimal)
			return;
		}
		try {
			doGetToBoxedTypeValidator(Integer.class).convert(outOfRange);
			fail("exception should have been thrown");
		} catch (IllegalArgumentException e) {
		}
	}

	public void testConvertNullValueForPrimitiveThrowsIllegalArgumentException()
			throws Exception {
		if (doGetToType(true) == null) {
			//return if primitive is not supported

			return;
		}

		try {
			doGetToPrimitiveValidator(Integer.class).convert(null);
			fail("exception should have been thrown");
		} catch (IllegalArgumentException e) {
		}
	}

	public void testConvertNullValueForBoxedTypeReturnsNull() throws Exception {
		assertNull(doGetToBoxedTypeValidator(Integer.class).convert(null));
	}

	public void testNonNumberThrowsIllegalArgumentException() throws Exception {
		try {
			doGetToBoxedTypeValidator(Integer.class).convert("");
			fail("exception should have been thrown");
		} catch (IllegalArgumentException e) {
		}
	}
}
