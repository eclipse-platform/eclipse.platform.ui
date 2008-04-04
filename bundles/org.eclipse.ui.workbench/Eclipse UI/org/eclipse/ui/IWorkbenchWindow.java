/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.services.IServiceLocator;

/**
 * A workbench window is a top level window in a workbench. Visually, a
 * workbench window has a menubar, a toolbar, a status bar, and a main area for
 * displaying a single page consisting of a collection of views and editors.
 * <p>
 * Each workbench window has a collection of 0 or more pages; the active page is
 * the one that is being presented to the end user; at most one page is active
 * in a window at a time.
 * </p>
 * <p>
 * The workbench window supports a few {@link IServiceLocator services} by
 * default. If these services are used to allocate resources, it is important to
 * remember to clean up those resources after you are done with them. Otherwise,
 * the resources will exist until the workbench window is closed. The supported
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
 * This interface is not intended to be implemented by clients.
 * </p>
 * 
 * @see IWorkbenchPage
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IWorkbenchWindow extends IPageService, IRunnableContext,
		IServiceLocator, IShellProvider {
    /**
     * Closes this workbench window.
     * <p>
     * If the window has an open editor with unsaved content, the user will be
     * given the opportunity to save it.
     * </p>
     * 
     * @return <code>true</code> if the window was successfully closed, and
     *         <code>false</code> if it is still open
     */
    public boolean close();

    /**
     * Returns the currently active page for this workbench window.
     * 
     * @return the active page, or <code>null</code> if none
     */
    public IWorkbenchPage getActivePage();

    /**
     * Returns a list of the pages in this workbench window.
     * <p>
     * Note that each window has its own pages; pages are never shared between
     * different windows.
     * </p>
     * 
     * @return a list of pages
     */
    public IWorkbenchPage[] getPages();

    /**
     * Returns the part service which tracks part activation within this
     * workbench window.
     * 
     * @return the part service
     */
    public IPartService getPartService();

    /**
     * Returns the selection service which tracks selection within this
     * workbench window.
     * 
     * @return the selection service
     */
    public ISelectionService getSelectionService();

    /**
     * Returns this workbench window's shell.
     * 
     * @return the shell containing this window's controls or <code>null</code>
     *   if the shell has not been created yet or if the window has been closed
     */
    public Shell getShell();

    /**
     * Returns the workbench for this window.
     * 
     * @return the workbench
     */
    public IWorkbench getWorkbench();

    /**
     * Returns whether the specified menu is an application menu as opposed to
     * a part menu. Application menus contain items which affect the workbench
     * or window. Part menus contain items which affect the active part (view
     * or editor).
     * <p>
     * This is typically used during "in place" editing. Application menus
     * should be preserved during menu merging. All other menus may be removed
     * from the window.
     * </p>
     * 
     * @param menuId
     *            the menu id
     * @return <code>true</code> if the specified menu is an application
     *         menu, and <code>false</code> if it is not
     */
    public boolean isApplicationMenu(String menuId);

    /**
     * Creates and opens a new workbench page. The perspective of the new page
     * is defined by the specified perspective ID. The new page become active.
     * <p>
     * <b>Note:</b> Since release 2.0, a window is limited to contain at most
     * one page. If a page exist in the window when this method is used, then
     * another window is created for the new page. Callers are strongly
     * recommended to use the <code>IWorkbench.showPerspective</code> APIs to
     * programmatically show a perspective.
     * </p>
     * 
     * @param perspectiveId
     *            the perspective id for the window's initial page
     * @param input
     *            the page input, or <code>null</code> if there is no current
     *            input. This is used to seed the input for the new page's
     *            views.
     * @return the new workbench page
     * @exception WorkbenchException
     *                if a page could not be opened
     * 
     * @see IWorkbench#showPerspective(String, IWorkbenchWindow, IAdaptable)
     */
    public IWorkbenchPage openPage(String perspectiveId, IAdaptable input)
            throws WorkbenchException;

    /**
     * Creates and opens a new workbench page. The default perspective is used
     * as a template for creating the page. The page becomes active.
     * <p>
     * <b>Note:</b> Since release 2.0, a window is limited to contain at most
     * one page. If a page exist in the window when this method is used, then
     * another window is created for the new page. Callers are strongly
     * recommended to use the <code>IWorkbench.showPerspective</code> APIs to
     * programmatically show a perspective.
     * </p>
     * 
     * @param input
     *            the page input, or <code>null</code> if there is no current
     *            input. This is used to seed the input for the new page's
     *            views.
     * @return the new workbench window
     * @exception WorkbenchException
     *                if a page could not be opened
     * 
     * @see IWorkbench#showPerspective(String, IWorkbenchWindow, IAdaptable)
     */
    public IWorkbenchPage openPage(IAdaptable input) throws WorkbenchException;

    /**
     * This specialization of IRunnableContext#run(boolean, boolean,
     * IRunnableWithProgress) blocks until the runnable has been run,
     * regardless of the value of <code>fork</code>.
     * It is recommended that <code>fork</code> is set to
     * true in most cases. If <code>fork</code> is set to <code>false</code>,
     * the runnable will run in the UI thread and it is the runnable's
     * responsibility to call <code>Display.readAndDispatch()</code>
     * to ensure UI responsiveness.
     * 
     * @since 3.2
     */
    public void run(boolean fork, boolean cancelable, IRunnableWithProgress runnable) throws InvocationTargetException, InterruptedException;

    /**
     * Sets or clears the currently active page for this workbench window.
     * 
     * @param page
     *            the new active page
     */
    public void setActivePage(IWorkbenchPage page);
    
    /**
	 * <p>
	 * Return the extension tracker for the workbench. This tracker may be used
	 * by plug-ins to ensure responsiveness to changes to the plug-in registry.
	 * </p>
	 * <p>
	 * The tracker at this level of the workbench is typically used to track
	 * elements that persist for the life of the workbench. For example, the
	 * action objects corresponding to new wizards contributed by plug-ins fall
	 * into this category.
	 * </p>
	 * 
	 * @return the extension tracker
	 * @see IWorkbench#getExtensionTracker()
	 * @see IWorkbenchPage#getExtensionTracker()
	 * @since 3.1
	 */
    public IExtensionTracker getExtensionTracker();
}
