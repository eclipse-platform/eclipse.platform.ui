/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.CommandManager;
import org.eclipse.core.commands.common.EventManager;
import org.eclipse.core.commands.contexts.ContextManager;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionDelta;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.IRegistryChangeListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.ExternalActionManager;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ExternalActionManager.CommandCallback;
import org.eclipse.jface.action.ExternalActionManager.IActiveChecker;
import org.eclipse.jface.bindings.BindingManager;
import org.eclipse.jface.bindings.BindingManagerEvent;
import org.eclipse.jface.bindings.IBindingManagerListener;
import org.eclipse.jface.commands.CommandImageManager;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.menus.SMenuManager;
import org.eclipse.jface.operation.ModalContext;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.OpenStrategy;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.window.WindowManager;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.DeviceData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.ILocalWorkingSetManager;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.activities.IWorkbenchActivitySupport;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.commands.ICommandImageService;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IWorkbenchCommandSupport;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.contexts.IWorkbenchContextSupport;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.help.IWorkbenchHelpSystem;
import org.eclipse.ui.internal.activities.ws.WorkbenchActivitySupport;
import org.eclipse.ui.internal.browser.WorkbenchBrowserSupport;
import org.eclipse.ui.internal.commands.CommandImageService;
import org.eclipse.ui.internal.commands.CommandService;
import org.eclipse.ui.internal.commands.WorkbenchCommandSupport;
import org.eclipse.ui.internal.contexts.ActiveContextSourceProvider;
import org.eclipse.ui.internal.contexts.ContextService;
import org.eclipse.ui.internal.contexts.WorkbenchContextSupport;
import org.eclipse.ui.internal.dialogs.PropertyPageContributorManager;
import org.eclipse.ui.internal.fieldassist.WorkbenchFieldDecorationSupport;
import org.eclipse.ui.internal.handlers.HandlerService;
import org.eclipse.ui.internal.help.WorkbenchHelpSystem;
import org.eclipse.ui.internal.intro.IIntroRegistry;
import org.eclipse.ui.internal.intro.IntroDescriptor;
import org.eclipse.ui.internal.keys.BindingService;
import org.eclipse.ui.internal.menus.LegacyActionPersistence;
import org.eclipse.ui.internal.menus.WorkbenchMenuService;
import org.eclipse.ui.internal.misc.Assert;
import org.eclipse.ui.internal.misc.Policy;
import org.eclipse.ui.internal.misc.StatusUtil;
import org.eclipse.ui.internal.misc.UIStats;
import org.eclipse.ui.internal.progress.ProgressManager;
import org.eclipse.ui.internal.registry.IActionSetDescriptor;
import org.eclipse.ui.internal.registry.UIExtensionTracker;
import org.eclipse.ui.internal.services.ActionSetSourceProvider;
import org.eclipse.ui.internal.services.ActivePartSourceProvider;
import org.eclipse.ui.internal.services.ActiveShellSourceProvider;
import org.eclipse.ui.internal.services.CurrentSelectionSourceProvider;
import org.eclipse.ui.internal.services.ISourceProviderService;
import org.eclipse.ui.internal.services.MenuSourceProvider;
import org.eclipse.ui.internal.services.ServiceLocator;
import org.eclipse.ui.internal.services.SourceProviderService;
import org.eclipse.ui.internal.testing.WorkbenchTestable;
import org.eclipse.ui.internal.themes.ColorDefinition;
import org.eclipse.ui.internal.themes.FontDefinition;
import org.eclipse.ui.internal.themes.ThemeElementHelper;
import org.eclipse.ui.internal.themes.WorkbenchThemeManager;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.intro.IIntroManager;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.operations.IWorkbenchOperationSupport;
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.ui.themes.IThemeManager;
import org.eclipse.ui.views.IViewRegistry;
import org.eclipse.ui.wizards.IWizardRegistry;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.SynchronousBundleListener;

/**
 * The workbench class represents the top of the Eclipse user interface. Its
 * primary responsability is the management of workbench windows, dialogs,
 * wizards, and other workbench-related windows.
 * <p>
 * Note that any code that is run during the creation of a workbench instance
 * should not required access to the display.
 * </p>
 * <p>
 * Note that this internal class changed significantly between 2.1 and 3.0.
 * Applications that used to define subclasses of this internal class need to be
 * rewritten to use the new workbench advisor API.
 * </p>
 */
public final class Workbench extends EventManager implements IWorkbench {
	
	private final class StartupProgressBundleListener implements SynchronousBundleListener {

		private final IProgressMonitor progressMonitor;
		private final int maximumProgressCount;

		// stack of names of bundles currently starting
		private final List starting;

		StartupProgressBundleListener(IProgressMonitor progressMonitor, int maximumProgressCount) {
			super();
			this.progressMonitor = progressMonitor;
			this.maximumProgressCount = maximumProgressCount;
			this.starting = new ArrayList();
		}

		public void bundleChanged(BundleEvent event) {
			int eventType = event.getType();
			String bundleName;

			synchronized (this) {
				if (eventType == BundleEvent.STARTING) {
					starting.add(bundleName = event.getBundle().getSymbolicName());
				} else if (eventType == BundleEvent.STARTED) {
					progressCount++;
					if (progressCount <= maximumProgressCount)
						progressMonitor.worked(1);
					int index = starting.lastIndexOf(event.getBundle().getSymbolicName());
					if (index >= 0)
						starting.remove(index);
					if (index != starting.size())
						return; // not currently displayed
					bundleName = index == 0 ? null : (String) starting.get(index - 1);
				} else {
					return; // uninteresting event
				}
			}

			String taskName;

			if (bundleName == null)
				taskName = WorkbenchMessages.Startup_Loading_Workbench;
			else
				taskName = NLS.bind(WorkbenchMessages.Startup_Loading, bundleName);

			progressMonitor.subTask(taskName);
		}
	}

	/**
	 * Family for the early startup job.
	 */
	public static final String EARLY_STARTUP_FAMILY = "earlyStartup"; //$NON-NLS-1$

	private static final String VERSION_STRING[] = { "0.046", "2.0" }; //$NON-NLS-1$ //$NON-NLS-2$

	private static final String DEFAULT_WORKBENCH_STATE_FILENAME = "workbench.xml"; //$NON-NLS-1$

	/**
	 * Holds onto the only instance of Workbench.
	 */
	private static Workbench instance;

	/**
	 * The testable object facade.
	 * 
	 * @since 3.0
	 */
	private static WorkbenchTestable testableObject;

	/**
	 * The display used for all UI interactions with this workbench.
	 * 
	 * @since 3.0
	 */
	private Display display;

	private WindowManager windowManager;

	private WorkbenchWindow activatedWindow;

	private EditorHistory editorHistory;

	private boolean runEventLoop = true;

	private boolean isStarting = true;

	private boolean isClosing = false;

	/**
	 * PlatformUI return code (as opposed to IPlatformRunnable return code).
	 */
	private int returnCode = PlatformUI.RETURN_UNSTARTABLE;

	/**
	 * Advisor providing application-specific configuration and customization of
	 * the workbench.
	 * 
	 * @since 3.0
	 */
	private WorkbenchAdvisor advisor;

	/**
	 * Object for configuring the workbench. Lazily initialized to an instance
	 * unique to the workbench instance.
	 * 
	 * @since 3.0
	 */
	private WorkbenchConfigurer workbenchConfigurer;

	// for dynamic UI
	/**
	 * ExtensionEventHandler handles extension life-cycle events.
	 */
	private ExtensionEventHandler extensionEventHandler;

	/**
	 * A count of how many large updates are going on. This tracks nesting of
	 * requests to disable services during a large update -- similar to the
	 * <code>setRedraw</code> functionality on <code>Control</code>. When
	 * this value becomes greater than zero, services are disabled. When this
	 * value becomes zero, services are enabled. Please see
	 * <code>largeUpdateStart()</code> and <code>largeUpdateEnd()</code>.
	 */
	private int largeUpdates = 0;

	/**
	 * The service locator maintained by the workbench. These services are
	 * initialized during workbench during the <code>init</code> method.
	 */
	private final ServiceLocator serviceLocator = new ServiceLocator();
	
	/**
	 * A count of how many plug-ins were loaded while restoring the
	 * workbench state. Initially -1 for unknown number.
	 */
	private int progressCount = -1;
	
	/**
	 * A field to hold the workbench windows that have been restored.  In the
	 * event that not all windows have been restored, this field allows the 
	 * openWindowsAfterRestore method to open some windows.
	 */
	private WorkbenchWindow[] createdWindows;

	/**
	 * Creates a new workbench.
	 * 
	 * @param display
	 *            the display to be used for all UI interactions with the
	 *            workbench
	 * @param advisor
	 *            the application-specific advisor that configures and
	 *            specializes this workbench instance
	 * @since 3.0
	 */
	private Workbench(Display display, WorkbenchAdvisor advisor) {
		super();

		if (instance != null && instance.isRunning()) {
			throw new IllegalStateException(
					WorkbenchMessages.Workbench_CreatingWorkbenchTwice);
		}
		Assert.isNotNull(display);
		Assert.isNotNull(advisor);
		this.advisor = advisor;
		this.display = display;
		Workbench.instance = this;

		// for dynamic UI [This seems to be for everything that isn't handled by
		// some
		// subclass of RegistryManager. I think that when an extension is moved
		// to the
		// RegistryManager implementation, then it should be removed from the
		// list in
		// ExtensionEventHandler#appear.
		// I've found that the new wizard extension in particular is a poor
		// choice to
		// use as an example, since the result of reading the registry is not
		// cached
		// -- so it is re-read each time. The only real contribution of this
		// dialog is
		// to show the user a nice dialog describing the addition.]
		extensionEventHandler = new ExtensionEventHandler(this);
		Platform.getExtensionRegistry().addRegistryChangeListener(
				extensionEventHandler);
	}

	/**
	 * Returns the one and only instance of the workbench, if there is one.
	 * 
	 * @return the workbench, or <code>null</code> if the workbench has not
	 *         been created, or has been created and already completed
	 */
	public static final Workbench getInstance() {
		return instance;
	}

	/**
	 * Creates the workbench and associates it with the the given display and
	 * workbench advisor, and runs the workbench UI. This entails processing and
	 * dispatching events until the workbench is closed or restarted.
	 * <p>
	 * This method is intended to be called by <code>PlatformUI</code>. Fails
	 * if the workbench UI has already been created.
	 * </p>
	 * <p>
	 * The display passed in must be the default display.
	 * </p>
	 * 
	 * @param display
	 *            the display to be used for all UI interactions with the
	 *            workbench
	 * @param advisor
	 *            the application-specific advisor that configures and
	 *            specializes the workbench
	 * @return return code {@link PlatformUI#RETURN_OK RETURN_OK}for normal
	 *         exit; {@link PlatformUI#RETURN_RESTART RETURN_RESTART}if the
	 *         workbench was terminated with a call to
	 *         {@link IWorkbench#restart IWorkbench.restart}; other values
	 *         reserved for future use
	 */
	public static final int createAndRunWorkbench(Display display,
			WorkbenchAdvisor advisor) {
		// create the workbench instance
		Workbench workbench = new Workbench(display, advisor);
		// run the workbench event loop
		int returnCode = workbench.runUI();
		return returnCode;
	}

