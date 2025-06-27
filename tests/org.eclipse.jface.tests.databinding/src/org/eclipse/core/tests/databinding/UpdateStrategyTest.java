/*******************************************************************************
 * Copyright (c) 2007, 2025 IBM Corporation and others.
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

package org.eclipse.core.tests.databinding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.conversion.text.NumberToStringConverter;
import org.eclipse.core.databinding.conversion.text.StringToNumberConverter;
import org.eclipse.core.databinding.observable.value.AbstractObservableValue;
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
import org.junit.Test;

/**
 * @since 1.1
 */
public class UpdateStrategyTest extends AbstractDefaultRealmTestCase {
	@Test
	public void testDefaultConverterForStringToInteger() throws Exception {
		assertDefaultConverter(String.class, Integer.class, StringToNumberConverter.class);
	}

	@Test
	public void testDefaultConverterForStringToIntegerPrimitive() throws Exception {
		assertDefaultConverter(String.class, Integer.TYPE, StringToNumberConverter.class);
	}

	@Test
	public void testDefaultConverterForStringToLong() throws Exception {
		assertDefaultConverter(String.class, Long.class, StringToNumberConverter.class);
	}

	@Test
	public void testDefaultConverterForStringToLongPrimitive() throws Exception {
		assertDefaultConverter(String.class, Long.TYPE, StringToNumberConverter.class);
	}

	@Test
	public void testDefaultConverterForStringToDouble() throws Exception {
		assertDefaultConverter(String.class, Double.class, StringToNumberConverter.class);
	}

	@Test
	public void testDefaultConverterForStringToDoublePrimitive() throws Exception {
		assertDefaultConverter(String.class, Double.TYPE, StringToNumberConverter.class);
	}

	@Test
	public void testDefaultConverterForStringToFloat() throws Exception {
		assertDefaultConverter(String.class, Float.class, StringToNumberConverter.class);
	}

	@Test
	public void testDefaultConverterForStringToFloatPrimitive() throws Exception {
		assertDefaultConverter(String.class, Float.TYPE, StringToNumberConverter.class);
	}

	@Test
	public void testDefaultConverterForStringToBigInteger() throws Exception {
		assertDefaultConverter(String.class, BigInteger.class, StringToNumberConverter.class);
	}

	@Test
	public void testDefaultConverterForIntegerToString() throws Exception {
		assertDefaultConverter(Integer.class, String.class, NumberToStringConverter.class);
	}

	@Test
	public void testDefaultConverterForIntegerPrimitiveToString() throws Exception {
		assertDefaultConverter(Integer.TYPE, String.class, NumberToStringConverter.class);
	}

	@Test
	public void testDefaultConverterForLongToString() throws Exception {
		assertDefaultConverter(Long.class, String.class, NumberToStringConverter.class);
	}

	@Test
	public void testDefaultConverterForLongPrimitiveToString() throws Exception {
		assertDefaultConverter(Long.TYPE, String.class, NumberToStringConverter.class);
	}

	@Test
	public void testDefaultConverterForDoubleToString() throws Exception {
		assertDefaultConverter(Double.class, String.class, NumberToStringConverter.class);
	}

	@Test
	public void testDefaultConverterForDoublePrimitiveToString() throws Exception {
		assertDefaultConverter(Double.TYPE, String.class, NumberToStringConverter.class);
	}

	@Test
	public void testDefaultConverterForFloatToString() throws Exception {
		assertDefaultConverter(Float.class, String.class, NumberToStringConverter.class);
	}

	@Test
	public void testDefaultConverterForFloatPrimitiveToString() throws Exception {
		assertDefaultConverter(Float.TYPE, String.class, NumberToStringConverter.class);
	}

	@Test
	public void testDefaultConverterForBigIntegerToString() throws Exception {
		assertDefaultConverter(BigInteger.class, String.class, NumberToStringConverter.class);
	}

	@Test
	public void testDefaultConverterForDateToString() throws Exception {
		assertDefaultConverter(Date.class, String.class, DateToStringConverter.class);
	}

	@Test
	public void testDefaultConverterForStringToBoolean() throws Exception {
		assertDefaultConverter(String.class, Boolean.class, StringToBooleanConverter.class);
	}

	@Test
	public void testDefaultConverterForStringToBooleanPrimitive() throws Exception {
		assertDefaultConverter(String.class, Boolean.TYPE, StringToBooleanPrimitiveConverter.class);
	}

