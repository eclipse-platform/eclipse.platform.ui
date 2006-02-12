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

package org.eclipse.jface.internal.databinding.api;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;

import org.eclipse.jface.internal.databinding.api.conversion.IConverter;
import org.eclipse.jface.internal.databinding.api.conversion.TheIdentityFunction;
import org.eclipse.jface.internal.databinding.api.validation.IDomainValidator;
import org.eclipse.jface.internal.databinding.api.validation.IValidator;
import org.eclipse.jface.internal.databinding.api.validation.ReadOnlyValidator;
import org.eclipse.jface.internal.databinding.api.validation.RegexStringValidator;
import org.eclipse.jface.internal.databinding.api.validation.String2BigDecimalValidator;
import org.eclipse.jface.internal.databinding.api.validation.String2ByteValidator;
import org.eclipse.jface.internal.databinding.api.validation.String2DateValidator;
import org.eclipse.jface.internal.databinding.api.validation.String2DoubleValidator;
import org.eclipse.jface.internal.databinding.api.validation.String2FloatValidator;
import org.eclipse.jface.internal.databinding.api.validation.String2IntValidator;
import org.eclipse.jface.internal.databinding.api.validation.String2LongValidator;
import org.eclipse.jface.internal.databinding.api.validation.String2ShortValidator;
import org.eclipse.jface.internal.databinding.api.validation.ValidationError;
import org.eclipse.jface.internal.databinding.nonapi.BindingMessages;
import org.eclipse.jface.internal.databinding.nonapi.Pair;

public final class DefaultBindSupportFactory implements IBindSupportFactory {
	
	private ValidatorRegistry validatorRegistry = new ValidatorRegistry();

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
		if (toType == null) {
			return null;
		}
		if (fromType == toType) {
			return TheIdentityFunction.IDENTITY;
		}
		// TODO string-based lookup of converter
		if (ConversionFunctionRegistry.canConvertPair(fromType, toType)) {
			return new FunctionalConverter(fromType, toType);
		}
		// FIXME: djo -- This doesn't always work in the case of object
		// types?
		Boolean assignableFromTo = isAssignableFromTo(fromType, toType);
		if (assignableFromTo!=null && assignableFromTo.booleanValue()) {
			return TheIdentityFunction.IDENTITY;
		}
		return null;
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
			return new Boolean(((Class) toType)
					.isAssignableFrom((Class) fromType));
		}
		return null;
	}
	
	private static class ValidatorRegistry {
		
		private HashMap validators = new HashMap();
	    
	    /**
	     * Associate a particular validator that can validate the conversion (fromClass, toClass)
	     * 
	     * @param fromClass The Class to convert from
	     * @param toClass The Class to convert to
	     * @param validator The IValidator
	     */
	    private void associate(Object fromClass, Object toClass, IValidator validator) {
	        validators.put(new Pair(fromClass, toClass), validator);
	    }
	    
	    /**
	     * Return an IVerifier for a specific class.
	     * 
	     * @param fromClass The Class to convert from
	     * @param toClass The Class to convert to
	     * @return An appropriate IValidator
	     */
	    private IValidator get(Object fromClass, Object toClass) {
	        IValidator result = (IValidator) validators.get(new Pair(fromClass, toClass));
	        if (result == null) {
	            return ReadOnlyValidator.getDefault();
	        }
	        return result;
	    }
	    
	    /**
	     * Adds the system-provided validators to the current validator registry.  This is done
	     * automatically for the validator registry singleton.
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
	        
	        associate(String.class, BigDecimal.class, new String2BigDecimalValidator());
	        
	        // Regex-implemented validators here...
	        associate(String.class, Character.TYPE, new RegexStringValidator(
	                "^.$|^$", ".", BindingMessages.getString("Validate_CharacterHelp"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	        associate(String.class, Character.class, new RegexStringValidator(
	                "^.$|^$", ".", BindingMessages.getString("Validate_CharacterHelp"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	        associate(String.class, Boolean.TYPE, new RegexStringValidator(
	        		BindingMessages.getString("Validate_BooleanPartialValidRegex"),  //$NON-NLS-1$
	        		BindingMessages.getString("Validate_BooleanValidRegex"),  //$NON-NLS-1$
	        		BindingMessages.getString("Validate_BooleanHelp"))); //$NON-NLS-1$
	        associate(String.class, Boolean.class, new RegexStringValidator(
	        		BindingMessages.getString("Validate_BooleanPartialValidRegex"),  //$NON-NLS-1$
	        		BindingMessages.getString("Validate_BooleanValidRegex"),  //$NON-NLS-1$
	        		BindingMessages.getString("Validate_BooleanHelp"))); //$NON-NLS-1$
	        associate(String.class, String.class, new RegexStringValidator("^.*$", "^.*$", "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	    }
	}

}