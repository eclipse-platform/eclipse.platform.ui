/*******************************************************************************
 * Copyright (c) 2003, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.application;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.internal.StartupThreading;
import org.eclipse.ui.internal.UISynchronizer;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.WorkbenchWindowConfigurer;
import org.eclipse.ui.internal.StartupThreading.StartupRunnable;
import org.eclipse.ui.internal.application.CompatibilityWorkbenchWindowAdvisor;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.ui.model.ContributionComparator;
import org.eclipse.ui.model.IContributionService;
import org.eclipse.ui.statushandlers.AbstractStatusHandler;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.statushandlers.WorkbenchErrorHandler;

/**
 * Public base class for configuring the workbench.
 * <p>
 * Note that the workbench advisor object is created in advance of creating the
 * workbench. However, by the time the workbench starts calling methods on this
 * class, <code>PlatformUI.getWorkbench</code> is guaranteed to have been
 * properly initialized.
 * </p>
 * <p>
 * Example of creating and running a workbench (in an
 * <code>IPlatformRunnable</code>):
 * 
 * <pre>
 * <code>
 *           public class MyApplication implements IPlatformRunnable {
 *             public Object run(Object args) {
 *               WorkbenchAdvisor workbenchAdvisor = new MyWorkbenchAdvisor();
 *               Display display = PlatformUI.createDisplay();
 *               int returnCode = PlatformUI.createAndRunWorkbench(display, workbenchAdvisor);
 *               if (returnCode == PlatformUI.RETURN_RESTART) {
 *                  return IPlatformRunnable.EXIT_RESTART;
 *               } else {
 *                  return IPlatformRunnable.EXIT_OK;
 *             }
 *           }
 * </code>
 * </pre>
 * 
 * </p>
 * <p>
 * An application should declare a subclass of <code>WorkbenchAdvisor</code>
 * and override methods to configure the workbench to suit the needs of the
 * particular application.
 * </p>
 * <p>
 * The following advisor methods are called at strategic points in the
 * workbench's lifecycle (all occur within the dynamic scope of the call to
 * {@link PlatformUI#createAndRunWorkbench PlatformUI.createAndRunWorkbench}):
 * <ul>
 * <li><code>initialize</code> - called first; before any windows; use to
 * register things</li>
 * <li><code>preStartup</code> - called second; after initialize but before
 * first window is opened; use to temporarily disable things during startup or
 * restore</li>
 * <li><code>postStartup</code> - called third; after first window is opened;
 * use to reenable things temporarily disabled in previous step</li>
 * <li><code>postRestore</code> - called after the workbench and its windows
 * has been recreated from a previously saved state; use to adjust the restored
 * workbench</li>
 * <li><code>preWindowOpen</code> - called as each window is being opened;
 * use to configure aspects of the window other than actions bars </li>
 * <li><code>fillActionBars</code> - called after <code>preWindowOpen</code>
 * to configure a window's action bars</li>
 * <li><code>postWindowRestore</code> - called after a window has been
 * recreated from a previously saved state; use to adjust the restored window</li>
 * <li><code>postWindowCreate</code> - called after a window has been
 * created, either from an initial state or from a restored state; used to
 * adjust the window</li>
 * <li><code>openIntro</code> - called immediately before a window is opened
 * in order to create the introduction component, if any.</li>
 * <li><code>postWindowOpen</code> - called after a window has been opened;
 * use to hook window listeners, etc.</li>
 * <li><code>preWindowShellClose</code> - called when a window's shell is
 * closed by the user; use to pre-screen window closings</li>
 * <li><code>eventLoopException</code> - called to handle the case where the
 * event loop has crashed; use to inform the user that things are not well</li>
 * <li><code>eventLoopIdle</code> - called when there are currently no more
 * events to be processed; use to perform other work or to yield until new
 * events enter the queue</li>
 * <li><code>preShutdown</code> - called immediately prior to workbench
 * shutdown before any windows have been closed; allows the advisor to veto the
 * shutdown</li>
 * <li><code>postShutdown</code> - called last; after event loop has
 * terminated and all windows have been closed; use to deregister things
 * registered during initialize</li>
 * </ul>
 * </p>
 * 
 * @since 3.0
 */
public abstract class WorkbenchAdvisor {