	/**
	 * Creates the <code>Display</code> to be used by the workbench.
	 * 
	 * @return the display
	 */
	public static Display createDisplay() {
		// setup the application name used by SWT to lookup resources on some
		// platforms
		String applicationName = WorkbenchPlugin.getDefault().getAppName();
		if (applicationName != null) {
			Display.setAppName(applicationName);
		}

		// create the display
		Display newDisplay = null;
		if (Policy.DEBUG_SWT_GRAPHICS || Policy.DEBUG_SWT_DEBUG) {
			DeviceData data = new DeviceData();
			if (Policy.DEBUG_SWT_GRAPHICS)
				data.tracking = true;
			if (Policy.DEBUG_SWT_DEBUG)
				data.debug = true;
			newDisplay = new Display(data);
		} else {
			newDisplay = new Display();
		}

		// workaround for 1GEZ9UR and 1GF07HN
		newDisplay.setWarnings(false);

		// Set the priority higher than normal so as to be higher
		// than the JobManager.
		Thread.currentThread().setPriority(
				Math.min(Thread.MAX_PRIORITY, Thread.NORM_PRIORITY + 1));

		initializeImages();
		
		return newDisplay;
	}

	/**
	 * Returns the testable object facade, for use by the test harness.
	 * 
	 * @return the testable object facade
	 * @since 3.0
	 */
	public static WorkbenchTestable getWorkbenchTestable() {
		if (testableObject == null) {
			testableObject = new WorkbenchTestable();
		}
		return testableObject;
	}

	/*
	 * (non-Javadoc) Method declared on IWorkbench.
	 */
	public void addWindowListener(IWindowListener l) {
		addListenerObject(l);
	}

	/*
	 * (non-Javadoc) Method declared on IWorkbench.
	 */
	public void removeWindowListener(IWindowListener l) {
		removeListenerObject(l);
	}

	/**
	 * Fire window opened event.
	 * 
	 * @param window
	 *            The window which just opened; should not be <code>null</code>.
	 */
	protected void fireWindowOpened(final IWorkbenchWindow window) {
		Object list[] = getListeners();
		for (int i = 0; i < list.length; i++) {
			final IWindowListener l = (IWindowListener) list[i];
			Platform.run(new SafeRunnable() {
				public void run() {
					l.windowOpened(window);
				}
			});
		}
	}

	/**
	 * Fire window closed event.
	 * 
	 * @param window
	 *            The window which just closed; should not be <code>null</code>.
	 */
	protected void fireWindowClosed(final IWorkbenchWindow window) {
		if (activatedWindow == window) {
			// Do not hang onto it so it can be GC'ed
			activatedWindow = null;
		}

		Object list[] = getListeners();
		for (int i = 0; i < list.length; i++) {
			final IWindowListener l = (IWindowListener) list[i];
			Platform.run(new SafeRunnable() {
				public void run() {
					l.windowClosed(window);
				}
			});
		}
	}

	/**
	 * Fire window activated event.
	 * 
	 * @param window
	 *            The window which was just activated; should not be
	 *            <code>null</code>.
	 */
	protected void fireWindowActivated(final IWorkbenchWindow window) {
		Object list[] = getListeners();
		for (int i = 0; i < list.length; i++) {
			final IWindowListener l = (IWindowListener) list[i];
			Platform.run(new SafeRunnable() {
				public void run() {
					l.windowActivated(window);
				}
			});
		}
	}

	/**
	 * Fire window deactivated event.
	 * 
	 * @param window
	 *            The window which was just deactivated; should not be
	 *            <code>null</code>.
	 */
	protected void fireWindowDeactivated(final IWorkbenchWindow window) {
		Object list[] = getListeners();
		for (int i = 0; i < list.length; i++) {
			final IWindowListener l = (IWindowListener) list[i];
			Platform.run(new SafeRunnable() {
				public void run() {
					l.windowDeactivated(window);
				}
			});
		}
	}

	/**
	 * Closes the workbench. Assumes that the busy cursor is active.
	 * 
	 * @param force
	 *            true if the close is mandatory, and false if the close is
	 *            allowed to fail
	 * @return true if the close succeeded, and false otherwise
	 */
	private boolean busyClose(final boolean force) {

		// notify the advisor of preShutdown and allow it to veto if not forced
		isClosing = advisor.preShutdown();
		if (!force && !isClosing) {
			return false;
		}

		// save any open editors if they are dirty
		isClosing = saveAllEditors(!force);
		if (!force && !isClosing) {
			return false;
		}

		boolean closeEditors = PrefUtil.getAPIPreferenceStore()
				.getBoolean(IWorkbenchPreferenceConstants.CLOSE_EDITORS_ON_EXIT);
		if (closeEditors) {
			Platform.run(new SafeRunnable() {
				public void run() {
					IWorkbenchWindow windows[] = getWorkbenchWindows();
					for (int i = 0; i < windows.length; i++) {
						IWorkbenchPage pages[] = windows[i].getPages();
						for (int j = 0; j < pages.length; j++) {
							isClosing = isClosing
									&& pages[j].closeAllEditors(false);
						}
					}
				}
			});
			if (!force && !isClosing) {
				return false;
			}
		}

		if (getWorkbenchConfigurer().getSaveAndRestore()) {
			Platform.run(new SafeRunnable() {
				public void run() {
					XMLMemento mem = recordWorkbenchState();
					// Save the IMemento to a file.
					saveMementoToFile(mem);
				}

				public void handleException(Throwable e) {
					String message;
					if (e.getMessage() == null) {
						message = WorkbenchMessages.ErrorClosingNoArg;
					} else {
						message = NLS.bind(
								WorkbenchMessages.ErrorClosingOneArg, e
										.getMessage());
					}

					if (!MessageDialog.openQuestion(null,
							WorkbenchMessages.Error, message)) {
						isClosing = false;
					}
				}
			});
		}
		if (!force && !isClosing) {
			return false;
		}

		Platform.run(new SafeRunnable(WorkbenchMessages.ErrorClosing) {
			public void run() {
				if (isClosing || force)
					isClosing = windowManager.close();
			}
		});

		if (!force && !isClosing) {
			return false;
		}

		shutdown();

		runEventLoop = false;
		return true;
	}

	/*
	 * (non-Javadoc) Method declared on IWorkbench.
	 */
	public boolean saveAllEditors(boolean confirm) {
		final boolean finalConfirm = confirm;
		final boolean[] result = new boolean[1];
		result[0] = true;

		Platform.run(new SafeRunnable(WorkbenchMessages.ErrorClosing) {
			public void run() {
				// Collect dirtyParts
				ArrayList dirtyParts = new ArrayList();
				ArrayList dirtyEditorsInput = new ArrayList();
				IWorkbenchWindow windows[] = getWorkbenchWindows();
				for (int i = 0; i < windows.length; i++) {
					IWorkbenchPage pages[] = windows[i].getPages();
					for (int j = 0; j < pages.length; j++) {
						WorkbenchPage page = (WorkbenchPage) pages[j];
						
                        ISaveablePart[] parts = page.getDirtyParts();
                        
                        for (int k = 0; k < parts.length; k++) {
                            ISaveablePart part = parts[k];
                            
                            if (part.isSaveOnCloseNeeded()) {
                                if (part instanceof IEditorPart) {
                                    IEditorPart editor = (IEditorPart)part;
                                    if (!dirtyEditorsInput.contains(editor
                                            .getEditorInput())) {
                                        dirtyParts.add(editor);
                                        dirtyEditorsInput.add(editor
                                                .getEditorInput());
                                    }
                                } else {
                                    dirtyParts.add(part);
                                }
                            }
                        }
   					}
				}
				if (dirtyParts.size() > 0) {
					IWorkbenchWindow w = getActiveWorkbenchWindow();
					if (w == null)
						w = windows[0];
					result[0] = EditorManager.saveAll(dirtyParts,
							finalConfirm, false, w);
				}
			}
		});
		return result[0];
	}

	/**
	 * Opens a new workbench window and page with a specific perspective.
	 * 
	 * Assumes that busy cursor is active.
	 */
	private IWorkbenchWindow busyOpenWorkbenchWindow(String perspID,
			IAdaptable input) throws WorkbenchException {
		// Create a workbench window (becomes active window)
		WorkbenchWindow newWindow = newWorkbenchWindow();
		newWindow.create(); // must be created before adding to window manager
		windowManager.add(newWindow);

		// Create the initial page.
		if (perspID != null) {
			try {
				newWindow.busyOpenPage(perspID, input);
			} catch (WorkbenchException e) {
				windowManager.remove(newWindow);
				throw e;
			}
		}

		// Open window after opening page, to avoid flicker.
		newWindow.open();

		return newWindow;
	}

	/*
	 * (non-Javadoc) Method declared on IWorkbench.
	 */
	public boolean close() {
		return close(PlatformUI.RETURN_OK, false);
	}

	/**
	 * Closes the workbench, returning the given return code from the run
	 * method. If forced, the workbench is closed no matter what.
	 * 
	 * @param returnCode
	 *            {@link PlatformUI#RETURN_OK RETURN_OK}for normal exit;
	 *            {@link PlatformUI#RETURN_RESTART RETURN_RESTART}if the
	 *            workbench was terminated with a call to
	 *            {@link IWorkbench#restart IWorkbench.restart};
	 *            {@link PlatformUI#RETURN_UNSTARTABLE RETURN_UNSTARTABLE}if
	 *            the workbench could not be started; other values reserved for
	 *            future use
	 * @param force
	 *            true to force the workbench close, and false for a "soft"
	 *            close that can be canceled
	 * @return true if the close was successful, and false if the close was
	 *         canceled
	 */
	/* package */
	boolean close(int returnCode, final boolean force) {
		this.returnCode = returnCode;
		final boolean[] ret = new boolean[1];
		BusyIndicator.showWhile(null, new Runnable() {
			public void run() {
				ret[0] = busyClose(force);
			}
		});
		return ret[0];
	}

