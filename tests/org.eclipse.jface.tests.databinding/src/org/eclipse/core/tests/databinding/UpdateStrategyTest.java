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

package org.eclipse.core.tests.databinding;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.conversion.NumberToStringConverter;
import org.eclipse.core.databinding.conversion.StringToNumberConverter;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.internal.databinding.conversion.DateToStringConverter;
import org.eclipse.core.internal.databinding.conversion.IdentityConverter;
import org.eclipse.core.internal.databinding.conversion.IntegerToStringConverter;
import org.eclipse.core.internal.databinding.conversion.NumberToBigDecimalConverter;
import org.eclipse.core.internal.databinding.conversion.NumberToBigIntegerConverter;
import org.eclipse.core.internal.databinding.conversion.NumberToByteConverter;
import org.eclipse.core.internal.databinding.conversion.NumberToDoubleConverter;
import org.eclipse.core.internal.databinding.conversion.NumberToFloatConverter;
import org.eclipse.core.internal.databinding.conversion.NumberToIntegerConverter;
import org.eclipse.core.internal.databinding.conversion.NumberToLongConverter;
import org.eclipse.core.internal.databinding.conversion.NumberToShortConverter;
import org.eclipse.core.internal.databinding.conversion.StatusToStringConverter;
import org.eclipse.core.internal.databinding.conversion.StringToBooleanConverter;
import org.eclipse.core.internal.databinding.conversion.StringToBooleanPrimitiveConverter;
import org.eclipse.core.internal.databinding.conversion.StringToByteConverter;
import org.eclipse.core.internal.databinding.conversion.StringToCharacterConverter;
import org.eclipse.core.internal.databinding.conversion.StringToDateConverter;
import org.eclipse.core.internal.databinding.conversion.StringToShortConverter;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;

/**
 * @since 1.1
 */
public class UpdateStrategyTest extends AbstractDefaultRealmTestCase {
	public void testDefaultConverterForStringToInteger() throws Exception {
		assertDefaultConverter(String.class, Integer.class, StringToNumberConverter.class);
	}

	public void testDefaultConverterForStringToIntegerPrimitive() throws Exception {
		assertDefaultConverter(String.class, Integer.TYPE, StringToNumberConverter.class);
	}

	public void testDefaultConverterForStringToLong() throws Exception {
		assertDefaultConverter(String.class, Long.class, StringToNumberConverter.class);
	}

	public void testDefaultConverterForStringToLongPrimitive() throws Exception {
		assertDefaultConverter(String.class, Long.TYPE, StringToNumberConverter.class);
	}

	public void testDefaultConverterForStringToDouble() throws Exception {
		assertDefaultConverter(String.class, Double.class, StringToNumberConverter.class);
	}

	public void testDefaultConverterForStringToDoublePrimitive() throws Exception {
		assertDefaultConverter(String.class, Double.TYPE, StringToNumberConverter.class);
	}

	public void testDefaultConverterForStringToFloat() throws Exception {
		assertDefaultConverter(String.class, Float.class, StringToNumberConverter.class);
	}

	public void testDefaultConverterForStringToFloatPrimitive() throws Exception {
		assertDefaultConverter(String.class, Float.TYPE, StringToNumberConverter.class);
	}

	public void testDefaultConverterForStringToBigInteger() throws Exception {
		assertDefaultConverter(String.class, BigInteger.class, StringToNumberConverter.class);
	}

	public void testDefaultConverterForIntegerToString() throws Exception {
		assertDefaultConverter(Integer.class, String.class, NumberToStringConverter.class);
	}

	public void testDefaultConverterForIntegerPrimitiveToString() throws Exception {
		assertDefaultConverter(Integer.TYPE, String.class, NumberToStringConverter.class);
	}

	public void testDefaultConverterForLongToString() throws Exception {
		assertDefaultConverter(Long.class, String.class, NumberToStringConverter.class);
	}

	public void testDefaultConverterForLongPrimitiveToString() throws Exception {
		assertDefaultConverter(Long.TYPE, String.class, NumberToStringConverter.class);
	}

