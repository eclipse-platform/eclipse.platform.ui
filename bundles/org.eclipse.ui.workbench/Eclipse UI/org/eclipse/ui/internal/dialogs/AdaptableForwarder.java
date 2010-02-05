/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.internal.util.Util;

/**
 * Class that wraps an object and forwards adapter calls if possible, otherwise
 * returns the object. This is used to maintain API compatibility with methods that
 * need an IAdaptable but when the operation supports a broader type.
 * 
 * @since 3.2
 */
public class AdaptableForwarder implements IAdaptable {

	private Object element;
	
	/**
	 * Create a new instance of the receiver.
	 * @param element
	 */
	public AdaptableForwarder(Object element) {
		this.element = element;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
        return Util.getAdapter(element, adapter);
	}
}