	/*
	 * (non-Javadoc) Method declared on IWorkbench.
	 */
	public IWorkbenchWindow getActiveWorkbenchWindow() {
		// Return null if called from a non-UI thread.
		// This is not spec'ed behaviour and is misleading, however this is how
		// it
		// worked in 2.1 and we cannot change it now.
		// For more details, see [Bug 57384] [RCP] Main window not active on
		// startup
		if (Display.getCurrent() == null) {
			return null;
		}

		// Look at the current shell and up its parent
		// hierarchy for a workbench window.
		Control shell = display.getActiveShell();
		while (shell != null) {
			Object data = shell.getData();
			if (data instanceof IWorkbenchWindow)
				return (IWorkbenchWindow) data;
			shell = shell.getParent();
		}

		// Look for the window that was last known being
		// the active one
		WorkbenchWindow win = getActivatedWindow();
		if (win != null) {
			return win;
		}

		// Look at all the shells and pick the first one
		// that is a workbench window.
		Shell shells[] = display.getShells();
		for (int i = 0; i < shells.length; i++) {
			Object data = shells[i].getData();
			if (data instanceof IWorkbenchWindow)
				return (IWorkbenchWindow) data;
		}

		// Can't find anything!
		return null;
	}

	/*
	 * Returns the editor history.
	 */
	protected EditorHistory getEditorHistory() {
		if (editorHistory == null) {
			editorHistory = new EditorHistory();
		}
		return editorHistory;
	}

	/*
	 * (non-Javadoc) Method declared on IWorkbench.
	 */
	public IEditorRegistry getEditorRegistry() {
		return WorkbenchPlugin.getDefault().getEditorRegistry();
	}

	/*
	 * Returns the number for a new window. This will be the first number > 0
	 * which is not used to identify another window in the workbench.
	 */
	private int getNewWindowNumber() {
		// Get window list.
		Window[] windows = windowManager.getWindows();
		int count = windows.length;

		// Create an array of booleans (size = window count).
		// Cross off every number found in the window list.
		boolean checkArray[] = new boolean[count];
		for (int nX = 0; nX < count; nX++) {
			if (windows[nX] instanceof WorkbenchWindow) {
				WorkbenchWindow ww = (WorkbenchWindow) windows[nX];
				int index = ww.getNumber() - 1;
				if (index >= 0 && index < count)
					checkArray[index] = true;
			}
		}

		// Return first index which is not used.
		// If no empty index was found then every slot is full.
		// Return next index.
		for (int index = 0; index < count; index++) {
			if (!checkArray[index])
				return index + 1;
		}
		return count + 1;
	}

	/*
	 * (non-Javadoc) Method declared on IWorkbench.
	 */
	public IWorkbenchOperationSupport getOperationSupport() {
		return WorkbenchPlugin.getDefault().getOperationSupport();
	}

	/*
	 * (non-Javadoc) Method declared on IWorkbench.
	 */
	public IPerspectiveRegistry getPerspectiveRegistry() {
		return WorkbenchPlugin.getDefault().getPerspectiveRegistry();
	}

	/*
	 * (non-Javadoc) Method declared on IWorkbench.
	 */
	public PreferenceManager getPreferenceManager() {
		return WorkbenchPlugin.getDefault().getPreferenceManager();
	}

	/*
	 * (non-Javadoc) Method declared on IWorkbench.
	 */
	public IPreferenceStore getPreferenceStore() {
		return WorkbenchPlugin.getDefault().getPreferenceStore();
	}

	/*
	 * (non-Javadoc) Method declared on IWorkbench.
	 */
	public ISharedImages getSharedImages() {
		return WorkbenchPlugin.getDefault().getSharedImages();
	}

	/**
	 * Returns the window manager for this workbench.
	 * 
	 * @return the window manager
	 */
	/* package */
	WindowManager getWindowManager() {
		return windowManager;
	}

	/*
	 * Answer the workbench state file.
	 */
	private File getWorkbenchStateFile() {
		IPath path = WorkbenchPlugin.getDefault().getDataLocation();
		if (path == null)
			return null;
		path = path.append(DEFAULT_WORKBENCH_STATE_FILENAME);
		return path.toFile();
	}

	/*
	 * (non-Javadoc) Method declared on IWorkbench.
	 */
	public int getWorkbenchWindowCount() {
		return windowManager.getWindowCount();
	}

	/*
	 * (non-Javadoc) Method declared on IWorkbench.
	 */
	public IWorkbenchWindow[] getWorkbenchWindows() {
		Window[] windows = windowManager.getWindows();
		IWorkbenchWindow[] dwindows = new IWorkbenchWindow[windows.length];
		System.arraycopy(windows, 0, dwindows, 0, windows.length);
		return dwindows;
	}

	/*
	 * (non-Javadoc) Method declared on IWorkbench.
	 */
	public IWorkingSetManager getWorkingSetManager() {
		return WorkbenchPlugin.getDefault().getWorkingSetManager();
	}

	/**
	 * {@inheritDoc}
	 */
	public ILocalWorkingSetManager createLocalWorkingSetManager() {
		return new LocalWorkingSetManager(WorkbenchPlugin.getDefault()
				.getBundleContext());
	}

	/**
	 * Initializes the workbench now that the display is created.
	 * 
	 * @return true if init succeeded.
	 */
	private boolean init(Display display) {
		// setup debug mode if required.
		if (WorkbenchPlugin.getDefault().isDebugging()) {
			WorkbenchPlugin.DEBUG = true;
			ModalContext.setDebugMode(true);
		}

		//Set up the JFace preference store
		JFaceUtil.initializeJFacePreferences();
				
		// create workbench window manager
		windowManager = new WindowManager();

		IIntroRegistry introRegistry = WorkbenchPlugin.getDefault()
				.getIntroRegistry();
		if (introRegistry.getIntroCount() > 0) {
			IProduct product = Platform.getProduct();
			if (product != null) {
				introDescriptor = (IntroDescriptor) introRegistry
						.getIntroForProduct(product.getId());
			}
		}

		// Initialize the activity support.
		workbenchActivitySupport = new WorkbenchActivitySupport();
		activityHelper = ActivityPersistanceHelper.getInstance();

		initializeDefaultServices();
		initializeFonts();
		initializeColors();
		initializeApplicationColors();
		initializeFieldDecorations();

		// now that the workbench is sufficiently initialized, let the advisor
		// have a turn.
		advisor.internalBasicInitialize(getWorkbenchConfigurer());

		// configure use of color icons in toolbars
		boolean useColorIcons = PrefUtil.getInternalPreferenceStore().getBoolean(
				IPreferenceConstants.COLOR_ICONS);
		ActionContributionItem.setUseColorIconsInToolbars(useColorIcons);

		// initialize workbench single-click vs double-click behavior
		initializeSingleClickOption();

		// attempt to restore a previous workbench state
		try {
			UIStats.start(UIStats.RESTORE_WORKBENCH, "Workbench"); //$NON-NLS-1$

			advisor.preStartup();

			if (!advisor.openWindows()) {
				return false;
			}

		} finally {
			UIStats.end(UIStats.RESTORE_WORKBENCH, this, "Workbench"); //$NON-NLS-1$
		}

		forceOpenPerspective();

		return true;
	}

	/**
	 * Establishes the relationship between JFace actions and the command
	 * manager.
	 */
	private void initializeCommandResolver() {
		ExternalActionManager.getInstance().setCallback(
				new CommandCallback(bindingManager, commandManager,
						new IActiveChecker() {
							public final boolean isActive(final String commandId) {
								return workbenchActivitySupport
										.getActivityManager().getIdentifier(
												commandId).isEnabled();
							}
						}));
	}

	/**
	 * Initialize colors defined by the new colorDefinitions extension point.
	 * Note this will be rolled into initializeColors() at some point.
	 * 
	 * @since 3.0
	 */
	private void initializeApplicationColors() {
		ColorDefinition[] colorDefinitions = WorkbenchPlugin.getDefault()
				.getThemeRegistry().getColors();
		ThemeElementHelper.populateRegistry(getThemeManager().getTheme(
				IThemeManager.DEFAULT_THEME), colorDefinitions,
				PrefUtil.getInternalPreferenceStore());
	}

	private void initializeSingleClickOption() {
		IPreferenceStore store = WorkbenchPlugin.getDefault()
				.getPreferenceStore();
		boolean openOnSingleClick = store
				.getBoolean(IPreferenceConstants.OPEN_ON_SINGLE_CLICK);
		boolean selectOnHover = store
				.getBoolean(IPreferenceConstants.SELECT_ON_HOVER);
		boolean openAfterDelay = store
				.getBoolean(IPreferenceConstants.OPEN_AFTER_DELAY);
		int singleClickMethod = openOnSingleClick ? OpenStrategy.SINGLE_CLICK
				: OpenStrategy.DOUBLE_CLICK;
		if (openOnSingleClick) {
			if (selectOnHover)
				singleClickMethod |= OpenStrategy.SELECT_ON_HOVER;
			if (openAfterDelay)
				singleClickMethod |= OpenStrategy.ARROW_KEYS_OPEN;
		}
		OpenStrategy.setOpenMethod(singleClickMethod);
	}

	/*
	 * Initializes the workbench fonts with the stored values.
	 */
	private void initializeFonts() {
		FontDefinition[] fontDefinitions = WorkbenchPlugin.getDefault()
				.getThemeRegistry().getFonts();
		ThemeElementHelper.populateRegistry(getThemeManager().getTheme(
				IThemeManager.DEFAULT_THEME), fontDefinitions,
				PrefUtil.getInternalPreferenceStore());
	}

	/*
	 * Initialize the workbench images.
	 * 
	 * @param windowImages An array of the descriptors of the images to be used
	 * in the corner of each window, or <code>null</code> if none. It is
	 * expected that the array will contain the same icon, rendered at different
	 * sizes.
	 * 
	 * @since 3.0
	 */
	private static void initializeImages() {
		ImageDescriptor[] windowImages = WorkbenchPlugin.getDefault()
				.getWindowImages();
		if (windowImages == null)
			return;

		Image[] images = new Image[windowImages.length];
		for (int i = 0; i < windowImages.length; ++i)
			images[i] = windowImages[i].createImage();
		Window.setDefaultImages(images);
	}

	/*
	 * Take the workbenches' images out of the shared registry.
	 * 
	 * @since 3.0
	 */
	private void uninitializeImages() {
		WorkbenchImages.dispose();
		Window.setDefaultImage(null);
	}

	/*
	 * Initialize the workbench colors.
	 * 
	 * @since 3.0
	 */
	private void initializeColors() {
		// @issue some colors are generic; some are app-specific
		WorkbenchColors.startup();
	}

	/*
	 * (non-Javadoc) Method declared on IWorkbench.
	 */
	public boolean isClosing() {
		return isClosing;
	}
	
