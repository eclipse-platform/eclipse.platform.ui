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

package org.eclipse.jface.internal.databinding.provisional.factories;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.internal.databinding.internal.ClassLookupSupport;
import org.eclipse.jface.internal.databinding.internal.Pair;
import org.eclipse.jface.internal.databinding.provisional.BindingException;
import org.eclipse.jface.internal.databinding.provisional.conversion.IConverter;
import org.eclipse.jface.internal.databinding.provisional.conversion.IdentityConverter;
import org.eclipse.jface.internal.databinding.provisional.validation.IDomainValidator;
import org.eclipse.jface.internal.databinding.provisional.validation.IValidator;
import org.eclipse.jface.internal.databinding.provisional.validation.ReadOnlyValidator;
import org.eclipse.jface.internal.databinding.provisional.validation.String2BigDecimalValidator;
import org.eclipse.jface.internal.databinding.provisional.validation.String2ByteValidator;
import org.eclipse.jface.internal.databinding.provisional.validation.String2DateValidator;
import org.eclipse.jface.internal.databinding.provisional.validation.String2DoubleValidator;
import org.eclipse.jface.internal.databinding.provisional.validation.String2FloatValidator;
import org.eclipse.jface.internal.databinding.provisional.validation.String2IntValidator;
import org.eclipse.jface.internal.databinding.provisional.validation.String2LongValidator;
import org.eclipse.jface.internal.databinding.provisional.validation.String2ShortValidator;
import org.eclipse.jface.internal.databinding.provisional.validation.ValidationError;

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

	private ValidatorRegistry validatorRegistry = new ValidatorRegistry();

	private Map converterMap;

	public IValidator createValidator(Object fromType, Object toType) {
		if (fromType == null || toType == null) {
			return new IValidator() {

				public ValidationError isPartiallyValid(Object value) {
					return null;
				}

				public ValidationError isValid(Object value) {
					return null;
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
		if (toClass.isAssignableFrom(fromClass)) {
			return new IdentityConverter(fromClass, toClass);
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
			Pair key = new Pair(currentFromClass.getName(), toClass.getName());
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
		// Since we found no converter yet, try a "downcast" converter
		if (fromClass.isAssignableFrom(toClass)) {
			return new IdentityConverter(fromClass, toClass);
		}
		return null;
	}

	private Map getConverterMap() {
		// using string-based lookup avoids loading of too many classes
		if (converterMap == null) {
			converterMap = new HashMap();
			converterMap
					.put(
							new Pair("java.util.Date", "java.lang.String"), "org.eclipse.jface.internal.databinding.provisional.conversion.ConvertDate2String"); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
			converterMap
					.put(
							new Pair("java.lang.String", "java.math.BigDecimal"), "org.eclipse.jface.internal.databinding.provisional.conversion.ConvertString2BigDecimal"); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
			converterMap
					.put(
							new Pair("java.lang.String", "java.lang.Boolean"), "org.eclipse.jface.internal.databinding.provisional.conversion.ConvertString2Boolean"); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
			converterMap
					.put(
							new Pair("java.lang.String", "java.lang.Byte"), "org.eclipse.jface.internal.databinding.provisional.conversion.ConvertString2Byte"); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
			converterMap
					.put(
							new Pair("java.lang.String", "java.lang.Character"), "org.eclipse.jface.internal.databinding.provisional.conversion.ConvertString2Character"); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
			converterMap
					.put(
							new Pair("java.lang.String", "java.util.Date"), "org.eclipse.jface.internal.databinding.provisional.conversion.ConvertString2Date"); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
			converterMap
					.put(
							new Pair("java.lang.String", "java.lang.Double"), "org.eclipse.jface.internal.databinding.provisional.conversion.ConvertString2Double"); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
			converterMap
					.put(
							new Pair("java.lang.String", "java.lang.Float"), "org.eclipse.jface.internal.databinding.provisional.conversion.ConvertString2Float"); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
			converterMap
					.put(
							new Pair("java.lang.String", "java.lang.Integer"), "org.eclipse.jface.internal.databinding.provisional.conversion.ConvertString2Integer"); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
			converterMap
					.put(
							new Pair("java.lang.String", "java.lang.Long"), "org.eclipse.jface.internal.databinding.provisional.conversion.ConvertString2Long"); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
			converterMap
					.put(
							new Pair("java.lang.String", "java.lang.Short"), "org.eclipse.jface.internal.databinding.provisional.conversion.ConvertString2Short"); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
			converterMap
					.put(
							new Pair("java.lang.Object", "java.lang.String"), "org.eclipse.jface.internal.databinding.provisional.conversion.ToStringConverter"); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		}
		return converterMap;
	}

	public IDomainValidator createDomainValidator(Object modelType) {
		return new IDomainValidator() {
			public ValidationError isValid(Object value) {
				return null;
			}
		};
	}

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
			return new Boolean((toClass).isAssignableFrom(fromClass));
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
					public ValidationError isPartiallyValid(Object value) {
						return null;
					}

					public ValidationError isValid(Object value) {
						return null;
					}
				};
			}
			return ReadOnlyValidator.getDefault();
		}

		/**
		 * Adds the system-provided validators to the current validator
		 * registry. This is done automatically for the validator registry
		 * singleton.
		 */
		private ValidatorRegistry() {
			// Standalone validators here...
			associate(String.class, Integer.TYPE, new String2IntValidator());
			associate(String.class, Byte.TYPE, new String2ByteValidator());
			associate(String.class, Short.TYPE, new String2ShortValidator());
			associate(String.class, Long.TYPE, new String2LongValidator());
			associate(String.class, Float.TYPE, new String2FloatValidator());
			associate(String.class, Double.TYPE, new String2DoubleValidator());

			associate(String.class, Integer.class, new String2IntValidator());
			associate(String.class, Byte.class, new String2ByteValidator());
			associate(String.class, Short.class, new String2ShortValidator());
			associate(String.class, Long.class, new String2LongValidator());
			associate(String.class, Float.class, new String2FloatValidator());
			associate(String.class, Double.class, new String2DoubleValidator());
			associate(String.class, Date.class, new String2DateValidator());

			associate(String.class, BigDecimal.class,
					new String2BigDecimalValidator());

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