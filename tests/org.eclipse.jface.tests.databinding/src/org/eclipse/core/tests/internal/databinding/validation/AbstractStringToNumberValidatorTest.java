/*******************************************************************************
 * Copyright (c) 2007 Brad Reynolds and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 ******************************************************************************/

package org.eclipse.core.tests.internal.databinding.validation;

import junit.framework.TestCase;

import org.eclipse.core.databinding.conversion.StringToNumberConverter;
import org.eclipse.core.internal.databinding.validation.AbstractStringToNumberValidator;
import org.eclipse.core.internal.databinding.validation.NumberFormatConverter;
import org.eclipse.core.runtime.IStatus;

/**
 * Tests for AbstractStringToNumberValidator. Most tests should be included in
 * StringToNumberValidatorTestHarness. This class is for the edge cases.
 *
 * @since 3.2
 */
public class AbstractStringToNumberValidatorTest extends TestCase {
	/**
	 * Test for bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=194353.
	 *
	 * @throws Exception
	 */
	public void testErrorMessagesAreNotCached() throws Exception {
		NumberFormatConverter c = StringToNumberConverter.toInteger(false);
		ValidatorStub v = new ValidatorStub(c);

		IStatus status1 = v.validate("1a");
		assertEquals(IStatus.ERROR, status1.getSeverity());

		IStatus status2 = v.validate("2b");
		assertEquals(IStatus.ERROR, status2.getSeverity());

		assertFalse("messages should not be equal", status1.getMessage().equals(status2.getMessage()));
	}

	static class ValidatorStub extends AbstractStringToNumberValidator {
		ValidatorStub(NumberFormatConverter c) {
			super(c, new Integer(Integer.MIN_VALUE), new Integer(Integer.MAX_VALUE));
		}

		@Override
		protected boolean isInRange(Number number) {
			return true;
		}
	}
}