	/**
	 * Initializes all of the default command-based services for the workbench.
	 * This also parses the registry and hooks up all the required listeners.
	 */
	private final void initializeDefaultServices() {
		/*
		 * Phase 1 of the initialization of commands. When this phase completes,
		 * all the services and managers will exist, and be accessible via the
		 * getService(Object) method.
		 */
		Command.DEBUG_COMMAND_EXECUTION = Policy.DEBUG_COMMANDS;
		commandManager = new CommandManager();
		final CommandService commandService = new CommandService(commandManager);
		commandService.readRegistry();
		serviceLocator.registerService(ICommandService.class, commandService);
		
		ContextManager.DEBUG = Policy.DEBUG_CONTEXTS;
		contextManager = new ContextManager();
		final IContextService contextService = new ContextService(
				contextManager);
		contextService.readRegistry();
		serviceLocator.registerService(IContextService.class, contextService);
		
		final IHandlerService handlerService = new HandlerService(
				commandManager);
		handlerService.readRegistry();
		serviceLocator.registerService(IHandlerService.class, handlerService);
		
		BindingManager.DEBUG = Policy.DEBUG_KEY_BINDINGS;
		bindingManager = new BindingManager(contextManager, commandManager);
		final IBindingService bindingService = new BindingService(
				bindingManager, commandService, this);
		bindingService.readRegistryAndPreferences(commandService);
		serviceLocator.registerService(IBindingService.class, bindingService);
		
		final CommandImageManager commandImageManager = new CommandImageManager();
		final CommandImageService commandImageService = new CommandImageService(
				commandImageManager, commandService);
		commandImageService.readRegistry();
		serviceLocator.registerService(ICommandImageService.class,
				commandImageService);
		
		final SMenuManager menuManager = new SMenuManager();
		final IMenuService menuService = new WorkbenchMenuService(menuManager,
				commandService);
		menuService.readRegistry();
		serviceLocator.registerService(IMenuService.class, menuService);

		/*
		 * Phase 2 of the initialization of commands. The source providers that
		 * the workbench provides are creating and registered with the above
		 * services. These source providers notify the services when particular
		 * pieces of workbench state change.
		 */
		final ISourceProviderService sourceProviderService = new SourceProviderService();
		serviceLocator.registerService(ISourceProviderService.class,
				sourceProviderService);
		final ActiveShellSourceProvider activeShellSourceProvider = new ActiveShellSourceProvider(
				this);
		handlerService.addSourceProvider(activeShellSourceProvider);
		contextService.addSourceProvider(activeShellSourceProvider);
		menuService.addSourceProvider(activeShellSourceProvider);
		sourceProviderService.registerProvider(activeShellSourceProvider);
		final ActivePartSourceProvider activePartSourceProvider = new ActivePartSourceProvider(
				this);
		handlerService.addSourceProvider(activePartSourceProvider);
		contextService.addSourceProvider(activePartSourceProvider);
		menuService.addSourceProvider(activePartSourceProvider);
		sourceProviderService.registerProvider(activePartSourceProvider);
		final ActiveContextSourceProvider activeContextSourceProvider = new ActiveContextSourceProvider(
				contextService);
		handlerService.addSourceProvider(activeContextSourceProvider);
		menuService.addSourceProvider(activeContextSourceProvider);
		sourceProviderService.registerProvider(activeContextSourceProvider);
		final CurrentSelectionSourceProvider currentSelectionSourceProvider = new CurrentSelectionSourceProvider(
				this);
		handlerService.addSourceProvider(currentSelectionSourceProvider);
		contextService.addSourceProvider(currentSelectionSourceProvider);
		menuService.addSourceProvider(currentSelectionSourceProvider);
		sourceProviderService.registerProvider(currentSelectionSourceProvider);
		actionSetSourceProvider = new ActionSetSourceProvider();
		handlerService.addSourceProvider(actionSetSourceProvider);
		contextService.addSourceProvider(actionSetSourceProvider);
		menuService.addSourceProvider(actionSetSourceProvider);
		sourceProviderService.registerProvider(actionSetSourceProvider);
		menuSourceProvider = new MenuSourceProvider();
		handlerService.addSourceProvider(menuSourceProvider);
		contextService.addSourceProvider(menuSourceProvider);
		menuService.addSourceProvider(menuSourceProvider);
		sourceProviderService.registerProvider(menuSourceProvider);

		/*
		 * Phase 3 of the initialization of commands. This handles the creation
		 * of wrappers for legacy APIs. By the time this phase completes, any
		 * code trying to access commands through legacy APIs should work.
		 */
		final LegacyActionPersistence deprecatedSupport = new LegacyActionPersistence(
				commandManager, bindingManager, commandImageManager,
				menuService);
		serviceLocator.registerService(LegacyActionPersistence.class,
				deprecatedSupport);
		deprecatedSupport.read();		
		workbenchContextSupport = new WorkbenchContextSupport(this,
				contextManager);
		workbenchCommandSupport = new WorkbenchCommandSupport(bindingManager,
				commandManager, contextManager, handlerService);
		initializeCommandResolver();

		addWindowListener(windowListener); 
		bindingManager.addBindingManagerListener(bindingManagerListener);
	}

	/**
	 * Returns true if the Workbench is in the process of starting.
	 * @return <code>true</code> if the Workbench is starting, but not yet running
	 * the event loop.
	 */
	public boolean isStarting() {
		return isStarting;
	}

	/*
	 * Creates a new workbench window.
	 * 
	 * @return the new workbench window
	 */
	private WorkbenchWindow newWorkbenchWindow() {
		return new WorkbenchWindow(getNewWindowNumber());
	}

	/*
	 * If a perspective was specified on the command line (-perspective) then
	 * force that perspective to open in the active window.
	 */
	private void forceOpenPerspective() {
		if (getWorkbenchWindowCount() == 0) {
			// there should be an open window by now, bail out.
			return;
		}

		String perspId = null;
		String[] commandLineArgs = Platform.getCommandLineArgs();
		for (int i = 0; i < commandLineArgs.length - 1; i++) {
			if (commandLineArgs[i].equalsIgnoreCase("-perspective")) { //$NON-NLS-1$
				perspId = commandLineArgs[i + 1];
				break;
			}
		}
		if (perspId == null) {
			return;
		}
		IPerspectiveDescriptor desc = getPerspectiveRegistry()
				.findPerspectiveWithId(perspId);
		if (desc == null) {
			return;
		}

		IWorkbenchWindow win = getActiveWorkbenchWindow();
		if (win == null) {
			win = getWorkbenchWindows()[0];
		}
		try {
			showPerspective(perspId, win);
		} catch (WorkbenchException e) {
			String msg = "Workbench exception showing specified command line perspective on startup."; //$NON-NLS-1$
			WorkbenchPlugin.log(msg, new Status(IStatus.ERROR,
					PlatformUI.PLUGIN_ID, 0, msg, e));
		}
	}

	/**
	 * Opens the initial workbench window.
	 */
	/* package */void openFirstTimeWindow() {
		final boolean showProgress = PrefUtil.getAPIPreferenceStore().getBoolean(IWorkbenchPreferenceConstants.SHOW_PROGRESS_ON_STARTUP);
		
		if(!showProgress) {
			doOpenFirstTimeWindow();
		} else {
			// We don't know how many plug-ins will be loaded,
			// assume we are loading a tenth of the installed plug-ins.
			// (The Eclipse SDK loads 7 of 86 plug-ins at startup as of 2005-5-20)
			final int expectedProgressCount = Math.max(1, WorkbenchPlugin
					.getDefault().getBundleCount() / 10);

			runStartupWithProgress(expectedProgressCount, new Runnable() {
				public void run() {
					doOpenFirstTimeWindow();
				}
			});
		}
	}

	private void runStartupWithProgress(final int expectedProgressCount,
			final Runnable runnable) {
		progressCount = 0;
		final double cutoff = 0.95;

		IProgressMonitor progressMonitor = StartupProgressMonitor.getInstance();
		if (progressMonitor == null) {
			// cannot report progress (e.g. if the splash screen is not showing)
			// fall back to starting without showing progress.
			runnable.run();
		} else {
			progressMonitor.beginTask("", expectedProgressCount); //$NON-NLS-1$
			SynchronousBundleListener bundleListener = new StartupProgressBundleListener(
					progressMonitor, (int) (expectedProgressCount * cutoff));
			WorkbenchPlugin.getDefault().addBundleListener(bundleListener);
			try {
				runnable.run();
				progressMonitor.subTask(WorkbenchMessages.Startup_Done);
				int remainingWork = expectedProgressCount
						- Math.min(progressCount,
								(int) (expectedProgressCount * cutoff));
				progressMonitor.worked(remainingWork);
				progressMonitor.done();
			} finally {
				WorkbenchPlugin.getDefault().removeBundleListener(
						bundleListener);
			}
		}
	}

	private void doOpenFirstTimeWindow() {
		try {
			busyOpenWorkbenchWindow(getPerspectiveRegistry()
					.getDefaultPerspective(), getDefaultPageInput());
		} catch (WorkbenchException e) {
			// Don't use the window's shell as the dialog parent,
			// as the window is not open yet (bug 76724).
			ErrorDialog.openError(null,
					WorkbenchMessages.Problems_Opening_Page, e.getMessage(), e
							.getStatus());
		}
	}

