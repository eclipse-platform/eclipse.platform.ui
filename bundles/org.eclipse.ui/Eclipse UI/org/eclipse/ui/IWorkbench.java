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
 * Creates a copy of an existing workbench page, and opens it in the workbench.
 * If the user preference for "Open Perspective" is "in current window", the 
 * page will be created in the current window.  Otherwise, it will be created 
 * in a new window.
 * On return, the new window and new page will be active.
 *
 * @param page the page to clone
 * @return the new workbench page
 * @exception WorkbenchException if a new page could not be opened
 * @since 2.0
 */
public IWorkbenchPage clonePage(IWorkbenchPage page) 
	throws WorkbenchException;
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
 * Returns the marker help registry for the workbench.
 * 
 * @since 2.0
 * @return the marker help registry
 */
public IMarkerHelpRegistry getMarkerHelpRegistry();
/**
 * Returns a list of the open main windows associated with this workbench.
 * Note that wizards and dialogs are not included in this list since they
 * are not considered main windows.
 *
 * @return a list of open windows
 */
public IWorkbenchWindow[] getWorkbenchWindows();
/**
 * Shows a workbench page with the given input using the default perspective.
 * Same as calling <code>openPage(input, getPerspectiveRegistry().getDefaultPerspective())</code>.
 * 
 * @param input the page input, or <code>null</code> if there is no current input.
 *		This is used to seed the input for the new page's views.
 * @return the workbench page which was opened or activated
 * @exception WorkbenchException if a new page could not be opened
 * @since 2.0
 */
public IWorkbenchPage openPage(IAdaptable input) 
	throws WorkbenchException;
/**
 * Shows a workbench page with the given input and perspective.
 * This may either create a new page or reuse an existing page,
 * depending on workbench preferences such as the "Open Perspective"
 * and "Reuse Perspectives" settings.
 * <p>
 * The exact policy for this is controlled by the workbench, and is subject to change.
 * The current policy is as follows:
 * <ul>
 * <li>
 * If there is an existing page with the given perspective and the same input,
 * it is activated instead of creating a new one.
 * </li>
 * <li>
 * Otherwise, if the "Reuse Perspectives" setting is checked and there is an 
 * existing page with the same input, it is activated and its perspective is
 * switched to the given perpective.
 * </li>
 * <li>
 * Otherwise, a new page is created with the given perspective. 
 * If the user preference for "Open Perspective" is "In a new window", 
 * the page is created in a new window.  
 * Otherwise, it is created in the current window.
 * On return, the new window and new page are active.
 * </li>
 * </ul>
 * </p>
 * <p>
 * In most cases where this method is used, the caller is tightly coupled to
 * a particular perspective, and may even define it in the registry.
 * </p>
 * <p>
 * A complete list of available perspectives can be retrieved from the perspective 
 * registry.
 * </p>
 * 
 * @param perspId the perspective id to show
 * @param input the page input, or <code>null</code> if there is no current input.
 *		This is used to seed the input for the page's views
 * @param keyState the state of the keyboard modifier keys, or 0 if undefined
 * @return the workbench page which was opened or activated
 * @exception WorkbenchException if a new page could not be opened
 * @see #getPerspectiveRegistry
 * @since 2.0
 */
public IWorkbenchPage openPage(String perspId, IAdaptable input, int keyState) 
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
 * 
 * @param perspectiveId the perspective id for the window's initial page
 * @param input the page input, or <code>null</code> if there is no current input.
 *		This is used to seed the input for the new page's views.
 * @return the new workbench window
 * @exception WorkbenchException if a new window and page could not be opened
 * 
 * @deprecated As of 2.0, the explicit creation of workbench windows is discouraged
 * @see IWorkbench#openPage(String, IAdaptable, int)
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
 * 
 * @deprecated As of 2.0, the explicit creation of workbench windows is discouraged
 * @see IWorkbench#openPage(IAdaptable)
 */
public IWorkbenchWindow openWorkbenchWindow(IAdaptable input)
	throws WorkbenchException;
}
