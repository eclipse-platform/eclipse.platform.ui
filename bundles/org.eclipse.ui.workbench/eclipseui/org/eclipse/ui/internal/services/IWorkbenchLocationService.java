/*******************************************************************************
 * Copyright (c) 2008, 2015 IBM Corporation and others.
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

package org.eclipse.ui.internal.services;

import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.part.MultiPageEditorSite;
import org.eclipse.ui.part.PageBookView;
import org.eclipse.ui.services.IServiceScopes;

/**
 * Query where you are in the workbench hierarchy.
 *
 * @since 3.4
 */
public interface IWorkbenchLocationService {
	/**
	 * Get the service scope.
	 *
	 * @return the service scope. May return <code>null</code>.
	 * @see IServiceScopes#PARTSITE_SCOPE
	 */
	String getServiceScope();

	/**
	 * A more numeric representation of the service level.
	 *
	 * @return the level - 0==workbench, 1==workbench window or dialog, etc
	 */
	int getServiceLevel();

	/**
	 * @return the workbench. May return <code>null</code>.
	 */
	IWorkbench getWorkbench();

	/**
	 * @return the workbench window in this service locator hierarchy. May return
	 *         <code>null</code>.
	 */
	IWorkbenchWindow getWorkbenchWindow();

	/**
	 * @return the part site in this service locator hierarchy. May return
	 *         <code>null</code>.
	 */
	IWorkbenchPartSite getPartSite();

	/**
	 * @return the inner editor site for a multi-page editor in this service locator
	 *         hierarchy. May return <code>null</code>.
	 * @see MultiPageEditorSite
	 */
	IEditorSite getMultiPageEditorSite();

	/**
	 * @return the inner page site for a page based view in this service locator
	 *         hierarchy. May return <code>null</code>.
	 * @see PageBookView
	 */
	IPageSite getPageSite();
}
