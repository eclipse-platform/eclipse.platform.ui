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

import org.eclipse.swt.widgets.Button;


/**
 * <em>This API is experimental and will change before 3.5 ships</em>
 * 
 * @since 3.5
 */
public interface IInstallationPageContainer {

	/**
	 * Register a button as belonging to a particular page in the container.
	 * The container will manage the placement and visibility of page buttons.
	 * 
	 * @param page the page that created the button
	 * @param button the button to be managed
	 * 
	 */
	public void registerPageButton(InstallationPage page, Button button);
	/**
	 * Closes the window that is hosting this container.
	 */
	public void closeContainer();
	
}
