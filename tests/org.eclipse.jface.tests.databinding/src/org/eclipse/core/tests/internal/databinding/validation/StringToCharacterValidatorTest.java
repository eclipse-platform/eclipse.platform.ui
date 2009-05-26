/*******************************************************************************
 * Copyright (c) 2007 Matt Carter and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matt Carter - initial API and implementation
 ******************************************************************************/

package org.eclipse.core.tests.internal.databinding.validation;

import junit.framework.TestCase;

import org.eclipse.core.internal.databinding.conversion.StringToCharacterConverter;
import org.eclipse.core.internal.databinding.validation.StringToCharacterValidator;

/**
 * @since 1.1
 */
public class StringToCharacterValidatorTest extends TestCase {

	private StringToCharacterValidator validator;
	private StringToCharacterValidator primitiveValidator;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		StringToCharacterConverter converter = StringToCharacterConverter
				.toCharacter(false);
		StringToCharacterConverter primitiveConverter = StringToCharacterConverter
				.toCharacter(true);
		validator = new StringToCharacterValidator(converter);
		primitiveValidator = new StringToCharacterValidator(primitiveConverter);
	}

	public void testValidatesCharacter() throws Exception {
		assertTrue(validator.validate("X").isOK());
	}

	public void testValidatesCharacterPrimitive() throws Exception {
		assertTrue(primitiveValidator.validate("X").isOK());
	}

	public void testNullCharacterIsValid() throws Exception {
		assertTrue(validator.validate(null).isOK());
	}

	public void testEmptyStringCharacterIsValid() throws Exception {
		assertTrue(validator.validate("").isOK());
	}

	public void testNullCharacterIsInvalidForPrimitive() throws Exception {
		assertFalse(primitiveValidator.validate(null).isOK());
	}

	public void testNonStringIsInvalid() throws Exception {
		assertFalse(primitiveValidator.validate(new Integer(4)).isOK());
	}

	public void testLongerThanOneCharacterIsInvalid() throws Exception {
		assertFalse(primitiveValidator.validate("XYZ").isOK());
	}

}
