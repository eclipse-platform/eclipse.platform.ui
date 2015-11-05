/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.IAdaptable;

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

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		return Adapters.adapt(element, adapter);
	}
}