	@Test
	public void testDefaultConverterForStringToByte() throws Exception {
		assertDefaultConverter(String.class, Byte.class, StringToByteConverter.class);
	}

	@Test
	public void testDefaultConverterForStringToBytePrimitive() throws Exception {
		assertDefaultConverter(String.class, Byte.TYPE, StringToByteConverter.class);
	}

	@Test
	public void testDefaultConverterForStringToCharacter() throws Exception {
		assertDefaultConverter(String.class, Character.class, StringToCharacterConverter.class);
	}

	@Test
	public void testDefaultConverterForStringToDate() throws Exception {
		assertDefaultConverter(String.class, Date.class, StringToDateConverter.class);
	}

	@Test
	public void testDefaultConverterForStringToShort() throws Exception {
		assertDefaultConverter(String.class, Short.class, StringToShortConverter.class);
	}

	@Test
	public void testDefaultConverterForStringToShortPrimitive() throws Exception {
		assertDefaultConverter(String.class, Short.TYPE, StringToShortConverter.class);
	}

	@Test
	public void testDefaultConverterForByteToString() throws Exception {
		assertDefaultConverter(Byte.class, String.class, IntegerToStringConverter.class);
	}

	@Test
	public void testDefaultConverterForBytePrimitiveToString() throws Exception {
		assertDefaultConverter(Byte.TYPE, String.class, IntegerToStringConverter.class);
	}

	@Test
	public void testDefaultConverterForShortToString() throws Exception {
		assertDefaultConverter(Short.class, String.class, IntegerToStringConverter.class);
	}

	@Test
	public void testDefaultConverterForShortPrimitiveToString() throws Exception {
		assertDefaultConverter(Short.TYPE, String.class, IntegerToStringConverter.class);
	}

	@Test
	public void testDefaultConverterForStatusToString() throws Exception {
		assertDefaultConverter(IStatus.class, String.class, StatusToStringConverter.class);
	}


	@Test
	public void testDefaultConverterForNumberToByte() throws Exception {
		assertFromNumberToNumberConverter(Byte.class, Byte.TYPE,
				NumberToByteConverter.class);
	}

	@Test
	public void testDefaultConverterForNumberToShort() throws Exception {
		assertFromNumberToNumberConverter(Short.class, Short.TYPE,
				NumberToShortConverter.class);
	}

	@Test
	public void testDefaultConverterForNumberToShortPrimitive()
			throws Exception {
		assertFromNumberToNumberConverter(Short.TYPE, Short.class,
				NumberToShortConverter.class);
	}

	@Test
	public void testDefaultConverterForNumberToInteger() throws Exception {
		assertFromNumberToNumberConverter(Integer.class, Integer.TYPE,
				NumberToIntegerConverter.class);
	}

	@Test
	public void testDefaultConverterForNumberToIntegerPrimitive()
			throws Exception {
		assertFromNumberToNumberConverter(Integer.TYPE, Integer.class,
				NumberToIntegerConverter.class);
	}

	@Test
	public void testDefaultConverterForNumberToLong() throws Exception {
		assertFromNumberToNumberConverter(Long.class, Long.TYPE,
				NumberToLongConverter.class);
	}

	@Test
	public void testDefaultConverterForNumberToLongPrimitive() throws Exception {
		assertFromNumberToNumberConverter(Long.TYPE, Long.class,
				NumberToLongConverter.class);
	}

	@Test
	public void testDefaultConverterForNumberToFloat() throws Exception {
		assertFromNumberToNumberConverter(Float.class, Float.TYPE,
				NumberToFloatConverter.class);
	}

	@Test
	public void testDefaultConverterForNumberToFloatPrimitive()
			throws Exception {
		assertFromNumberToNumberConverter(Float.TYPE, Float.class,
				NumberToFloatConverter.class);
	}

	@Test
	public void testDefaultConverterForNumberToDouble() throws Exception {
		assertFromNumberToNumberConverter(Double.class, Double.TYPE,
				NumberToDoubleConverter.class);
	}

	@Test
	public void testDefaultConverterForNumberToDoublePrimitive()
			throws Exception {
		assertFromNumberToNumberConverter(Double.TYPE, Double.class,
				NumberToDoubleConverter.class);
	}

