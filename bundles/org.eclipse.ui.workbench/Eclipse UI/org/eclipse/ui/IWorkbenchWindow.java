/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui;

import org.eclipse.core.runtime.IAdaptable; 
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.swt.widgets.Shell;

/**
 * A workbench window is a top level window in a workbench. Visually, a workbench 
 * window has a menubar, a toolbar, a status bar, and a main area for displaying
 * a single page consisting of a collection of views and editors.
 * <p>
 * Each workbench window has a collection of 0 or more pages; the active page is
 * the one that is being presented to the end user; at most one page is active
 * in a window at a time.
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 *
 * @see IWorkbenchPage
 */
public interface IWorkbenchWindow extends IPageService,
	IRunnableContext {
/**
 * Closes this workbench window.
 * <p>
 * If the window has an open editor with unsaved content, the user will be
 * given the opportunity to save it.
 * </p>
 *
 * @return <code>true</code> if the window was successfully closed,
 *   and <code>false</code> if it is still open
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
 * Returns the part service which tracks part activation within this workbench
 * window.
 *
 * @return the part service
 */
public IPartService getPartService();
/**
 * Returns the selection service which tracks selection within this workbench
 * window.
 *
 * @return the selection service
 */
public ISelectionService getSelectionService();
/**
 * Returns this workbench window's shell.
 *
 * @return the shell containing this window's controls
 */
public Shell getShell();
/**
 * Returns the workbench for this window.
 *
 * @return the workbench
 */
public IWorkbench getWorkbench();
/**
 * Returns whether the specified menu is an application menu as
 * opposed to a part menu.  Application menus contain items which affect
 * the workbench or window.  Part menus contain items which affect 
 * the active part (view or editor).
 * <p>
 * This is typically used during "in place" editing.  Application menus
 * should be preserved during menu merging.  All other menus may be removed
 * from the window.
 * </p>
 *
 * @param menuId the menu id
 * @return <code>true</code> if the specified menu is an application menu,
 *  and <code>false</code> if is not
 */
public boolean isApplicationMenu(String menuId);
/**
 * Creates and opens a new workbench page.  The perspective of the new page 
 * is defined by the specified perspective ID.  The new page become active.
 * <p>
 * <b>Note:</b> Since release 2.0, a window is limited to contain at most
 * one page. If a page exist in the window when this method is used, then
 * another window is created for the new page.  Callers are strongly
 * recommended to use the <code>IWorkbench.showPerspective</code> APIs to
 * programmatically show a perspective.
 * </p>
 * 
 * @param perspectiveId the perspective id for the window's initial page
 * @param input the page input, or <code>null</code> if there is no current input.
 *		This is used to seed the input for the new page's views.
 * @return the new workbench page
 * @exception WorkbenchException if a page could not be opened
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
 * another window is created for the new page.  Callers are strongly
 * recommended to use the <code>IWorkbench.showPerspective</code> APIs to
 * programmatically show a perspective.
 * </p>
 * 
 * @param input the page input, or <code>null</code> if there is no current input.
 *		This is used to seed the input for the new page's views.
 * @return the new workbench window
 * @exception WorkbenchException if a page could not be opened
 * 
 * @see IWorkbench#showPerspective(String, IWorkbenchWindow, IAdaptable)
 */
public IWorkbenchPage openPage(IAdaptable input)
	throws WorkbenchException;
/**
 * Sets or clears the currently active page for this workbench window.
 * 
 * @param page the new active page
 */
public void setActivePage(IWorkbenchPage page);
}
