/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.part.components.interfaces;

/**
 * Parts can implement this interface if they wish to overload the default
 * setFocus behavior.
 * 
 * @since 3.1
 */
public interface IFocusable {
	/**
	 * Instructs the part to accept keyboard focus.
     * 
     * @return true iff the part accepted focus
	 */
	public boolean setFocus();
}
