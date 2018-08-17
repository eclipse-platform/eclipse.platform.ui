/*******************************************************************************
 * Copyright (c) 2007, 2009 IBM Corporation and others.
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
 *     Matthew Hall - bug 270461
 ******************************************************************************/

package org.eclipse.core.tests.databinding;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import org.eclipse.core.databinding.BindingException;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.internal.databinding.conversion.IdentityConverter;
import org.eclipse.core.internal.databinding.validation.NumberToByteValidator;
import org.eclipse.core.internal.databinding.validation.NumberToDoubleValidator;
import org.eclipse.core.internal.databinding.validation.NumberToFloatValidator;
import org.eclipse.core.internal.databinding.validation.NumberToIntegerValidator;
import org.eclipse.core.internal.databinding.validation.NumberToLongValidator;
import org.eclipse.core.internal.databinding.validation.NumberToShortValidator;
import org.eclipse.core.internal.databinding.validation.NumberToUnboundedNumberValidator;
import org.eclipse.core.internal.databinding.validation.StringToByteValidator;
import org.eclipse.core.internal.databinding.validation.StringToDateValidator;
import org.eclipse.core.internal.databinding.validation.StringToDoubleValidator;
import org.eclipse.core.internal.databinding.validation.StringToFloatValidator;
import org.eclipse.core.internal.databinding.validation.StringToIntegerValidator;
import org.eclipse.core.internal.databinding.validation.StringToLongValidator;
import org.eclipse.core.internal.databinding.validation.StringToShortValidator;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;
import org.junit.Test;

/**
 * @since 1.1
 */
public class UpdateValueStrategyTest extends AbstractDefaultRealmTestCase {
	@Test
	public void testDefaultValidatorForStringToInteger() throws Exception {
		assertDefaultValidator(String.class, Integer.class,
				StringToIntegerValidator.class);
	}

	@Test
	public void testDefaultValidatorForStringToIntegerPrimitive()
			throws Exception {
		assertDefaultValidator(String.class, Integer.TYPE,
				StringToIntegerValidator.class);
	}

	@Test
	public void testDefaultValidatorForStringToLong() throws Exception {
		assertDefaultValidator(String.class, Long.class,
				StringToLongValidator.class);
	}

	@Test
	public void testDefaultValidatorForStringToLongPrimitive() throws Exception {
		assertDefaultValidator(String.class, Long.TYPE,
				StringToLongValidator.class);
	}

	@Test
	public void testDefaultValidatorForStringToFloat() throws Exception {
		assertDefaultValidator(String.class, Float.class,
				StringToFloatValidator.class);
	}

	@Test
	public void testDefaultValidatorForStringToFloatPrimitive()
			throws Exception {
		assertDefaultValidator(String.class, Float.TYPE,
				StringToFloatValidator.class);
	}

	@Test
	public void testDefaultValidatorForStringToDouble() throws Exception {
		assertDefaultValidator(String.class, Double.class,
				StringToDoubleValidator.class);
	}

	@Test
	public void testDefaultValidatorForStringToDoublePrimitive()
			throws Exception {
		assertDefaultValidator(String.class, Double.TYPE,
				StringToDoubleValidator.class);
	}

	@Test
	public void testDefaultValidatorForStringToByte() throws Exception {
		assertDefaultValidator(String.class, Byte.class,
				StringToByteValidator.class);
	}

	@Test
	public void testDefaultValidatorForStringToBytePrimitive() throws Exception {
		assertDefaultValidator(String.class, Byte.TYPE,
				StringToByteValidator.class);
	}

	@Test
	public void testDefaultValidatorForStringToShort() throws Exception {
		assertDefaultValidator(String.class, Short.class,
				StringToShortValidator.class);
	}

	@Test
	public void testDefaultValidatorForStringToShortPrimitive()
			throws Exception {
		assertDefaultValidator(String.class, Short.TYPE,
				StringToShortValidator.class);
	}

	@Test
	public void testDefaultValidatorForStringToDate() throws Exception {
		assertDefaultValidator(String.class, Date.class,
				StringToDateValidator.class);
	}

	@Test
	public void testDefaultValidatorForNumberToByte() throws Exception {
		assertDefaultValidator(Integer.class, Byte.class,
				NumberToByteValidator.class);
	}

	@Test
	public void testDefaultValidatorForNumberToShort() throws Exception {
		assertDefaultValidator(Integer.class, Short.class,
				NumberToShortValidator.class);
	}

