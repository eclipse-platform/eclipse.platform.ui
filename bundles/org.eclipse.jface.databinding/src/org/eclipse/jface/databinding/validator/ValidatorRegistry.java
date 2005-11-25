/*
 * Copyright (C) 2005 db4objects Inc.  http://www.db4o.com
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     db4objects - Initial API and implementation
 */
package org.eclipse.jface.databinding.validator;

import java.util.Date;
import java.util.HashMap;

import org.eclipse.jface.databinding.internal.BindingMessages;
import org.eclipse.jface.databinding.internal.Pair;
import org.eclipse.jface.databinding.validators.String2ByteValidator;
import org.eclipse.jface.databinding.validators.String2DateValidator;
import org.eclipse.jface.databinding.validators.String2DoubleValidator;
import org.eclipse.jface.databinding.validators.String2FloatValidator;
import org.eclipse.jface.databinding.validators.String2IntValidator;
import org.eclipse.jface.databinding.validators.String2LongValidator;
import org.eclipse.jface.databinding.validators.String2ShortValidator;


/**
 * ValidatorRegistry.  A registry for validators.  A singleton is provided that includes all
 * of the system-provided validators by default, but clients are free to use as many instances
 * of this class as they like.  Client-created instances do not contain the system-provided
 * validators by default, but these can be added by calling addSystemValidators().
 */
public class ValidatorRegistry {
	
	private static ValidatorRegistry registry = null;
	
	/**
	 * Return the default validator registry.
	 * 
	 * @return the default validator registry.
	 */
	public static ValidatorRegistry getDefault() {
		if (registry == null) {
			registry = new ValidatorRegistry();
			registry.addSystemValidators();
		}
		return registry;
	}
	
	private HashMap validators = new HashMap();
    
    /**
     * Associate a particular validator that can validate the conversion (fromClass, toClass)
     * 
     * @param fromClass The Class to convert from
     * @param toClass The Class to convert to
     * @param validator The IValidator
     */
    public void associate(Class fromClass, Class toClass, IValidator validator) {
        validators.put(new Pair(fromClass, toClass), validator);
    }
    
    /**
     * Return an IVerifier for a specific class.
     * 
     * @param fromClass The Class to convert from
     * @param toClass The Class to convert to
     * @return An appropriate IValidator
     */
    public IValidator get(Class fromClass, Class toClass) {
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
    public void addSystemValidators() {
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
        
        // Regex-implemented validators here...
        associate(String.class, Character.TYPE, new RegularExpressionStringValidator(
                "^.$|^$", ".", BindingMessages.getString("Validate_CharacterHelp"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        associate(String.class, Character.class, new RegularExpressionStringValidator(
                "^.$|^$", ".", BindingMessages.getString("Validate_CharacterHelp"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        associate(String.class, Boolean.TYPE, new RegularExpressionStringValidator(
        		BindingMessages.getString("Validate_BooleanPartialValidRegex"),  //$NON-NLS-1$
        		BindingMessages.getString("Validate_BooleanValidRegex"),  //$NON-NLS-1$
        		BindingMessages.getString("Validate_BooleanHelp"))); //$NON-NLS-1$
        associate(String.class, Boolean.class, new RegularExpressionStringValidator(
        		BindingMessages.getString("Validate_BooleanPartialValidRegex"),  //$NON-NLS-1$
        		BindingMessages.getString("Validate_BooleanValidRegex"),  //$NON-NLS-1$
        		BindingMessages.getString("Validate_BooleanHelp"))); //$NON-NLS-1$
        associate(String.class, String.class, new RegularExpressionStringValidator("^.*$", "^.*$", "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
}
