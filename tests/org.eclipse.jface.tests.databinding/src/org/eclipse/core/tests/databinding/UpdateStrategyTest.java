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

import java.math.BigInteger;
import java.util.Date;

import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.conversion.NumberToStringConverter;
import org.eclipse.core.databinding.conversion.StringToNumberConverter;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.internal.databinding.conversion.DateToStringConverter;
import org.eclipse.core.internal.databinding.conversion.IntegerToStringConverter;
import org.eclipse.core.internal.databinding.conversion.StringToBooleanConverter;
import org.eclipse.core.internal.databinding.conversion.StringToBooleanPrimitiveConverter;
import org.eclipse.core.internal.databinding.conversion.StringToByteConverter;
import org.eclipse.core.internal.databinding.conversion.StringToCharacterConverter;
import org.eclipse.core.internal.databinding.conversion.StringToDateConverter;
import org.eclipse.core.internal.databinding.conversion.StringToShortConverter;
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
	
	private void assertDefaultConverter(Class fromType, Class toType, Class converterType) {
		WritableValue source = WritableValue.withValueType(fromType);
		WritableValue destination = WritableValue.withValueType(toType);
		
		UpdateStrategyStub strategy = new UpdateStrategyStub();
		strategy.fillDefaults(source, destination);
		
		IConverter converter = strategy.converter;
		assertNotNull("converter not null", converter);
		assertEquals("fromType [" + fromType + "]" , fromType, converter.getFromType());
		assertEquals("toType [" + toType + "]", toType, converter.getToType());
		assertTrue("converter instanceof " + converterType, converterType.isInstance(converter));
	}
	
	class UpdateStrategyStub extends UpdateValueStrategy {
		IConverter converter;
		
		protected void fillDefaults(IObservableValue source,
				IObservableValue destination) {
			super.fillDefaults(source, destination);
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.core.databinding.UpdateValueStrategy#setConverter(org.eclipse.core.databinding.conversion.IConverter)
		 */
		public UpdateValueStrategy setConverter(IConverter converter) {
			this.converter = converter;
			return super.setConverter(converter);
		}
	}
}
