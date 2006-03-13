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

import java.util.Date;

import org.eclipse.jface.internal.databinding.internal.BindingMessages;
import org.eclipse.jface.internal.databinding.provisional.conversion.DateConversionSupport;


/**
 * DateValidator.  An IValidator implementation for dates.
 */
public class String2DateValidator extends DateConversionSupport implements IValidator {
	/* (non-Javadoc)
	 * @see org.eclipse.jface.databinding.validator.IValidator#isPartiallyValid(java.lang.Object)
	 */
	public ValidationError isPartiallyValid(Object fragment) {
		// TODO: Can we do any sensible (locale-independent) checking here?
		return null;
	}
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.databinding.validator.IValidator#isValid(java.lang.Object)
     */
    public ValidationError isValid(Object value) {
        return parse((String)value)!=null ? null : ValidationError.error(getHint());
    }

	private String getHint() {
		Date sampleDate=new Date();
		StringBuffer samples=new StringBuffer();
		for(int formatterIdx=1;formatterIdx<numFormatters()-2;formatterIdx++) {
			samples.append('\'');
			samples.append(format(sampleDate,formatterIdx));
			samples.append("', "); //$NON-NLS-1$
		}
        samples.append('\'');
        samples.append(format(sampleDate,0));
        samples.append('\'');
		return BindingMessages.getString("Examples") + ": "+samples+",...";  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
	}
}
