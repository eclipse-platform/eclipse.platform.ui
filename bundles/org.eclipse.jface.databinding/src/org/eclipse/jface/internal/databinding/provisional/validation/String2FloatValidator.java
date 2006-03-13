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
package org.eclipse.jface.internal.databinding.provisional.validation;

import org.eclipse.jface.internal.databinding.internal.BindingMessages;


/**
 * FloatValidator.  Verify string to float data conversion
 */
public class String2FloatValidator implements IValidator {
    
	public ValidationError isPartiallyValid(Object fragment) {
		if (((String)fragment).matches("\\-?[0-9]*\\.?[0-9]*([0-9]+[e|E]\\-?([0-9]+\\.)?[0-9]*)?")) //$NON-NLS-1$
            return null;

        return ValidationError.error(getHint());
	}
    
    public ValidationError isValid(Object value) {
        try {
            Float.parseFloat((String)value);
            return null;
        } catch (Exception e) {
            return ValidationError.error(getHint());
        }
    }

	private String getHint() {
		return BindingMessages.getString("Validate_Like") +  //$NON-NLS-1$
		BindingMessages.getString("Validate_Number_Examples") //$NON-NLS-1$
		+ Float.MIN_VALUE + 
		", " + Float.MAX_VALUE + "."; //$NON-NLS-1$ //$NON-NLS-2$
	}

}
