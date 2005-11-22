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
 * TheNullConverter.  TheNullObjectPattern for any type to String conversion.
 */
public class TheNullStringFunction implements IConversionFunction {

	/**
	 * Null converter singleton
	 */
	public static final IConversionFunction NULL = new TheNullStringFunction();

	/* (non-Javadoc)
	 * @see org.eclipse.jface.binding.converter.IConverter#convert(java.lang.Object)
	 */
	public Object convert(Object source) {
        return ""; //$NON-NLS-1$
	}

}
