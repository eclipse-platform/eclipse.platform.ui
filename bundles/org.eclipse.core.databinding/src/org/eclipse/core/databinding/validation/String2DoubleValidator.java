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
package org.eclipse.core.databinding.validation;

/**
 * IntegerValidator.  Validate String to Double data input
 */
public class String2DoubleValidator extends String2DoublePrimitiveValidator {
    
    /* (non-Javadoc)
     * @see org.eclipse.core.databinding.validator.IValidator#isValid(java.lang.Object)
     */
    public ValidationError isValid(Object value) {
       if ("".equals(value)) { //$NON-NLS-1$
          return null;
       }
       return super.isValid(value);
    }
}
