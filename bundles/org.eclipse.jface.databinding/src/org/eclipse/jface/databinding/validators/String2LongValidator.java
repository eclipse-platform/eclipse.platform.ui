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
package org.eclipse.jface.databinding.validators;

import org.eclipse.jface.databinding.internal.BindingMessages;
import org.eclipse.jface.databinding.validator.IValidator;


/**
 * LongValidator.  Validate String to Long/long data input
 */
public class String2LongValidator implements IValidator {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.databinding.validator.IValidator#isPartiallyValid(java.lang.Object)
	 */
	public String isPartiallyValid(Object fragment) {
		if (((String)fragment).matches("\\-?[0-9]*")) //$NON-NLS-1$
            return null;

        return getHint();
	}
    
	/* (non-Javadoc)
	 * @see org.eclipse.jface.databinding.validator.IValidator#isValid(java.lang.Object)
	 */
	public String isValid(Object value) {
        try {
            Long.parseLong((String)value);
            return null;
        } catch (Throwable t) {
            return getHint();
        }
    }    

	private String getHint() {
		return BindingMessages.getString("Validate_RangeStart") + Long.MIN_VALUE +  //$NON-NLS-1$
			BindingMessages.getString("and") + Long.MAX_VALUE + "."; //$NON-NLS-1$ //$NON-NLS-2$
	}

}
