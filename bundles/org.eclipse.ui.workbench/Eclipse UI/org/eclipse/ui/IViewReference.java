/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui;

/**
 * Defines a reference to an IViewPart.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 */
public interface IViewReference extends IWorkbenchPartReference {
	/**
	 * Returns the IViewPart referenced by this object.
	 * Returns null if the view was not instanciated or
	 * it failed to be restored. Tries to restore the view
	 * if <code>restore</code> is true.
	 */
	public IViewPart getView(boolean restore);
	/**
	 * Returns true if the view is a fast view otherwise returns false.
	 */	
	public boolean isFastView();
}
