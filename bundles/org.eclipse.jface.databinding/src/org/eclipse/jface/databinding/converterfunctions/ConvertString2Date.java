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
 * Convert a String to a java.util.Date, respecting the current locale
 * 
 * @since 3.2
 */
public class ConvertString2Date extends DateConversionSupport implements IConversionFunction {
	public Object convert(Object source) {
		return parse(source.toString());
	}	
}
