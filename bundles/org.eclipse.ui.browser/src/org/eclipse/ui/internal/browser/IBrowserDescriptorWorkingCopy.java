/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.browser;

/**
 * A working copy of an external web browser.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 *
 * @see org.eclipse.ui.browser.IWebBrowser
 * @see IBrowserDescriptor
 * @since 1.0
 */
public interface IBrowserDescriptorWorkingCopy extends IBrowserDescriptor {
	/**
	 * Renames this browser to the specified name. The new name cannot be <code>null</code>.
	 *
	 * @param name the new name for this browser
	 */
	public void setName(String name);

	/**
	 * Set the location of this browsers executable. The location must not be <code>null</code>.
	 *
	 * @param location the new location
	 */
	public void setLocation(String location);

	/**
	 * Set the parameters for this browser. If there are no parameters, the parameters
	 * should be null.
	 *
	 * @param params the new parameters
	 */
	public void setParameters(String params);

	/**
	 * Saves the changes to this working copy and returns the resulting web browser.
	 *
	 * @return the modified or created browser
	 */
	public IBrowserDescriptor save();
}