	public void testDefaultConverterForDoubleToString() throws Exception {
		assertDefaultConverter(Double.class, String.class, NumberToStringConverter.class);
	}

	public void testDefaultConverterForDoublePrimitiveToString() throws Exception {
		assertDefaultConverter(Double.TYPE, String.class, NumberToStringConverter.class);
	}

	public void testDefaultConverterForFloatToString() throws Exception {
		assertDefaultConverter(Float.class, String.class, NumberToStringConverter.class);
	}

	public void testDefaultConverterForFloatPrimitiveToString() throws Exception {
		assertDefaultConverter(Float.TYPE, String.class, NumberToStringConverter.class);
	}

	public void testDefaultConverterForBigIntegerToString() throws Exception {
		assertDefaultConverter(BigInteger.class, String.class, NumberToStringConverter.class);
	}

	public void testDefaultConverterForDateToString() throws Exception {
		assertDefaultConverter(Date.class, String.class, DateToStringConverter.class);
	}

	public void testDefaultConverterForStringToBoolean() throws Exception {
		assertDefaultConverter(String.class, Boolean.class, StringToBooleanConverter.class);
	}

	public void testDefaultConverterForStringToBooleanPrimitive() throws Exception {
		assertDefaultConverter(String.class, Boolean.TYPE, StringToBooleanPrimitiveConverter.class);
	}

	public void testDefaultConverterForStringToByte() throws Exception {
		assertDefaultConverter(String.class, Byte.class, StringToByteConverter.class);
	}

	public void testDefaultConverterForStringToBytePrimitive() throws Exception {
		assertDefaultConverter(String.class, Byte.TYPE, StringToByteConverter.class);
	}

	public void testDefaultConverterForStringToCharacter() throws Exception {
		assertDefaultConverter(String.class, Character.class, StringToCharacterConverter.class);
	}

	public void testDefaultConverterForStringToDate() throws Exception {
		assertDefaultConverter(String.class, Date.class, StringToDateConverter.class);
	}

	public void testDefaultConverterForStringToShort() throws Exception {
		assertDefaultConverter(String.class, Short.class, StringToShortConverter.class);
	}

	public void testDefaultConverterForStringToShortPrimitive() throws Exception {
		assertDefaultConverter(String.class, Short.TYPE, StringToShortConverter.class);
	}

	public void testDefaultConverterForByteToString() throws Exception {
		assertDefaultConverter(Byte.class, String.class, IntegerToStringConverter.class);
	}

	public void testDefaultConverterForBytePrimitiveToString() throws Exception {
		assertDefaultConverter(Byte.TYPE, String.class, IntegerToStringConverter.class);
	}

	public void testDefaultConverterForShortToString() throws Exception {
		assertDefaultConverter(Short.class, String.class, IntegerToStringConverter.class);
	}

	public void testDefaultConverterForShortPrimitiveToString() throws Exception {
		assertDefaultConverter(Short.TYPE, String.class, IntegerToStringConverter.class);
	}

	public void testDefaultConverterForStatusToString() throws Exception {
		assertDefaultConverter(IStatus.class, String.class, StatusToStringConverter.class);
	}


	public void testDefaultConverterForNumberToByte() throws Exception {
		assertFromNumberToNumberConverter(Byte.class, Byte.TYPE,
				NumberToByteConverter.class);
	}

	public void testDefaultConverterForNumberToShort() throws Exception {
		assertFromNumberToNumberConverter(Short.class, Short.TYPE,
				NumberToShortConverter.class);
	}

	public void testDefaultConverterForNumberToShortPrimitive()
			throws Exception {
		assertFromNumberToNumberConverter(Short.TYPE, Short.class,
				NumberToShortConverter.class);
	}

	public void testDefaultConverterForNumberToInteger() throws Exception {
		assertFromNumberToNumberConverter(Integer.class, Integer.TYPE,
				NumberToIntegerConverter.class);
	}

	public void testDefaultConverterForNumberToIntegerPrimitive()
			throws Exception {
		assertFromNumberToNumberConverter(Integer.TYPE, Integer.class,
				NumberToIntegerConverter.class);
	}