	@Test
	public void testDefaultValidatorForNumberToInteger() throws Exception {
		assertDefaultValidator(Short.class, Integer.class,
				NumberToIntegerValidator.class);
	}

	@Test
	public void testDefaultValidatorForNumberToLong() throws Exception {
		assertDefaultValidator(Short.class, Long.class,
				NumberToLongValidator.class);
	}

	@Test
	public void testDefaultValidatorForNumberToFloat() throws Exception {
		assertDefaultValidator(Short.class, Float.class,
				NumberToFloatValidator.class);
	}

	@Test
	public void testDefaultValidatorForNumberToDouble() throws Exception {
		assertDefaultValidator(Short.class, Double.class,
				NumberToDoubleValidator.class);
	}

	@Test
	public void testDefaultValidatorForNumberToBigInteger() throws Exception {
		assertDefaultValidator(Short.class, BigInteger.class,
				NumberToUnboundedNumberValidator.class);
	}

	@Test
	public void testDefaultValidatorForNumberToBigDecimal() throws Exception {
		assertDefaultValidator(Short.class, BigDecimal.class,
				NumberToUnboundedNumberValidator.class);
	}

	@Test
	public void testCachesDefaultedValidators() throws Exception {
		WritableValue<String> source = WritableValue.withValueType(String.class);
		WritableValue<Integer> destination = WritableValue.withValueType(Integer.class);

		UpdateValueStrategyStub<String, Integer> strategy = new UpdateValueStrategyStub<>();
		strategy.fillDefaults(source, destination);

		IValidator<String> validator = strategy.validator;
		assertNotNull(validator);

		strategy = new UpdateValueStrategyStub<>();
		strategy.fillDefaults(source, destination);

		assertSame(validator, strategy.validator);
	}

	@Test
	public void testFillDefaults_AssertSourceTypeExtendsConverterFromType() {
		// Valid use: source type String extends converter from-type Object
		UpdateValueStrategyStub<Object, Object> strategy = new UpdateValueStrategyStub<>();
		strategy.setConverter(new IdentityConverter(Object.class, Object.class));
		strategy.fillDefaults(WritableValue.withValueType(String.class),
				WritableValue.withValueType(Object.class));

		// Invalid use: source type Object does not extend converter from-type
		// String
		strategy = new UpdateValueStrategyStub<>();
		strategy.setConverter(new IdentityConverter(String.class, Object.class));
		try {
			strategy.fillDefaults(WritableValue.withValueType(Object.class),
					WritableValue.withValueType(Object.class));
			fail("Expected BindingException since Object does not extend String");
		} catch (BindingException expected) {
		}
	}

	@Test
	public void testFillDefaults_AssertConverterToTypeExtendsDestinationType() {
		// Valid use: converter to-type String extends destination type Object
		UpdateValueStrategyStub<String, Object> strategy = new UpdateValueStrategyStub<>();
		strategy.setConverter(new IdentityConverter(Object.class, String.class));
		strategy.fillDefaults(WritableValue.withValueType(Object.class),
				WritableValue.withValueType(Object.class));

		// Invalid use: converter to-type Object does not extend destination
		// type String
		strategy = new UpdateValueStrategyStub<>();
		strategy.setConverter(new IdentityConverter(Object.class, Object.class));
		try {
			strategy.fillDefaults(WritableValue.withValueType(Object.class),
					WritableValue.withValueType(String.class));
			fail("Expected BindingException since Object does not extend String");
		} catch (BindingException expected) {
		}
	}

	private void assertDefaultValidator(Class<?> fromType, Class<?> toType, Class<?> validatorType) {
		WritableValue<Object> source = WritableValue.withValueType(fromType);
		WritableValue<Object> destination = WritableValue.withValueType(toType);

		UpdateValueStrategyStub<Object, Object> strategy = new UpdateValueStrategyStub<Object, Object>();
		strategy.fillDefaults(source, destination);

		IValidator<?> validator = strategy.validator;
		assertNotNull("validator not null", validator);
		assertTrue("converter instanceof " + validatorType, validatorType.isInstance(validator));
	}

	class UpdateValueStrategyStub<S, D> extends UpdateValueStrategy<S, D> {
		IValidator<S> validator;

		@Override
		protected void fillDefaults(IObservableValue<? extends S> source, IObservableValue<? super D> destination) {
			super.fillDefaults(source, destination);
		}

		@Override
		protected IValidator<S> createValidator(Object fromType, Object toType) {
			validator = super.createValidator(fromType, toType);
			return validator;
		}
	}
}
