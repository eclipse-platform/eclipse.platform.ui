/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.application;

/**
 * Public base class for configuring the workbench.
 * <p>
 * Note that the workbench adviser object is created in advance of creating the
 * workbench. However, by the time the workbench starts calling methods on this
 * class, <code>PlatformUI.getWorkbench</code> is guaranteed to have been
 * properly initialized.
 * </p>
 * <p>
 * Example of creating, configuring, and running a workbench (in an
 * <code>IPlatformRunnable</code>):
 * <pre>
 * <code>
 * public class MyApplication implements IPlatformRunnable {
 *   public Object run(Object args) {
 *     WorkbenchAdviser workbenchAdviser = new MyWorkbenchAdviser();
 *     IWorkbench workbench = PlatformUI.createWorkbench(workbenchAdviser);
 *     boolean restart = workbench.runUI();
 *     return (restart ? IPlatformRunnable.EXIT_RESTART : IPlatformRunnable.EXIT_OK);
 *   }
 * }
 * </code>
 * </pre>
 * </p>
 * <p>
 * An application should declare a subclass of <code>WorkbenchAdviser</code>
 * and override methods to configure the workbench to suit the needs of the
 * particular application.
 * </p>
 * <p>
 * The following advisor methods are called at strategic points in the
 * workbench's lifecycle (all occur within the dynamic scope of the call
 * to {@link IWorkbench#runUI IWorkbench.runUI}):
 * <ul>
 * <li><code>initialize</code> - called first; before any windows; use to
 * register things; IWorkench.close not an option</li>
 * <li><code>preStartup</code> - called second; after initialize but
 * before first window is opened; use to temporarily disable things during
 * startup or restore; IWorkench.close not an option</li>
 * <li><code>postStartup</code> - called third; after first window is
 * opened; use to reenable things temporarily disabled in previous step;
 * IWorkench.close not an option</li>
 * <li><code>preWindowOpen</code> - called as each window is being opened; 
 *  use to configure the window; IWorkench.close not an option</li>
 * <li><code>postWindowClose</code> - called as each window is being closed; 
 *  use to unhook listeners, etc.; IWorkench.close not an option</li>
 * <li><code>preShutdown</code> - called just after event loop has terminated
 * but before any windows have been closed; use to deregister things registered
 * during initialize; IWorkench.close not an option</li>
 * <li><code>postShutdown</code> - called last; after event loop has terminated
 * and all windows have been closed; use to deregister things registered during
 * initialize; IWorkench.close not an option</li>
 * </ul>
 * </p>
 * 
 * @since 3.0
 */
public abstract class WorkbenchAdviser {
	
	/**
	 * Creates and initializes a new workbench adviser instance.
	 */
	protected WorkbenchAdviser() {
		// do nothing
	}

	/**
	 * Performs arbitrary initialization before the workbench starts running.
	 * <p>
	 * This method is called during workbench initialization prior to any
	 * windows being opened. Clients must not call this method. 
	 * The default implementation does nothing. Subclasses may override. 
	 * Typical clients will use the configurer passed in to tweak the
	 * workbench, and hang on to the configurer if further tweaking may be
	 * required in the future.
	 * </p>
	 * 
	 * @param configurer an object for configuring the workbench
	 */
	public void initialize(IWorkbenchConfigurer configurer) {
		// do nothing
	}

	/**
	 * Performs arbitrary actions just before the first workbench window is
	 * opened (or restored).
	 * <p>
	 * This method is called after the workbench has been initialized and
	 * just before the first window is about to be opened. Clients must not call
	 * this method.
	 * The default implementation does nothing. Subclasses may override.
	 * </p>
	 */
	public void preStartup() {
		// do nothing
	}

	/**
	 * Performs arbitrary actions after the first workbench window has been
	 * opened (or restored), but before the main event loop is run.
	 * <p>
	 * This method is called just after the first window has been opened.
	 * Clients must not call this method.
	 * The default implementation does nothing. Subclasses may override.
	 * It is okay to IWorkbench.close() from this method.
	 * </p>
	 */
	public void postStartup() {
		// do nothing
	}

	/**
	 * Performs arbitrary finalization before the workbench is about to
	 * shut down.
	 * <p>
	 * This method is called immediately prior to workbench shutdown before any
	 * windows have been closed. Clients must not call this method. 
	 * The default implementation does nothing. Subclasses may override.
	 * </p>
	 * 
	 * @issue veto?
	 * @issue about to close last window - window closing
	 */
	public void preShutdown() {
		// do nothing
	}
	
	/**
	 * Performs arbitrary finalization after the workbench stops running.
	 * <p>
	 * This method is called during workbench shutdown after all windows
	 * have been closed. Clients must not call this method. 
	 * The default implementation does nothing. Subclasses may override.
	 * </p>
	 */
	public void postShutdown() {
		// do nothing
	}
	
	/**
	 * Performs arbitrary actions before the given workbench window is
	 * opened.
	 * <p>
	 * This method is called before the window's controls have been created.
	 * Clients must not call this method.
	 * The default implementation does nothing. Subclasses may override.
	 * Typical clients will use the configurer passed in to tweak the
	 * workbench window in an application-specific way.
	 * </p>
	 * 
	 * @param configurer an object for configuring the particular workbench
	 * window being opened
	 */
	public void preWindowOpen(IWorkbenchWindowConfigurer configurer) {
		// do nothing
	}
	
	/**
	 * Performs arbitrary actions after the given workbench window is
	 * closed.
	 * <p>
	 * This method is called after the window's controls have been disposed.
	 * Clients must not call this method.
	 * The default implementation does nothing. Subclasses may override.
	 * Typical clients will use the configurer passed in to tweak the
	 * workbench window in an application-specific way.
	 * </p>
	 * 
	 * @param configurer an object for configuring the particular workbench
	 * window being closed
	 */
	public void postWindowClose(IWorkbenchWindowConfigurer window) {
		// do nothing
	}

	/**
	 * Returns whether the given menu is an application menu of the given
	 * window.
	 * <p>
	 * The default implementation returns true. Subclasses may override.
	 * </p>
	 * 
	 * @param configurer an object for configuring the workbench window
	 * @param menuId the menu id
	 * @return <code>true</code> for application menus, and <code>false</code>
	 * for part-specific menus
	 * @see org.eclipse.ui.IWorkbenchWindow#isApplicationMenu
	 * @issue investigate whether there's a better way to handle these
	 */
	public boolean isApplicationMenu(IWorkbenchWindowConfigurer window, String menuId) {
		return true;
	}
	
	/**
	 * Return whether the given id is that of a cool item for the given window.
	 * <p>
	 * The default implementation returns true. Subclasses may override.
	 * </p>
	 * 
	 * @param configurer an object for configuring the workbench window
	 * @param id the coll item id
	 * @return <code>true</code> for a cool item, and <code>false</code>
	 * otherwise
	 * @issue investigate whether there's a better way to handle these
	 */
	public boolean isWorkbenchCoolItemId(IWorkbenchWindowConfigurer window, String id) {
		return true;
	}
}

