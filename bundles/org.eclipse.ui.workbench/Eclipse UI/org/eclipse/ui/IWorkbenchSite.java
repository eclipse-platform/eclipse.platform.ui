/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.services.IServiceLocator;

/**
 * The common interface between the workbench and its parts, including pages
 * within parts.
 * <p>
 * The workbench site supports a few {@link IServiceLocator services} by
 * default. If these services are used to allocate resources, it is important to
 * remember to clean up those resources after you are done with them. Otherwise,
 * the resources will exist until the workbench site is disposed. The supported
 * services are:
 * </p>
 * <ul>
 * <li>{@link ICommandService}</li>
 * <li>{@link IContextService}</li>
 * <li>{@link IHandlerService}</li>
 * <li>{@link IBindingService}. Resources allocated through this service will
 * not be cleaned up until the workbench shuts down.</li>
 * </ul>
 * <p>
 * This interface is not intended to be implemented or extended by clients.
 * </p>
 * 
 * @see org.eclipse.ui.IWorkbenchPartSite
 * @see org.eclipse.ui.part.IPageSite
 * @since 2.0
 * @noimplement This interface is not intended to be implemented by clients.
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