	public void testDefaultConverterForNumberToLong() throws Exception {
		assertFromNumberToNumberConverter(Long.class, Long.TYPE,
				NumberToLongConverter.class);
	}

	public void testDefaultConverterForNumberToLongPrimitive() throws Exception {
		assertFromNumberToNumberConverter(Long.TYPE, Long.class,
				NumberToLongConverter.class);
	}

	public void testDefaultConverterForNumberToFloat() throws Exception {
		assertFromNumberToNumberConverter(Float.class, Float.TYPE,
				NumberToFloatConverter.class);
	}

	public void testDefaultConverterForNumberToFloatPrimitive()
			throws Exception {
		assertFromNumberToNumberConverter(Float.TYPE, Float.class,
				NumberToFloatConverter.class);
	}

	public void testDefaultConverterForNumberToDouble() throws Exception {
		assertFromNumberToNumberConverter(Double.class, Double.TYPE,
				NumberToDoubleConverter.class);
	}

	public void testDefaultConverterForNumberToDoublePrimitive()
			throws Exception {
		assertFromNumberToNumberConverter(Double.TYPE, Double.class,
				NumberToDoubleConverter.class);
	}

	public void testDefaultConverterForNumberToBigInteger() throws Exception {
		assertFromNumberToNumberConverter(BigInteger.class, null,
				NumberToBigIntegerConverter.class);
	}

	public void testDefaultConverterForNumberToBigDecimal() throws Exception {
		assertFromNumberToNumberConverter(BigDecimal.class, null,
				NumberToBigDecimalConverter.class);
	}


	private static Class[] primitiveNumberTypes = new Class[] { Byte.TYPE,
			Short.TYPE, Integer.TYPE, Long.TYPE, Float.TYPE, Double.TYPE };

	private static Class[] boxedNumberTypes = new Class[] { Byte.class,
			Short.class, Integer.class, Long.class, Float.class, Double.class,
			BigInteger.class, BigDecimal.class };

	private void assertFromNumberToNumberConverter(Class toType,
			Class toCounterPrimitiveType, Class converterType) {

		for (int i = 0; i < primitiveNumberTypes.length; i++) {
			Class primitiveType = primitiveNumberTypes[i];

			if (!primitiveType.equals(toType)
					&& !primitiveType.equals(toCounterPrimitiveType)) {
				assertDefaultConverter(primitiveType, toType, converterType);
			} else if (!primitiveType.equals(toType)) {
				assertDefaultConverter(primitiveType, toType,
						IdentityConverter.class);
			}
		}

		for (int i = 0; i < boxedNumberTypes.length; i++) {
			Class boxedType = boxedNumberTypes[i];

			if (!boxedType.equals(toType)
					&& !boxedType.equals(toCounterPrimitiveType)) {
				assertDefaultConverter(boxedType, toType, converterType);
			} else if (!boxedType.equals(toType)) {
				assertDefaultConverter(boxedType, toType,
						IdentityConverter.class);
			}
		}
	}

	private void assertDefaultConverter(Class fromType, Class toType, Class converterType) {
		WritableValue source = WritableValue.withValueType(fromType);
		WritableValue destination = WritableValue.withValueType(toType);

		UpdateStrategyStub strategy = new UpdateStrategyStub();
		strategy.fillDefaults(source, destination);

		IConverter converter = strategy.converter;
		assertNotNull("converter not null", converter);
		assertEquals("fromType [" + fromType + "]" , fromType, converter.getFromType());
		assertEquals("toType [" + toType + "]", toType, converter.getToType());
		assertTrue("converter should be instanceof " + converterType
				+ " but was instanceof " + converter.getClass(), converterType
				.isInstance(converter));
	}

	class UpdateStrategyStub extends UpdateValueStrategy {
		IConverter converter;

		@Override
		protected void fillDefaults(IObservableValue source,
				IObservableValue destination) {
			super.fillDefaults(source, destination);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.core.databinding.UpdateValueStrategy#setConverter(org.eclipse.core.databinding.conversion.IConverter)
		 */
		@Override
		public UpdateValueStrategy setConverter(IConverter converter) {
			this.converter = converter;
			return super.setConverter(converter);
		}
	}
}