	/**
	 * Bit flag for {@link #fillActionBars fillActionBars} indicating that the
	 * operation is not filling the action bars of an actual workbench window,
	 * but rather a proxy (used for perspective customization).
	 * 
	 * @deprecated use {@link ActionBarAdvisor#FILL_PROXY instead}
	 */
	public static final int FILL_PROXY = ActionBarAdvisor.FILL_PROXY;

	/**
	 * Bit flag for {@link #fillActionBars fillActionBars} indicating that the
	 * operation is supposed to fill (or describe) the workbench window's menu
	 * bar.
	 * 
	 * @deprecated use {@link ActionBarAdvisor#FILL_MENU_BAR instead}
	 */
	public static final int FILL_MENU_BAR = ActionBarAdvisor.FILL_MENU_BAR;

	/**
	 * Bit flag for {@link #fillActionBars fillActionBars} indicating that the
	 * operation is supposed to fill (or describe) the workbench window's cool
	 * bar.
	 * 
	 * @deprecated use {@link ActionBarAdvisor#FILL_COOL_BAR instead}
	 */
	public static final int FILL_COOL_BAR = ActionBarAdvisor.FILL_COOL_BAR;

	/**
	 * Bit flag for {@link #fillActionBars fillActionBars} indicating that the
	 * operation is supposed to fill (or describe) the workbench window's status
	 * line.
	 * 
	 * @deprecated use {@link ActionBarAdvisor#FILL_STATUS_LINE instead}
	 */
	public static final int FILL_STATUS_LINE = ActionBarAdvisor.FILL_STATUS_LINE;

	/**
	 * The workbench configurer.
	 */
	private IWorkbenchConfigurer workbenchConfigurer;

	/**
	 * The workbench error handler.
	 */
	private AbstractStatusHandler workbenchErrorHandler;

	private boolean introOpened;

	/**
	 * Creates and initializes a new workbench advisor instance.
	 */
	protected WorkbenchAdvisor() {
		// do nothing
	}

	/**
	 * Remembers the configurer and calls <code>initialize</code>.
	 * <p>
	 * For internal use by the workbench only.
	 * </p>
	 * 
	 * @param configurer
	 *            an object for configuring the workbench
	 */
	public final void internalBasicInitialize(IWorkbenchConfigurer configurer) {
		if (workbenchConfigurer != null) {
			throw new IllegalStateException();
		}
		this.workbenchConfigurer = configurer;
		initialize(configurer);
	}

	/**
	 * Performs arbitrary initialization before the workbench starts running.
	 * <p>
	 * This method is called during workbench initialization prior to any
	 * windows being opened. Clients must not call this method directly
	 * (although super calls are okay). The default implementation does nothing.
	 * Subclasses may override. Typical clients will use the configurer passed
	 * in to tweak the workbench. If further tweaking is required in the future,
	 * the configurer may be obtained using <code>getWorkbenchConfigurer</code>.
	 * </p>
	 * 
	 * @param configurer
	 *            an object for configuring the workbench
	 */
	public void initialize(IWorkbenchConfigurer configurer) {
		// do nothing
	}

	/**
	 * Returns the workbench configurer for the advisor. Can be
	 * <code>null</code> if the advisor is not initialized yet.
	 * 
	 * @return the workbench configurer, or <code>null</code> if the advisor
	 *         is not initialized yet
	 */
	protected IWorkbenchConfigurer getWorkbenchConfigurer() {
		return workbenchConfigurer;
	}

	/**
	 * Returns the workbench error handler for the advisor.
	 * 
	 * @return the workbench error handler
	 * @since 3.3
	 */
	public AbstractStatusHandler getWorkbenchErrorHandler() {
		if (workbenchErrorHandler == null) {
			workbenchErrorHandler = new WorkbenchErrorHandler();
		}
		return workbenchErrorHandler;
	}

	/**
	 * Performs arbitrary actions just before the first workbench window is
	 * opened (or restored).
	 * <p>
	 * This method is called after the workbench has been initialized and just
	 * before the first window is about to be opened. Clients must not call this
	 * method directly (although super calls are okay). The default
	 * implementation does nothing. Subclasses may override.
	 * </p>
	 */
	public void preStartup() {
		// do nothing
	}

