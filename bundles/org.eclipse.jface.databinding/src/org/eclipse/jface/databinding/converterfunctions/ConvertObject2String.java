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

import org.eclipse.jface.databinding.converterfunction.ConversionFunctionRegistry;
import org.eclipse.jface.databinding.converterfunction.IConversionFunction;


/**
 * ConvertObject2String. This is the fall-back converter.  If the property type
 * is abstract, we might not have a converter registered for the property type.
 * In that case, this converter will be used, which will try to find a 
 * converter that will convert the source object's actual (concrete) type
 * to a String and use that if it finds one.
 * <p>
 * Note that there is no way to reverse this process.
 *
 * @author djo
 */
public class ConvertObject2String implements IConversionFunction {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.binding.converter.IConverter#convert(java.lang.Object)
	 */
	public Object convert(Object source) {
        if (source == null) 
            return ""; //$NON-NLS-1$
        
        IConversionFunction converter = ConversionFunctionRegistry.get(source.getClass(), String.class);
        if (converter != null) {
            return converter.convert(source);
        }
        
		throw new IllegalArgumentException("Unable to find a converter for " + source.getClass().getName() + " to String"); //$NON-NLS-1$ //$NON-NLS-2$
	}

}
