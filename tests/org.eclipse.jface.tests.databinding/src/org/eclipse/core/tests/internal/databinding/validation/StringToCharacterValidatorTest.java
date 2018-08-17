/*******************************************************************************
 * Copyright (c) 2007 Matt Carter and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matt Carter - initial API and implementation
 ******************************************************************************/

package org.eclipse.core.tests.internal.databinding.validation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.internal.databinding.conversion.StringToCharacterConverter;
import org.eclipse.core.internal.databinding.validation.StringToCharacterValidator;
import org.junit.Before;
import org.junit.Test;

/**
 * @since 1.1
 */
public class StringToCharacterValidatorTest {

	private StringToCharacterValidator validator;
	private StringToCharacterValidator primitiveValidator;

	@Before
	public void setUp() throws Exception {
		StringToCharacterConverter converter = StringToCharacterConverter
				.toCharacter(false);
		StringToCharacterConverter primitiveConverter = StringToCharacterConverter
				.toCharacter(true);
		validator = new StringToCharacterValidator(converter);
		primitiveValidator = new StringToCharacterValidator(primitiveConverter);
	}

	@Test
	public void testValidatesCharacter() throws Exception {
		assertTrue(validator.validate("X").isOK());
	}

	@Test
	public void testValidatesCharacterPrimitive() throws Exception {
		assertTrue(primitiveValidator.validate("X").isOK());
	}

	@Test
	public void testNullCharacterIsValid() throws Exception {
		assertTrue(validator.validate(null).isOK());
	}

	@Test
	public void testEmptyStringCharacterIsValid() throws Exception {
		assertTrue(validator.validate("").isOK());
	}

	@Test
	public void testNullCharacterIsInvalidForPrimitive() throws Exception {
		assertFalse(primitiveValidator.validate(null).isOK());
	}

	@Test
	public void testNonStringIsInvalid() throws Exception {
		assertFalse(primitiveValidator.validate(Integer.valueOf(4)).isOK());
	}

	@Test
	public void testLongerThanOneCharacterIsInvalid() throws Exception {
		assertFalse(primitiveValidator.validate("XYZ").isOK());
	}

}