	/**
	 * Performs arbitrary actions after the workbench windows have been opened
	 * (or restored), but before the main event loop is run.
	 * <p>
	 * This method is called just after the windows have been opened. Clients
	 * must not call this method directly (although super calls are okay). The
	 * default implementation does nothing. Subclasses may override. It is okay
	 * to call <code>IWorkbench.close()</code> from this method.
	 * </p>
	 */
	public void postStartup() {
		// do nothing
	}

	/**
	 * Performs arbitrary finalization before the workbench is about to shut
	 * down.
	 * <p>
	 * This method is called immediately prior to workbench shutdown before any
	 * windows have been closed. Clients must not call this method directly
	 * (although super calls are okay). The default implementation returns
	 * <code>true</code>. Subclasses may override.
	 * </p>
	 * <p>
	 * The advisor may veto a regular shutdown by returning <code>false</code>,
	 * although this will be ignored if the workbench is being forced to shut
	 * down.
	 * </p>
	 * 
	 * @return <code>true</code> to allow the workbench to proceed with
	 *         shutdown, <code>false</code> to veto a non-forced shutdown
	 */
	public boolean preShutdown() {
		return true;
	}

	/**
	 * Performs arbitrary finalization after the workbench stops running.
	 * <p>
	 * This method is called during workbench shutdown after all windows have
	 * been closed. Clients must not call this method directly (although super
	 * calls are okay). The default implementation does nothing. Subclasses may
	 * override.
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
	 * never be called. In practice, it comes into play when there are bugs in
	 * the code that trigger unchecked runtime exceptions. It is also activated
	 * when the system runs short of memory, etc. Fatal errors (ThreadDeath) are
	 * not passed on to this method, as there is nothing that could be done.
	 * </p>
	 * <p>
	 * Clients must not call this method directly (although super calls are
	 * okay). The default implementation logs the problem so that it does not go
	 * unnoticed. Subclasses may override or extend this method. It is generally
	 * a bad idea to override with an empty method, and you should be especially
	 * careful when handling Errors.
	 * </p>
	 * 
	 * @param exception
	 *            the uncaught exception that was thrown inside the UI event
	 *            loop
	 */
	public void eventLoopException(Throwable exception) {
		// Protection from client doing super(null) call
		if (exception == null) {
			return;
		}

		try {
			StatusManager.getManager().handle(
					new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH,
							"Unhandled event loop exception", exception)); //$NON-NLS-1$

			if (WorkbenchPlugin.DEBUG) {
				exception.printStackTrace();
			}
		} catch (Throwable e) {
			// One of the log listeners probably failed. Core should have logged
			// the
			// exception since its the first listener.
			System.err.println("Error while logging event loop exception:"); //$NON-NLS-1$
			exception.printStackTrace();
			System.err.println("Logging exception:"); //$NON-NLS-1$
			e.printStackTrace();
		}
	}

	/**
	 * Performs arbitrary work or yields when there are no events to be
	 * processed.
	 * <p>
	 * This method is called when there are currently no more events on the
	 * queue to be processed at the moment.
	 * </p>
	 * <p>
	 * Clients must not call this method directly (although super calls are
	 * okay). The default implementation yields until new events enter the
	 * queue. Subclasses may override or extend this method. It is generally a
	 * bad idea to override with an empty method. It is okay to call
	 * <code>IWorkbench.close()</code> from this method.
	 * </p>
	 * 
	 * @param display
	 *            the main display of the workbench UI
	 */
	public void eventLoopIdle(Display display) {
		// default: yield cpu until new events enter the queue
		display.sleep();
	}

	/**
	 * Creates a new workbench window advisor for configuring a new workbench
	 * window via the given workbench window configurer. Clients should override
	 * to provide their own window configurer. This method replaces all the
	 * other window and action bar lifecycle methods on the workbench advisor.
	 * <p>
	 * The default implementation creates a window advisor that calls back to
	 * the legacy window and action bar lifecycle methods on the workbench
	 * advisor, for backwards compatibility with 3.0.
	 * </p>
	 * 
	 * @param configurer
	 *            the workbench window configurer
	 * @return a new workbench window advisor
	 * @since 3.1
	 */
	public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(
			IWorkbenchWindowConfigurer configurer) {
		return new CompatibilityWorkbenchWindowAdvisor(this, configurer);
	}

	/**
	 * Performs arbitrary actions before the given workbench window is opened.
	 * <p>
	 * This method is called before the window's controls have been created.
	 * Clients must not call this method directly (although super calls are
	 * okay). The default implementation does nothing. Subclasses may override.
	 * Typical clients will use the configurer passed in to tweak the workbench
	 * window in an application-specific way; however, filling the window's menu
	 * bar, tool bar, and status line must be done in
	 * {@link #fillActionBars fillActionBars}, which is called immediately
	 * after this method is called.
	 * </p>
	 * 
	 * @param configurer
	 *            an object for configuring the particular workbench window
	 *            being opened
	 * 
	 * @deprecated since 3.1, override
	 *             {@link WorkbenchWindowAdvisor#preWindowOpen()} instead
	 * @see #createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer)
	 */
	public void preWindowOpen(IWorkbenchWindowConfigurer configurer) {
		// do nothing
	}

	/**
	 * Configures the action bars using the given action bar configurer. Under
	 * normal circumstances, <code>flags</code> does not include
	 * <code>FILL_PROXY</code>, meaning this is a request to fill the
	 * actions\ bars of the given workbench window; the remaining flags indicate
	 * which combination of the menu bar (<code>FILL_MENU_BAR</code>), the
	 * tool bar (<code>FILL_COOL_BAR</code>), and the status line (<code>FILL_STATUS_LINE</code>)
	 * are to be filled.
	 * <p>
	 * If <code>flags</code> does include <code>FILL_PROXY</code>, then
	 * this is a request to describe the actions bars of the given workbench
	 * window (which will already have been filled); again, the remaining flags
	 * indicate which combination of the menu bar, the tool bar, and the status
	 * line are to be described. The actions included in the proxy action bars
	 * can be the same instances as in the actual window's action bars. Calling
	 * <code>ActionFactory</code> to create new action instances is not
	 * recommended, because these actions internally register listeners with the
	 * window and there is no opportunity to dispose of these actions.
	 * </p>
	 * <p>
	 * This method is called just after {@link #preWindowOpen preWindowOpen}.
	 * Clients must not call this method directly (although super calls are
	 * okay). The default implementation does nothing. Subclasses may override.
	 * </p>
	 * 
	 * @param window
	 *            the workbench window
	 * @param configurer
	 *            the action bar configurer object
	 * @param flags
	 *            bit mask composed from the constants
	 *            {@link #FILL_MENU_BAR FILL_MENU_BAR},
	 *            {@link #FILL_COOL_BAR FILL_COOL_BAR},
	 *            {@link #FILL_STATUS_LINE FILL_STATUS_LINE}, and
	 *            {@link #FILL_PROXY FILL_PROXY} Note: should 1st param be
	 *            IWorkbenchWindowConfigurer to be more consistent with other
	 *            methods? Note: suggest adding ActionBuilder as API, to
	 *            encapsulate the action building outside of the advisor, and to
	 *            handle the common pattern of hanging onto the action builder
	 *            in order to properly handle FILL_PROXY
	 * 
	 * @deprecated since 3.1, override
	 *             {@link ActionBarAdvisor#fillActionBars(int)} instead
	 * @see #createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer)
	 * @see WorkbenchWindowAdvisor#createActionBarAdvisor(IActionBarConfigurer)
	 */
	public void fillActionBars(IWorkbenchWindow window,
			IActionBarConfigurer configurer, int flags) {
		// do nothing by default
	}

	/**
	 * Performs arbitrary actions after the given workbench window has been
	 * restored, but before it is opened.
	 * <p>
	 * This method is called after a previously-saved window have been
	 * recreated. This method is not called when a new window is created from
	 * scratch. This method is never called when a workbench is started for the
	 * very first time, or when workbench state is not saved or restored.
	 * Clients must not call this method directly (although super calls are
	 * okay). The default implementation does nothing. Subclasses may override.
	 * It is okay to call <code>IWorkbench.close()</code> from this method.
	 * </p>
	 * 
	 * @param configurer
	 *            an object for configuring the particular workbench window just
	 *            restored
	 * 
	 * @deprecated since 3.1, override
	 *             {@link WorkbenchWindowAdvisor#postWindowRestore()} instead
	 * @see #createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer)
	 */
	public void postWindowRestore(IWorkbenchWindowConfigurer configurer)
			throws WorkbenchException {
		// do nothing
	}

	/**
	 * Opens the introduction componenet.
	 * <p>
	 * Clients must not call this method directly (although super calls are
	 * okay). The default implementation opens the intro in the first window
	 * provided the preference IWorkbenchPreferences.SHOW_INTRO is
	 * <code>true</code>. If an intro is shown then this preference will be
	 * set to <code>false</code>. Subsequently, and intro will be shown only
	 * if <code>WorkbenchConfigurer.getSaveAndRestore()</code> returns
	 * <code>true</code> and the introduction was visible on last shutdown.
	 * Subclasses may override.
	 * </p>
	 * 
	 * @param configurer
	 *            configurer an object for configuring the particular workbench
	 *            window just created
	 * 
	 * @deprecated since 3.1, override
	 *             {@link WorkbenchWindowAdvisor#openIntro()} instead
	 * @see #createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer)
	 */
	public void openIntro(IWorkbenchWindowConfigurer configurer) {
		if (introOpened) {
			return;
		}

		introOpened = true;

		boolean showIntro = PrefUtil.getAPIPreferenceStore().getBoolean(
				IWorkbenchPreferenceConstants.SHOW_INTRO);

		if (!showIntro) {
			return;
		}

		if (getWorkbenchConfigurer().getWorkbench().getIntroManager()
				.hasIntro()) {
			getWorkbenchConfigurer().getWorkbench().getIntroManager()
					.showIntro(configurer.getWindow(), false);

			PrefUtil.getAPIPreferenceStore().setValue(
					IWorkbenchPreferenceConstants.SHOW_INTRO, false);
			PrefUtil.saveAPIPrefs();
		}
	}

	/**
	 * Performs arbitrary actions after the given workbench window has been
	 * created (possibly after being restored), but has not yet been opened.
	 * <p>
	 * This method is called after a new window has been created from scratch,
	 * or when a previously-saved window has been restored. In the latter case,
	 * this method is called after <code>postWindowRestore</code>. Clients
	 * must not call this method directly (although super calls are okay). The
	 * default implementation does nothing. Subclasses may override.
	 * </p>
	 * 
	 * @param configurer
	 *            an object for configuring the particular workbench window just
	 *            created
	 * 
	 * @deprecated since 3.1, override
	 *             {@link WorkbenchWindowAdvisor#postWindowCreate()} instead
	 * @see #createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer)
	 */
	public void postWindowCreate(IWorkbenchWindowConfigurer configurer) {
		// do nothing
	}

	/**
	 * Performs arbitrary actions after the given workbench window has been
	 * opened (possibly after being restored).
	 * <p>
	 * This method is called after a window has been opened. This method is
	 * called after a new window has been created from scratch, or when a
	 * previously-saved window has been restored. Clients must not call this
	 * method directly (although super calls are okay). The default
	 * implementation does nothing. Subclasses may override.
	 * </p>
	 * 
	 * @param configurer
	 *            an object for configuring the particular workbench window just
	 *            opened
	 * 
	 * @deprecated since 3.1, override
	 *             {@link WorkbenchWindowAdvisor#postWindowOpen()} instead
	 * @see #createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer)
	 */
	public void postWindowOpen(IWorkbenchWindowConfigurer configurer) {
		// do nothing
	}

	/**
	 * Performs arbitrary actions as the given workbench window's shell is being
	 * closed directly, and possibly veto the close.
	 * <p>
	 * This method is called from a ShellListener associated with the workbench
	 * window. It is not called when the window is being closed for other
	 * reasons. Clients must not call this method directly (although super calls
	 * are okay). The default implementation does nothing. Subclasses may
	 * override. Typical clients may use the configurer passed in to access the
	 * workbench window being closed. If this method returns <code>false</code>,
	 * then the user's request to close the shell is ignored. This gives the
	 * workbench advisor an opportunity to query the user and/or veto the
	 * closing of a window under some circumstances.
	 * </p>
	 * 
	 * @param configurer
	 *            an object for configuring the particular workbench window
	 *            whose shell is being closed
	 * @return <code>true</code> to allow the window to close, and
	 *         <code>false</code> to prevent the window from closing
	 * @see org.eclipse.ui.IWorkbenchWindow#close
	 * 
	 * @deprecated since 3.1, override
	 *             {@link WorkbenchWindowAdvisor#preWindowShellClose()} instead
	 * @see #createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer)
	 */
	public boolean preWindowShellClose(IWorkbenchWindowConfigurer configurer) {
		// do nothing, but allow the close() to proceed
		return true;
	}

	/**
	 * Performs arbitrary actions after the given workbench window is closed.
	 * <p>
	 * This method is called after the window's controls have been disposed.
	 * Clients must not call this method directly (although super calls are
	 * okay). The default implementation does nothing. Subclasses may override.
	 * Typical clients will use the configurer passed in to tweak the workbench
	 * window in an application-specific way.
	 * </p>
	 * 
	 * @param configurer
	 *            an object for configuring the particular workbench window
	 *            being closed
	 * 
	 * @deprecated since 3.1, override
	 *             {@link WorkbenchWindowAdvisor#postWindowClose()} instead
	 * @see #createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer)
	 */
	public void postWindowClose(IWorkbenchWindowConfigurer configurer) {
		// do nothing
	}

	/**
	 * Returns whether the menu with the given id is an application menu of the
	 * given window. This is used during OLE "in place" editing. Application
	 * menus should be preserved during menu merging. All other menus may be
	 * removed from the window.
	 * <p>
	 * The default implementation returns false. Subclasses may override.
	 * </p>
	 * 
	 * @param configurer
	 *            an object for configuring the workbench window
	 * @param menuId
	 *            the menu id
	 * @return <code>true</code> for application menus, and <code>false</code>
	 *         for part-specific menus
	 * 
	 * @deprecated since 3.1, override
	 *             {@link ActionBarAdvisor#isApplicationMenu(String)} instead
	 * @see WorkbenchWindowAdvisor#createActionBarAdvisor(IActionBarConfigurer)
	 */
	public boolean isApplicationMenu(IWorkbenchWindowConfigurer configurer,
			String menuId) {
		// default: not an application menu
		return false;
	}

	/**
	 * Returns the default input for newly created workbench pages when the
	 * input is not explicitly specified.
	 * <p>
	 * The default implementation returns <code>null</code>. Subclasses may
	 * override.
	 * </p>
	 * 
	 * @return the default input for a new workbench window page, or
	 *         <code>null</code> if none
	 * 
	 * @see #createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer)
	 */
	public IAdaptable getDefaultPageInput() {
		// default: no input
		return null;
	}

	/**
	 * Returns the id of the perspective to use for the initial workbench
	 * window, or <code>null</code> if no initial perspective should be shown
	 * in the initial workbench window.
	 * <p>
	 * This method is called during startup when the workbench is creating the
	 * first new window. Subclasses must implement.
	 * </p>
	 * <p>
	 * If the {@link IWorkbenchPreferenceConstants#DEFAULT_PERSPECTIVE_ID}
	 * preference is specified, it supercedes the perspective specified here.
	 * </p>
	 * 
	 * @return the id of the perspective for the initial window, or
	 *         <code>null</code> if no initial perspective should be shown
	 */
	public abstract String getInitialWindowPerspectiveId();

	/**
	 * Returns the id of the preference page that should be presented most
	 * prominently.
	 * <p>
	 * The default implementation returns <code>null</code>. Subclasses may
	 * override.
	 * </p>
	 * 
	 * @return the id of the preference page, or <code>null</code> if none
	 */
	public String getMainPreferencePageId() {
		// default: no opinion
		return null;
	}

	/**
	 * Creates the contents of the window.
	 * <p>
	 * The default implementation adds a menu bar, a cool bar, a status line, a
	 * perspective bar, and a fast view bar. The visibility of these controls
	 * can be configured using the <code>setShow*</code> methods on
	 * <code>IWorkbenchWindowConfigurer</code>.
	 * </p>
	 * <p>
	 * Subclasses may override to define custom window contents and layout, but
	 * must call <code>IWorkbenchWindowConfigurer.createPageComposite</code>.
	 * </p>
	 * 
	 * @param configurer
	 *            the window configurer
	 * @param shell
	 *            the window's shell
	 * @see IWorkbenchWindowConfigurer#createMenuBar
	 * @see IWorkbenchWindowConfigurer#createCoolBarControl
	 * @see IWorkbenchWindowConfigurer#createStatusLineControl
	 * @see IWorkbenchWindowConfigurer#createPageComposite
	 * 
	 * @deprecated since 3.1, override
	 *             {@link WorkbenchWindowAdvisor#createWindowContents(Shell)}
	 *             instead
	 * @see #createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer)
	 */
	public void createWindowContents(IWorkbenchWindowConfigurer configurer,
			Shell shell) {
		((WorkbenchWindowConfigurer) configurer).createDefaultContents(shell);
	}

	/**
	 * Opens the workbench windows on startup. The default implementation tries
	 * to restore the previously saved workbench state using
	 * <code>IWorkbenchConfigurer.restoreWorkbenchState()</code>. If there
	 * was no previously saved state, or if the restore failed, then a
	 * first-time window is opened using
	 * <code>IWorkbenchConfigurer.openFirstTimeWindow</code>.
	 * 
	 * @return <code>true</code> to proceed with workbench startup, or
	 *         <code>false</code> to exit
	 */
	public boolean openWindows() {
		final Display display = PlatformUI.getWorkbench().getDisplay();
		final boolean result [] = new boolean[1];
		
		// spawn another init thread.  For API compatibility We guarantee this method is called from 
		// the UI thread but it could take enough time to disrupt progress reporting.
		// spawn a new thread to do the grunt work of this initialization and spin the event loop 
		// ourselves just like it's done in Workbench.
		final boolean[] initDone = new boolean[]{false};
		final Throwable [] error = new Throwable[1];
		Thread initThread = new Thread() {
			/* (non-Javadoc)
			 * @see java.lang.Thread#run()
			 */
			public void run() {
				try {
					//declare us to be a startup thread so that our syncs will be executed 
					UISynchronizer.startupThread.set(Boolean.TRUE);
					final IWorkbenchConfigurer [] myConfigurer = new IWorkbenchConfigurer[1];
					StartupThreading.runWithoutExceptions(new StartupRunnable() {
	
						public void runWithException() throws Throwable {
							myConfigurer[0] = getWorkbenchConfigurer();
							
						}});
					
					IStatus status = myConfigurer[0].restoreState();
					if (!status.isOK()) {
						if (status.getCode() == IWorkbenchConfigurer.RESTORE_CODE_EXIT) {
							result[0] = false;
							return;
						}
						if (status.getCode() == IWorkbenchConfigurer.RESTORE_CODE_RESET) {
							myConfigurer[0].openFirstTimeWindow();
						}
					}
					result[0] = true;
				} catch (Throwable e) {
					error[0] = e;
				}
				finally {
					initDone[0] = true;
					display.wake();
				}
			}};
			initThread.start();

			while (true) {
				if (!display.readAndDispatch()) {
					if (initDone[0])
						break;
					display.sleep();
				}
				
			}
			
			// can only be a runtime or error
			if (error[0] instanceof Error)
				throw (Error)error[0];
			else if (error[0] instanceof RuntimeException)
				throw (RuntimeException)error[0];
		
			return result[0];
	}

	/**
	 * Saves arbitrary application-specific state information for this workbench
	 * advisor.
	 * <p>
	 * The default implementation simply returns an OK status. Subclasses may
	 * extend or override.
	 * </p>
	 * 
	 * @param memento
	 *            the memento in which to save the advisor's state
	 * @return a status object indicating whether the save was successful
	 * @since 3.1
	 */
	public IStatus saveState(IMemento memento) {
		return Status.OK_STATUS;
	}

	/**
	 * Restores arbitrary application-specific state information for this
	 * workbench advisor.
	 * <p>
	 * The default implementation simply returns an OK status. Subclasses may
	 * extend or override.
	 * </p>
	 * 
	 * @param memento
	 *            the memento from which to restore the advisor's state
	 * @return a status object indicating whether the restore was successful
	 * @since 3.1
	 */
	public IStatus restoreState(IMemento memento) {
		return Status.OK_STATUS;
	}

	/**
	 * Return the contribution comparator for the particular type of
	 * contribution. The default implementation of this class returns a
	 * comparator that sorts the items by label.
	 * 
	 * The contributionType may be one of the constants in
	 * {@link IContributionService} or it can be a value defined by the user.
	 * 
	 * @param contributionType
	 *            the contribution type
	 * @return the comparator, must not return <code>null</code>
	 * @see IContributionService#getComparatorFor(String)
	 * @since 3.4
	 */
	public ContributionComparator getComparatorFor(String contributionType) {
		return new ContributionComparator();
	}
}