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
import org.eclipse.jface.databinding.internal.BindingMessages;



/**
 * ConvertString2Boolean.
 */
public class ConvertString2Boolean implements IConversionFunction {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.binding.converter.IConverter#convert(java.lang.Object)
	 */
	public Object convert(Object source) {
        String s = (String) source;
        if (s.equals(BindingMessages.getString("Yes")) || s.equals(BindingMessages.getString("yes")) || s.equals(BindingMessages.getString("true")) || s.equals(BindingMessages.getString("True"))) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            return Boolean.TRUE;
        if (s.equals(BindingMessages.getString("No")) || s.equals(BindingMessages.getString("no")) || s.equals(BindingMessages.getString("false")) || s.equals(BindingMessages.getString("False")))    //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$
            return Boolean.FALSE;
        
		throw new IllegalArgumentException(s + " is not a legal boolean value"); //$NON-NLS-1$
	}

}
