/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.services.IServiceLocator;

/**
 * The common interface between the workbench and its parts, including pages
 * within parts.
 * <p>
 * This interface is not intended to be implemented or extended by clients.
 * </p>
 * 
 * @see org.eclipse.ui.IWorkbenchPartSite
 * @see org.eclipse.ui.part.IPageSite
 * @since 2.0
 */
public interface IWorkbenchSite extends IAdaptable, IShellProvider,
		IServiceLocator {

	/**
	 * Returns the page containing this workbench site.
	 * 
	 * @return the page containing this workbench site
	 */
	public IWorkbenchPage getPage();

	/**
	 * Returns the selection provider for this workbench site.
	 * 
	 * @return the selection provider, or <code>null</code> if none
	 */
	public ISelectionProvider getSelectionProvider();

	/**
	 * Returns the shell for this workbench site. Not intended to be called from
	 * outside the UI thread. Clients should call IWorkbench.getDisplay() to
	 * gain access to the display rather than calling getShell().getDisplay().
	 * 
	 * <p>
	 * For compatibility, this method will not throw an exception if called from
	 * outside the UI thread, but the returned Shell may be wrong.
	 * </p>
	 * 
	 * @return the shell for this workbench site
	 */
	public Shell getShell();

	/**
	 * Returns the workbench window containing this workbench site.
	 * 
	 * @return the workbench window containing this workbench site
	 */
	public IWorkbenchWindow getWorkbenchWindow();

	/**
	 * Sets the selection provider for this workbench site.
	 * 
	 * @param provider
	 *            the selection provider, or <code>null</code> to clear it
	 */
	public void setSelectionProvider(ISelectionProvider provider);

}
