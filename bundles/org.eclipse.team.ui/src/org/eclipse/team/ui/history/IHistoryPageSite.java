/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.ui.history;

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.*;

/**
 * A site which provides access to the context in which this page
 * is being displayed. Instances of this interface serve a similar purpose
 * to <code>IWorkbenchSite</code> instances but is provided as a separate
 * objects to allow clients to access the different site types 
 * (view, editor, dialog) using a common interface. This interface also provides
 * access to the part for the site because this is required by some UI
 * components. Clients should not need to access the part.
 * <p>
 * Clients can determine the type of workbench site by doing <code>instanceof</code>
 * checks on the object returned by <code>getWorkbenchSite</code>. Similar
 * <code>instanceof</code> checks can be done with the part.
 * <p>
 * This interface is not intended to be implemented by clients.
 * 
 * @since 3.2
 */
public interface IHistoryPageSite {
	/**
	 * Return the workbench part site for the page
	 * or <code>null</code> if a workbench part site is not available (e.g. if
	 * the page is being shown in a dialog). 
	 * @return the workbench part site for the page or <code>null</code>
	 */
	IWorkbenchPartSite getWorkbenchPartSite();
	
	/**
	 * Return the workbench part for the page
	 * or <code>null</code> if a workbench part is not available (e.g. if
	 * the page is being shown in a dialog). 
	 * @return the workbench part for the page or <code>null</code>
	 */
	IWorkbenchPart getPart();
	
	/**
	 * Returns the shell for this site.
	 * @return the shell for this site
	 */
	Shell getShell();

	/**
	 * Get the selection provider that gives access to the selection
	 * of the synchronize page associated with this page site.
	 * @return the selection provider for the page
	 */
	ISelectionProvider getSelectionProvider();

	/**
	 * Sets the selection provider for this workbench site.
	 * @param provider the selection provider, or <code>null</code> to clear it
	 */
	void setSelectionProvider(ISelectionProvider provider);

	/**
	 * Get the keybinding service for the site or <code>null</code>
	 * if one is not available.
	 * @return the keybinding service for the site or <code>null</code>
	 * if one is not available
	 */
	IKeyBindingService getKeyBindingService();

	/**
	 * Give the page focus.
	 */
	void setFocus();
	
	/**
	 * Returns the action bars for this history page site.
	 *
	 * @return the action bars
	 */
	IActionBars getActionBars();

	/**
	 * Sets the Toolbar Manager for this history page site.
	 * @param toolBarManager
	 */
	void setToolBarManager(ToolBarManager toolBarManager);

}
