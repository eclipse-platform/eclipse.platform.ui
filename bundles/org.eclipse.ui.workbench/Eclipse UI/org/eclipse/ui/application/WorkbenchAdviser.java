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
 * Example of creating and running a workbench (in an
 * <code>IPlatformRunnable</code>):
 * <pre>
 * <code>
 * public class MyApplication implements IPlatformRunnable {
 *   public Object run(Object args) {
 *     WorkbenchAdviser workbenchAdviser = new MyWorkbenchAdviser();
 *     boolean restart = PlatformUI.createAndRunWorkbench(workbenchAdviser);
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
 * to {@link PlatformUI#createAndRunWorkbench PlatformUI.createAndRunWorkbench}):
 * <ul>
 * <li><code>initialize</code> - called first; before any windows; use to
 * register things</li>
 * <li><code>preStartup</code> - called second; after initialize but
 * before first window is opened; use to temporarily disable things during
 * startup or restore</li>
 * <li><code>postStartup</code> - called third; after first window is
 * opened; use to reenable things temporarily disabled in previous step</li>
 * <li><code>postRestore</code> - called after the workbench and its windows
 * has been recreated from a previously saved state; use to adjust the
 * restored workbench</li>
 * <li><code>preWindowOpen</code> - called as each window is being opened; 
 *  use to configure the window</li>
 * <li><code>postWindowRestore</code> - called after a window has been
 * recreated from a previously saved state; use to adjust the restored
 * window</li>
 * <li><code>postWindowClose</code> - called as each window is being closed; 
 *  use to unhook listeners, etc.</li>
 * <li><code>eventLoopException</code> - called to handle the case where the
 * event loop has crashed; use to inform the user that things are not well</li>
 * <li><code>preShutdown</code> - called just after event loop has terminated
 * but before any windows have been closed; use to deregister things registered
 * during initialize</li>
 * <li><code>postShutdown</code> - called last; after event loop has terminated
 * and all windows have been closed; use to deregister things registered during
 * initialize</li>
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
	 * Performs arbitrary actions after the workbench windows have been
	 * opened (or restored), but before the main event loop is run.
	 * <p>
	 * This method is called just after the windows have been opened.
	 * Clients must not call this method.
	 * The default implementation does nothing. Subclasses may override.
	 * It is okay to <code>IWorkbench.close()</code> from this method.
	 * </p>
	 */
	public void postStartup() {
		// do nothing
	}

	/**
	 * Performs arbitrary actions after the workbench and its windows have been
	 * restored, but before the main event loop is run.
	 * <p>
	 * This method is called after previously-saved windows have been recreated,
	 * and before <code>postStartup</code>. This method is not called when the
	 * workbench is started for the very first time, or if workbench state is
	 * not saved or restored. Clients must not call this method.
	 * The default implementation does nothing. Subclasses may override.
	 * It is okay to <code>IWorkbench.close()</code> from this method.
	 * </p>
	 */
	public void postRestore() {
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
	 * Performs arbitrary actions when the event loop crashes (the code that
	 * handles a UI event throws an exception that is not caught).
	 * <p>
	 * This method is called when the code handling a UI event throws an
	 * exception. In a perfectly functioning application, this method would
	 * never be called. In practice, it comes into play when there is bugs
	 * in the code that trigger unchecked runtime exceptions. It is also
	 * activated when the system runs short of memory, etc. 
	 * Fatal errors (ThreadDeath) are not passed on to this method, as there
	 * is nothing that could be done.
	 * 
	 * Clients must not call this method.
	 * The default implementation makes an emergency exit from the workbench
	 * after logging the problem. Subclasses may override or extend
	 * this method, but must be especially careful when handling Errors.
	 * </p>
	 * 
	 * @param exception the uncaught exception that was thrown inside the UI
	 * event loop
	 * @issue Does this also get called when a dialog event loop crashes?
	 */
	public void eventLoopException(Throwable exception) {
		// TODO: carefully log the problem
		// TODO: call IWorkbenchConfigurer.emergencyClose()
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
	 * Performs arbitrary actions after the given workbench window has been
	 * restored.
	 * <p>
	 * This method is called after a previously-saved window have been
	 * recreated. This method is not called when a new window is created from
	 * scratch. This method is never called when a workbench is started for the
	 * very first time, or when workbench state is not saved or restored.
	 * Clients must not call this method.
	 * The default implementation does nothing. Subclasses may override.
	 * It is okay to <code>IWorkbench.close()</code> from this method.
	 * </p>
	 * 
	 * @param configurer an object for configuring the particular workbench
	 * window just restored
	 */
	public void postWindowRestore(IWorkbenchWindowConfigurer configurer) {
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
	public void postWindowClose(IWorkbenchWindowConfigurer configurer) {
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
	public boolean isApplicationMenu(IWorkbenchWindowConfigurer configurer, String menuId) {
		return true;
	}
}

