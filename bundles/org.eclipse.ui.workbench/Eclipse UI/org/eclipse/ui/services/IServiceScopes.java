/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.services;


/**
 * Different levels of service locators supported by the workbench.
 * 
 * @since 3.3
 */
public interface IServiceScopes {
	/**
	 * The global service locator scope.
	 */
	public static final String WORKBENCH_SCOPE = "org.eclipse.ui.services.IWorkbench"; //$NON-NLS-1$

	/**
	 * A workbench window service locator scope.
	 */
	public static final String WINDOW_SCOPE = "org.eclipse.ui.IWorkbenchWindow"; //$NON-NLS-1$
	
	/**
	 * A part site service locator scope.  Found in editors and views.
	 */
	public static final String PARTSITE_SCOPE = "org.eclipse.ui.part.IWorkbenchPartSite"; //$NON-NLS-1$
	
	/**
	 * A page site service locator scope.  Found in pages in a PageBookView.
	 */
	public static final String PAGESITE_SCOPE = "org.eclipse.ui.part.PageSite"; //$NON-NLS-1$
	
	/**
	 * An editor site within a MultiPageEditorPart.
	 */
	public static final String MPESITE_SCOPE = "org.eclipse.ui.part.MultiPageEditorSite"; //$NON-NLS-1$
}
