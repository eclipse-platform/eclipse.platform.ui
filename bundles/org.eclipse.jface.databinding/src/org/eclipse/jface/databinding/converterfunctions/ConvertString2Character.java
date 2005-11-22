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
package org.eclipse.jface.databinding.converterfunctions;

import org.eclipse.jface.databinding.converterfunction.IConversionFunction;



/**
 * ConvertString2Character.
 */
public class ConvertString2Character implements IConversionFunction {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.binding.converter.IConverter#convert(java.lang.Object)
	 */
	public Object convert(Object source) {
        String s = (String) source;
        Character result;
        
        if (s.length() > 1)
            throw new IllegalArgumentException("String2Character: string too long: " + s); //$NON-NLS-1$
        
        try {
            result = new Character(s.charAt(0));
        } catch (Exception e) {
            throw new IllegalArgumentException("String2Character: " + e.getMessage() + ": " + s); //$NON-NLS-1$ //$NON-NLS-2$
        }
        
		return result;
	}

}
