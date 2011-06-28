/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.browser;

import org.eclipse.ui.browser.IWebBrowser;

/**
 * A 
 * 
 * <p>This interface is not intended to be implemented by clients.</p>
 * 
 * @since 1.0
 */
public interface IBrowserExt {
	/**
	 * Returns the id of this client. Each known client has a distinct id. 
	 * Ids are intended to be used internally as keys; they are not
	 * intended to be shown to end users.
	 * 
	 * @return the client id
	 */
	public String getId();

	/**
	 * Returns the displayable name for this client.
	 * <p>
	 * Note that this name is appropriate for the current locale.
	 * </p>
	 *
	 * @return a displayable name for this client
	 */
	public String getName();

	/**
	 * Returns the parameters for this browser.
	 *
	 * @return the parameters for this browser
	 */
	public String getParameters();

	/**
	 * Returns the browser's executable.
	 *
	 * @return the browser's executable
	 */
	public String getExecutable();
	
	/**
	 * Returns the OSs that this browser is available on.
	 *
	 * @return an array of OSs that this browser runs on
	 */
	public String getOS();
	
	/**
	 * Returns the default install locations of this browser.
	 *
	 * @return the default install locations of this browser
	 */
	public String[] getDefaultLocations();
	
	/**
	 * Checks whether the factory can work on the user system.
	 * 
	 * @return false if the factory cannot work on this system; for example the
	 *    required native browser required by browser adapters that it
	 *    creates is not installed
	 */
	public boolean isAvailable();

	/**
	 * Obtains a new instance of a web browser.
	 * 
	 * @return instance of IBrowser
	 */
	public IWebBrowser createBrowser(String id, String location, String parameters);
}
