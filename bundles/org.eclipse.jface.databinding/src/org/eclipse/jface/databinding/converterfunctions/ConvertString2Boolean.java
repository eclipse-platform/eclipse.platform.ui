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

import org.eclipse.jface.databinding.converter.Messages;
import org.eclipse.jface.databinding.converterfunction.IConversionFunction;



/**
 * ConvertString2Boolean.
 *
 * @author djo
 */
public class ConvertString2Boolean implements IConversionFunction {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.binding.converter.IConverter#convert(java.lang.Object)
	 */
	public Object convert(Object source) {
        String s = (String) source;
        if (s.equals(Messages.Boolean_Yes) || s.equals(Messages.Boolean_yes) || s.equals(Messages.Boolean_true) || s.equals(Messages.Boolean_True))
            return Boolean.TRUE;
        if (s.equals(Messages.Boolean_No) || s.equals(Messages.Boolean_no) || s.equals(Messages.Boolean_false) || s.equals(Messages.Boolean_False))
            return Boolean.FALSE;
        
		throw new IllegalArgumentException(s + " is not a legal boolean value"); //$NON-NLS-1$
	}

}
