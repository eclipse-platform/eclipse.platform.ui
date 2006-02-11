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

package org.eclipse.jface.internal.databinding.api.validation;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;

import org.eclipse.jface.internal.databinding.nonapi.BindingMessages;
import org.eclipse.jface.internal.databinding.nonapi.Pair;


/**
 * ValidatorRegistry.  A registry for validators.  Instances do not contain the system-provided
 * validators by default, but these can be added by calling addSystemValidators().
 * 
 * @since 3.2
 */
public class ValidatorRegistry {
	
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
