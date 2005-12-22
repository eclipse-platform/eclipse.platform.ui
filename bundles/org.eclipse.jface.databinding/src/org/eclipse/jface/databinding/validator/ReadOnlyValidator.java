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

import org.eclipse.jface.internal.databinding.BindingMessages;


/**
 * ReadOnlyValidator.  The validator that can be used for read-only fields.
 */
public class ReadOnlyValidator implements IValidator {
    
    private static ReadOnlyValidator singleton = null;
    
    /**
     * Returns the ReadOnlyValidator
     * 
     * @return the ReadOnlyValidator
     */
    public static ReadOnlyValidator getDefault() {
        if (singleton == null) {
            singleton = new ReadOnlyValidator();
        }
        return singleton;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.jface.databinding.validator.IValidator#isPartiallyValid(java.lang.Object)
	 */
	public String isPartiallyValid(Object fragment) {
		// No changes are allowed
		return BindingMessages.getString("Validate_NoChangeAllowed"); //$NON-NLS-1$
	}

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ICellEditorValidator#isValid(java.lang.Object)
     */
    public String isValid(Object value) {
        // The current value is always valid
        return null;
    }

}
