/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
 * 
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IViewReference extends IWorkbenchPartReference {

	/**
	 * Returns the secondary ID for the view.
	 *
	 * @return the secondary ID, or <code>null</code> if there is no secondary id
	 * @see IWorkbenchPage#showView(String, String, int)
	 * @since 3.0
	 */
	String getSecondaryId();

	/**
	 * Returns the <code>IViewPart</code> referenced by this object. Returns
	 * <code>null</code> if the view was not instantiated or it failed to be
	 * restored. Tries to restore the view if <code>restore</code> is true.
	 */
	IViewPart getView(boolean restore);

	/**
	 * Returns true if the view is a fast view otherwise returns false.
	 */
	boolean isFastView();
}