	@Test
	public void testDefaultConverterForNumberToBigInteger() throws Exception {
		assertFromNumberToNumberConverter(BigInteger.class, null,
				NumberToBigIntegerConverter.class);
	}

	@Test
	public void testDefaultConverterForNumberToBigDecimal() throws Exception {
		assertFromNumberToNumberConverter(BigDecimal.class, null,
				NumberToBigDecimalConverter.class);
	}


	private static Class<?>[] primitiveNumberTypes = new Class[] { Byte.TYPE,
			Short.TYPE, Integer.TYPE, Long.TYPE, Float.TYPE, Double.TYPE };

	private static Class<?>[] boxedNumberTypes = new Class[] { Byte.class,
			Short.class, Integer.class, Long.class, Float.class, Double.class,
			BigInteger.class, BigDecimal.class };

	private void assertFromNumberToNumberConverter(Class<?> toType, Class<?> toCounterPrimitiveType,
			Class<?> converterType) {

		for (Class<?> primitiveType : primitiveNumberTypes) {
			if (!primitiveType.equals(toType) && !primitiveType.equals(toCounterPrimitiveType)) {
				assertDefaultConverter(primitiveType, toType, converterType);
			} else if (!primitiveType.equals(toType)) {
				assertDefaultConverter(primitiveType, toType, IdentityConverter.class);
			}
		}
		for (Class<?> boxedType : boxedNumberTypes) {
			if (!boxedType.equals(toType) && !boxedType.equals(toCounterPrimitiveType)) {
				assertDefaultConverter(boxedType, toType, converterType);
			} else if (!boxedType.equals(toType)) {
				assertDefaultConverter(boxedType, toType, IdentityConverter.class);
			}
		}
	}

	private void assertDefaultConverter(Class<?> fromType, Class<?> toType, Class<?> converterType) {
		WritableValue<Object> source = WritableValue.withValueType(fromType);
		WritableValue<Object> destination = WritableValue.withValueType(toType);

		UpdateStrategyStub<Object, Object> strategy = new UpdateStrategyStub<>();
		strategy.fillDefaults(source, destination);

		IConverter<?, ?> converter = strategy.conv;
		assertNotNull("converter not null", converter);
		assertEquals("fromType [" + fromType + "]" , fromType, converter.getFromType());
		assertEquals("toType [" + toType + "]", toType, converter.getToType());
		assertTrue("converter should be instanceof " + converterType
				+ " but was instanceof " + converter.getClass(), converterType
				.isInstance(converter));
	}

	class UpdateStrategyStub<S, D> extends UpdateValueStrategy<S, D> {
		IConverter<?, ?> conv;

		@Override
		protected void fillDefaults(IObservableValue<? extends S> source, IObservableValue<? super D> destination) {
			super.fillDefaults(source, destination);
		}

		@Override
		public UpdateValueStrategy<S, D> setConverter(IConverter<? super S, ? extends D> converter) {
			this.conv = converter;
			return super.setConverter(converter);
		}
	}

	@Test
	public void testDefaultConverterWithTypeErasure() {
		WritableValue<Set<?>> source = WritableValue.withValueType(Set.class);
		WritableValue<List<?>> destination = WritableValue.withValueType(List.class);

		UpdateStrategyStub<Set<?>, List<?>> strategy = new UpdateStrategyStub<>();
		strategy.fillDefaults(source, destination);

		RuntimeException ex = assertThrows(RuntimeException.class, () -> strategy.convert(new HashSet<>()));
		assertTrue("Type erasure was missed", ex.getCause() instanceof ClassCastException);
	}

	// https://github.com/eclipse-platform/eclipse.platform.ui/pull/3009#issuecomment-3012956414
	@Test
	public void testDefaultConverterWithPrimitiveTypeAsDestinationAndNonClassTypeAsSourceType() {
		IObservableValue<Boolean> source = new AbstractObservableValue() {
			@Override
			public Object getValueType() {
				return new Object(); // non-class type, happens e.g. in EMF databinding
			}

			@Override
			protected Object doGetValue() {
				return Boolean.TRUE;
			}
		};
		WritableValue<Boolean> destination = WritableValue.withValueType(boolean.class);

		UpdateStrategyStub<Object, Boolean> strategy = new UpdateStrategyStub<>();
		strategy.fillDefaults(source, destination);

		Boolean result = strategy.convert(source.getValue());
		assertTrue(result);
	}
}
