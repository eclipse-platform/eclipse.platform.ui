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
import org.eclipse.jface.preference.IPreferenceStore;
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
 * @see org.eclipse.ui.plugin.AbstractUIPlugin#getWorkbench()
 */
public interface IWorkbench {
/**
 * Adds a window listener.
 * 
 * @param listener the window listener to add
 * @since 2.0
 */
public void addWindowListener(IWindowListener listener);	
/**
 * Removes a window listener.
 * 
 * @param listener the window listener to remove
 * @since 2.0
 */
public void removeWindowListener(IWindowListener l);
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
 * Returns the preference store for the workbench.
 *
 * @return the workbench preference store
 * @since 2.0
 */
public IPreferenceStore getPreferenceStore();
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
 * Returns the working set manager for the workbench.
 *
 * @return the working set manager
 * @since 2.0
 */
public IWorkingSetManager getWorkingSetManager();
/**
 * Creates and opens a new workbench window with one page.  The perspective of
 * the new page is defined by the specified perspective ID.  The new window and new 
 * page become active.
 * <p>
 * <b>Note:</b> The caller is responsible to ensure the action using this method
 * will explicitly inform the user a new window will be opened. Otherwise, callers
 * are strongly recommended to use the <code>openPerspective</code> APIs to
 * programmatically show a perspective to avoid confusing the user.
 * </p><p>
 * In most cases where this method is used the caller is tightly coupled to
 * a particular perspective.  They define it in the registry and contribute some
 * user interface action to open or activate it.  In situations like this a
 * static variable is often used to identify the perspective Id.
 * </p><p>
 * The workbench also defines a number of menu items to activate or open each
 * registered perspective. A complete list of these perspectives is available 
 * from the perspective registry found on <code>IWorkbench</code>.
 * </p>
 * 
 * @param perspectiveId the perspective id for the window's initial page
 * @param input the page input, or <code>null</code> if there is no current input.
 *		This is used to seed the input for the new page's views.
 * @return the new workbench window
 * @exception WorkbenchException if a new window and page could not be opened
 * 
 * @see IWorkbench#showPerspective
 */
public IWorkbenchWindow openWorkbenchWindow(String perspID, IAdaptable input)
	throws WorkbenchException;
/**
 * Creates and opens a new workbench window with one page.  The perspective of
 * the new page is defined by the default perspective ID.  The new window and new 
 * page become active.
 * <p>
 * <b>Note:</b> The caller is responsible to ensure the action using this method
 * will explicitly inform the user a new window will be opened. Otherwise, callers
 * are strongly recommended to use the <code>openPerspective</code> APIs to
 * programmatically show a perspective to avoid confusing the user.
 * </p><p>
 * The workbench also defines a number of menu items to activate or open each
 * registered perspective. A complete list of these perspectives is available 
 * from the perspective registry found on <code>IWorkbench</code>.
 * </p>
 *
 * @param input the page input, or <code>null</code> if there is no current input.
 *		This is used to seed the input for the new page's views.
 * @return the new workbench window
 * @exception WorkbenchException if a new window and page could not be opened
 * 
 * @see IWorkbench#showPerspective
 */
public IWorkbenchWindow openWorkbenchWindow(IAdaptable input)
	throws WorkbenchException;
/**
 * Closes then restarts this workbench.
 * <p>
 * If the workbench has an open editor with unsaved content, the user will be
 * given the opportunity to save it.
 * </p>
 *
 * @return <code>true</code> if the workbench was successfully closed,
 *   and <code>false</code> if it could not be closed
 * 
 * @since 2.0
 */
public boolean restart();

/**
 * Shows the specified perspective to the user. The caller should use this method
 * when the perspective to be shown is not dependent on the page's input. That is,
 * the perspective can open in any page depending on user preferences.
 * <p>
 * The perspective may be shown in the specified window, in another existing window,
 * or in a new window depending on user preferences. The exact policy is controlled
 * by the workbench to ensure consistency to the user. The policy is subject to
 * change. The current policy is as follows:
 * <ul>
 * <li>
 * If the specified window has the requested perspective open, then the window
 * is given focus and the perspective is shown. The page's input is ignored.
 * </li><li>
 * If another window that has the workspace root as input and the requested
 * perpective open and active, then the window is given focus.
 * </li><li>
 * Otherwise the requested perspective is opened and shown in the specified
 * window or in a new window depending on the current user preference for opening
 * perspectives, and that window is given focus.
 * </li>
 * </ul>
 * </p><p>
 * The workbench also defines a number of menu items to activate or open each
 * registered perspective. A complete list of these perspectives is available 
 * from the perspective registry found on <code>IWorkbench</code>.
 * </p>
 * 
 * @param perspectiveId the perspective ID to show
 * @param window the workbench window of the action calling this method.
 * @return the workbench page that the perspective was shown
 * @exception WorkbenchException if the perspective could not be shown
 * 
 * @since 2.0
 */
public IWorkbenchPage showPerspective(String perspectiveId, IWorkbenchWindow window) 
	throws WorkbenchException;

/**
 * Shows the specified perspective to the user. The caller should use this method
 * when the perspective to be shown is dependent on the page's input. That is,
 * the perspective can only open in any page with the specified input.
 * <p>
 * The perspective may be shown in the specified window, in another existing window,
 * or in a new window depending on user preferences. The exact policy is controlled
 * by the workbench to ensure consistency to the user. The policy is subject to
 * change. The current policy is as follows:
 * <ul>
 * <li>
 * If the specified window has the requested perspective open and the same requested
 * input, then the window is given focus and the perspective is shown.
 * </li><li>
 * If another window has the requested input and the requested
 * perpective open and active, then that window is given focus.
 * </li><li>
 * If the specified window has the same requested input but not the requested
 * perspective, then the window is given focus and the perspective is opened and shown
 * on condition that the user preference is not to open perspectives in a new window.
 * </li><li>
 * Otherwise the requested perspective is opened and shown in a new window, and the
 * window is given focus.
 * </li>
 * </ul>
 * </p><p>
 * The workbench also defines a number of menu items to activate or open each
 * registered perspective. A complete list of these perspectives is available 
 * from the perspective registry found on <code>IWorkbench</code>.
 * </p>
 * 
 * @param perspectiveId the perspective ID to show
 * @param window the workbench window of the action calling this method.
 * @param input the page input, or <code>null</code> if there is no current input.
 *		This is used to seed the input for the page's views
 * @return the workbench page that the perspective was shown
 * @exception WorkbenchException if the perspective could not be shown
 * 
 * @since 2.0
 */
public IWorkbenchPage showPerspective(String perspectiveId, IWorkbenchWindow window, IAdaptable input) 
	throws WorkbenchException;

/**
 * Returns the decorator manager.
 * <p>
 * Any client using the decorator manager should come up with the text and image for the element 
 * (including any of the part's own decorations) before calling the decorator manager.
 * It should also add a listener to be notified when decorations change.
 * </p>
 * <p>
 * Note that if the element implements <code>IAdaptable</code>, decorators may use this
 * mechanism to obtain an adapter (for example an <code>IResource</code>), and derive the
 * decoration from the adapter rather than the element.
 * Since the adapter may differ from the original element, those using the decorator manager
 * should be prepared to handle notification that the decoration for the adapter has changed, in addition to 
 * handling notification that the decoration for the element has changed.
 * That is, it needs to be able to map back from the adapter to the element.
 * </p>
 * 
 * @return the decorator manager
 */
public IDecoratorManager getDecoratorManager();
/**
 * Save all dirty editors in the workbench. Opens
 * a dialog to prompt the user if <code>confirm</code> is 
 * true. Return true if successful. Return false if the
 * user has cancelled the command.
 * 
 * @param confirm prompt the user if true
 * @return boolean false if the operation was cancelled.
 */
public boolean saveAllEditors(boolean confirm);
}
