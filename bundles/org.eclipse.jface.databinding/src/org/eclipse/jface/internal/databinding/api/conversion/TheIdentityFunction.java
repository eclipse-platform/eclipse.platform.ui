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
package org.eclipse.jface.internal.databinding.api.conversion;




/**
 * TheIdentityConverter.  Returns the source value (the identity function).
 */
public class TheIdentityFunction implements IConverter {

	/**
	 * Identity converter singleton
	 */
	public static final IConverter IDENTITY = new TheIdentityFunction();

	/* (non-Javadoc)
	 * @see org.eclipse.jface.binding.converter.IConverter#convert(java.lang.Object)
	 */
	public Object convert(Object source) {
        if (source == null) {
            return ""; //$NON-NLS-1$
        }
		return source;
	}

	public Object getFromType() {
		return null;
	}

	public Object getToType() {
		return null;
	}

}
