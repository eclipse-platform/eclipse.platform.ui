package org.eclipse.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.preference.PreferenceManager;

/**
 * A workbench is the root object for the Eclipse Platform user interface.
 * <p>
 * A <b>workbench</b> has one or more main windows which present to the end user
 * information based on some underlying model, typically on resources in an
 * underlying workspace. A workbench usually starts with a single open window,
 * and automatically closes when its last window closes.
 * </p>
 * <p>
 * Each <b>workbench window</b> has a collection of <b>pages</b>; the active
 * page is the one that is being presented to the end user; at most one page is
 * active in a window at a time.
 * </p>
 * <p>
 * Each workbench page has a collection of <b>workbench parts</b>, of which there
 * are two kinds: views and editors. A page's parts are arranged (tiled or 
 * stacked) for presentation on the screen. The arrangement is not fixed; the 
 * user can arrange the parts as they see fit. A <b>perspective</b> is a
 * template for a page, capturing a collection of parts and their arrangement.
 * </p>
 * <p>
 * The platform creates a workbench when the workbench plug-in is activated;
 * since this happens at most once during the life of the running platform,
 * there is only one workbench instance. Due to its singular nature, it is
 * commonly referred to as <it>the</it> workbench.
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 *
 * @see org.eclipse.ui.plugin.IAbstractUIPlugin#getWorkbench
 */
public interface IWorkbench {
/**
 * Closes this workbench and all its open windows.
 * <p>
 * If the workbench has an open editor with unsaved content, the user will be
 * given the opportunity to save it.
 * </p>
 *
 * @return <code>true</code> if the workbench was successfully closed,
 *   and <code>false</code> if it is still open
 */
public boolean close();
/**
 * Returns the currently active window for this workbench (if any).
 * 
 * @return the active workbench window, or <code>null</code> if the currently 
 *   active window is not a workbench window
 */
public IWorkbenchWindow getActiveWorkbenchWindow();
/**
 * Returns the editor registry for the workbench.
 *
 * @return the workbench editor registry
 */
public IEditorRegistry getEditorRegistry();
/**
 * Returns the perspective registry for the workbench.
 *
 * @return the workbench perspective registry
 */
public IPerspectiveRegistry getPerspectiveRegistry();
/**
 * Returns the preference manager for the workbench.
 *
 * @return the workbench preference manager
 */
public PreferenceManager getPreferenceManager();
/**
 * Returns the shared images for the workbench.
 *
 * @return the shared image manager
 */
public ISharedImages getSharedImages();
/**
 * Returns a list of the open main windows associated with this workbench.
 * Note that wizards and dialogs are not included in this list since they
 * are not considered main windows.
 *
 * @return a list of open windows
 */
public IWorkbenchWindow [] getWorkbenchWindows();
/**
 * Creates and opens a new workbench page.  If the user preference for 
 * "Open Perspective" is "in current window", the page will be created in
 * the current window.  Otherwise, it will be created in a new window.
 * The perspective of the new page is the default perspective.
 * On return, the new window and new page be active.
 *
 * @param input the page input, or <code>null</code> if there is no current input.
 *		This is used to seed the input for the new page's views.
 * @return the new workbench page
 * @exception WorkbenchException if a new page could not be opened
 */
public IWorkbenchPage openPage(final IAdaptable input) 
	throws WorkbenchException;
/**
 * Creates and opens a new workbench page.  If the user preference for 
 * "Open Perspective" is "in current window", the page will be created in
 * the current window.  Otherwise, it will be created in a new window.
 * The perspective of the new page is defined by the specified perspective ID.  
 * On return, the new window and new page be active.
 * <p>
 * In most cases where this method is used the caller is tightly coupled to
 * a particular perspective.  They define it in the registry and contribute some
 * user interface action to open or activate it.  In situations like this a
 * static variable is often used to identify the perspective Id.
 * </p><p>
 * The workbench also defines a number of menu items to activate or open each
 * registered perspective. A complete list of these perspectives is available 
 * from the perspective registry found on IWorkbenchPlugin.
 * </p>
 * @param perspectiveId the perspective id for the window's initial page
 * @param input the page input, or <code>null</code> if there is no current input.
 *		This is used to seed the input for the new page's views.
 * @return the new workbench page
 * @exception WorkbenchException if a new page could not be opened
 */
public IWorkbenchPage openPage(final String perspID, final IAdaptable input) 
	throws WorkbenchException;
/**
 * Creates and opens a new workbench window with one page.  The perspective of
 * the new page is defined by the specified perspective ID.  The new window and new 
 * page become active.
 * <p>
 * In most cases where this method is used the caller is tightly coupled to
 * a particular perspective.  They define it in the registry and contribute some
 * user interface action to open or activate it.  In situations like this a
 * static variable is often used to identify the perspective Id.
 * </p><p>
 * The workbench also defines a number of menu items to activate or open each
 * registered perspective. A complete list of these perspectives is available 
 * from the perspective registry found on IWorkbenchPlugin.
 * </p>
 * @param perspectiveId the perspective id for the window's initial page
 * @param input the page input, or <code>null</code> if there is no current input.
 *		This is used to seed the input for the new page's views.
 * @return the new workbench window
 * @exception WorkbenchException if a new window and page could not be opened
 */
public IWorkbenchWindow openWorkbenchWindow(String perspID, IAdaptable input)
	throws WorkbenchException;
/**
 * Creates and opens a new workbench window. The default perspective is used
 * as a template for creating the new window's first page. The new window and new 
 * page become active.
 *
 * @param input the page input, or <code>null</code> if there is no current input.
 *		This is used to seed the input for the new page's views.
 * @return the new workbench window
 * @exception WorkbenchException if a new window and page could not be opened
 */
public IWorkbenchWindow openWorkbenchWindow(IAdaptable input)
	throws WorkbenchException;
}
