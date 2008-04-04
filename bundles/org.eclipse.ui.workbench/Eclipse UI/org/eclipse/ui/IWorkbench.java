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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.activities.IWorkbenchActivitySupport;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IWorkbenchCommandSupport;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.contexts.IWorkbenchContextSupport;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.help.IWorkbenchHelpSystem;
import org.eclipse.ui.intro.IIntroManager;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.operations.IWorkbenchOperationSupport;
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.ui.services.IServiceLocator;
import org.eclipse.ui.themes.IThemeManager;
import org.eclipse.ui.views.IViewRegistry;
import org.eclipse.ui.wizards.IWizardRegistry;

/**
 * A workbench is the root object for the Eclipse Platform user interface.
 * <p>
 * A <b>workbench</b> has one or more main windows which present to the end
 * user information based on some underlying model, typically on resources in an
 * underlying workspace. A workbench usually starts with a single open window,
 * and automatically closes when its last window closes.
 * </p>
 * <p>
 * Each <b>workbench window</b> has a collection of <b>pages</b>; the active
 * page is the one that is being presented to the end user; at most one page is
 * active in a window at a time.
 * </p>
 * <p>
 * Each workbench page has a collection of <b>workbench parts</b>, of which
 * there are two kinds: views and editors. A page's parts are arranged (tiled or
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
 * The workbench supports a few {@link IServiceLocator services} by default. If
 * these services are used to allocate resources, it is important to remember to
 * clean up those resources after you are done with them. Otherwise, the
 * resources will exist until the workbench shuts down. The supported services
 * are:
 * </p>
 * <ul>
 * <li>{@link IBindingService}</li>
 * <li>{@link ICommandService}</li>
 * <li>{@link IContextService}</li>
 * <li>{@link IHandlerService}</li>
 * </ul>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * 
 * @see org.eclipse.ui.PlatformUI#getWorkbench
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IWorkbench extends IAdaptable, IServiceLocator {
	/**
	 * Returns the display for this workbench.
	 * <p>
	 * Code should always ask the workbench for the display rather than rely on
	 * {@link Display#getDefault Display.getDefault()}.
	 * </p>
	 * 
	 * @return the display to be used for all UI interactions with this
	 *         workbench
	 * @since 3.0
	 */
	public Display getDisplay();

	/**
	 * Returns the progress service for the workbench.
	 * 
	 * @return the progress service
	 * @since 3.0
	 */
	public IProgressService getProgressService();

	/**
	 * Adds a workbench listener.
	 * 
	 * @param listener
	 *            the workbench listener to add
	 * @since 3.2
	 */
	public void addWorkbenchListener(IWorkbenchListener listener);

	/**
	 * Removes a workbench listener.
	 * 
	 * @param listener
	 *            the workbench listener to remove
	 * @since 3.2
	 */
	public void removeWorkbenchListener(IWorkbenchListener listener);
	
	/**
	 * Adds a window listener.
	 * 
	 * @param listener
	 *            the window listener to add
	 * @since 2.0
	 */
	public void addWindowListener(IWindowListener listener);

	/**
	 * Removes a window listener.
	 * 
	 * @param listener
	 *            the window listener to remove
	 * @since 2.0
	 */
	public void removeWindowListener(IWindowListener listener);

	/**
	 * Closes this workbench and all its open windows.
	 * <p>
	 * If the workbench has an open editor with unsaved content, the user will
	 * be given the opportunity to save it.
	 * </p>
	 * 
	 * @return <code>true</code> if the workbench was successfully closed, and
	 *         <code>false</code> if it is still open
	 */
	public boolean close();

	/**
	 * Returns the currently active window for this workbench (if any). Returns
	 * <code>null</code> if there is no active workbench window. Returns
	 * <code>null</code> if called from a non-UI thread.
	 * 
	 * @return the active workbench window, or <code>null</code> if there is
	 *         no active workbench window or if called from a non-UI thread
	 */
	public IWorkbenchWindow getActiveWorkbenchWindow();

	/**
	 * Returns the editor registry for the workbench.
	 * 
	 * @return the workbench editor registry
	 */
	public IEditorRegistry getEditorRegistry();

	/**
	 * <p>
	 * Returns the undoable operation support for the workbench.
	 * </p>
	 * 
	 * @return the workbench operation support
	 * 
	 * @since 3.1
	 */
	public IWorkbenchOperationSupport getOperationSupport();

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
	 * @deprecated this returns the internal preference store for the workbench,
	 *             which clients should not use. Use
	 *             {@link PlatformUI#getPreferenceStore()} instead. Note that
	 *             these preference stores are not the same. If you were
	 *             previously storing preferences in the store returned by this
	 *             method you should move them to your own plugin preference
	 *             store.
	 */
	public IPreferenceStore getPreferenceStore();

	/**
	 * Returns the shared images for the workbench.
	 * 
	 * @return the shared image manager
	 */
	public ISharedImages getSharedImages();

	/**
	 * Returns the number of open main windows associated with this workbench.
	 * Note that wizards and dialogs are not included in this list since they
	 * are not considered main windows.
	 * 
	 * @return the number of open windows
	 * @since 3.0
	 */
	public int getWorkbenchWindowCount();

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
	 * Creates a new local working set manager. Clients of local working set
	 * managers are responsible for calling {@link IWorkingSetManager#dispose()}
	 * when the working sets it manages are no longer needed.
	 * 
	 * @return the local working set manager
	 * @since 3.1
	 */
	public ILocalWorkingSetManager createLocalWorkingSetManager();

	/**
	 * Creates and opens a new workbench window with one page. The perspective
	 * of the new page is defined by the specified perspective ID. The new
	 * window and new page become active.
	 * <p>
	 * <b>Note:</b> The caller is responsible to ensure the action using this
	 * method will explicitly inform the user a new window will be opened.
	 * Otherwise, callers are strongly recommended to use the
	 * <code>openPerspective</code> APIs to programmatically show a
	 * perspective to avoid confusing the user.
	 * </p>
	 * <p>
	 * In most cases where this method is used the caller is tightly coupled to
	 * a particular perspective. They define it in the registry and contribute
	 * some user interface action to open or activate it. In situations like
	 * this a static variable is often used to identify the perspective ID.
	 * </p>
	 * 
	 * @param perspectiveId
	 *            the perspective id for the window's initial page, or
	 *            <code>null</code> for no initial page
	 * @param input
	 *            the page input, or <code>null</code> if there is no current
	 *            input. This is used to seed the input for the new page's
	 *            views.
	 * @return the new workbench window
	 * @exception WorkbenchException
	 *                if a new window and page could not be opened
	 * 
	 * @see IWorkbench#showPerspective(String, IWorkbenchWindow, IAdaptable)
	 */
	public IWorkbenchWindow openWorkbenchWindow(String perspectiveId,
			IAdaptable input) throws WorkbenchException;

	/**
	 * Creates and opens a new workbench window with one page. The perspective
	 * of the new page is defined by the default perspective ID. The new window
	 * and new page become active.
	 * <p>
	 * <b>Note:</b> The caller is responsible to ensure the action using this
	 * method will explicitly inform the user a new window will be opened.
	 * Otherwise, callers are strongly recommended to use the
	 * <code>openPerspective</code> APIs to programmatically show a
	 * perspective to avoid confusing the user.
	 * </p>
	 * 
	 * @param input
	 *            the page input, or <code>null</code> if there is no current
	 *            input. This is used to seed the input for the new page's
	 *            views.
	 * @return the new workbench window
	 * @exception WorkbenchException
	 *                if a new window and page could not be opened
	 * 
	 * @see IWorkbench#showPerspective(String, IWorkbenchWindow, IAdaptable)
	 */
	public IWorkbenchWindow openWorkbenchWindow(IAdaptable input)
			throws WorkbenchException;

	/**
	 * Closes then restarts this workbench.
	 * <p>
	 * If the workbench has an open editor with unsaved content, the user will
	 * be given the opportunity to save it.
	 * </p>
	 * 
	 * @return <code>true</code> if the workbench was successfully closed, and
	 *         <code>false</code> if it could not be closed
	 * 
	 * @since 2.0
	 */
	public boolean restart();

	/**
	 * Shows the specified perspective to the user. The caller should use this
	 * method when the perspective to be shown is not dependent on the page's
	 * input. That is, the perspective can open in any page depending on user
	 * preferences.
	 * <p>
	 * The perspective may be shown in the specified window, in another existing
	 * window, or in a new window depending on user preferences. The exact
	 * policy is controlled by the workbench to ensure consistency to the user.
	 * The policy is subject to change. The current policy is as follows:
	 * <ul>
	 * <li>If the specified window has the requested perspective open, then the
	 * window is given focus and the perspective is shown. The page's input is
	 * ignored.</li>
	 * <li>If another window that has the workspace root as input and the
	 * requested perspective open and active, then the window is given focus.
	 * </li>
	 * <li>Otherwise the requested perspective is opened and shown in the
	 * specified window or in a new window depending on the current user
	 * preference for opening perspectives, and that window is given focus.
	 * </li>
	 * </ul>
	 * </p>
	 * <p>
	 * The workbench also defines a number of menu items to activate or open
	 * each registered perspective. A complete list of these perspectives is
	 * available from the perspective registry found on <code>IWorkbench</code>.
	 * </p>
	 * 
	 * @param perspectiveId
	 *            the perspective ID to show
	 * @param window
	 *            the workbench window of the action calling this method.
	 * @return the workbench page that the perspective was shown
	 * @exception WorkbenchException
	 *                if the perspective could not be shown
	 * 
	 * @since 2.0
	 */
	public IWorkbenchPage showPerspective(String perspectiveId,
			IWorkbenchWindow window) throws WorkbenchException;

	/**
	 * Shows the specified perspective to the user. The caller should use this
	 * method when the perspective to be shown is dependent on the page's input.
	 * That is, the perspective can only open in any page with the specified
	 * input.
	 * <p>
	 * The perspective may be shown in the specified window, in another existing
	 * window, or in a new window depending on user preferences. The exact
	 * policy is controlled by the workbench to ensure consistency to the user.
	 * The policy is subject to change. The current policy is as follows:
	 * <ul>
	 * <li>If the specified window has the requested perspective open and the
	 * same requested input, then the window is given focus and the perspective
	 * is shown.</li>
	 * <li>If another window has the requested input and the requested
	 * perspective open and active, then that window is given focus.</li>
	 * <li>If the specified window has the same requested input but not the
	 * requested perspective, then the window is given focus and the perspective
	 * is opened and shown on condition that the user preference is not to open
	 * perspectives in a new window.</li>
	 * <li>Otherwise the requested perspective is opened and shown in a new
	 * window, and the window is given focus.</li>
	 * </ul>
	 * </p>
	 * <p>
	 * The workbench also defines a number of menu items to activate or open
	 * each registered perspective. A complete list of these perspectives is
	 * available from the perspective registry found on <code>IWorkbench</code>.
	 * </p>
	 * 
	 * @param perspectiveId
	 *            the perspective ID to show
	 * @param window
	 *            the workbench window of the action calling this method.
	 * @param input
	 *            the page input, or <code>null</code> if there is no current
	 *            input. This is used to seed the input for the page's views
	 * @return the workbench page that the perspective was shown
	 * @exception WorkbenchException
	 *                if the perspective could not be shown
	 * 
	 * @since 2.0
	 */
	public IWorkbenchPage showPerspective(String perspectiveId,
			IWorkbenchWindow window, IAdaptable input)
			throws WorkbenchException;

	/**
	 * Returns the decorator manager.
	 * <p>
	 * Any client using the decorator manager should come up with the text and
	 * image for the element (including any of the part's own decorations)
	 * before calling the decorator manager. It should also add a listener to be
	 * notified when decorations change.
	 * </p>
	 * <p>
	 * Note that if the element implements <code>IAdaptable</code>,
	 * decorators may use this mechanism to obtain an adapter (for example an
	 * <code>IResource</code>), and derive the decoration from the adapter
	 * rather than the element. Since the adapter may differ from the original
	 * element, those using the decorator manager should be prepared to handle
	 * notification that the decoration for the adapter has changed, in addition
	 * to handling notification that the decoration for the element has changed.
	 * That is, it needs to be able to map back from the adapter to the element.
	 * </p>
	 * 
	 * @return the decorator manager
	 */
	public IDecoratorManager getDecoratorManager();

	/**
	 * Save all dirty editors in the workbench. Opens a dialog to prompt the
	 * user if <code>confirm</code> is true. Return true if successful. Return
	 * false if the user has canceled the command.
	 * 
	 * @param confirm <code>true</code> to ask the user before saving unsaved
	 *            changes (recommended), and <code>false</code> to save
	 *            unsaved changes without asking
	 * @return <code>true</code> if the command succeeded, and
	 *         <code>false</code> if the operation was canceled by the user or
	 *         an error occurred while saving
	 */
	public boolean saveAllEditors(boolean confirm);

	/**
	 * Returns the element factory with the given id.
	 * 
	 * @param factoryId
	 *            the id of the element factory
	 * @return the element factory, or <code>null</code> if none
	 * @see IElementFactory
	 * @since 3.0
	 */
	public IElementFactory getElementFactory(String factoryId);

	/**
	 * Returns an interface to manage activities at the workbench level.
	 * 
	 * @return an interface to manage activities at the workbench level.
	 *         Guaranteed not to be <code>null</code>.
	 * @since 3.0
	 */
	IWorkbenchActivitySupport getActivitySupport();

	/**
	 * Returns an interface to manage commands at the workbench level.
	 * 
	 * @return an interface to manage commands at the workbench level.
	 *         Guaranteed not to be <code>null</code>.
	 * @since 3.0
	 * @deprecated Please use {@link IServiceLocator#getService(Class)} instead.
	 * @see ICommandService
	 * @see IHandlerService
	 */
	IWorkbenchCommandSupport getCommandSupport();

	/**
	 * Returns an interface to manage contexts at the workbench level.
	 * 
	 * @return an interface to manage contexts at the workbench level.
	 *         Guaranteed not to be <code>null</code>.
	 * @since 3.0
	 * @deprecated Please use {@link IServiceLocator#getService(Class)} instead.
	 * @see IContextService
	 */
	IWorkbenchContextSupport getContextSupport();

	/**
	 * Return the theme manager for this workbench.
	 * 
	 * @return the theme manager for this workbench.Guaranteed not to be
	 *         <code>null</code>.
	 * @since 3.0
	 */
	public IThemeManager getThemeManager();

	/**
	 * Return the intro manager for this workbench.
	 * 
	 * @return the intro manager for this workbench. Guaranteed not to be
	 *         <code>null</code>.
	 * @since 3.0
	 */
	public IIntroManager getIntroManager();

	/**
	 * Return the help system for this workbench.
	 * 
	 * @return the help system
	 * @since 3.1
	 */
	public IWorkbenchHelpSystem getHelpSystem();

	/**
	 * Return the browser support for this workbench.
	 * 
	 * @return the browser support system
	 * @since 3.1
	 */
	public IWorkbenchBrowserSupport getBrowserSupport();

	/**
	 * Returns a boolean indicating whether the workbench is in the process of
	 * closing.
	 * 
	 * @return <code>true</code> if the workbench is in the process of
	 *         closing, <code>false</code> otherwise
	 * @since 3.1
	 */
	public boolean isClosing();

	/**
	 * <p>
	 * Return the extension tracker for the workbench. This tracker may be used
	 * by plug-ins to ensure responsiveness to changes to the plug-in registry.
	 * </p>
	 * <p>
	 * The tracker at this level of the workbench is typically used to track
	 * elements that persist for the life of the workbench. For example,
	 * <code>IEditorDescriptor</code> objects fall into this category.
	 * </p>
	 * 
	 * @return the extension tracker
	 * @see IWorkbenchWindow#getExtensionTracker()
	 * @see IWorkbenchPage#getExtensionTracker()
	 * @since 3.1
	 */
	public IExtensionTracker getExtensionTracker();

	/**
	 * Returns the view registry for the workbench.
	 * 
	 * @return the workbench view registry
	 * @since 3.1
	 */
	public IViewRegistry getViewRegistry();

	/**
	 * Return the new wizard registry.
	 * 
	 * @return the new wizard registry
	 * @since 3.1
	 */
	public IWizardRegistry getNewWizardRegistry();

	/**
	 * Return the import wizard registry.
	 * 
	 * @return the import wizard registry
	 * @since 3.1
	 */
	public IWizardRegistry getImportWizardRegistry();

	/**
	 * Return the export wizard registry.
	 * 
	 * @return the export wizard registry
	 * @since 3.1
	 */
	public IWizardRegistry getExportWizardRegistry();
	
	/**
	 * Save all dirty saveables in the workbench that match the given filter.
	 * Opens a dialog to prompt the user if <code>confirm</code> is true.
	 * Return true if successful. Return false if the user has canceled the
	 * command.
	 * 
	 * @since 3.3
	 * 
	 * @param shellProvider the provider used to obtain a shell in prompting is
	 *            required. Clients can use a workbench window for this.
	 * @param runnableContext a runnable context that will be used to provide a
	 *            progress monitor while the save is taking place. Clients can
	 *            use a workbench window for this.
	 * @param filter the filter used to determine if a particular dirty saveable
	 *            needs to be saved or <code>null</code> if all dirty
	 *            saveables should be saved.
	 * @param confirm <code>true</code> to ask the user before saving unsaved
	 *            changes (recommended), and <code>false</code> to save
	 *            unsaved changes without asking
	 * @return <code>true</code> if the command succeeded, and
	 *         <code>false</code> if the operation was canceled by the user or
	 *         an error occurred while saving
	 */
	public boolean saveAll(IShellProvider shellProvider,
			IRunnableContext runnableContext, ISaveableFilter filter,
			boolean confirm);
}
