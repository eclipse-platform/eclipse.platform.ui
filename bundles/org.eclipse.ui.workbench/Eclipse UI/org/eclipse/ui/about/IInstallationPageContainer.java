/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
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
 * <em>This API is experiemental and will change before 3.5 ships</em>
 * 
 * @since 3.5
 */
public interface IInstallationPageContainer {

	/**
	 * Updates the message (or error message) shown in the message line to
	 * reflect the state of the currently active page in this container.
	 * <p>
	 * This method is called by the container itself when its preference page
	 * changes and may be called by the page at other times to force a message
	 * update.
	 * </p>
	 */
	public void updateMessage();

	/**
	 * URI to be provided to the IMenuService for additions to the toolbar.
	 * 
	 * @return
	 */
	public String getToolbarURI();

	/**
	 * URI to be provided to the IMenuService for additions to the button bar.
	 * 
	 * This may not be desirable. We've never had a "button manager" before now,
	 * and this may be a can of worms we dont want to open.
	 * 
	 * @return
	 */
	public String getButtonBarURI();
}
