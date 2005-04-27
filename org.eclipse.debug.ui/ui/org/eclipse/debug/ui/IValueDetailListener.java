/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.ui;


import org.eclipse.debug.core.model.IValue;

/**
 * Notified of detailed value descriptions.
 * <p>
 * Clients may implement this interface.
 * </p>
 * @see IDebugModelPresentation
 * @since 2.0
 */

public interface IValueDetailListener {
	/**
	 * Notifies this listener that the details for the given
	 * value have been computed as the specified result.
	 *  
	 * @param value the value for which the detail is provided
	 * @param result the detailed description of the given value
	 */
	public void detailComputed(IValue value, String result);
}
