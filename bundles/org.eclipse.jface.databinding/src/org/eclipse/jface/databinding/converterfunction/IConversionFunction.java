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
package org.eclipse.jface.databinding.converterfunction;

/**
 * Interface IConversionFunction. An interface for objects that can convert from one data
 * type to another.
 */
public interface IConversionFunction {
	/**
     * Method convert.  Convert the value in 'source' to some other type
     * and return it.
     * 
	 * @param source The value to convert
	 * @return The converted type.
	 */
	public Object convert(Object source);
}
