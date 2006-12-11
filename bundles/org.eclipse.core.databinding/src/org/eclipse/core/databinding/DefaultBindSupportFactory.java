/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.core.databinding;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.conversion.IdentityConverter;
import org.eclipse.core.databinding.conversion.ToStringConverter;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ObjectToPrimitiveValidator;
import org.eclipse.core.databinding.validation.String2BytePrimitiveValidator;
import org.eclipse.core.databinding.validation.String2ByteValidator;
import org.eclipse.core.databinding.validation.String2DateValidator;
import org.eclipse.core.databinding.validation.String2DoublePrimitiveValidator;
import org.eclipse.core.databinding.validation.String2DoubleValidator;
import org.eclipse.core.databinding.validation.String2FloatPrimitiveValidator;
import org.eclipse.core.databinding.validation.String2FloatValidator;
import org.eclipse.core.databinding.validation.String2IntegerPrimitiveValidator;
import org.eclipse.core.databinding.validation.String2IntegerValidator;
import org.eclipse.core.databinding.validation.String2LongPrimitiveValidator;
import org.eclipse.core.databinding.validation.String2LongValidator;
import org.eclipse.core.databinding.validation.String2ShortPrimitiveValidator;
import org.eclipse.core.databinding.validation.String2ShortValidator;
import org.eclipse.core.internal.databinding.ClassLookupSupport;
import org.eclipse.core.internal.databinding.Pair;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Default bind support factory. This factory adds the following converters and
 * validators:
 * 
 * TODO list converters and validators
 * 
 * @since 1.0
 * 
 */
public final class DefaultBindSupportFactory extends BindSupportFactory {

	private static final String INTEGER_TYPE = "java.lang.Integer.TYPE"; //$NON-NLS-1$

	private static final String BYTE_TYPE = "java.lang.Byte.TYPE"; //$NON-NLS-1$

	private static final String DOUBLE_TYPE = "java.lang.Double.TYPE"; //$NON-NLS-1$

	private static final String BOOLEAN_TYPE = "java.lang.Boolean.TYPE"; //$NON-NLS-1$

	private static final String FLOAT_TYPE = "java.lang.Float.TYPE"; //$NON-NLS-1$

	private static final String LONG_TYPE = "java.lang.Long.TYPE"; //$NON-NLS-1$

	private static final String SHORT_TYPE = "java.lang.Short.TYPE"; //$NON-NLS-1$

	private ValidatorRegistry validatorRegistry = new ValidatorRegistry();

	private Map converterMap;

