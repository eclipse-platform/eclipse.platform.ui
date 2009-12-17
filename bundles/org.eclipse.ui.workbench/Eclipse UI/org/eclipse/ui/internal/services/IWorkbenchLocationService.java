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
	public String getServiceScope();

	/**
	 * A more numeric representation of the service level.
	 * 
	 * @return the level - 0==workbench, 1==workbench window or dialog, etc
	 */
	public int getServiceLevel();

	/**
	 * @return the workbench. May return <code>null</code>.
	 */
	public IWorkbench getWorkbench();

	/**
	 * @return the workbench window in this service locator hierarchy. May
	 * 	return <code>null</code>.
	 */
	public IWorkbenchWindow getWorkbenchWindow();

	/**
	 * @return the part site in this service locator hierarchy. May return
	 * 	<code>null</code>.
	 */
	public IWorkbenchPartSite getPartSite();

	/**
	 * @return the inner editor site for a multi-page editor in this service
	 * 	locator hierarchy. May return <code>null</code>.
	 * @see MultiPageEditorSite
	 */
	public IEditorSite getMultiPageEditorSite();

	/**
	 * @return the inner page site for a page based view in this service locator
	 * 	hierarchy. May return <code>null</code>.
	 * @see PageBookView
	 */
	public IPageSite getPageSite();
}
