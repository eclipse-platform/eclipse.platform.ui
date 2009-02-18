/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
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
public interface IInstallationPageSources {

	/**
	 * The variable name for the active installation page.  
	 */
	public static final String ACTIVE_PAGE = "org.eclipse.ui.installationPage.activePage"; //$NON-NLS-1$
	/**
	 * The variable name for the id of the active installation page.  This can be used in
	 * a <code>visibleWhen</code> expression for command contributions to the installation dialog
	 * button bar.
	 */
	public static final String ACTIVE_PAGE_ID = "org.eclipse.ui.installationPage.activePage.id"; //$NON-NLS-1$
	/**
	 * The variable name for the selection inside the active installation page.  This can be used in
	 * a <code>activeWhen</code> expression for command contributions to the installation dialog
	 * button bar.  Note that it is up to each page to set and unset this variable as necessary inside
	 * the installation page.
	 */
	public static final String ACTIVE_PAGE_SELECTION = "org.eclipse.ui.installationPage.activePage.selection"; //$NON-NLS-1$

	
}