	/*
	 * Restores the workbench UI from the workbench state file (workbench.xml).
	 * 
	 * @return a status object indicating OK if a window was opened,
	 * RESTORE_CODE_RESET if no window was opened but one should be, and
	 * RESTORE_CODE_EXIT if the workbench should close immediately
	 */
	/* package */IStatus restoreState() {

		if (!getWorkbenchConfigurer().getSaveAndRestore()) {
			String msg = WorkbenchMessages.Workbench_restoreDisabled;
			return new Status(IStatus.WARNING, WorkbenchPlugin.PI_WORKBENCH,
					IWorkbenchConfigurer.RESTORE_CODE_RESET, msg, null);
		}
		// Read the workbench state file.
		final File stateFile = getWorkbenchStateFile();
		// If there is no state file cause one to open.
		if (stateFile == null || !stateFile.exists()) {
			String msg = WorkbenchMessages.Workbench_noStateToRestore;
			return new Status(IStatus.WARNING, WorkbenchPlugin.PI_WORKBENCH,
					IWorkbenchConfigurer.RESTORE_CODE_RESET, msg, null);
		}

		final IStatus result[] = { new Status(IStatus.OK,
				WorkbenchPlugin.PI_WORKBENCH, IStatus.OK, "", null) }; //$NON-NLS-1$
		Platform.run(new SafeRunnable(WorkbenchMessages.ErrorReadingState) {
			public void run() throws Exception {
				FileInputStream input = new FileInputStream(stateFile);
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(input, "utf-8")); //$NON-NLS-1$
				IMemento memento = XMLMemento.createReadRoot(reader);

				// Validate known version format
				String version = memento
						.getString(IWorkbenchConstants.TAG_VERSION);
				boolean valid = false;
				for (int i = 0; i < VERSION_STRING.length; i++) {
					if (VERSION_STRING[i].equals(version)) {
						valid = true;
						break;
					}
				}
				if (!valid) {
					reader.close();
					String msg = WorkbenchMessages.Invalid_workbench_state_ve;
					MessageDialog.openError((Shell) null,
							WorkbenchMessages.Restoring_Problems, msg);
					stateFile.delete();
					result[0] = new Status(IStatus.ERROR,
							WorkbenchPlugin.PI_WORKBENCH,
							IWorkbenchConfigurer.RESTORE_CODE_RESET, msg, null);
					return;
				}

				// Validate compatible version format
				// We no longer support the release 1.0 format
				if (VERSION_STRING[0].equals(version)) {
					reader.close();
					String msg = WorkbenchMessages.Workbench_incompatibleSavedStateVersion;
					boolean ignoreSavedState = new MessageDialog(null,
							WorkbenchMessages.Workbench_incompatibleUIState,
							null, msg, MessageDialog.WARNING, new String[] {
									IDialogConstants.OK_LABEL,
									IDialogConstants.CANCEL_LABEL }, 0).open() == 0;
					// OK is the default
					if (ignoreSavedState) {
						stateFile.delete();
						result[0] = new Status(IStatus.WARNING,
								WorkbenchPlugin.PI_WORKBENCH,
								IWorkbenchConfigurer.RESTORE_CODE_RESET, msg,
								null);
					} else {
						result[0] = new Status(IStatus.WARNING,
								WorkbenchPlugin.PI_WORKBENCH,
								IWorkbenchConfigurer.RESTORE_CODE_EXIT, msg,
								null);
					}
					return;
				}

				// Restore the saved state
				IStatus restoreResult = restoreState(memento);
				reader.close();
				if (restoreResult.getSeverity() == IStatus.ERROR) {
					ErrorDialog.openError(null,
							WorkbenchMessages.Workspace_problemsTitle,
							WorkbenchMessages.Workbench_problemsRestoringMsg,
							restoreResult);
				}
			}

			public void handleException(Throwable e) {
				super.handleException(e);
				String msg = e.getMessage() == null ? "" : e.getMessage(); //$NON-NLS-1$
				result[0] = new Status(IStatus.ERROR,
						WorkbenchPlugin.PI_WORKBENCH,
						IWorkbenchConfigurer.RESTORE_CODE_RESET, msg, e);
				stateFile.delete();
			}

		});
		// ensure at least one window was opened
		if (result[0].isOK() && windowManager.getWindows().length == 0) {
			String msg = WorkbenchMessages.Workbench_noWindowsRestored;
			result[0] = new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH,
					IWorkbenchConfigurer.RESTORE_CODE_RESET, msg, null);
		}
		return result[0];
	}

	/*
	 * (non-Javadoc) Method declared on IWorkbench.
	 */
	public IWorkbenchWindow openWorkbenchWindow(IAdaptable input)
			throws WorkbenchException {
		return openWorkbenchWindow(getPerspectiveRegistry()
				.getDefaultPerspective(), input);
	}

	/*
	 * (non-Javadoc) Method declared on IWorkbench.
	 */
	public IWorkbenchWindow openWorkbenchWindow(final String perspID,
			final IAdaptable input) throws WorkbenchException {
		// Run op in busy cursor.
		final Object[] result = new Object[1];
		BusyIndicator.showWhile(null, new Runnable() {
			public void run() {
				try {
					result[0] = busyOpenWorkbenchWindow(perspID, input);
				} catch (WorkbenchException e) {
					result[0] = e;
				}
			}
		});
		if (result[0] instanceof IWorkbenchWindow) {
			return (IWorkbenchWindow) result[0];
		} else if (result[0] instanceof WorkbenchException) {
			throw (WorkbenchException) result[0];
		} else {
			throw new WorkbenchException(
					WorkbenchMessages.Abnormal_Workbench_Conditi);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbench#restoreWorkbenchWindow(org.eclipse.ui.IMemento)
	 */
	IWorkbenchWindow restoreWorkbenchWindow(IMemento memento) throws WorkbenchException {
		WorkbenchWindow newWindow = newWorkbenchWindow();
		newWindow.create();

		windowManager.add(newWindow);

		// whether the window was opened
		boolean opened = false;

		try {
			newWindow.restoreState(memento, null);
			newWindow.fireWindowRestored();
			newWindow.open();
			opened = true;
		} finally {
			if (!opened)
				newWindow.close();
		}

		return newWindow;
	}
	
	/*
	 * Record the workbench UI in a document
	 */
	private XMLMemento recordWorkbenchState() {
		XMLMemento memento = XMLMemento
				.createWriteRoot(IWorkbenchConstants.TAG_WORKBENCH);
		IStatus status = saveState(memento);
		if (status.getSeverity() != IStatus.OK) {
			// don't use newWindow as parent because it has not yet been opened
			// (bug 76724)
			ErrorDialog.openError(null,
					WorkbenchMessages.Workbench_problemsSaving,
					WorkbenchMessages.Workbench_problemsSavingMsg, status);
		}
		return memento;
	}

	/*
	 * (non-Javadoc) Method declared on IWorkbench.
	 */
	public boolean restart() {
		// this is the return code from run() to trigger a restart
		return close(PlatformUI.RETURN_RESTART, false);
	}

	/*
	 * Restores the state of the previously saved workbench
	 */
	private IStatus restoreState(final IMemento memento) {

		final MultiStatus result = new MultiStatus(PlatformUI.PLUGIN_ID, IStatus.OK,
				WorkbenchMessages.Workbench_problemsRestoring, null);
		
		final boolean showProgress = PrefUtil.getAPIPreferenceStore().getBoolean(IWorkbenchPreferenceConstants.SHOW_PROGRESS_ON_STARTUP);
		
		try {
			/*
			 * Restored windows will be set in the createdWindows field to be used by the openWindowsAfterRestore() method 
			 */
			if(!showProgress) {
				doRestoreState(memento, result);
			} else {
				// Retrieve how many plug-ins were loaded while restoring the workbench
				Integer lastProgressCount = memento.getInteger(IWorkbenchConstants.TAG_PROGRESS_COUNT);
				
				// If we don't know how many plug-ins were loaded last time,
				// assume we are loading half of the installed plug-ins.
				final int expectedProgressCount = Math.max(1, lastProgressCount==null ? WorkbenchPlugin.getDefault().getBundleCount() / 2 : lastProgressCount.intValue());

				runStartupWithProgress(expectedProgressCount, new Runnable() {
					public void run() {
						doRestoreState(memento, result);
					}});
			}
		} finally {
			openWindowsAfterRestore();
		}
		return result;
	}

	/**
	 * Returns the ids of all plug-ins that extend the
	 * <code>org.eclipse.ui.startup</code> extension point.
	 * 
	 * @return the ids of all plug-ins containing 1 or more startup extensions
	 */
	public String[] getEarlyActivatedPlugins() {
		IExtensionPoint point = Platform.getExtensionRegistry()
				.getExtensionPoint(PlatformUI.PLUGIN_ID,
						IWorkbenchConstants.PL_STARTUP);
		IExtension[] extensions = point.getExtensions();
		ArrayList pluginIds = new ArrayList(extensions.length);
		for (int i = 0; i < extensions.length; i++) {
			String id = extensions[i].getNamespace();
			if (!pluginIds.contains(id)) {
				pluginIds.add(id);
			}
		}
		return (String[]) pluginIds.toArray(new String[pluginIds.size()]);
	}

	/**
	 * Returns the ids of the early activated plug-ins that have been disabled by the user.
	 * 
	 * @return the ids of the early activated plug-ins that have been disabled by the user
	 */
	public String[] getDisabledEarlyActivatedPlugins() {
		String pref = PrefUtil.getInternalPreferenceStore().getString(
				IPreferenceConstants.PLUGINS_NOT_ACTIVATED_ON_STARTUP);
		return Util.getArrayFromList(pref, ";"); //$NON-NLS-1$
	}
	
	/*
	 * Starts all plugins that extend the <code> org.eclipse.ui.startup </code>
	 * extension point, and that the user has not disabled via the preference
	 * page.
	 */
	private void startPlugins() {
		IExtensionRegistry registry = Platform.getExtensionRegistry();

		// bug 55901: don't use getConfigElements directly, for pre-3.0
		// compat, make sure to allow both missing class
		// attribute and a missing startup element
		IExtensionPoint point = registry.getExtensionPoint(
				PlatformUI.PLUGIN_ID, IWorkbenchConstants.PL_STARTUP);

		final IExtension[] extensions = point.getExtensions();
		if (extensions.length == 0) {
			return;
		}
		Job job = new Job("Workbench early startup") { //$NON-NLS-1$
			protected IStatus run(IProgressMonitor monitor) { 
		        HashSet disabledPlugins = new HashSet(Arrays.asList(getDisabledEarlyActivatedPlugins()));
				monitor.beginTask(WorkbenchMessages.Workbench_startingPlugins, extensions.length);
				for (int i = 0; i < extensions.length; ++i) {
					if (monitor.isCanceled() || !isRunning()) {
						return Status.CANCEL_STATUS;
					}
					IExtension extension = extensions[i];

					// if the plugin is not in the set of disabled plugins, then
					// execute the code to start it
					if (!disabledPlugins.contains(extension.getNamespace())) {
						monitor.subTask(extension.getNamespace());
						Platform.run(new EarlyStartupRunnable(extension));
					}
					monitor.worked(1);
				}
				monitor.done();
				return Status.OK_STATUS;
			}
			
			public boolean belongsTo(Object family) {
				return EARLY_STARTUP_FAMILY.equals(family);
			}
		};
		job.setSystem(true);
		job.schedule();
	}

	/**
	 * Internal method for running the workbench UI. This entails processing and
	 * dispatching events until the workbench is closed or restarted.
	 * 
	 * @return return code {@link PlatformUI#RETURN_OK RETURN_OK}for normal
	 *         exit; {@link PlatformUI#RETURN_RESTART RETURN_RESTART}if the
	 *         workbench was terminated with a call to
	 *         {@link IWorkbench#restart IWorkbench.restart};
	 *         {@link PlatformUI#RETURN_UNSTARTABLE RETURN_UNSTARTABLE}if the
	 *         workbench could not be started; other values reserved for future
	 *         use
	 * @since 3.0
	 */
	private int runUI() {
		UIStats.start(UIStats.START_WORKBENCH, "Workbench"); //$NON-NLS-1$

		// deadlock code
		boolean avoidDeadlock = true;

		String[] commandLineArgs = Platform.getCommandLineArgs();
		for (int i = 0; i < commandLineArgs.length; i++) {
			if (commandLineArgs[i].equalsIgnoreCase("-allowDeadlock")) //$NON-NLS-1$
				avoidDeadlock = false;
		}

		if (avoidDeadlock) {
			UILockListener uiLockListener = new UILockListener(display);
			Platform.getJobManager().setLockListener(uiLockListener);
			display.setSynchronizer(new UISynchronizer(display, uiLockListener));
		}
		
		// ModalContext should not spin the event loop (there is no UI yet to block)
		ModalContext.setAllowReadAndDispatch(false);
		
		// if the -debug command line argument is used and the event loop is being
		// run while starting the Workbench, log a warning.
        if (WorkbenchPlugin.DEBUG) {
			display.asyncExec(new Runnable(){
				public void run() {
					if(isStarting()) {
						WorkbenchPlugin.log(StatusUtil.newStatus(
								IStatus.WARNING, 
								"Event loop should not be run while the Workbench is starting.",  //$NON-NLS-1$
								new RuntimeException()));
					}
				}});
        }
		
		Listener closeListener = new Listener() {
			public void handleEvent(Event event) {
				event.doit = close();
			}
		};

		// Initialize an exception handler.
		Window.IExceptionHandler handler = ExceptionHandler.getInstance();

		try {
			// react to display close event by closing workbench nicely
			display.addListener(SWT.Close, closeListener);

			// install backstop to catch exceptions thrown out of event loop
			Window.setExceptionHandler(handler);

			// initialize workbench and restore or open one window
			boolean initOK = init(display);

			// drop the splash screen now that a workbench window is up
			Platform.endSplash();

			// let the advisor run its start up code
			if (initOK) {
				advisor.postStartup(); // may trigger a close/restart
			}

			if (initOK && runEventLoop) {
				// start eager plug-ins
				startPlugins();
				addStartupRegistryListener();

			//	WWinPluginAction.refreshActionList();
				
				display.asyncExec(new Runnable() {
					public void run() {
						UIStats.end(UIStats.START_WORKBENCH, this, "Workbench"); //$NON-NLS-1$
						UIStats.startupComplete();
					}
				});

				getWorkbenchTestable().init(display, this);

				// allow ModalContext to spin the event loop
				ModalContext.setAllowReadAndDispatch(true);
				isStarting = false;

				// the event loop
				runEventLoop(handler, display);
			}

		} catch (final Exception e) {
			if (!display.isDisposed()) {
				handler.handleException(e);
			} else {
				String msg = "Exception in Workbench.runUI after display was disposed"; //$NON-NLS-1$
				WorkbenchPlugin.log(msg, new Status(IStatus.ERROR,
						WorkbenchPlugin.PI_WORKBENCH, 1, msg, e));
			}
		} finally {
			// mandatory clean up

			// The runEventLoop flag may not have been cleared if an exception
			// occurred
			// Needs to be false to ensure PlatformUI.isWorkbenchRunning()
			// returns false.
			runEventLoop = false;

			if (!display.isDisposed()) {
				display.removeListener(SWT.Close, closeListener);
			}
		}

		// restart or exit based on returnCode
		return returnCode;
	}

	/*
	 * Runs an event loop for the workbench.
	 */
	private void runEventLoop(Window.IExceptionHandler handler, Display display) {
		runEventLoop = true;
		while (runEventLoop) {
			try {
				if (!display.readAndDispatch()) {
					getAdvisor().eventLoopIdle(display);
				}
			} catch (Throwable t) {
				handler.handleException(t);
			}
		}
	}

	/*
	 * Saves the current state of the workbench so it can be restored later on
	 */
	private IStatus saveState(IMemento memento) {
		MultiStatus result = new MultiStatus(PlatformUI.PLUGIN_ID, IStatus.OK,
				WorkbenchMessages.Workbench_problemsSaving, null);

		// Save the version number.
		memento.putString(IWorkbenchConstants.TAG_VERSION, VERSION_STRING[1]);
		
        // Save how many plug-ins were loaded while restoring the workbench
        if (progressCount != -1)
            memento.putInteger(IWorkbenchConstants.TAG_PROGRESS_COUNT, progressCount);

		// Save the advisor state.
		IMemento advisorState = memento
				.createChild(IWorkbenchConstants.TAG_WORKBENCH_ADVISOR);
		result.add(getAdvisor().saveState(advisorState));

		// Save the workbench windows.
		IWorkbenchWindow[] windows = getWorkbenchWindows();
		for (int nX = 0; nX < windows.length; nX++) {
			WorkbenchWindow window = (WorkbenchWindow) windows[nX];
			IMemento childMem = memento
					.createChild(IWorkbenchConstants.TAG_WINDOW);
			result.merge(window.saveState(childMem));
		}
		result.add(getEditorHistory().saveState(
				memento.createChild(IWorkbenchConstants.TAG_MRU_LIST)));
		return result;
	}

	/*
	 * Save the workbench UI in a persistence file.
	 */
	private boolean saveMementoToFile(XMLMemento memento) {
		// Save it to a file.
		// XXX: nobody currently checks the return value of this method.
		File stateFile = getWorkbenchStateFile();
		if (stateFile == null)
			return false;
		try {
			FileOutputStream stream = new FileOutputStream(stateFile);
			OutputStreamWriter writer = new OutputStreamWriter(stream, "utf-8"); //$NON-NLS-1$
			memento.save(writer);
			writer.close();
		} catch (IOException e) {
			stateFile.delete();
			MessageDialog.openError((Shell) null,
					WorkbenchMessages.SavingProblem,
					WorkbenchMessages.ProblemSavingState);
			return false;
		}

		// Success !
		return true;
	}

	/*
	 * (non-Javadoc) Method declared on IWorkbench.
	 */
	public IWorkbenchPage showPerspective(String perspectiveId,
			IWorkbenchWindow window) throws WorkbenchException {
		Assert.isNotNull(perspectiveId);

		// If the specified window has the requested perspective open, then the
		// window
		// is given focus and the perspective is shown. The page's input is
		// ignored.
		WorkbenchWindow win = (WorkbenchWindow) window;
		if (win != null) {
			WorkbenchPage page = win.getActiveWorkbenchPage();
			if (page != null) {
				IPerspectiveDescriptor perspectives[] = page
						.getOpenPerspectives();
				for (int i = 0; i < perspectives.length; i++) {
					IPerspectiveDescriptor persp = perspectives[i];
					if (perspectiveId.equals(persp.getId())) {
						win.makeVisible();
						page.setPerspective(persp);
						return page;
					}
				}
			}
		}

		// If another window that has the workspace root as input and the
		// requested
		// perpective open and active, then the window is given focus.
		IAdaptable input = getDefaultPageInput();
		IWorkbenchWindow[] windows = getWorkbenchWindows();
		for (int i = 0; i < windows.length; i++) {
			win = (WorkbenchWindow) windows[i];
			if (window != win) {
				WorkbenchPage page = win.getActiveWorkbenchPage();
				if (page != null) {
					boolean inputSame = false;
					if (input == null)
						inputSame = (page.getInput() == null);
					else
						inputSame = input.equals(page.getInput());
					if (inputSame) {
						Perspective persp = page.getActivePerspective();
						if (perspectiveId.equals(persp.getDesc().getId())) {
							Shell shell = win.getShell();
							shell.open();
							if (shell.getMinimized())
								shell.setMinimized(false);
							return page;
						}
					}
				}
			}
		}

		// Otherwise the requested perspective is opened and shown in the
		// specified
		// window or in a new window depending on the current user preference
		// for opening
		// perspectives, and that window is given focus.
		win = (WorkbenchWindow) window;
		if (win != null) {
			IPreferenceStore store = WorkbenchPlugin.getDefault()
					.getPreferenceStore();
			int mode = store.getInt(IPreferenceConstants.OPEN_PERSP_MODE);
			IWorkbenchPage page = win.getActiveWorkbenchPage();
			IPerspectiveDescriptor persp = null;
			if (page != null)
				persp = page.getPerspective();

			// Only open a new window if user preference is set and the window
			// has an active perspective.
			if (IPreferenceConstants.OPM_NEW_WINDOW == mode && persp != null) {
				IWorkbenchWindow newWindow = openWorkbenchWindow(perspectiveId,
						input);
				return newWindow.getActivePage();
			}

			IPerspectiveDescriptor desc = getPerspectiveRegistry()
					.findPerspectiveWithId(perspectiveId);
			if (desc == null)
				throw new WorkbenchException(
						NLS
								.bind(
										WorkbenchMessages.WorkbenchPage_ErrorCreatingPerspective,
										perspectiveId));
			win.getShell().open();
			if (page == null)
				page = win.openPage(perspectiveId, input);
			else
				page.setPerspective(desc);
			return page;
		}

		// Just throw an exception....
		throw new WorkbenchException(NLS
				.bind(WorkbenchMessages.Workbench_showPerspectiveError,
						perspectiveId));
	}

	/*
	 * (non-Javadoc) Method declared on IWorkbench.
	 */
	public IWorkbenchPage showPerspective(String perspectiveId,
			IWorkbenchWindow window, IAdaptable input)
			throws WorkbenchException {
		Assert.isNotNull(perspectiveId);

		// If the specified window has the requested perspective open and the
		// same requested
		// input, then the window is given focus and the perspective is shown.
		boolean inputSameAsWindow = false;
		WorkbenchWindow win = (WorkbenchWindow) window;
		if (win != null) {
			WorkbenchPage page = win.getActiveWorkbenchPage();
			if (page != null) {
				boolean inputSame = false;
				if (input == null)
					inputSame = (page.getInput() == null);
				else
					inputSame = input.equals(page.getInput());
				if (inputSame) {
					inputSameAsWindow = true;
					IPerspectiveDescriptor perspectives[] = page
							.getOpenPerspectives();
					for (int i = 0; i < perspectives.length; i++) {
						IPerspectiveDescriptor persp = perspectives[i];
						if (perspectiveId.equals(persp.getId())) {
							win.makeVisible();
							page.setPerspective(persp);
							return page;
						}
					}
				}
			}
		}

		// If another window has the requested input and the requested
		// perpective open and active, then that window is given focus.
		IWorkbenchWindow[] windows = getWorkbenchWindows();
		for (int i = 0; i < windows.length; i++) {
			win = (WorkbenchWindow) windows[i];
			if (window != win) {
				WorkbenchPage page = win.getActiveWorkbenchPage();
				if (page != null) {
					boolean inputSame = false;
					if (input == null)
						inputSame = (page.getInput() == null);
					else
						inputSame = input.equals(page.getInput());
					if (inputSame) {
						Perspective persp = page.getActivePerspective();
						if (perspectiveId.equals(persp.getDesc().getId())) {
							win.getShell().open();
							return page;
						}
					}
				}
			}
		}

		// If the specified window has the same requested input but not the
		// requested
		// perspective, then the window is given focus and the perspective is
		// opened and shown
		// on condition that the user preference is not to open perspectives in
		// a new window.
		win = (WorkbenchWindow) window;
		if (inputSameAsWindow && win != null) {
			IPreferenceStore store = WorkbenchPlugin.getDefault()
					.getPreferenceStore();
			int mode = store.getInt(IPreferenceConstants.OPEN_PERSP_MODE);

			if (IPreferenceConstants.OPM_NEW_WINDOW != mode) {
				IWorkbenchPage page = win.getActiveWorkbenchPage();
				IPerspectiveDescriptor desc = getPerspectiveRegistry()
						.findPerspectiveWithId(perspectiveId);
				if (desc == null)
					throw new WorkbenchException(
							NLS
									.bind(
											WorkbenchMessages.WorkbenchPage_ErrorCreatingPerspective,
											perspectiveId));
				win.getShell().open();
				if (page == null)
					page = win.openPage(perspectiveId, input);
				else
					page.setPerspective(desc);
				return page;
			}
		}

		// If the specified window has no active perspective, then open the
		// requested perspective and show the specified window.
		if (win != null) {
			IWorkbenchPage page = win.getActiveWorkbenchPage();
			IPerspectiveDescriptor persp = null;
			if (page != null)
				persp = page.getPerspective();
			if (persp == null) {
				IPerspectiveDescriptor desc = getPerspectiveRegistry()
						.findPerspectiveWithId(perspectiveId);
				if (desc == null)
					throw new WorkbenchException(
							NLS
									.bind(
											WorkbenchMessages.WorkbenchPage_ErrorCreatingPerspective,
											perspectiveId));
				win.getShell().open();
				if (page == null)
					page = win.openPage(perspectiveId, input);
				else
					page.setPerspective(desc);
				return page;
			}
		}

		// Otherwise the requested perspective is opened and shown in a new
		// window, and the
		// window is given focus.
		IWorkbenchWindow newWindow = openWorkbenchWindow(perspectiveId, input);
		return newWindow.getActivePage();
	}

	/*
	 * Shuts down the application.
	 */
	private void shutdown() {
		// shutdown application-specific portions first
		advisor.postShutdown();

		cancelEarlyStartup();
		
		// for dynamic UI
		Platform.getExtensionRegistry().removeRegistryChangeListener(
				extensionEventHandler);
		Platform.getExtensionRegistry().removeRegistryChangeListener(
				startupRegistryListener);
		
		// Bring down all of the services.
		serviceLocator.dispose();
		
		workbenchActivitySupport.dispose();
		WorkbenchHelpSystem.disposeIfNecessary();
		
		// shutdown the rest of the workbench
		WorkbenchColors.shutdown();
		activityHelper.shutdown();
		uninitializeImages();
		if (WorkbenchPlugin.getDefault() != null) {
			WorkbenchPlugin.getDefault().reset();
		}
		WorkbenchThemeManager.getInstance().dispose();
		PropertyPageContributorManager.getManager().dispose();
		ObjectActionContributorManager.getManager().dispose();
		if (tracker != null)
			tracker.close();
	}

	/**
	 * Cancels the early startup job, if it's still running.
	 */
	private void cancelEarlyStartup() {
		Platform.getJobManager().cancel(EARLY_STARTUP_FAMILY);
		// We do not currently wait for any plug-in currently being started to complete
		// (e.g. by doing a join on EARLY_STARTUP_FAMILY), since they may do a syncExec,
		// which would hang.  See bug 94537 for rationale.
	}

	/*
	 * (non-Javadoc) Method declared on IWorkbench.
	 */
	public IDecoratorManager getDecoratorManager() {
		return WorkbenchPlugin.getDefault().getDecoratorManager();
	}

	/*
	 * Returns the workbench window which was last known being the active one,
	 * or <code> null </code> .
	 */
	private WorkbenchWindow getActivatedWindow() {
		if (activatedWindow != null) {
			Shell shell = activatedWindow.getShell();
			if (shell != null && !shell.isDisposed()) {
				return activatedWindow;
			}
		}

		return null;
	}

	/*
	 * Sets the workbench window which was last known being the active one, or
	 * <code> null </code> .
	 */
	/* package */
	void setActivatedWindow(WorkbenchWindow window) {
		activatedWindow = window;
	}

	/**
	 * Returns the unique object that applications use to configure the
	 * workbench.
	 * <p>
	 * IMPORTANT This method is declared package-private to prevent regular
	 * plug-ins from downcasting IWorkbench to Workbench and getting hold of the
	 * workbench configurer that would allow them to tamper with the workbench.
	 * The workbench configurer is available only to the application.
	 * </p>
	 */
	/* package */
	WorkbenchConfigurer getWorkbenchConfigurer() {
		if (workbenchConfigurer == null) {
			workbenchConfigurer = new WorkbenchConfigurer();
		}
		return workbenchConfigurer;
	}

	/**
	 * Returns the workbench advisor that created this workbench.
	 * <p>
	 * IMPORTANT This method is declared package-private to prevent regular
	 * plug-ins from downcasting IWorkbench to Workbench and getting hold of the
	 * workbench advisor that would allow them to tamper with the workbench. The
	 * workbench advisor is internal to the application.
	 * </p>
	 */
	/* package */
	WorkbenchAdvisor getAdvisor() {
		return advisor;
	}

	/*
	 * (non-Javadoc) Method declared on IWorkbench.
	 */
	public Display getDisplay() {
		return display;
	}

	/**
	 * Returns the default perspective id, which may be <code>null</code>.
	 * 
	 * @return the default perspective id, or <code>null</code>
	 */
	public String getDefaultPerspectiveId() {
		return getAdvisor().getInitialWindowPerspectiveId();
	}

	/**
	 * Returns the default workbench window page input.
	 * 
	 * @return the default window page input or <code>null</code> if none
	 */
	public IAdaptable getDefaultPageInput() {
		return getAdvisor().getDefaultPageInput();
	}

	/**
	 * Returns the id of the preference page that should be presented most
	 * prominently.
	 * 
	 * @return the id of the preference page, or <code>null</code> if none
	 */
	public String getMainPreferencePageId() {
		String id = getAdvisor().getMainPreferencePageId();
		return id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbench
	 * @since 3.0
	 */
	public IElementFactory getElementFactory(String factoryId) {
		Assert.isNotNull(factoryId);
		return WorkbenchPlugin.getDefault().getElementFactory(factoryId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbench#getProgressService()
	 */
	public IProgressService getProgressService() {
		return ProgressManager.getInstance();
	}

	private WorkbenchActivitySupport workbenchActivitySupport;

	private WorkbenchCommandSupport workbenchCommandSupport;

	private WorkbenchContextSupport workbenchContextSupport;

	/**
	 * The single instance of the binding manager used by the workbench. This is
	 * initialized in <code>Workbench.init(Display)</code> and then never
	 * changed. This value will only be <code>null</code> if the
	 * initialization call has not yet completed.
	 * 
	 * @since 3.1
	 */
	private BindingManager bindingManager;

	/**
	 * The single instance of the command manager used by the workbench. This is
	 * initialized in <code>Workbench.init(Display)</code> and then never
	 * changed. This value will only be <code>null</code> if the
	 * initialization call has not yet completed.
	 * 
	 * @since 3.1
	 */
	private CommandManager commandManager;

	/**
	 * The single instance of the context manager used by the workbench. This is
	 * initialized in <code>Workbench.init(Display)</code> and then never
	 * changed. This value will only be <code>null</code> if the
	 * initialization call has not yet completed.
	 * 
	 * @since 3.1
	 */
	private ContextManager contextManager;

	public IWorkbenchActivitySupport getActivitySupport() {
		return workbenchActivitySupport;
	}

	public IWorkbenchCommandSupport getCommandSupport() {
		return workbenchCommandSupport;
	}

	public IWorkbenchContextSupport getContextSupport() {
		return workbenchContextSupport;
	}

	private final IWindowListener windowListener = new IWindowListener() {

		public void windowActivated(IWorkbenchWindow window) {
			updateActiveWorkbenchWindowMenuManager(true);
		}

		public void windowClosed(IWorkbenchWindow window) {
			updateActiveWorkbenchWindowMenuManager(true);
		}

		public void windowDeactivated(IWorkbenchWindow window) {
			updateActiveWorkbenchWindowMenuManager(true);
		}

		public void windowOpened(IWorkbenchWindow window) {
			updateActiveWorkbenchWindowMenuManager(true);
		}
	};

	private final IBindingManagerListener bindingManagerListener = new IBindingManagerListener() {

		public void bindingManagerChanged(
				BindingManagerEvent bindingManagerEvent) {
			if (bindingManagerEvent.isActiveBindingsChanged()) {
				updateActiveWorkbenchWindowMenuManager(true);
			}
		}
	};
	
	/**
	 * The source provider that tracks the activation of action sets within the
	 * workbench. This source provider is <code>null</code> until
	 * {@link #initializeDefaultServices()} is called.
	 */
	private ActionSetSourceProvider actionSetSourceProvider;
	
	private WorkbenchWindow activeWorkbenchWindow = null;

	private void updateActiveWorkbenchWindowMenuManager(boolean textOnly) {
		if (activeWorkbenchWindow != null) {
			activeWorkbenchWindow
					.removeActionSetsListener(actionSetSourceProvider);
			activeWorkbenchWindow = null;
		}
		boolean actionSetsUpdated = false;

		final IWorkbenchWindow workbenchWindow = getActiveWorkbenchWindow();

		if (workbenchWindow instanceof WorkbenchWindow) {
			activeWorkbenchWindow = (WorkbenchWindow) workbenchWindow;
			if (activeWorkbenchWindow.isClosing()) {
				return;
			}

			// Update the action sets.
			final Shell windowShell = activeWorkbenchWindow.getShell();
			final Shell activeShell = getDisplay().getActiveShell();
			if (Util.equals(windowShell, activeShell)) {
				activeWorkbenchWindow
						.addActionSetsListener(actionSetSourceProvider);
				final WorkbenchPage page = activeWorkbenchWindow
						.getActiveWorkbenchPage();
				final IActionSetDescriptor[] newActionSets;
				if (page != null) {
					newActionSets = page.getActionSets();
					final ActionSetsEvent event = new ActionSetsEvent(
							newActionSets);
					actionSetSourceProvider.actionSetsChanged(event);
					actionSetsUpdated = true;
				}
			}

			final MenuManager menuManager = activeWorkbenchWindow
					.getMenuManager();

			if (textOnly)
				menuManager.update(IAction.TEXT);
			else
				menuManager.updateAll(true);
		}

		if (!actionSetsUpdated) {
			final ActionSetsEvent event = new ActionSetsEvent(null);
			actionSetSourceProvider.actionSetsChanged(event);
		}
	}

	private ActivityPersistanceHelper activityHelper;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbench#getIntroManager()
	 */
	public IIntroManager getIntroManager() {
		return getWorkbenchIntroManager();
	}

	/**
	 * @return the workbench intro manager
	 * @since 3.0
	 */
	/* package */WorkbenchIntroManager getWorkbenchIntroManager() {
		if (introManager == null) {
			introManager = new WorkbenchIntroManager(this);
		}
		return introManager;
	}

	private WorkbenchIntroManager introManager;

	/**
	 * @return the intro extension for this workbench.
	 * 
	 * @since 3.0
	 */
	public IntroDescriptor getIntroDescriptor() {
		return introDescriptor;
	}

	/**
	 * This method exists as a test hook. This method should <strong>NEVER</strong>
	 * be called by clients.
	 * 
	 * @param descriptor
	 *            The intro descriptor to use.
	 * @since 3.0
	 */
	public void setIntroDescriptor(IntroDescriptor descriptor) {
		if (getIntroManager().getIntro() != null) {
			getIntroManager().closeIntro(getIntroManager().getIntro());
		}
		introDescriptor = descriptor;
	}

	/**
	 * The descriptor for the intro extension that is valid for this workspace,
	 * <code>null</code> if none.
	 */
	private IntroDescriptor introDescriptor;

	private IExtensionTracker tracker;

	private IRegistryChangeListener startupRegistryListener = new IRegistryChangeListener() {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.IRegistryChangeListener#registryChanged(org.eclipse.core.runtime.IRegistryChangeEvent)
		 */
		public void registryChanged(IRegistryChangeEvent event) {
			final IExtensionDelta[] deltas = event.getExtensionDeltas(
					PlatformUI.PLUGIN_ID, IWorkbenchConstants.PL_STARTUP);
			if (deltas.length == 0)
				return;
			final String disabledPlugins = PrefUtil
					.getInternalPreferenceStore()
					.getString(
							IPreferenceConstants.PLUGINS_NOT_ACTIVATED_ON_STARTUP);

			for (int i = 0; i < deltas.length; i++) {
				IExtension extension = deltas[i].getExtension();
				if (deltas[i].getKind() == IExtensionDelta.REMOVED)
					continue;

				// if the plugin is not in the set of disabled plugins,
				// then
				// execute the code to start it
				if (disabledPlugins.indexOf(extension.getNamespace()) == -1)
					Platform.run(new EarlyStartupRunnable(extension));
			}

		}
	};

	private String factoryID;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbench#getThemeManager()
	 */
	public IThemeManager getThemeManager() {
		return WorkbenchThemeManager.getInstance();
	}

	/**
	 * Returns <code>true</code> if the workbench is running,
	 * <code>false</code> if it has been terminated.
	 * 
	 * @return <code>true</code> if the workbench is running,
	 *         <code>false</code> if it has been terminated.
	 */
	public boolean isRunning() {
		return runEventLoop;
	}

	/**
	 * Return the presentation ID specified by the preference or the default ID
	 * if undefined.
	 * 
	 * @return the presentation ID
	 * @see IWorkbenchPreferenceConstants#PRESENTATION_FACTORY_ID
	 */
	public String getPresentationId() {
		if (factoryID != null) {
			return factoryID;
		}
		
		factoryID = PrefUtil.getAPIPreferenceStore().getString(
				IWorkbenchPreferenceConstants.PRESENTATION_FACTORY_ID);

		// Workaround for bug 58975 - New preference mechanism does not properly
		// initialize defaults
		// Ensure that the UI plugin has started too.
		if (factoryID == null || factoryID.equals("")) { //$NON-NLS-1$
			factoryID = "org.eclipse.ui.presentations.default"; //$NON-NLS-1$
		}
		return factoryID;
	}

	/**
	 * <p>
	 * Indicates the start of a large update within the workbench. This is used
	 * to disable CPU-intensive, change-sensitive services that were temporarily
	 * disabled in the midst of large changes. This method should always be
	 * called in tandem with <code>largeUpdateEnd</code>, and the event loop
	 * should not be allowed to spin before that method is called.
	 * </p>
	 * <p>
	 * Important: always use with <code>largeUpdateEnd</code>!
	 * </p>
	 */
	public final void largeUpdateStart() {
		if (largeUpdates++ == 0) {
			// TODO Consider whether these lines still need to be here.
			// workbenchCommandSupport.setProcessing(false);
			// workbenchContextSupport.setProcessing(false);

			final IWorkbenchWindow[] windows = getWorkbenchWindows();
			for (int i = 0; i < windows.length; i++) {
				IWorkbenchWindow window = windows[i];
				if (window instanceof WorkbenchWindow) {
					((WorkbenchWindow) window).largeUpdateStart();
				}
			}
		}
	}

	/**
	 * <p>
	 * Indicates the end of a large update within the workbench. This is used to
	 * re-enable services that were temporarily disabled in the midst of large
	 * changes. This method should always be called in tandem with
	 * <code>largeUpdateStart</code>, and the event loop should not be
	 * allowed to spin before this method is called.
	 * </p>
	 * <p>
	 * Important: always protect this call by using <code>finally</code>!
	 * </p>
	 */
	public final void largeUpdateEnd() {
		if (--largeUpdates == 0) {
			// TODO Consider whether these lines still need to be here.
			// workbenchCommandSupport.setProcessing(true);
			// workbenchContextSupport.setProcessing(true);

			// Perform window-specific blocking.
			final IWorkbenchWindow[] windows = getWorkbenchWindows();
			for (int i = 0; i < windows.length; i++) {
				IWorkbenchWindow window = windows[i];
				if (window instanceof WorkbenchWindow) {
					((WorkbenchWindow) window).largeUpdateEnd();
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbench#getExtensionTracker()
	 */
	public IExtensionTracker getExtensionTracker() {
		if (tracker == null) {
			tracker = new UIExtensionTracker(getDisplay());
		}
		return tracker;
	}

	/**
	 * Adds the listener that handles startup plugins
	 * 
	 * @since 3.1
	 */
	private void addStartupRegistryListener() {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		registry.addRegistryChangeListener(startupRegistryListener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbench#getHelpSystem()
	 */
	public IWorkbenchHelpSystem getHelpSystem() {
		return WorkbenchHelpSystem.getInstance();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbench#getHelpSystem()
	 */
	public IWorkbenchBrowserSupport getBrowserSupport() {
		return WorkbenchBrowserSupport.getInstance();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbench#getViewRegistry()
	 */
	public IViewRegistry getViewRegistry() {
		return WorkbenchPlugin.getDefault().getViewRegistry();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbench#getNewWizardRegistry()
	 */
	public IWizardRegistry getNewWizardRegistry() {
		return WorkbenchPlugin.getDefault().getNewWizardRegistry();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbench#getImportWizardRegistry()
	 */
	public IWizardRegistry getImportWizardRegistry() {
		return WorkbenchPlugin.getDefault().getImportWizardRegistry();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbench#getExportWizardRegistry()
	 */
	public IWizardRegistry getExportWizardRegistry() {
		return WorkbenchPlugin.getDefault().getExportWizardRegistry();
	}

	public final Object getAdapter(final Class key) {
		return serviceLocator.getService(key);
	}

	private void doRestoreState(final IMemento memento, final MultiStatus status) {
		IMemento childMem;
		try {
			UIStats.start(UIStats.RESTORE_WORKBENCH, "MRUList"); //$NON-NLS-1$
			IMemento mruMemento = memento
			.getChild(IWorkbenchConstants.TAG_MRU_LIST);
			if (mruMemento != null) {
				status.add(getEditorHistory().restoreState(mruMemento));
			}
		} finally {
			UIStats.end(UIStats.RESTORE_WORKBENCH, this, "MRUList"); //$NON-NLS-1$
		}
		
		// Restore advisor state.
		IMemento advisorState = memento
		.getChild(IWorkbenchConstants.TAG_WORKBENCH_ADVISOR);
		if (advisorState != null)
			status.add(getAdvisor().restoreState(advisorState));
		
		// Get the child windows.
		IMemento[] children = memento
		.getChildren(IWorkbenchConstants.TAG_WINDOW);
		
		createdWindows = new WorkbenchWindow[children.length];
		
		// Read the workbench windows.
		for (int i = 0; i < children.length; i++) {
			childMem = children[i];
			WorkbenchWindow newWindow = newWorkbenchWindow();
			createdWindows[i] = newWindow;
			newWindow.create();
			
			// allow the application to specify an initial perspective to open
			// @issue temporary workaround for ignoring initial perspective
			// String initialPerspectiveId =
			// getAdvisor().getInitialWindowPerspectiveId();
			// if (initialPerspectiveId != null) {
			// IPerspectiveDescriptor desc =
			// getPerspectiveRegistry().findPerspectiveWithId(initialPerspectiveId);
			// result.merge(newWindow.restoreState(childMem, desc));
			// }
			// add the window so that any work done in newWindow.restoreState
			// that relies on Workbench methods has windows to work with
			windowManager.add(newWindow);
			
			// now that we've added it to the window manager we need to listen
			// for any exception that might hose us before we get a chance to
			// open it. If one occurs, remove the new window from the manager.
			// Assume that the new window is a phantom for now
			boolean restored = false;
			try {
				status.merge(newWindow.restoreState(childMem, null));
				try {
					newWindow.fireWindowRestored();
				} catch (WorkbenchException e) {
					status.add(e.getStatus());
				}
				// everything worked so far, don't close now
				restored = true;
			} finally {
				if (!restored) {
					// null the window in newWindowHolder so that it won't be opened later on
					createdWindows[i] = null;
					newWindow.close();
				}
			}
		}
	}

	private void openWindowsAfterRestore() {
		if (createdWindows == null)
			return;
		// now open the windows (except the ones that were nulled because we closed them above)
		for(int i=0; i<createdWindows.length; i++) {
			if(createdWindows[i]!=null) {
				boolean opened = false;
				try {
					createdWindows[i].open();
					opened = true;
				} finally {
					if(!opened) {
						createdWindows[i].close();
					}
				}
			}
		}
		createdWindows = null;
	}

	public final Object getService(final Object key) {
		return serviceLocator.getService(key);
	}
	
	public final boolean hasService(final Object key) {
		return serviceLocator.hasService(key);
	}
	
	/**
	 * The source provider that tracks which context menus (i.e., menus with
	 * target identifiers) are now showing. This value is <code>null</code>
	 * until {@link #initializeDefaultServices()} is called.
	 */
	private MenuSourceProvider menuSourceProvider;
	
	/**
	 * Adds the ids of a menu that is now showing to the menu source provider.
	 * This is used for legacy action-based handlers which need to become active
	 * only for the duration of a menu being visible.
	 * 
	 * @param menuIds
	 *            The identifiers of the menu that is now showing; must not be
	 *            <code>null</code>.
	 */
	public final void addShowingMenus(final Set menuIds) {
		menuSourceProvider.addShowingMenus(menuIds);
	}
	
	/**
	 * Removes the ids of a menu that is now hidden from the menu source
	 * provider. This is used for legacy action-based handlers which need to
	 * become active only for the duration of a menu being visible.
	 * 
	 * @param menuIds
	 *            The identifiers of the menu that is now hidden; must not be
	 *            <code>null</code>.
	 */
	public final void removeShowingMenus(final Set menuIds) {
		menuSourceProvider.removeShowingMenus(menuIds);
	}
	
	/*
	 * Initialize any common workbench field decorations.
	 * The decorations are placed in the JFace FieldDecorationRegistry.
	 * Image management is performed by WorkbenchImages and its registry, so
	 * there are no life cycle issues here.
	 * 
	 * @since 3.2
	 */
	private void initializeFieldDecorations() {
		WorkbenchFieldDecorationSupport.initializeFieldDecorations();
	}
}
