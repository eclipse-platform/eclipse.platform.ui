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
 *     Matt Carter - bug 180392
 *                 - bug 197679 (Character support completed)
 *******************************************************************************/

package org.eclipse.core.databinding;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.Format;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.conversion.text.NumberToStringConverter;
import org.eclipse.core.databinding.conversion.text.StringToNumberConverter;
import org.eclipse.core.databinding.util.Policy;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.internal.databinding.Activator;
import org.eclipse.core.internal.databinding.BindingMessages;
import org.eclipse.core.internal.databinding.ClassLookupSupport;
import org.eclipse.core.internal.databinding.Pair;
import org.eclipse.core.internal.databinding.conversion.CharacterToStringConverter;
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
import org.eclipse.core.internal.databinding.conversion.ObjectToStringConverter;
import org.eclipse.core.internal.databinding.conversion.StringToByteConverter;
import org.eclipse.core.internal.databinding.conversion.StringToCharacterConverter;
import org.eclipse.core.internal.databinding.conversion.StringToNumberParser;
import org.eclipse.core.internal.databinding.conversion.StringToShortConverter;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * @since 1.0
 */
/* package */class UpdateStrategy<S, D> {

	private static final String STATUS = "org.eclipse.core.runtime.IStatus"; //$NON-NLS-1$
	private static final String JAVA_MATH_BIGINTEGER = "java.math.BigInteger"; //$NON-NLS-1$
	private static final String JAVA_LANG_BOOLEAN = "java.lang.Boolean"; //$NON-NLS-1$
	private static final String JAVA_UTIL_DATE = "java.util.Date"; //$NON-NLS-1$
	private static final String JAVA_MATH_BIGDECIMAL = "java.math.BigDecimal"; //$NON-NLS-1$
	private static final String JAVA_LANG_BYTE = "java.lang.Byte"; //$NON-NLS-1$
	private static final String JAVA_LANG_OBJECT = "java.lang.Object"; //$NON-NLS-1$
	private static final String JAVA_LANG_CHARACTER = "java.lang.Character"; //$NON-NLS-1$
	private static final String JAVA_LANG_LONG = "java.lang.Long"; //$NON-NLS-1$
	private static final String JAVA_LANG_INTEGER = "java.lang.Integer"; //$NON-NLS-1$
	private static final String JAVA_LANG_STRING = "java.lang.String"; //$NON-NLS-1$
	private static final String JAVA_LANG_DOUBLE = "java.lang.Double"; //$NON-NLS-1$
	private static final String JAVA_LANG_SHORT = "java.lang.Short"; //$NON-NLS-1$
	private static final String JAVA_LANG_FLOAT = "java.lang.Float"; //$NON-NLS-1$

	private static final String BOOLEAN_CLASS = "boolean.class"; //$NON-NLS-1$
	private static final String SHORT_CLASS = "short.class"; //$NON-NLS-1$
	private static final String BYTE_CLASS = "byte.class"; //$NON-NLS-1$
	private static final String DOUBLE_CLASS = "double.class"; //$NON-NLS-1$
	private static final String FLOAT_CLASS = "float.class"; //$NON-NLS-1$
	private static final String INTEGER_CLASS = "int.class"; //$NON-NLS-1$
	private static final String LONG_CLASS = "long.class"; //$NON-NLS-1$
	private static final String CHARACTER_CLASS = "char.class"; //$NON-NLS-1$

	private static Map<Pair, Object> converterMap;

	private static Class<?> autoboxed(Class<?> clazz) {
		if (clazz == Float.TYPE)
			return Float.class;
		else if (clazz == Double.TYPE)
			return Double.class;
		else if (clazz == Short.TYPE)
			return Short.class;
		else if (clazz == Integer.TYPE)
			return Integer.class;
		else if (clazz == Long.TYPE)
			return Long.class;
		else if (clazz == Byte.TYPE)
			return Byte.class;
		else if (clazz == Boolean.TYPE)
			return Boolean.class;
		else if (clazz == Character.TYPE)
			return Character.class;
		return clazz;
	}

	protected IConverter<? super S, ? extends D> converter;

	final protected void checkAssignable(Object toType, Object fromType,
			String errorString) {
		Boolean assignableFromModelToModelConverter = isAssignableFromTo(
				fromType, toType);
		if (assignableFromModelToModelConverter != null
				&& !assignableFromModelToModelConverter.booleanValue()) {
			throw new BindingException(errorString
					+ " Expected: " + fromType + ", actual: " + toType); //$NON-NLS-1$//$NON-NLS-2$
		}
	}

	/**
	 * Tries to create a converter that can convert from values of type fromType.
	 * Returns <code>null</code> if no converter could be created. Either toType or
	 * modelDescription can be <code>null</code>, but not both.
	 *
	 * @param fromType source type
	 * @param toType   target type
	 * @return an IConverter, or <code>null</code> if unsuccessful
	 */
	protected IConverter<?, ?> createConverter(Object fromType, Object toType) {
		if (!(fromType instanceof Class) || !(toType instanceof Class)) {
			return new DefaultConverter(fromType, toType);
		}
		Class<?> toClass = (Class<?>) toType;
		Class<?> originalToClass = toClass;
		if (toClass.isPrimitive()) {
			toClass = autoboxed(toClass);
		}
		Class<?> fromClass = (Class<?>) fromType;
		Class<?> originalFromClass = fromClass;
		if (fromClass.isPrimitive()) {
			fromClass = autoboxed(fromClass);
		}
		if (!((Class<?>) toType).isPrimitive() && toClass.isAssignableFrom(fromClass)) {
			return new IdentityConverter(originalFromClass, originalToClass);
		}
		if (((Class<?>) fromType).isPrimitive() && ((Class<?>) toType).isPrimitive() && fromType.equals(toType)) {
			return new IdentityConverter(originalFromClass, originalToClass);
		}
		Map<Pair, Object> converterMap = getConverterMap();
		Class<?>[] supertypeHierarchyFlattened = ClassLookupSupport.getTypeHierarchyFlattened(fromClass);
		for (Class<?> currentFromClass : supertypeHierarchyFlattened) {
			if (currentFromClass == toType) {
				// converting to toType is just a widening
				return new IdentityConverter(fromClass, toClass);
			}
			Pair key = new Pair(getKeyForClass(fromType, currentFromClass), getKeyForClass(toType, toClass));
			Object converterOrClassname = converterMap.get(key);
			if (converterOrClassname instanceof IConverter) {
				return (IConverter<?, ?>) converterOrClassname;
			} else if (converterOrClassname instanceof String) {
				String classname = (String) converterOrClassname;
				Class<?> converterClass;
				try {
					converterClass = Class.forName(classname);
					IConverter<?, ?> result = (IConverter<?, ?>) converterClass.getDeclaredConstructor().newInstance();
					converterMap.put(key, result);
					return result;
				} catch (Exception e) {
					Policy
							.getLog()
							.log(
									new Status(
											IStatus.ERROR,
											Policy.JFACE_DATABINDING,
											0,
											"Error while instantiating default converter", e)); //$NON-NLS-1$
				}
			}
		}
		// Since we found no converter yet, try a "downcast" converter;
		// the IdentityConverter will automatically check the actual types at
		// runtime.
		if (fromClass.isAssignableFrom(toClass)) {
			return new IdentityConverter(originalFromClass, originalToClass);
		}
		return new DefaultConverter(fromType, toType);
	}

	private synchronized static Map<Pair, Object> getConverterMap() {
		// using string-based lookup avoids loading of too many classes
		if (converterMap == null) {
			// NumberFormats to be shared across converters for the formatting of
			// integer values, non-integer, big-integer and big non-integer numbers
			Format integerFormat = StringToNumberParser.getDefaultIntegerFormat();
			Format numberFormat = StringToNumberParser.getDefaultNumberFormat();
			Format bigIntegerFormat = StringToNumberParser.getDefaultIntegerBigDecimalFormat();
			Format bigNumberFormat = StringToNumberParser.getDefaultBigDecimalFormat();

			converterMap = new HashMap<>();
			// Standard and Boxed Types
			converterMap
					.put(
							new Pair(JAVA_UTIL_DATE, JAVA_LANG_STRING), "org.eclipse.core.internal.databinding.conversion.DateToStringConverter"); //$NON-NLS-1$
			converterMap
					.put(
							new Pair(JAVA_LANG_STRING, JAVA_LANG_BOOLEAN), "org.eclipse.core.internal.databinding.conversion.StringToBooleanConverter"); //$NON-NLS-1$
			converterMap
					.put(
							new Pair(JAVA_LANG_STRING, JAVA_LANG_BYTE), StringToByteConverter.toByte(integerFormat, false));
			converterMap
					.put(
							new Pair(JAVA_LANG_STRING, JAVA_UTIL_DATE), "org.eclipse.core.internal.databinding.conversion.StringToDateConverter"); //$NON-NLS-1$
			converterMap
					.put(
							new Pair(JAVA_LANG_STRING, JAVA_LANG_SHORT), StringToShortConverter.toShort(integerFormat, false));
			converterMap
					.put(
							new Pair(JAVA_LANG_STRING, JAVA_LANG_CHARACTER), StringToCharacterConverter.toCharacter(false));
			converterMap
					.put(
							new Pair(JAVA_LANG_STRING, JAVA_LANG_INTEGER), StringToNumberConverter.toInteger(integerFormat, false));
			converterMap
					.put(
							new Pair(JAVA_LANG_STRING, JAVA_LANG_DOUBLE), StringToNumberConverter.toDouble(numberFormat, false));
			converterMap
					.put(
							new Pair(JAVA_LANG_STRING, JAVA_LANG_LONG), StringToNumberConverter.toLong(integerFormat, false));
			converterMap
					.put(
							new Pair(JAVA_LANG_STRING, JAVA_LANG_FLOAT), StringToNumberConverter.toFloat(numberFormat, false));
			converterMap
					.put(
							new Pair(JAVA_LANG_STRING, JAVA_MATH_BIGINTEGER), StringToNumberConverter.toBigInteger(bigIntegerFormat));
			converterMap
					.put(
							new Pair(JAVA_LANG_STRING, JAVA_MATH_BIGDECIMAL), StringToNumberConverter.toBigDecimal(bigNumberFormat));
			converterMap
					.put(
							new Pair(JAVA_LANG_INTEGER, JAVA_LANG_STRING), NumberToStringConverter.fromInteger(integerFormat, false));
			converterMap
					.put(
							new Pair(JAVA_LANG_LONG, JAVA_LANG_STRING), NumberToStringConverter.fromLong(integerFormat, false));
			converterMap
					.put(
							new Pair(JAVA_LANG_DOUBLE, JAVA_LANG_STRING), NumberToStringConverter.fromDouble(numberFormat, false));
			converterMap
					.put(
							new Pair(JAVA_LANG_FLOAT, JAVA_LANG_STRING), NumberToStringConverter.fromFloat(numberFormat, false));
			converterMap
					.put(
							new Pair(JAVA_MATH_BIGINTEGER, JAVA_LANG_STRING), NumberToStringConverter.fromBigInteger(bigIntegerFormat));
			converterMap
					.put(
							new Pair(JAVA_MATH_BIGDECIMAL, JAVA_LANG_STRING), NumberToStringConverter.fromBigDecimal(bigNumberFormat));
			converterMap
					.put(
							new Pair(JAVA_LANG_BYTE, JAVA_LANG_STRING), IntegerToStringConverter.fromByte(integerFormat, false));
			converterMap
					.put(
							new Pair(JAVA_LANG_SHORT, JAVA_LANG_STRING), IntegerToStringConverter.fromShort(integerFormat, false));
			converterMap
					.put(
							new Pair(JAVA_LANG_CHARACTER, JAVA_LANG_STRING), CharacterToStringConverter.fromCharacter(false));

			converterMap
					.put(
							new Pair(JAVA_LANG_OBJECT, JAVA_LANG_STRING), "org.eclipse.core.internal.databinding.conversion.ObjectToStringConverter"); //$NON-NLS-1$

			// Integer.class
			converterMap
					.put(
							new Pair(JAVA_LANG_STRING, INTEGER_CLASS), StringToNumberConverter.toInteger(integerFormat, true));
			converterMap
					.put(
							new Pair(INTEGER_CLASS, JAVA_LANG_INTEGER), new IdentityConverter(Integer.class, Integer.class));
			converterMap
					.put(
							new Pair(INTEGER_CLASS, JAVA_LANG_OBJECT), new IdentityConverter(Integer.class, Object.class));
			converterMap
					.put(
							new Pair(INTEGER_CLASS, JAVA_LANG_STRING), NumberToStringConverter.fromInteger(integerFormat, true));

			// Byte.class
			converterMap
					.put(
							new Pair(JAVA_LANG_STRING, BYTE_CLASS), StringToByteConverter.toByte(integerFormat, true));
			converterMap
					.put(
							new Pair(BYTE_CLASS, JAVA_LANG_BYTE), new IdentityConverter(Byte.class, Byte.class));
			converterMap
					.put(
							new Pair(BYTE_CLASS, JAVA_LANG_STRING), IntegerToStringConverter.fromByte(integerFormat, true));
			converterMap
					.put(
							new Pair(BYTE_CLASS, JAVA_LANG_OBJECT), new IdentityConverter(Byte.class, Object.class));

			// Double.class
			converterMap
					.put(
							new Pair(JAVA_LANG_STRING, DOUBLE_CLASS), StringToNumberConverter.toDouble(numberFormat, true));
			converterMap
					.put(
							new Pair(DOUBLE_CLASS, JAVA_LANG_STRING), NumberToStringConverter.fromDouble(numberFormat, true));

			converterMap
					.put(
							new Pair(DOUBLE_CLASS, JAVA_LANG_DOUBLE), new IdentityConverter(Double.class, Double.class));
			converterMap
					.put(
							new Pair(DOUBLE_CLASS, JAVA_LANG_OBJECT), new IdentityConverter(Double.class, Object.class));

			// Boolean.class
			converterMap
					.put(
							new Pair(JAVA_LANG_STRING, BOOLEAN_CLASS), "org.eclipse.core.internal.databinding.conversion.StringToBooleanPrimitiveConverter"); //$NON-NLS-1$
			converterMap
					.put(
							new Pair(BOOLEAN_CLASS, JAVA_LANG_BOOLEAN), new IdentityConverter(Boolean.class, Boolean.class));
			converterMap
					.put(
							new Pair(BOOLEAN_CLASS, JAVA_LANG_STRING), new ObjectToStringConverter(Boolean.class));
			converterMap
					.put(
							new Pair(BOOLEAN_CLASS, JAVA_LANG_OBJECT), new IdentityConverter(Boolean.class, Object.class));

			// Float.class
			converterMap
					.put(
							new Pair(JAVA_LANG_STRING, FLOAT_CLASS), StringToNumberConverter.toFloat(numberFormat, true));
			converterMap
					.put(
							new Pair(FLOAT_CLASS, JAVA_LANG_STRING), NumberToStringConverter.fromFloat(numberFormat, true));
			converterMap
					.put(
							new Pair(FLOAT_CLASS, JAVA_LANG_FLOAT), new IdentityConverter(Float.class, Float.class));
			converterMap
					.put(
							new Pair(FLOAT_CLASS, JAVA_LANG_OBJECT), new IdentityConverter(Float.class, Object.class));

			// Short.class
			converterMap
					.put(
							new Pair(JAVA_LANG_STRING, SHORT_CLASS), StringToShortConverter.toShort(integerFormat, true));
			converterMap
					.put(
							new Pair(SHORT_CLASS, JAVA_LANG_SHORT), new IdentityConverter(Short.class, Short.class));
			converterMap
					.put(
							new Pair(SHORT_CLASS, JAVA_LANG_STRING), IntegerToStringConverter.fromShort(integerFormat, true));
			converterMap
					.put(
							new Pair(SHORT_CLASS, JAVA_LANG_OBJECT), new IdentityConverter(Short.class, Object.class));

			// Long.class
			converterMap
					.put(
							new Pair(JAVA_LANG_STRING, LONG_CLASS), StringToNumberConverter.toLong(integerFormat, true));
			converterMap
					.put(
							new Pair(LONG_CLASS, JAVA_LANG_STRING), NumberToStringConverter.fromLong(integerFormat, true));
			converterMap
					.put(
							new Pair(LONG_CLASS, JAVA_LANG_LONG), new IdentityConverter(Long.class, Long.class));
			converterMap
					.put(
							new Pair(LONG_CLASS, JAVA_LANG_OBJECT), new IdentityConverter(Long.class, Object.class));

			// Character.class
			converterMap
					.put(
							new Pair(JAVA_LANG_STRING, CHARACTER_CLASS), StringToCharacterConverter.toCharacter(true));
			converterMap
					.put(
							new Pair(CHARACTER_CLASS, JAVA_LANG_CHARACTER), new IdentityConverter(Character.class, Character.class));
			converterMap
					.put(
							new Pair(CHARACTER_CLASS, JAVA_LANG_STRING), CharacterToStringConverter.fromCharacter(true));
			converterMap
					.put(
							new Pair(CHARACTER_CLASS, JAVA_LANG_OBJECT), new IdentityConverter(Character.class, Object.class));

			// Miscellaneous
			converterMap
					.put(
							new Pair(
									STATUS, JAVA_LANG_STRING), "org.eclipse.core.internal.databinding.conversion.StatusToStringConverter"); //$NON-NLS-1$

			addNumberToByteConverters(converterMap, integerFormat,
					integerClasses);
			addNumberToByteConverters(converterMap, numberFormat, floatClasses);

			addNumberToShortConverters(converterMap, integerFormat,
					integerClasses);
			addNumberToShortConverters(converterMap, numberFormat, floatClasses);

			addNumberToIntegerConverters(converterMap, integerFormat,
					integerClasses);
			addNumberToIntegerConverters(converterMap, numberFormat,
					floatClasses);

			addNumberToLongConverters(converterMap, integerFormat,
					integerClasses);
			addNumberToLongConverters(converterMap, numberFormat, floatClasses);

			addNumberToFloatConverters(converterMap, integerFormat,
					integerClasses);
			addNumberToFloatConverters(converterMap, numberFormat, floatClasses);

			addNumberToDoubleConverters(converterMap, integerFormat,
					integerClasses);
			addNumberToDoubleConverters(converterMap, numberFormat,
					floatClasses);

			addNumberToBigIntegerConverters(converterMap, bigIntegerFormat,
					integerClasses);
			addNumberToBigIntegerConverters(converterMap, bigNumberFormat,
					floatClasses);

			addNumberToBigDecimalConverters(converterMap, bigIntegerFormat,
					integerClasses);
			addNumberToBigDecimalConverters(converterMap, bigNumberFormat,
					floatClasses);
		}

		return converterMap;
	}

	private static final Class<?>[] integerClasses = { byte.class, Byte.class, short.class, Short.class, int.class,
			Integer.class, long.class, Long.class, BigInteger.class };

	private static final Class<?>[] floatClasses = { float.class, Float.class, double.class, Double.class,
			BigDecimal.class };

	/**
	 * Registers converters to boxed and unboxed types from a list of from
	 * classes.
	 */
	private static void addNumberToByteConverters(Map<Pair, Object> map, Format numberFormat, Class<?>[] fromTypes) {

		for (Class<?> fromType : fromTypes) {
			if (!fromType.equals(Byte.class) && !fromType.equals(byte.class)) {
				String fromName = (fromType.isPrimitive()) ? getKeyForClass(
						fromType, null) : fromType.getName();

				map.put(new Pair(fromName, BYTE_CLASS),
						new NumberToByteConverter(numberFormat, fromType, true));
				map.put(new Pair(fromName, Byte.class.getName()),
						new NumberToByteConverter(numberFormat, fromType, false));
			}
		}
	}

	/**
	 * Registers converters to boxed and unboxed types from a list of from
	 * classes.
	 */
	private static void addNumberToShortConverters(Map<Pair, Object> map, Format numberFormat, Class<?>[] fromTypes) {
		for (Class<?> fromType : fromTypes) {
			if (!fromType.equals(Short.class) && !fromType.equals(short.class)) {
				String fromName = (fromType.isPrimitive()) ? getKeyForClass(
						fromType, null) : fromType.getName();

				map.put(new Pair(fromName, SHORT_CLASS),
						new NumberToShortConverter(numberFormat, fromType, true));
				map.put(new Pair(fromName, Short.class.getName()),
						new NumberToShortConverter(numberFormat, fromType,
								false));
			}
		}
	}

	/**
	 * Registers converters to boxed and unboxed types from a list of from
	 * classes.
	 */
	private static void addNumberToIntegerConverters(Map<Pair, Object> map, Format numberFormat, Class<?>[] fromTypes) {
		for (Class<?> fromType : fromTypes) {
			if (!fromType.equals(Integer.class)
					&& !fromType.equals(int.class)) {
				String fromName = (fromType.isPrimitive()) ? getKeyForClass(
						fromType, null) : fromType.getName();

				map.put(new Pair(fromName, INTEGER_CLASS),
						new NumberToIntegerConverter(numberFormat, fromType,
								true));
				map.put(new Pair(fromName, Integer.class.getName()),
						new NumberToIntegerConverter(numberFormat, fromType,
								false));
			}
		}
	}

	/**
	 * Registers converters to boxed and unboxed types from a list of from
	 * classes.
	 */
	private static void addNumberToLongConverters(Map<Pair, Object> map, Format numberFormat, Class<?>[] fromTypes) {
		for (Class<?> fromType : fromTypes) {
			if (!fromType.equals(Long.class) && !fromType.equals(long.class)) {
				String fromName = (fromType.isPrimitive()) ? getKeyForClass(
						fromType, null) : fromType.getName();

				map.put(new Pair(fromName, LONG_CLASS),
						new NumberToLongConverter(numberFormat, fromType, true));
				map.put(new Pair(fromName, Long.class.getName()),
						new NumberToLongConverter(numberFormat, fromType, false));
			}
		}
	}

	/**
	 * Registers converters to boxed and unboxed types from a list of from
	 * classes.
	 */
	private static void addNumberToFloatConverters(Map<Pair, Object> map, Format numberFormat, Class<?>[] fromTypes) {
		for (Class<?> fromType : fromTypes) {
			if (!fromType.equals(Float.class) && !fromType.equals(float.class)) {
				String fromName = (fromType.isPrimitive()) ? getKeyForClass(
						fromType, null) : fromType.getName();

				map.put(new Pair(fromName, FLOAT_CLASS),
						new NumberToFloatConverter(numberFormat, fromType, true));
				map.put(new Pair(fromName, Float.class.getName()),
						new NumberToFloatConverter(numberFormat, fromType,
								false));
			}
		}
	}

	/**
	 * Registers converters to boxed and unboxed types from a list of from
	 * classes.
	 */
	private static void addNumberToDoubleConverters(Map<Pair, Object> map, Format numberFormat, Class<?>[] fromTypes) {
		for (Class<?> fromType : fromTypes) {
			if (!fromType.equals(Double.class) && !fromType.equals(double.class)) {
				String fromName = (fromType.isPrimitive()) ? getKeyForClass(
						fromType, null) : fromType.getName();

				map.put(new Pair(fromName, DOUBLE_CLASS),
						new NumberToDoubleConverter(numberFormat, fromType,
								true));
				map.put(new Pair(fromName, Double.class.getName()),
						new NumberToDoubleConverter(numberFormat, fromType,
								false));
			}
		}
	}

	/**
	 * Registers converters to boxed and unboxed types from a list of from
	 * classes.
	 */
	private static void addNumberToBigIntegerConverters(Map<Pair, Object> map, Format numberFormat,
			Class<?>[] fromTypes) {
		for (Class<?> fromType : fromTypes) {
			if (!fromType.equals(BigInteger.class)) {
				String fromName = (fromType.isPrimitive()) ? getKeyForClass(
						fromType, null) : fromType.getName();

				map.put(new Pair(fromName, BigInteger.class.getName()),
						new NumberToBigIntegerConverter(numberFormat, fromType));
			}
		}
	}

	/**
	 * Registers converters to boxed and unboxed types from a list of from
	 * classes.
	 */
	private static void addNumberToBigDecimalConverters(Map<Pair, Object> map, Format numberFormat,
			Class<?>[] fromTypes) {
		for (Class<?> fromType : fromTypes) {
			if (!fromType.equals(BigDecimal.class)) {
				String fromName = (fromType.isPrimitive()) ? getKeyForClass(
						fromType, null) : fromType.getName();

				map.put(new Pair(fromName, BigDecimal.class.getName()),
						new NumberToBigDecimalConverter(numberFormat, fromType));
			}
		}
	}

	private static String getKeyForClass(Object originalValue,
			Class<?> filteredValue) {
		if (originalValue instanceof Class) {
			Class<?> originalClass = (Class<?>) originalValue;
			if (originalClass.equals(int.class)) {
				return INTEGER_CLASS;
			} else if (originalClass.equals(byte.class)) {
				return BYTE_CLASS;
			} else if (originalClass.equals(boolean.class)) {
				return BOOLEAN_CLASS;
			} else if (originalClass.equals(double.class)) {
				return DOUBLE_CLASS;
			} else if (originalClass.equals(float.class)) {
				return FLOAT_CLASS;
			} else if (originalClass.equals(long.class)) {
				return LONG_CLASS;
			} else if (originalClass.equals(short.class)) {
				return SHORT_CLASS;
			}
		}
		return filteredValue.getName();
	}

	/**
	 * Returns {@link Boolean#TRUE} if the from type is assignable to the to type,
	 * or {@link Boolean#FALSE} if it not, or <code>null</code> if unknown.
	 *
	 * @param fromType source type to assign
	 * @param toType   target type to check assignability against
	 * @return whether fromType is assignable to toType, or <code>null</code> if
	 *         unknown
	 */
	protected Boolean isAssignableFromTo(Object fromType, Object toType) {
		if (fromType instanceof Class && toType instanceof Class) {
			Class<?> toClass = (Class<?>) toType;
			if (toClass.isPrimitive()) {
				toClass = autoboxed(toClass);
			}
			Class<?> fromClass = (Class<?>) fromType;
			if (fromClass.isPrimitive()) {
				fromClass = autoboxed(fromClass);
			}
			return toClass.isAssignableFrom(fromClass) ? Boolean.TRUE
					: Boolean.FALSE;
		}
		return null;
	}

	/**
	 * @param ex
	 *            the exception, that was caught
	 * @return the validation status
	 */
	protected IStatus logErrorWhileSettingValue(Exception ex) {
		IStatus errorStatus = ValidationStatus
				.error(BindingMessages.getString(BindingMessages.VALUEBINDING_ERROR_WHILE_SETTING_VALUE), ex);
		Policy.getLog().log(errorStatus);
		return errorStatus;
	}

	/**
	 * Converts the value from the source type to the destination type.
	 * <p>
	 * Default implementation will use the setConverter(IConverter), if one exists.
	 * If no converter exists no conversion occurs.
	 * </p>
	 *
	 * @param value source value to convert
	 * @return the converted value
	 */
	@SuppressWarnings("unchecked")
	public D convert(S value) {
		if (converter != null) {
			try {
				return converter.convert(value);
			} catch (Exception ex) {
				Policy.getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, ex.getMessage(), ex));
				throw ex;
			}
		}
		return (D) value;
	}

	/*
	 * Default converter implementation, does not perform any conversion.
	 */
	static final class DefaultConverter implements IConverter<Object, Object> {

		private final Object toType;

		private final Object fromType;

		DefaultConverter(Object fromType, Object toType) {
			this.toType = toType;
			this.fromType = fromType;
		}

		@Override
		public Object convert(Object fromObject) {
			// Explicit cast necessary due to potential type erasure
			if (toType instanceof Class<?> clazz) {
				return (clazz.isPrimitive() ? autoboxed(clazz) : clazz).cast(fromObject);
			}
			return fromObject;
		}

		@Override
		public Object getFromType() {
			return fromType;
		}

		@Override
		public Object getToType() {
			return toType;
		}
	}

}