/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.browser;
/**
 * An interface to an external Web browser.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * @see IBrowserDescriptorWorkingCopy
 * @since 1.0
 */
public interface IBrowserDescriptor {
	public static final String URL_PARAMETER = "%URL%"; //$NON-NLS-1$

	/**
	 * Returns the displayable name of the Web browser.
	 *  
	 * @return the name
	 */
	public String getName();

	/**
	 * Returns the absolute path to the location of the Web browser's executable.
	 * The location is platform specific and may return null if no location has been
	 * set.
	 * 
	 * @return the path to the executable
	 */
	public String getLocation();

	/**
	 * Returns the parameters that should be used to launch the Web browser executable.
	 * %URL% is used if the URL should be inserted into the parameters. If there are
	 * no parameters, <code>null</code> is returned.
	 * 
	 * @return the parameters
	 */
	public String getParameters();

	/**
	 * Deletes this Web browser. The browser will no longer be available to users.
	 * This method has no effect if the browser has already been deleted or has never
	 * been saved.
	 */
	public void delete();

	/**
	 * Returns whether this browser is a working copy. Browsers which return
	 * <code>true</code> to this method can be safely cast to 
	 * <code>org.eclipse.ui.internal.browser.IBrowserDescriptorWorkingCopy</code>
	 * 
	 * @return whether this browser is a working copy
	 */
	public boolean isWorkingCopy();

	/**
	 * Returns a working copy of this browser. Changes to the working copy will be
	 * applied to this browser when saved.
	 * 
	 * @return a working copy of this browser
	 */
	public IBrowserDescriptorWorkingCopy getWorkingCopy();
}