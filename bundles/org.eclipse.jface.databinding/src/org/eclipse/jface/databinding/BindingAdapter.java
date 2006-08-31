/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.databinding;

import org.eclipse.jface.internal.databinding.provisional.validation.ValidationError;

/**
 * A default implementation for an IBindingListener event handler.
 * 
 * @since 1.0
 * 
 * implementation note: This class implements a deprecated interface for backwards compatibility.
 */
public class BindingAdapter implements IBindingListener {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.databinding.IBindingListener#bindingEvent(org.eclipse.jface.databinding.BindingEvent)
	 */
	public ValidationError bindingEvent(BindingEvent e) {
		return null;
	}

}