	public IValidator createValidator(Object fromType, Object toType) {
		if (fromType == null || toType == null) {
			return new IValidator() {

				public IStatus validate(Object value) {
					return Status.OK_STATUS;
				}
			};
		}

		IValidator dataTypeValidator = findValidator(fromType, toType);
		if (dataTypeValidator == null) {
			throw new BindingException(
					"No IValidator is registered for conversions from " + fromType + " to " + toType); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return dataTypeValidator;
	}

	private IValidator findValidator(Object fromType, Object toType) {
		// TODO string-based lookup of validator
		return validatorRegistry.get(fromType, toType);
	}

	public IConverter createConverter(Object fromType, Object toType) {
		if (!(fromType instanceof Class) || !(toType instanceof Class)) {
			return null;
		}
		Class toClass = (Class) toType;
		if (toClass.isPrimitive()) {
			toClass = autoboxed(toClass);
		}
		Class fromClass = (Class) fromType;
		if (fromClass.isPrimitive()) {
			fromClass = autoboxed(fromClass);
		}
		if (!((Class) toType).isPrimitive()
				&& toClass.isAssignableFrom(fromClass)) {
			return new IdentityConverter(fromClass, toClass);
		}
		if (((Class) fromType).isPrimitive() && ((Class) toType).isPrimitive()
				&& fromType.equals(toType)) {
			return new IdentityConverter((Class) fromType, (Class) toType);
		}
		Map converterMap = getConverterMap();
		Class[] supertypeHierarchyFlattened = ClassLookupSupport
				.getTypeHierarchyFlattened(fromClass);
		for (int i = 0; i < supertypeHierarchyFlattened.length; i++) {
			Class currentFromClass = supertypeHierarchyFlattened[i];
			if (currentFromClass == toType) {
				// converting to toType is just a widening
				return new IdentityConverter(fromClass, toClass);
			}
			Pair key = new Pair(getKeyForClass(fromType, currentFromClass),
					getKeyForClass(toType, toClass));
			Object converterOrClassname = converterMap.get(key);
			if (converterOrClassname instanceof IConverter) {
				return (IConverter) converterOrClassname;
			} else if (converterOrClassname instanceof String) {
				String classname = (String) converterOrClassname;
				Class converterClass;
				try {
					converterClass = Class.forName(classname);
					IConverter result = (IConverter) converterClass
							.newInstance();
					converterMap.put(key, result);
					return result;
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
		// Since we found no converter yet, try a "downcast" converter;
		// the IdentityConverter will automatically check the actual types at
		// runtime.
		if (fromClass.isAssignableFrom(toClass)) {
			return new IdentityConverter(fromClass, toClass);
		}
		return null;
	}

	private String getKeyForClass(Object originalValue, Class filteredValue) {
		if (originalValue instanceof Class) {
			Class originalClass = (Class) originalValue;
			if (originalClass.equals(Integer.TYPE)) {
				return INTEGER_TYPE;
			} else if (originalClass.equals(Byte.TYPE)) {
				return BYTE_TYPE;
			} else if (originalClass.equals(Boolean.TYPE)) {
				return BOOLEAN_TYPE;
			} else if (originalClass.equals(Double.TYPE)) {
				return DOUBLE_TYPE;
			} else if (originalClass.equals(Float.TYPE)) {
				return FLOAT_TYPE;
			} else if (originalClass.equals(Long.TYPE)) {
				return LONG_TYPE;
			} else if (originalClass.equals(Short.TYPE)) {
				return SHORT_TYPE;
			}
		}
		return filteredValue.getName();
	}

	private Map getConverterMap() {
		// using string-based lookup avoids loading of too many classes
		if (converterMap == null) {
			converterMap = new HashMap();
			converterMap
					.put(
							new Pair("java.util.Date", "java.lang.String"), "org.eclipse.core.databinding.conversion.ConvertDate2String"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			converterMap
					.put(
							new Pair("java.lang.String", "java.math.BigDecimal"), "org.eclipse.core.databinding.conversion.ConvertString2BigDecimal"); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
			converterMap
					.put(
							new Pair("java.lang.String", "java.lang.Boolean"), "org.eclipse.core.databinding.conversion.ConvertString2Boolean"); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
			converterMap
					.put(
							new Pair("java.lang.String", "java.lang.Byte"), "org.eclipse.core.databinding.conversion.ConvertString2Byte"); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
			converterMap
					.put(
							new Pair("java.lang.String", "java.lang.Character"), "org.eclipse.core.databinding.conversion.ConvertString2Character"); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
			converterMap
					.put(
							new Pair("java.lang.String", "java.util.Date"), "org.eclipse.core.databinding.conversion.ConvertString2Date"); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
			converterMap
					.put(
							new Pair("java.lang.String", "java.lang.Double"), "org.eclipse.core.databinding.conversion.ConvertString2Double"); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
			converterMap
					.put(
							new Pair("java.lang.String", "java.lang.Float"), "org.eclipse.core.databinding.conversion.ConvertString2Float"); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
			converterMap
					.put(
							new Pair("java.lang.String", "java.lang.Integer"), "org.eclipse.core.databinding.conversion.ConvertString2Integer"); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
			converterMap
					.put(
							new Pair("java.lang.String", "java.lang.Long"), "org.eclipse.core.databinding.conversion.ConvertString2Long"); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
			converterMap
					.put(
							new Pair("java.lang.String", "java.lang.Short"), "org.eclipse.core.databinding.conversion.ConvertString2Short"); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
			converterMap
					.put(
							new Pair("java.lang.Object", "java.lang.String"), "org.eclipse.core.databinding.conversion.ToStringConverter"); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$

			// Integer.TYPE
			converterMap
					.put(
							new Pair("java.lang.String", INTEGER_TYPE), "org.eclipse.core.databinding.conversion.ConvertString2IntegerPrimitive"); //$NON-NLS-1$ //$NON-NLS-2$
			converterMap
					.put(
							new Pair(INTEGER_TYPE, "java.lang.Integer"), new IdentityConverter(Integer.TYPE, Integer.class)); //$NON-NLS-1$
			converterMap
					.put(
							new Pair(INTEGER_TYPE, "java.lang.String"), new ToStringConverter(Integer.TYPE)); //$NON-NLS-1$
			converterMap
					.put(
							new Pair(INTEGER_TYPE, "java.lang.Object"), new IdentityConverter(Integer.TYPE, Object.class)); //$NON-NLS-1$

			// Byte.TYPE
			converterMap
					.put(
							new Pair("java.lang.String", BYTE_TYPE), "org.eclipse.core.databinding.conversion.ConvertString2BytePrimitive"); //$NON-NLS-1$ //$NON-NLS-2$
			converterMap
					.put(
							new Pair(BYTE_TYPE, "java.lang.Byte"), new IdentityConverter(Byte.TYPE, Byte.class)); //$NON-NLS-1$
			converterMap
					.put(
							new Pair(BYTE_TYPE, "java.lang.String"), new ToStringConverter(Byte.TYPE)); //$NON-NLS-1$
			converterMap
					.put(
							new Pair(BYTE_TYPE, "java.lang.Object"), new IdentityConverter(Byte.TYPE, Object.class)); //$NON-NLS-1$

			// Double.TYPE
			converterMap
					.put(
							new Pair("java.lang.String", DOUBLE_TYPE), "org.eclipse.core.databinding.conversion.ConvertString2DoublePrimitive"); //$NON-NLS-1$ //$NON-NLS-2$
			converterMap
					.put(
							new Pair(DOUBLE_TYPE, "java.lang.Double"), new IdentityConverter(Double.TYPE, Double.class)); //$NON-NLS-1$
			converterMap
					.put(
							new Pair(DOUBLE_TYPE, "java.lang.String"), new ToStringConverter(Double.TYPE)); //$NON-NLS-1$
			converterMap
					.put(
							new Pair(DOUBLE_TYPE, "java.lang.Object"), new IdentityConverter(Double.TYPE, Object.class)); //$NON-NLS-1$

			// Boolean.TYPE
			converterMap
					.put(
							new Pair("java.lang.String", BOOLEAN_TYPE), "org.eclipse.core.databinding.conversion.ConvertString2BooleanPrimitive"); //$NON-NLS-1$ //$NON-NLS-2$
			converterMap
					.put(
							new Pair(BOOLEAN_TYPE, "java.lang.Boolean"), new IdentityConverter(Boolean.TYPE, Boolean.class)); //$NON-NLS-1$
			converterMap
					.put(
							new Pair(BOOLEAN_TYPE, "java.lang.String"), new ToStringConverter(Boolean.TYPE)); //$NON-NLS-1$
			converterMap
					.put(
							new Pair(BOOLEAN_TYPE, "java.lang.Object"), new IdentityConverter(Boolean.TYPE, Object.class)); //$NON-NLS-1$

			// Float.TYPE
			converterMap
					.put(
							new Pair("java.lang.String", FLOAT_TYPE), "org.eclipse.core.databinding.conversion.ConvertString2FloatPrimitive"); //$NON-NLS-1$ //$NON-NLS-2$
			converterMap
					.put(
							new Pair(FLOAT_TYPE, "java.lang.Float"), new IdentityConverter(Float.TYPE, Float.class)); //$NON-NLS-1$
			converterMap
					.put(
							new Pair(FLOAT_TYPE, "java.lang.String"), new ToStringConverter(Float.TYPE)); //$NON-NLS-1$
			converterMap
					.put(
							new Pair(FLOAT_TYPE, "java.lang.Object"), new IdentityConverter(Float.TYPE, Object.class)); //$NON-NLS-1$		

			// Short.TYPE
			converterMap
					.put(
							new Pair("java.lang.String", SHORT_TYPE), "org.eclipse.core.databinding.conversion.ConvertString2ShortPrimitive"); //$NON-NLS-1$ //$NON-NLS-2$
			converterMap
					.put(
							new Pair(SHORT_TYPE, "java.lang.Short"), new IdentityConverter(Short.TYPE, Short.class)); //$NON-NLS-1$
			converterMap
					.put(
							new Pair(SHORT_TYPE, "java.lang.String"), new ToStringConverter(Short.TYPE)); //$NON-NLS-1$
			converterMap
					.put(
							new Pair(SHORT_TYPE, "java.lang.Object"), new IdentityConverter(Short.TYPE, Object.class)); //$NON-NLS-1$		

			// Long.TYPE
			converterMap
					.put(
							new Pair("java.lang.String", LONG_TYPE), "org.eclipse.core.databinding.conversion.ConvertString2LongPrimitive"); //$NON-NLS-1$ //$NON-NLS-2$
			converterMap
					.put(
							new Pair(LONG_TYPE, "java.lang.Long"), new IdentityConverter(Long.TYPE, Long.class)); //$NON-NLS-1$
			converterMap
					.put(
							new Pair(LONG_TYPE, "java.lang.String"), new ToStringConverter(Long.TYPE)); //$NON-NLS-1$
			converterMap
					.put(
							new Pair(LONG_TYPE, "java.lang.Object"), new IdentityConverter(Long.TYPE, Object.class)); //$NON-NLS-1$		

		}

		return converterMap;
	}

	// --------------------------- OLD

	// public IConverter createConverter(Object fromType, Object toType) {
	// if (!(fromType instanceof Class) || !(toType instanceof Class)) {
	// return null;
	// }
	// Class toClass = (Class) toType;
	// if (toClass.isPrimitive()) {
	// toClass = autoboxed(toClass);
	// }
	// Class fromClass = (Class) fromType;
	// if (fromClass.isPrimitive()) {
	// fromClass = autoboxed(fromClass);
	// }
	// if (toClass.isAssignableFrom(fromClass)) {
	// return new IdentityConverter(fromClass, toClass);
	// }
	// Map converterMap = getConverterMap();
	// Class[] supertypeHierarchyFlattened = ClassLookupSupport
	// .getTypeHierarchyFlattened(fromClass);
	// for (int i = 0; i < supertypeHierarchyFlattened.length; i++) {
	// Class currentFromClass = supertypeHierarchyFlattened[i];
	// if (currentFromClass == toType) {
	// // converting to toType is just a widening
	// return new IdentityConverter(fromClass, toClass);
	// }
	// Pair key = new Pair(currentFromClass.getName(), toClass.getName());
	// Object converterOrClassname = converterMap.get(key);
	// if (converterOrClassname instanceof IConverter) {
	// return (IConverter) converterOrClassname;
	// } else if (converterOrClassname instanceof String) {
	// String classname = (String) converterOrClassname;
	// Class converterClass;
	// try {
	// converterClass = Class.forName(classname);
	// IConverter result = (IConverter) converterClass
	// .newInstance();
	// converterMap.put(key, result);
	// return result;
	// } catch (ClassNotFoundException e) {
	// e.printStackTrace();
	// } catch (InstantiationException e) {
	// e.printStackTrace();
	// } catch (IllegalAccessException e) {
	// e.printStackTrace();
	// }
	// }
	// }
	// // Since we found no converter yet, try a "downcast" converter
	// if (fromClass.isAssignableFrom(toClass)) {
	// return new IdentityConverter(fromClass, toClass);
	// }
	// return null;
	// }
	//
	// private Map getConverterMap() {
	// // using string-based lookup avoids loading of too many classes
	// if (converterMap == null) {
	// converterMap = new HashMap();
	// converterMap
	// .put(
	// new Pair("java.util.Date", "java.lang.String"),
	// "org.eclipse.core.databinding.conversion.ConvertDate2String");
	// //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
	// converterMap
	// .put(
	// new Pair("java.lang.String", "java.math.BigDecimal"),
	// "org.eclipse.core.databinding.conversion.ConvertString2BigDecimal");
	// //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
	// converterMap
	// .put(
	// new Pair("java.lang.String", "java.lang.Boolean"),
	// "org.eclipse.core.databinding.conversion.ConvertString2Boolean");
	// //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
	// converterMap
	// .put(
	// new Pair("java.lang.String", "java.lang.Byte"),
	// "org.eclipse.core.databinding.conversion.ConvertString2Byte");
	// //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
	// converterMap
	// .put(
	// new Pair("java.lang.String", "java.lang.Character"),
	// "org.eclipse.core.databinding.conversion.ConvertString2Character");
	// //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
	// converterMap
	// .put(
	// new Pair("java.lang.String", "java.util.Date"),
	// "org.eclipse.core.databinding.conversion.ConvertString2Date");
	// //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
	// converterMap
	// .put(
	// new Pair("java.lang.String", "java.lang.Double"),
	// "org.eclipse.core.databinding.conversion.ConvertString2Double");
	// //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
	// converterMap
	// .put(
	// new Pair("java.lang.String", "java.lang.Float"),
	// "org.eclipse.core.databinding.conversion.ConvertString2Float");
	// //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
	// converterMap
	// .put(
	// new Pair("java.lang.String", "java.lang.Integer"),
	// "org.eclipse.core.databinding.conversion.ConvertString2Integer");
	// //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
	// converterMap
	// .put(
	// new Pair("java.lang.String", "java.lang.Long"),
	// "org.eclipse.core.databinding.conversion.ConvertString2Long");
	// //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
	// converterMap
	// .put(
	// new Pair("java.lang.String", "java.lang.Short"),
	// "org.eclipse.core.databinding.conversion.ConvertString2Short");
	// //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
	// converterMap
	// .put(
	// new Pair("java.lang.Object", "java.lang.String"),
	// "org.eclipse.core.databinding.conversion.ToStringConverter");
	// //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
	// }
	// return converterMap;
	// }

	// --------------------------- OLD

	public Boolean isAssignableFromTo(Object fromType, Object toType) {
		if (fromType instanceof Class && toType instanceof Class) {
			Class toClass = (Class) toType;
			if (toClass.isPrimitive()) {
				toClass = autoboxed(toClass);
			}
			Class fromClass = (Class) fromType;
			if (fromClass.isPrimitive()) {
				fromClass = autoboxed(fromClass);
			}
			return toClass.isAssignableFrom(fromClass) ? Boolean.TRUE
					: Boolean.FALSE;
		}
		return null;
	}

	private Class autoboxed(Class clazz) {
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
		return clazz;
	}

	private static class ValidatorRegistry {

		private HashMap validators = new HashMap();

		/**
		 * Associate a particular validator that can validate the conversion
		 * (fromClass, toClass)
		 * 
		 * @param fromClass
		 *            The Class to convert from
		 * @param toClass
		 *            The Class to convert to
		 * @param validator
		 *            The IValidator
		 */
		private void associate(Object fromClass, Object toClass,
				IValidator validator) {
			validators.put(new Pair(fromClass, toClass), validator);
		}

		/**
		 * Return an IVerifier for a specific class.
		 * 
		 * @param fromClass
		 *            The Class to convert from
		 * @param toClass
		 *            The Class to convert to
		 * @return An appropriate IValidator
		 */
		private IValidator get(Object fromClass, Object toClass) {
			IValidator result = (IValidator) validators.get(new Pair(fromClass,
					toClass));
			if (result != null)
				return result;
			if (fromClass != null && toClass != null && fromClass == toClass) {
				return new IValidator() {
					public IStatus validate(Object value) {
						return Status.OK_STATUS;
					}
				};
			}
			return new IValidator() {
				public IStatus validate(Object value) {
					return Status.OK_STATUS;
				}
			};
		}

		/**
		 * Adds the system-provided validators to the current validator
		 * registry. This is done automatically for the validator registry
		 * singleton.
		 */
		private ValidatorRegistry() {
			// Standalone validators here...
			associate(String.class, Integer.TYPE,
					new String2IntegerPrimitiveValidator());
			associate(String.class, Byte.TYPE,
					new String2BytePrimitiveValidator());
			associate(String.class, Short.TYPE,
					new String2ShortPrimitiveValidator());
			associate(String.class, Long.TYPE,
					new String2LongPrimitiveValidator());
			associate(String.class, Float.TYPE,
					new String2FloatPrimitiveValidator());
			associate(String.class, Double.TYPE,
					new String2DoublePrimitiveValidator());

			associate(String.class, Integer.class,
					new String2IntegerValidator());
			associate(String.class, Byte.class, new String2ByteValidator());
			associate(String.class, Short.class, new String2ShortValidator());
			associate(String.class, Long.class, new String2LongValidator());
			associate(String.class, Float.class, new String2FloatValidator());
			associate(String.class, Double.class, new String2DoubleValidator());
			associate(String.class, Date.class, new String2DateValidator());

			associate(Integer.class, Integer.TYPE,
					new ObjectToPrimitiveValidator(Integer.TYPE));
			associate(Byte.class, Byte.TYPE, new ObjectToPrimitiveValidator(
					Byte.TYPE));
			associate(Short.class, Short.TYPE, new ObjectToPrimitiveValidator(
					Short.TYPE));
			associate(Long.class, Long.TYPE, new ObjectToPrimitiveValidator(
					Long.TYPE));
			associate(Float.class, Float.TYPE, new ObjectToPrimitiveValidator(
					Float.TYPE));
			associate(Double.class, Double.TYPE,
					new ObjectToPrimitiveValidator(Double.TYPE));
			associate(Boolean.class, Boolean.TYPE,
					new ObjectToPrimitiveValidator(Boolean.TYPE));

			associate(Object.class, Integer.TYPE,
					new ObjectToPrimitiveValidator(Integer.TYPE));
			associate(Object.class, Byte.TYPE, new ObjectToPrimitiveValidator(
					Byte.TYPE));
			associate(Object.class, Short.TYPE, new ObjectToPrimitiveValidator(
					Short.TYPE));
			associate(Object.class, Long.TYPE, new ObjectToPrimitiveValidator(
					Long.TYPE));
			associate(Object.class, Float.TYPE, new ObjectToPrimitiveValidator(
					Float.TYPE));
			associate(Object.class, Double.TYPE,
					new ObjectToPrimitiveValidator(Double.TYPE));
			associate(Object.class, Boolean.TYPE,
					new ObjectToPrimitiveValidator(Boolean.TYPE));

			// Regex-implemented validators here...
			// associate(String.class, Character.TYPE, new RegexStringValidator(
			// "^.$|^$", ".",
			// BindingMessages.getString("Validate_CharacterHelp")));
			// //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			// associate(String.class, Character.class, new
			// RegexStringValidator(
			// "^.$|^$", ".",
			// BindingMessages.getString("Validate_CharacterHelp")));
			// //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			// associate(String.class, Boolean.TYPE, new RegexStringValidator(
			// BindingMessages.getString("Validate_BooleanPartialValidRegex"),
			// //$NON-NLS-1$
			// BindingMessages.getString("Validate_BooleanValidRegex"),
			// //$NON-NLS-1$
			// BindingMessages.getString("Validate_BooleanHelp")));
			// //$NON-NLS-1$
			// associate(String.class, Boolean.class, new RegexStringValidator(
			// BindingMessages.getString("Validate_BooleanPartialValidRegex"),
			// //$NON-NLS-1$
			// BindingMessages.getString("Validate_BooleanValidRegex"),
			// //$NON-NLS-1$
			// BindingMessages.getString("Validate_BooleanHelp")));
			// //$NON-NLS-1$
			// associate(String.class, String.class, new
			// RegexStringValidator("^.*$", "^.*$", "")); //$NON-NLS-1$
			// //$NON-NLS-2$ //$NON-NLS-3$
		}
	}

}