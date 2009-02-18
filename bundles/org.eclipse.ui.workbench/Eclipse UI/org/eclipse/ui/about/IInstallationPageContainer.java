/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.about;


/**
 * <em>This API is experimental and will change before 3.5 ships</em>
 * 
 * @since 3.5
 */
public interface IInstallationPageContainer {

	/**
	 * URI to be provided to the IMenuService for additions to the button bar.
	 * 
	 * This may not be desirable. We've never had a "button manager" before now,
	 * and this may be a can of worms we dont want to open.
	 * 
	 * @return the button bar uri
	 */
	public String getButtonBarURI();
	
	/**
	 * Closes the window that is hosting this container.
	 */
	public void closeContainer();
	
}
