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
 * ConvertBoolean2String.
 *
 * @author djo
 */
public class ToStringConverter implements IConversionFunction {
	
	/**
	 * A singleton for the toString() converter function
	 */
	public static final ToStringConverter TOSTRINGFUNCTION = new ToStringConverter();

	/* (non-Javadoc)
	 * @see org.eclipse.jface.binding.converter.IConverter#convert(java.lang.Object)
	 */
	public Object convert(Object source) {
        Boolean bool = (Boolean) source;
        if (bool.booleanValue()) {
            return BindingMessages.getString("Yes"); //$NON-NLS-1$
        }
        return BindingMessages.getString("No"); //$NON-NLS-1$
	}

}
