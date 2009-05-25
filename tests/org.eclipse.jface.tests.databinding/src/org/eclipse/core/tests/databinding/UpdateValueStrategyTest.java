/*******************************************************************************
 * Copyright (c) 2007, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matthew Hall - bug 270461
 ******************************************************************************/

package org.eclipse.core.tests.databinding;

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

/**
 * @since 1.1
 */
public class UpdateValueStrategyTest extends AbstractDefaultRealmTestCase {
	public void testDefaultValidatorForStringToInteger() throws Exception {
		assertDefaultValidator(String.class, Integer.class,
				StringToIntegerValidator.class);
	}

	public void testDefaultValidatorForStringToIntegerPrimitive()
			throws Exception {
		assertDefaultValidator(String.class, Integer.TYPE,
				StringToIntegerValidator.class);
	}

	public void testDefaultValidatorForStringToLong() throws Exception {
		assertDefaultValidator(String.class, Long.class,
				StringToLongValidator.class);
	}

	public void testDefaultValidatorForStringToLongPrimitive() throws Exception {
		assertDefaultValidator(String.class, Long.TYPE,
				StringToLongValidator.class);
	}

	public void testDefaultValidatorForStringToFloat() throws Exception {
		assertDefaultValidator(String.class, Float.class,
				StringToFloatValidator.class);
	}

	public void testDefaultValidatorForStringToFloatPrimitive()
			throws Exception {
		assertDefaultValidator(String.class, Float.TYPE,
				StringToFloatValidator.class);
	}

	public void testDefaultValidatorForStringToDouble() throws Exception {
		assertDefaultValidator(String.class, Double.class,
				StringToDoubleValidator.class);
	}

	public void testDefaultValidatorForStringToDoublePrimitive()
			throws Exception {
		assertDefaultValidator(String.class, Double.TYPE,
				StringToDoubleValidator.class);
	}

	public void testDefaultValidatorForStringToByte() throws Exception {
		assertDefaultValidator(String.class, Byte.class,
				StringToByteValidator.class);
	}

	public void testDefaultValidatorForStringToBytePrimitive() throws Exception {
		assertDefaultValidator(String.class, Byte.TYPE,
				StringToByteValidator.class);
	}

	public void testDefaultValidatorForStringToShort() throws Exception {
		assertDefaultValidator(String.class, Short.class,
				StringToShortValidator.class);
	}

	public void testDefaultValidatorForStringToShortPrimitive()
			throws Exception {
		assertDefaultValidator(String.class, Short.TYPE,
				StringToShortValidator.class);
	}

	public void testDefaultValidatorForStringToDate() throws Exception {
		assertDefaultValidator(String.class, Date.class,
				StringToDateValidator.class);
	}

	public void testDefaultValidatorForNumberToByte() throws Exception {
		assertDefaultValidator(Integer.class, Byte.class,
				NumberToByteValidator.class);
	}

	public void testDefaultValidatorForNumberToShort() throws Exception {
		assertDefaultValidator(Integer.class, Short.class,
				NumberToShortValidator.class);
	}

	public void testDefaultValidatorForNumberToInteger() throws Exception {
		assertDefaultValidator(Short.class, Integer.class,
				NumberToIntegerValidator.class);
	}

	public void testDefaultValidatorForNumberToLong() throws Exception {
		assertDefaultValidator(Short.class, Long.class,
				NumberToLongValidator.class);
	}

	public void testDefaultValidatorForNumberToFloat() throws Exception {
		assertDefaultValidator(Short.class, Float.class,
				NumberToFloatValidator.class);
	}

	public void testDefaultValidatorForNumberToDouble() throws Exception {
		assertDefaultValidator(Short.class, Double.class,
				NumberToDoubleValidator.class);
	}

	public void testDefaultValidatorForNumberToBigInteger() throws Exception {
		assertDefaultValidator(Short.class, BigInteger.class,
				NumberToUnboundedNumberValidator.class);
	}

	public void testDefaultValidatorForNumberToBigDecimal() throws Exception {
		assertDefaultValidator(Short.class, BigDecimal.class,
				NumberToUnboundedNumberValidator.class);
	}

	public void testCachesDefaultedValidators() throws Exception {
		WritableValue source = WritableValue.withValueType(String.class);
		WritableValue destination = WritableValue.withValueType(Integer.class);

		UpdateValueStrategyStub strategy = new UpdateValueStrategyStub();
		strategy.fillDefaults(source, destination);

		IValidator validator = strategy.validator;
		assertNotNull(validator);

		strategy = new UpdateValueStrategyStub();
		strategy.fillDefaults(source, destination);

		assertSame(validator, strategy.validator);
	}

	public void testFillDefaults_AssertSourceTypeExtendsConverterFromType() {
		// Valid use: source type String extends converter from-type Object
		UpdateValueStrategyStub strategy = new UpdateValueStrategyStub();
		strategy
				.setConverter(new IdentityConverter(Object.class, Object.class));
		strategy.fillDefaults(WritableValue.withValueType(String.class),
				WritableValue.withValueType(Object.class));

		// Invalid use: source type Object does not extend converter from-type
		// String
		strategy = new UpdateValueStrategyStub();
		strategy
				.setConverter(new IdentityConverter(String.class, Object.class));
		try {
			strategy.fillDefaults(WritableValue.withValueType(Object.class),
					WritableValue.withValueType(Object.class));
			fail("Expected BindingException since Object does not extend String");
		} catch (BindingException expected) {
		}
	}

	public void testFillDefaults_AssertConverterToTypeExtendsDestinationType() {
		// Valid use: converter to-type String extends destination type Object
		UpdateValueStrategyStub strategy = new UpdateValueStrategyStub();
		strategy
				.setConverter(new IdentityConverter(Object.class, String.class));
		strategy.fillDefaults(WritableValue.withValueType(Object.class),
				WritableValue.withValueType(Object.class));

		// Invalid use: converter to-type Object does not extend destination
		// type String
		strategy = new UpdateValueStrategyStub();
		strategy
				.setConverter(new IdentityConverter(Object.class, Object.class));
		try {
			strategy.fillDefaults(WritableValue.withValueType(Object.class),
					WritableValue.withValueType(String.class));
			fail("Expected BindingException since Object does not extend String");
		} catch (BindingException expected) {
		}
	}

	private void assertDefaultValidator(Class fromType, Class toType,
			Class validatorType) {
		WritableValue source = WritableValue.withValueType(fromType);
		WritableValue destination = WritableValue.withValueType(toType);

		UpdateValueStrategyStub strategy = new UpdateValueStrategyStub();
		strategy.fillDefaults(source, destination);

		IValidator validator = strategy.validator;
		assertNotNull("validator not null", validator);
		assertTrue("converter instanceof " + validatorType, validatorType
				.isInstance(validator));
	}

	class UpdateValueStrategyStub extends UpdateValueStrategy {
		IValidator validator;

		protected void fillDefaults(IObservableValue source,
				IObservableValue destination) {
			super.fillDefaults(source, destination);
		}

		protected IValidator createValidator(Object fromType, Object toType) {
			validator = super.createValidator(fromType, toType);
			return validator;
		}
	}
}
