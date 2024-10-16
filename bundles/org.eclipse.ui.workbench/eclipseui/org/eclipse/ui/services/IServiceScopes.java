/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.ui.services;

/**
 * Different levels of service locators supported by the workbench.
 *
 * @since 3.3
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IServiceScopes {
	/**
	 * The global service locator scope.
	 */
	String WORKBENCH_SCOPE = "org.eclipse.ui.services.IWorkbench"; //$NON-NLS-1$

	/**
	 * A sub-scope to the global scope that is not the workbench window.
	 *
	 * @since 3.5
	 */
	String DIALOG_SCOPE = "org.eclipse.ui.services.IDialog"; //$NON-NLS-1$
	/**
	 * A workbench window service locator scope.
	 */
	String WINDOW_SCOPE = "org.eclipse.ui.IWorkbenchWindow"; //$NON-NLS-1$

	/**
	 * A part site service locator scope. Found in editors and views.
	 */
	String PARTSITE_SCOPE = "org.eclipse.ui.part.IWorkbenchPartSite"; //$NON-NLS-1$

	/**
	 * A page site service locator scope. Found in pages in a PageBookView.
	 */
	String PAGESITE_SCOPE = "org.eclipse.ui.part.PageSite"; //$NON-NLS-1$

	/**
	 * An editor site within a MultiPageEditorPart.
	 */
	String MPESITE_SCOPE = "org.eclipse.ui.part.MultiPageEditorSite"; //$NON-NLS-1$
}
