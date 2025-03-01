/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Francis Upton - <francisu@ieee.org> - Bug 217777
 *     Tristan Hume - <trishume@gmail.com> - Bug 2369
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 422533, 440136, 445724, 366708, 418661, 456897, 472654, 481516, 486543
 *     Terry Parker <tparker@google.com> - Bug 416673
 *     Sergey Prigogin <eclipse.sprigogin@gmail.com> - Bug 438324
 *     Snjezana Peco <snjeza.peco@gmail.com> - Bug 405542
 *     Andrey Loskutov <loskutov@gmx.de> - Bug 372799
 *     Mickael Istria (Red Hat Inc.) - Bug 469918
 *     Patrik Suzzi <psuzzi@gmail.com> - Bug 487297
 *     Daniel Kruegler <daniel.kruegler@gmail.com> - Bug 520926
 *     Christian Georgi (SAP SE) - Bug 540440
 *     Paul Pazderski <paul-eclipse@ppazderski.de> - Bug 550950
 *******************************************************************************/

package org.eclipse.ui.internal;

import com.ibm.icu.util.ULocale;
import com.ibm.icu.util.ULocale.Category;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.CommandManager;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.EventManager;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.commands.contexts.ContextManager;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionDelta;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IRegistryChangeListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Platform.OS;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.InjectionException;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.internal.workbench.E4Workbench;
import org.eclipse.e4.ui.internal.workbench.E4XMIResource;
import org.eclipse.e4.ui.internal.workbench.renderers.swt.IUpdateService;
import org.eclipse.e4.ui.internal.workbench.swt.E4Application;
import org.eclipse.e4.ui.internal.workbench.swt.IEventLoopAdvisor;
import org.eclipse.e4.ui.internal.workbench.swt.PartRenderingEngine;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.commands.MBindingContext;
import org.eclipse.e4.ui.model.application.commands.MBindingTable;
import org.eclipse.e4.ui.model.application.commands.MCategory;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MCommandsFactory;
import org.eclipse.e4.ui.model.application.commands.impl.CommandsFactoryImpl;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.menu.MTrimContribution;
import org.eclipse.e4.ui.services.EContextService;
import org.eclipse.e4.ui.workbench.IModelResourceHandler;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.ISaveHandler;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.ExternalActionManager;
import org.eclipse.jface.action.ExternalActionManager.CommandCallback;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.bindings.BindingManager;
import org.eclipse.jface.bindings.IBindingManagerListener;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.ModalContext;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.BidiUtils;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.OpenStrategy;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.util.Util;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.internal.location.LocationHelper;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.osgi.service.runnable.StartupMonitor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.DeviceData;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.ILocalWorkingSetManager;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.ISaveableFilter;
import org.eclipse.ui.ISaveablesLifecycleListener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.ISourceProvider;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.Saveable;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.activities.IWorkbenchActivitySupport;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.commands.ICommandImageService;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.contexts.IWorkbenchContextSupport;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.help.IWorkbenchHelpSystem;
import org.eclipse.ui.internal.StartupThreading.StartupRunnable;
import org.eclipse.ui.internal.WorkbenchWindow.WWinPartServiceSaveHandler;
import org.eclipse.ui.internal.actions.CommandAction;
import org.eclipse.ui.internal.activities.ws.WorkbenchActivitySupport;
import org.eclipse.ui.internal.browser.WorkbenchBrowserSupport;
import org.eclipse.ui.internal.commands.CommandImageManager;
import org.eclipse.ui.internal.commands.CommandImageService;
import org.eclipse.ui.internal.commands.CommandService;
import org.eclipse.ui.internal.contexts.ActiveContextSourceProvider;
import org.eclipse.ui.internal.contexts.ContextService;
import org.eclipse.ui.internal.contexts.WorkbenchContextSupport;
import org.eclipse.ui.internal.dialogs.PropertyPageContributorManager;
import org.eclipse.ui.internal.e4.compatibility.CompatibilityEditor;
import org.eclipse.ui.internal.e4.compatibility.CompatibilityPart;
import org.eclipse.ui.internal.e4.compatibility.E4Util;
import org.eclipse.ui.internal.handlers.LegacyHandlerService;
import org.eclipse.ui.internal.help.WorkbenchHelpSystem;
import org.eclipse.ui.internal.intro.IIntroRegistry;
import org.eclipse.ui.internal.intro.IntroDescriptor;
import org.eclipse.ui.internal.keys.BindingService;
import org.eclipse.ui.internal.keys.show.ShowKeysListener;
import org.eclipse.ui.internal.menus.FocusControlSourceProvider;
import org.eclipse.ui.internal.menus.WorkbenchMenuService;
import org.eclipse.ui.internal.misc.Policy;
import org.eclipse.ui.internal.misc.StatusUtil;
import org.eclipse.ui.internal.misc.UIStats;
import org.eclipse.ui.internal.model.ContributionService;
import org.eclipse.ui.internal.progress.ProgressManager;
import org.eclipse.ui.internal.progress.ProgressManagerUtil;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.internal.registry.ViewDescriptor;
import org.eclipse.ui.internal.services.EvaluationService;
import org.eclipse.ui.internal.services.IServiceLocatorCreator;
import org.eclipse.ui.internal.services.IWorkbenchLocationService;
import org.eclipse.ui.internal.services.MenuSourceProvider;
import org.eclipse.ui.internal.services.ServiceLocator;
import org.eclipse.ui.internal.services.ServiceLocatorCreator;
import org.eclipse.ui.internal.services.SourceProviderService;
import org.eclipse.ui.internal.services.WorkbenchLocationService;
import org.eclipse.ui.internal.splash.EclipseSplashHandler;
import org.eclipse.ui.internal.splash.SplashHandlerFactory;
import org.eclipse.ui.internal.testing.ContributionInfoMessages;
import org.eclipse.ui.internal.testing.WorkbenchTestable;
import org.eclipse.ui.internal.themes.ColorDefinition;
import org.eclipse.ui.internal.themes.FontDefinition;
import org.eclipse.ui.internal.themes.ThemeElementHelper;
import org.eclipse.ui.internal.themes.WorkbenchThemeManager;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.ui.intro.IIntroManager;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.model.IContributionService;
import org.eclipse.ui.operations.IWorkbenchOperationSupport;
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.ui.progress.WorkbenchJob;
import org.eclipse.ui.services.IDisposable;
import org.eclipse.ui.services.IEvaluationService;
import org.eclipse.ui.services.IServiceScopes;
import org.eclipse.ui.services.ISourceProviderService;
import org.eclipse.ui.splash.AbstractSplashHandler;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.swt.IFocusService;
import org.eclipse.ui.testing.ContributionInfo;
import org.eclipse.ui.themes.ITheme;
import org.eclipse.ui.themes.IThemeManager;
import org.eclipse.ui.views.IViewDescriptor;
import org.eclipse.ui.views.IViewRegistry;
import org.eclipse.ui.wizards.IWizardRegistry;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;

/**
 * The workbench class represents the top of the Eclipse user interface. Its
 * primary responsibility is the management of workbench windows, dialogs,
 * wizards, and other workbench-related windows.
 * <p>
 * Note that any code that is run during the creation of a workbench instance
 * should not required access to the display.
 * </p>
 */
public final class Workbench extends EventManager implements IWorkbench, org.eclipse.e4.ui.workbench.IWorkbench {

	public static final String WORKBENCH_AUTO_SAVE_JOB = "Workbench Auto-Save Job"; //$NON-NLS-1$

	private static final String WORKBENCH_AUTO_SAVE_BACKGROUND_JOB = "Workbench Auto-Save Background Job"; //$NON-NLS-1$

	public static final String MEMENTO_KEY = "memento"; //$NON-NLS-1$

	public static final String EDITOR_TAG = "Editor"; //$NON-NLS-1$

	public static final String PROP_EXIT_CODE = "eclipse.exitcode"; //$NON-NLS-1$
	private static final String CMD_DATA = "-data"; //$NON-NLS-1$

	private static final String EDGE_USER_DATA_FOLDER = "org.eclipse.swt.internal.win32.Edge.userDataFolder"; //$NON-NLS-1$

	private static final String SWT_RESCALE_AT_RUNTIME_PROPERTY = "swt.autoScale.updateOnRuntime"; //$NON-NLS-1$

	private static final class StartupProgressBundleListener implements ServiceListener {

		private final SubMonitor subMonitor;
		private Display displayForStartupListener;

		StartupProgressBundleListener(IProgressMonitor progressMonitor, Display display) {
			displayForStartupListener = display;
			subMonitor = SubMonitor.convert(progressMonitor);
			subMonitor.setTaskName(NLS.bind(WorkbenchMessages.Startup_Loading, Platform.getProduct().getName()));
		}

		@Override
		public void serviceChanged(ServiceEvent event) {
			subMonitor.setWorkRemaining(5).worked(1);
			spinEventQueueToUpdateSplash(displayForStartupListener);
		}
	}

	/**
	 * Family for the early startup job.
	 */
	public static final String EARLY_STARTUP_FAMILY = "earlyStartup"; //$NON-NLS-1$

	public static final String DEFAULT_WORKBENCH_STATE_FILENAME = "workbench.xml"; //$NON-NLS-1$

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
	 * Signals that the workbench should create a splash implementation when
	 * instantiated. Initial value is <code>true</code>.
	 *
	 * @since 3.3
	 */
	private static boolean createSplash = true;

	/**
	 * The splash handler.
	 */
	private static AbstractSplashHandler splash;

	/**
	 * The display used for all UI interactions with this workbench.
	 *
	 * @since 3.0
	 */
	private Display display;

	private boolean workbenchAutoSave = true;

	private EditorHistory editorHistory;

	private boolean runEventLoop = true;

	private boolean isStarting = true;

	private boolean isClosing = false;

	/**
	 * A boolean field to indicate whether all the workbench windows have been
	 * closed or not.
	 */
	private boolean windowsClosed = false;

	/**
	 * PlatformUI return code
	 */
	private int returnCode = PlatformUI.RETURN_UNSTARTABLE;

	/**
	 * Advisor providing application-specific configuration and customization of the
	 * workbench.
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
	 * <code>setRedraw</code> functionality on <code>Control</code>. When this value
	 * becomes greater than zero, services are disabled. When this value becomes
	 * zero, services are enabled. Please see <code>largeUpdateStart()</code> and
	 * <code>largeUpdateEnd()</code>.
	 */
	private int largeUpdates = 0;

	/**
	 * The service locator maintained by the workbench. These services are
	 * initialized during workbench during the <code>init</code> method.
	 */
	private final ServiceLocator serviceLocator;

	/**
	 * Listener list for registered IWorkbenchListeners .
	 */
	private ListenerList<IWorkbenchListener> workbenchListeners = new ListenerList<>(ListenerList.IDENTITY);

	private ServiceRegistration workbenchService;

	private MApplication application;

	private IEclipseContext e4Context;

	private IEventBroker eventBroker;

	private IExtensionRegistry registry;

	boolean initializationDone = false;

	private WorkbenchWindow windowBeingCreated = null;

	private Listener backForwardListener;

	private Job autoSaveJob;

	private String id;
	private ServiceRegistration<?> e4WorkbenchService;

	// flag used to identify if the application model needs to be saved
	private boolean applicationModelChanged = false;

	private IWorkbenchWindow windowWhileInit;

	/**
	 * Creates a new workbench.
	 *
	 * @param display the display to be used for all UI interactions with the
	 *                workbench
	 * @param advisor the application-specific advisor that configures and
	 *                specializes this workbench instance
	 * @since 3.0
	 */
	@SuppressWarnings("restriction")
	private Workbench(Display display, final WorkbenchAdvisor advisor, MApplication app, IEclipseContext appContext) {
		this.advisor = Objects.requireNonNull(advisor);
		this.display = Objects.requireNonNull(display);
		if (OS.isWindows()) {
			setEdgeDataDirectory(this.display);
		}

		application = app;
		e4Context = appContext;

		this.id = createId();

		if (instance != null && instance.isRunning()) {
			throw new IllegalStateException(WorkbenchMessages.Workbench_CreatingWorkbenchTwice);
		}

		Workbench.instance = this;

		StartupThreading.setDisplay(display);
		eventBroker = e4Context.get(IEventBroker.class);
		registry = e4Context.get(IExtensionRegistry.class);

		appContext.set(Workbench.class, this);
		appContext.set(IWorkbench.class, this);
		appContext.set(IEventLoopAdvisor.class, new IEventLoopAdvisor() {
			@Override
			public void eventLoopIdle(Display display) {
				advisor.eventLoopIdle(display);
			}

			@Override
			public void eventLoopException(Throwable exception) {
				advisor.eventLoopException(exception);
			}
		});
		appContext.set(org.eclipse.e4.core.services.contributions.IContributionFactory.class,
				new WorkbenchContributionFactory(this));

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
		registry.addRegistryChangeListener(extensionEventHandler);
		IServiceLocatorCreator slc = new ServiceLocatorCreator();
		serviceLocator = (ServiceLocator) slc.createServiceLocator(null, null, () -> {
			final Display display1 = getDisplay();
			if (display1 != null && !display1.isDisposed()) {
				MessageDialog.openInformation(null, WorkbenchMessages.Workbench_NeedsClose_Title,
						WorkbenchMessages.Workbench_NeedsClose_Message);
				close(PlatformUI.RETURN_RESTART, true);
			}
		}, appContext);
		serviceLocator.registerService(IServiceLocatorCreator.class, slc);
		serviceLocator.registerService(IWorkbenchLocationService.class,
				new WorkbenchLocationService(IServiceScopes.WORKBENCH_SCOPE, this, null, null, null, null, 0));
	}

	private static void setEdgeDataDirectory(Display display) {
		Location workspaceLocation = Platform.getInstanceLocation();
		if (workspaceLocation == null) {
			return;
		}
		try {
			URL swtMetadataLocationURL = workspaceLocation
					.getDataArea(FrameworkUtil.getBundle(Browser.class).getSymbolicName());
			display.setData(EDGE_USER_DATA_FOLDER, new File(swtMetadataLocationURL.getFile()).toString());
		} catch (IOException e) {
			WorkbenchPlugin.log("Invalid workspace location to be set for Edge browser.", e); //$NON-NLS-1$
		}
	}

	/**
	 * Returns the one and only instance of the workbench, if there is one.
	 *
	 * @return the workbench, or <code>null</code> if the workbench has not been
	 *         created, or has been created and already completed
	 */
	public static Workbench getInstance() {
		return instance;
	}

	/**
	 * Creates the workbench and associates it with the the given display and
	 * workbench advisor, and runs the workbench UI. This entails processing and
	 * dispatching events until the workbench is closed or restarted.
	 * <p>
	 * This method is intended to be called by <code>PlatformUI</code>. Fails if the
	 * workbench UI has already been created.
	 * </p>
	 * <p>
	 * The display passed in must be the default display.
	 * </p>
	 *
	 * @param display the display to be used for all UI interactions with the
	 *                workbench
	 * @param advisor the application-specific advisor that configures and
	 *                specializes the workbench
	 * @return return code {@link PlatformUI#RETURN_OK RETURN_OK}for normal exit;
	 *         {@link PlatformUI#RETURN_RESTART RETURN_RESTART}if the workbench was
	 *         terminated with a call to {@link IWorkbench#restart
	 *         IWorkbench.restart}; other values reserved for future use
	 */
	public static int createAndRunWorkbench(final Display display, final WorkbenchAdvisor advisor) {
		final int[] returnCode = new int[1];
		Realm.runWithDefault(DisplayRealm.getRealm(display), () -> {
			boolean showProgress = PrefUtil.getAPIPreferenceStore()
					.getBoolean(IWorkbenchPreferenceConstants.SHOW_PROGRESS_ON_STARTUP);

			final String nlExtensions = Platform.getNLExtensions();
			if (nlExtensions.length() > 0) {
				ULocale.setDefault(Category.FORMAT,
						new ULocale(ULocale.getDefault(Category.FORMAT).getBaseName() + nlExtensions));
			}

			System.setProperty(org.eclipse.e4.ui.workbench.IWorkbench.XMI_URI_ARG,
					"org.eclipse.ui.workbench/LegacyIDE.e4xmi"); //$NON-NLS-1$
			Object obj = getApplication(Platform.getCommandLineArgs());

			IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
			if (!store.isDefault(IPreferenceConstants.LAYOUT_DIRECTION)) {
				int orientation = store.getInt(IPreferenceConstants.LAYOUT_DIRECTION);
				Window.setDefaultOrientation(orientation);
			}
			if (obj instanceof E4Application) {
				E4Application e4app = (E4Application) obj;
				E4Workbench e4Workbench = e4app.createE4Workbench(getApplicationContext(), display);

				MApplication appModel = e4Workbench.getApplication();
				IEclipseContext context = e4Workbench.getContext();

				// create the workbench instance
				Workbench workbench = new Workbench(display, advisor, appModel, context);

				Dictionary<String, Object> properties = new Hashtable<>();
				properties.put(Constants.SERVICE_RANKING, Integer.valueOf(Integer.MAX_VALUE - 1));
				ServiceRegistration<?> registration[] = new ServiceRegistration[1];
				StartupMonitor startupMonitor = new StartupMonitor() {
					@Override
					public void applicationRunning() {
						registration[0].unregister(); // unregister ourself
						// fire part visibility events now that we're up
						for (IWorkbenchWindow window : workbench.getWorkbenchWindows()) {
							IWorkbenchPage page = window.getActivePage();
							if (page != null) {
								((WorkbenchPage) page).fireInitialPartVisibilityEvents();
							}
						}
					}
					@Override
					public void update() {
						// do nothing - we come into the picture far too late
						// for this to be relevant
					}
				};
				registration[0] = FrameworkUtil.getBundle(WorkbenchPlugin.class).getBundleContext()
						.registerService(StartupMonitor.class.getName(), startupMonitor,
						properties);

				// listener for updating the splash screen
				ServiceListener serviceListener = null;
				createSplash = WorkbenchPlugin.isSplashHandleSpecified();
				if (createSplash) {

					// prime the splash nice and early
					workbench.createSplashWrapper();

					// Bug 539376, 427393, 455162: show the splash screen after
					// the image is loaded. See IDEApplication#checkInstanceLocation
					// where the splash shell got hidden to avoid empty shell
					AbstractSplashHandler handler = getSplash();
					if (handler != null) {
						Shell splashShell = handler.getSplash();
						if (splashShell != null && !splashShell.isDisposed()) {
							splashShell.setVisible(true);
							splashShell.forceActive();
						}
					}
					spinEventQueueToUpdateSplash(display);

					if (handler != null && showProgress) {
						IProgressMonitor progressMonitor = SubMonitor.convert(handler.getBundleProgressMonitor());
						serviceListener = new Workbench.StartupProgressBundleListener(progressMonitor, display);
						WorkbenchPlugin.getDefault().getBundleContext().addServiceListener(serviceListener);
					}

				}

				setSearchContribution(appModel, true);
				// run the legacy workbench once
				returnCode[0] = workbench.runUI();

				if (returnCode[0] == PlatformUI.RETURN_OK) {
					// run the e4 event loop and instantiate ... well, stuff
					if (serviceListener != null) {
						WorkbenchPlugin.getDefault().getBundleContext().removeServiceListener(serviceListener);
					}
					e4Workbench.createAndRunUI(e4Workbench.getApplication());
				}
				if (returnCode[0] != PlatformUI.RETURN_UNSTARTABLE) {
					setSearchContribution(appModel, false);
					e4app.saveModel();
				}

				// if a restart was triggered via E4Workbench the return
				// code needs to be set appropriately
				if (e4Workbench.isRestart()) {
					returnCode[0] = PlatformUI.RETURN_RESTART;
				} else {
					e4Workbench.close();
					returnCode[0] = workbench.returnCode;
				}
			}
		});
		return returnCode[0];
	}

	private static void setRescaleAtRuntimePropertyFromPreference() {
		if (System.getProperty(SWT_RESCALE_AT_RUNTIME_PROPERTY) != null) {
			WorkbenchPlugin.log(Status.warning(SWT_RESCALE_AT_RUNTIME_PROPERTY
					+ " is configured (e.g., via the INI), but the according preference should be preferred instead." //$NON-NLS-1$
			));
		} else {
			boolean rescaleAtRuntime = ConfigurationScope.INSTANCE.getNode(WorkbenchPlugin.PI_WORKBENCH)
					.getBoolean(IWorkbenchPreferenceConstants.RESCALING_AT_RUNTIME, false);
			System.setProperty(SWT_RESCALE_AT_RUNTIME_PROPERTY, Boolean.toString(rescaleAtRuntime));
		}
	}

	private static void setSearchContribution(MApplication app, boolean enabled) {
		for (MTrimContribution contribution : app.getTrimContributions()) {
			if ("org.eclipse.ui.ide.application.trimcontribution.QuickAccess".contains(contribution //$NON-NLS-1$
					.getElementId())) {
				// allows us to handle the case where someone opens a workspace
				// with Luna and then with Kepler
				contribution.setToBeRendered(enabled);
			}
		}
	}

	private static ServiceTracker instanceAppContext;

	static IApplicationContext getApplicationContext() {
		if (instanceAppContext == null) {
			instanceAppContext = new ServiceTracker(WorkbenchPlugin.getDefault().getBundleContext(),
					IApplicationContext.class.getName(), null);
			instanceAppContext.open();
		}
		return (IApplicationContext) instanceAppContext.getService();
	}

	static Object getApplication(@SuppressWarnings("unused") String[] args) {
		// Find the name of the application as specified by the PDE JUnit
		// launcher.
		// If no application is specified, the 3.0 default workbench application
		// is returned.
		IExtension extension = Platform.getExtensionRegistry().getExtension(Platform.PI_RUNTIME,
				Platform.PT_APPLICATIONS, "org.eclipse.e4.ui.workbench.swt.E4Application"); //$NON-NLS-1$

		Assert.isNotNull(extension);

		// If the extension does not have the correct grammar, return null.
		// Otherwise, return the application object.
		try {
			IConfigurationElement[] elements = extension.getConfigurationElements();
			if (elements.length > 0) {
				IConfigurationElement[] runs = elements[0].getChildren("run"); //$NON-NLS-1$
				if (runs.length > 0) {
					Object runnable;
					runnable = runs[0].createExecutableExtension("class");//$NON-NLS-1$
					if (runnable instanceof IApplication)
						return runnable;
				}
			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Creates the <code>Display</code> to be used by the workbench.
	 *
	 * @return the display
	 */
	public static Display createDisplay() {
		// setup the application name used by SWT to lookup resources on some platforms
		String applicationName = System.getProperty("eclipse.appName", WorkbenchPlugin.getDefault().getAppName()); //$NON-NLS-1$
		if (applicationName != null) {
			Display.setAppName(applicationName);
		}

		setRescaleAtRuntimePropertyFromPreference();

		// create the display
		Display newDisplay = Display.getCurrent();
		if (newDisplay == null) {
			if (Policy.DEBUG_SWT_GRAPHICS || Policy.DEBUG_SWT_DEBUG) {
				DeviceData data = new DeviceData();
				if (Policy.DEBUG_SWT_GRAPHICS) {
					data.tracking = true;
				}
				if (Policy.DEBUG_SWT_DEBUG) {
					data.debug = true;
				}
				newDisplay = new Display(data);
			} else {
				newDisplay = new Display();
			}
		}

		// workaround for 1GEZ9UR and 1GF07HN
		newDisplay.setWarnings(false);

		// Set the priority higher than normal so as to be higher
		// than the JobManager.
		Thread.currentThread().setPriority(Math.min(Thread.MAX_PRIORITY, Thread.NORM_PRIORITY + 1));

		initializeImages();

		return newDisplay;
	}

	/**
	 * Create the splash wrapper and set it to work.
	 *
	 * @since 3.3
	 */
	private void createSplashWrapper() {

		SafeRunnable run = new SafeRunnable() {
			Image background = null;

			@Override
			public void run() throws Exception {
				String splashLoc = System.getProperty("org.eclipse.equinox.launcher.splash.location"); //$NON-NLS-1$
				background = loadSplashScreenImage(display, splashLoc);

				// create the splash
				getSplash();
				if (splash == null) {
					createSplash = false;
					return;
				}

				Shell splashShell = splash.getSplash();
				if (splashShell == null) {
					splashShell = WorkbenchPlugin.getSplashShell(display);

					if (splashShell == null)
						return;
					if (background != null)
						splashShell.setBackgroundImage(background);
				}

				Dictionary<String, Object> properties = new Hashtable<>();
				properties.put(Constants.SERVICE_RANKING, Integer.valueOf(Integer.MAX_VALUE));
				BundleContext context = WorkbenchPlugin.getDefault().getBundleContext();
				final ServiceRegistration<?> registration[] = new ServiceRegistration[1];
				StartupMonitor startupMonitor = new StartupMonitor() {

					@Override
					public void applicationRunning() {
						if (background != null)
							background.dispose();
						registration[0].unregister(); // unregister ourself
						if (splash != null)
							splash.dispose();
						WorkbenchPlugin.unsetSplashShell(display);
					}

					@Override
					public void update() {
						// do nothing - we come into the picture far too late
						// for this to be relevant
					}
				};
				registration[0] = context.registerService(StartupMonitor.class.getName(), startupMonitor, properties);

				splash.init(splashShell);
			}

			@Override
			public void handleException(Throwable e) {
				StatusManager.getManager()
						.handle(StatusUtil.newStatus(WorkbenchPlugin.PI_WORKBENCH, "Could not instantiate splash", e)); //$NON-NLS-1$
				createSplash = false;
				splash = null;
				if (background != null)
					background.dispose();

			}
		};
		SafeRunner.run(run);
	}

	// Ensure that the splash screen is rendered
	private static void spinEventQueueToUpdateSplash(final Display display) {
		if (!display.isDisposed() && display.getThread() == Thread.currentThread()) {
			int safetyCounter = 0;
			while (display.readAndDispatch() && safetyCounter++ < 100) {
				// process until the queue is empty or until we hit the safetyCounter limit
			}
		}
	}

	/**
	 * Load an image from a filesystem path.
	 *
	 * @param splashLoc the location to load from
	 * @return the image or <code>null</code>
	 * @since 3.3
	 */
	private static Image loadSplashScreenImage(Display display, String splashLoc) {
		Image background = null;
		if (splashLoc != null) {
			try (InputStream input = new BufferedInputStream(new FileInputStream(splashLoc))) {
				background = getImage(display, input);
			} catch (SWTException | IOException | NumberFormatException e) {
				StatusManager.getManager().handle(StatusUtil.newStatus(WorkbenchPlugin.PI_WORKBENCH, e));
			}
		}
		return background;
	}

	private static Image getImage(Display display, InputStream input) {
		Image image = new Image(display, input);

		if (Util.isMac()) {
			/*
			 * Due to a bug in MacOS Sonoma
			 * (https://github.com/eclipse-platform/eclipse.platform.swt/issues/772) ,Splash
			 * Screen gets flipped.As a workaround the image is flipped and returned.
			 */
			if (Integer.parseInt(System.getProperty("os.version").split("\\.")[0]) == 14) { //$NON-NLS-1$ //$NON-NLS-2$
				GC gc = new GC(image);
				Transform tr = new Transform(display);
				tr.setElements(1, 0, 0, -1, 0, 0);
				gc.setTransform(tr);
				gc.drawImage(image, 0, -(image.getBounds().height));
				tr.dispose();
				gc.dispose();
			}
		}
		return image;
	}

	/**
	 * Return the splash handler for this application. If none is specifically
	 * provided the default Eclipse implementation is returned.
	 *
	 * @return the splash handler for this application or <code>null</code>
	 * @since 3.3
	 */
	private static AbstractSplashHandler getSplash() {
		if (!createSplash)
			return null;

		if (splash == null) {

			IProduct product = Platform.getProduct();
			if (product != null)
				splash = SplashHandlerFactory.findSplashHandlerFor(product);

			if (splash == null)
				splash = new EclipseSplashHandler();
		}
		return splash;
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

	@Override
	public void addWorkbenchListener(IWorkbenchListener listener) {
		workbenchListeners.add(listener);
	}

	@Override
	public void removeWorkbenchListener(IWorkbenchListener listener) {
		workbenchListeners.remove(listener);
	}

	/**
	 * Fire workbench preShutdown event, stopping at the first one to veto
	 *
	 * @param forced flag indicating whether the shutdown is being forced
	 * @return <code>true</code> to allow the workbench to proceed with shutdown,
	 *         <code>false</code> to veto a non-forced shutdown
	 * @since 3.2
	 */
	boolean firePreShutdown(final boolean forced) {
		for (final IWorkbenchListener l : workbenchListeners) {
			final boolean[] result = new boolean[] { false };
			SafeRunnable.run(new SafeRunnable() {
				@Override
				public void run() {
					result[0] = l.preShutdown(Workbench.this, forced);
				}
			});
			if (!result[0]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Fire workbench postShutdown event.
	 *
	 * @since 3.2
	 */
	void firePostShutdown() {
		for (final IWorkbenchListener l : workbenchListeners) {
			SafeRunnable.run(new SafeRunnable() {
				@Override
				public void run() {
					l.postShutdown(Workbench.this);
				}
			});
		}
	}

	@Override
	public void addWindowListener(IWindowListener l) {
		addListenerObject(l);
	}

	@Override
	public void removeWindowListener(IWindowListener l) {
		removeListenerObject(l);
	}

	/**
	 * Fire window opened event.
	 *
	 * @param window The window which just opened; should not be <code>null</code>.
	 */
	protected void fireWindowOpened(final IWorkbenchWindow window) {
		Object list[] = getListeners();
		for (Object element : list) {
			final IWindowListener l = (IWindowListener) element;
			SafeRunner.run(new SafeRunnable() {
				@Override
				public void run() {
					l.windowOpened(window);
				}
			});
		}
	}

	/**
	 * Fire window closed event.
	 *
	 * @param window The window which just closed; should not be <code>null</code>.
	 */
	protected void fireWindowClosed(final IWorkbenchWindow window) {
		Object list[] = getListeners();
		for (Object element : list) {
			final IWindowListener l = (IWindowListener) element;
			SafeRunner.run(new SafeRunnable() {
				@Override
				public void run() {
					l.windowClosed(window);
				}
			});
		}
	}

	/**
	 * Fire window activated event.
	 *
	 * @param window The window which was just activated; should not be
	 *               <code>null</code>.
	 */
	protected void fireWindowActivated(final IWorkbenchWindow window) {
		Object list[] = getListeners();
		for (Object element : list) {
			final IWindowListener l = (IWindowListener) element;
			SafeRunner.run(new SafeRunnable() {
				@Override
				public void run() {
					l.windowActivated(window);
				}
			});
		}
	}

	/**
	 * Fire window deactivated event.
	 *
	 * @param window The window which was just deactivated; should not be
	 *               <code>null</code>.
	 */
	protected void fireWindowDeactivated(final IWorkbenchWindow window) {
		Object list[] = getListeners();
		for (Object element : list) {
			final IWindowListener l = (IWindowListener) element;
			SafeRunner.run(new SafeRunnable() {
				@Override
				public void run() {
					l.windowDeactivated(window);
				}
			});
		}
	}

	/**
	 * Closes the workbench. Assumes that the busy cursor is active.
	 *
	 * @param force true if the close is mandatory, and false if the close is
	 *              allowed to fail
	 * @return true if the close succeeded, and false otherwise
	 */
	private boolean busyClose(final boolean force) {
		// notify the advisor of preShutdown and allow it to veto if not forced
		isClosing = advisor.preShutdown();
		if (!force && !isClosing) {
			return false;
		}

		// notify regular workbench clients of preShutdown and allow them to
		// veto if not forced
		isClosing = firePreShutdown(force);
		if (!force && !isClosing) {
			return false;
		}

		// save any open editors if they are dirty
		isClosing = saveAllParts(!force, true);
		if (!force && !isClosing) {
			return false;
		}

		// stop the workbench auto-save job so it can't conflict with shutdown
		if (autoSaveJob != null) {
			autoSaveJob.cancel();
			autoSaveJob = null;
		}

		boolean closeEditors = !force
				&& PrefUtil.getAPIPreferenceStore().getBoolean(IWorkbenchPreferenceConstants.CLOSE_EDITORS_ON_EXIT);
		if (closeEditors) {
			SafeRunner.run(new SafeRunnable() {
				@Override
				public void run() {
					IWorkbenchWindow windows[] = getWorkbenchWindows();
					for (IWorkbenchWindow window : windows) {
						IWorkbenchPage pages[] = window.getPages();
						for (IWorkbenchPage page : pages) {
							isClosing = isClosing && page.closeAllEditors(false);
						}
					}
				}
			});
			if (!force && !isClosing) {
				return false;
			}
		}

		// persist editor inputs and close editors that can't be persisted
		// also persists views
		persist(true);

		if (!force && !isClosing) {
			return false;
		}

		SafeRunner.run(new SafeRunnable(WorkbenchMessages.ErrorClosing) {
			@Override
			public void run() {
				if (isClosing || force) {
					E4Util.unsupported("Need to close since no windowManager"); //$NON-NLS-1$
					MWindow selectedWindow = application.getSelectedElement();
					WorkbenchWindow selected = null;
					for (IWorkbenchWindow window : getWorkbenchWindows()) {
						WorkbenchWindow ww = (WorkbenchWindow) window;
						if (ww.getModel() == selectedWindow) {
							selected = ww;
						} else {
							((WorkbenchWindow) window).close(false);
						}
					}

					if (selected != null) {
						selected.close(false);
					}

					windowsClosed = true;
				}
			}
		});

		if (!force && !isClosing) {
			return false;
		}

		// Fire an E4 lifecycle notification.
		// Bug 520926: This event must be fired after all veto chances have passed:
		UIEvents.publishEvent(UIEvents.UILifeCycle.APP_SHUTDOWN_STARTED, application);

		shutdown();

		IPresentationEngine engine = application.getContext().get(IPresentationEngine.class);
		engine.stop();

		runEventLoop = false;
		return true;
	}

	/**
	 * Saves the state of the workbench in the same way that closing the it would.
	 * Can be called while the editor is running so that if it crashes the workbench
	 * state can be recovered.
	 *
	 * @param shutdown If true, will close any editors that cannot be persisted.
	 *                 Will also skip saving the model to the disk since that is
	 *                 done later in shutdown.
	 */
	private void persist(final boolean shutdown) {
		// persist editors that can be and possibly close the others
		SafeRunner.run(new SafeRunnable() {
			@Override
			public void run() {
				IWorkbenchWindow windows[] = getWorkbenchWindows();
				for (IWorkbenchWindow window : windows) {
					IWorkbenchPage pages[] = window.getPages();
					for (IWorkbenchPage page : pages) {
						List<EditorReference> editorReferences = ((WorkbenchPage) page).getInternalEditorReferences();
						List<EditorReference> referencesToClose = new ArrayList<>();
						for (EditorReference reference : editorReferences) {
							IEditorPart editor = reference.getEditor(false);
							if (editor != null && !reference.persist() && shutdown) {
								referencesToClose.add(reference);
							}
						}
						if (shutdown) {
							for (EditorReference reference : referencesToClose) {
								((WorkbenchPage) page).closeEditor(reference);
							}
						}
					}
				}
			}
		});

		// persist workbench state
		if (getWorkbenchConfigurer().getSaveAndRestore()) {
			SafeRunner.run(new SafeRunnable() {
				@Override
				public void run() {
					persistWorkbenchState();
				}

				@Override
				public void handleException(Throwable e) {
					String message;
					if (e.getMessage() == null) {
						message = WorkbenchMessages.ErrorClosingNoArg;
					} else {
						message = NLS.bind(WorkbenchMessages.ErrorClosingOneArg, e.getMessage());
					}

					if (!MessageDialog.openQuestion(null, WorkbenchMessages.Error, message)) {
						isClosing = false;
					}
				}
			});
		}

		// persist view states
		SafeRunner.run(new SafeRunnable() {
			@Override
			public void run() {
				IWorkbenchWindow windows[] = getWorkbenchWindows();
				for (IWorkbenchWindow window : windows) {
					IWorkbenchPage pages[] = window.getPages();
					for (IWorkbenchPage page : pages) {
						IViewReference[] references = page.getViewReferences();
						for (IViewReference reference : references) {
							if (reference.getView(false) != null) {
								((ViewReference) reference).persist();
							}
						}
					}
				}
			}
		});

		// now that we have updated the model, save it to workbench.xmi
		// skip this during shutdown to be efficient since it is done again
		// later
		if (!shutdown) {
			persistWorkbenchModel();
		}
	}

	private boolean detectWorkbenchCorruption(MApplication application) {
		if (application.getChildren().isEmpty()) {
			WorkbenchPlugin.log("When auto-saving the workbench model, there were no top-level windows. " //$NON-NLS-1$
					+ " Skipped saving the model.", //$NON-NLS-1$
					new Exception()); // log a stack trace to assist debugging
			return true;
		}
		return false;
	}

	/**
	 * Copy the model, clean it up and write it out to workbench.xmi. Called as part
	 * of persist(false) during auto-save.
	 */
	private void persistWorkbenchModel() {
		if (Job.getJobManager().find(WORKBENCH_AUTO_SAVE_JOB).length > 0) {
			return;
		}
		final MApplication appCopy = (MApplication) EcoreUtil.copy((EObject) application);
		if (detectWorkbenchCorruption(appCopy)) {
			return;
		}
		final IModelResourceHandler handler = e4Context.get(IModelResourceHandler.class);

		Job cleanAndSaveJob = new Job(WORKBENCH_AUTO_SAVE_BACKGROUND_JOB) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				final Resource res = handler.createResourceWithApp(appCopy);
				cleanUpCopy(appCopy);
				try {
					if (!detectWorkbenchCorruption((MApplication) res.getContents().get(0))) {
						Map<String, Object> options = new HashMap<>();
						options.put(E4XMIResource.OPTION_FILTER_PERSIST_STATE, Boolean.TRUE);
						res.save(options);
					}
				} catch (IOException e) {
					// Just auto-save, we don't really care
				} finally {
					res.unload();
					res.getResourceSet().getResources().remove(res);
				}
				return Status.OK_STATUS;
			}

			@Override
			public boolean belongsTo(Object family) {
				return WORKBENCH_AUTO_SAVE_JOB.equals(family);
			}

		};
		cleanAndSaveJob.setPriority(Job.SHORT);
		cleanAndSaveJob.setSystem(true);
		cleanAndSaveJob.schedule();
	}

	private static void cleanUpCopy(MApplication appCopy) {
		// clean up all trim bars that come from trim bar contributions
		// the trim elements that need to be removed are stored in the trimBar.
		setSearchContribution(appCopy, false);
	}

	@Override
	public boolean saveAllEditors(boolean confirm) {
		return saveAllEditors(confirm, false);
	}

	private boolean saveAllEditors(boolean confirm, boolean closing) {
		IWorkbenchWindow[] windows = getWorkbenchWindows();
		if (windows.length == 0) {
			return true;
		}

		Set<IWorkbenchPart> dirtyParts = new HashSet<>();
		for (IWorkbenchWindow window : windows) {
			WorkbenchPage page = (WorkbenchPage) window.getActivePage();
			if (page != null) {
				Collections.addAll(dirtyParts, page.getDirtyWorkbenchParts());
			}
		}

		IWorkbenchWindow activeWindow = getActiveWorkbenchWindow();
		if (activeWindow == null) {
			activeWindow = windows[0];
		}
		return WorkbenchPage.saveAll(new ArrayList<>(dirtyParts), confirm, closing, true, activeWindow, activeWindow);
	}

	private boolean saveAllParts(boolean confirm, boolean closing) {
		// Code to handle dirtied Editors and E4 parts too.
		EPartService partService = e4Context.get(EPartService.class);
		if (partService != null) {
			Collection<MPart> parts = getDirtyMParts();
			if (parts != null && parts.size() > 0) {
				MPart selected = null;
				for (MPart part : parts) {
					selected = part;
					break;
				}
				EModelService modelService = e4Context.get(EModelService.class);
				if (modelService != null) {
					IEclipseContext context = modelService.getContainingContext(selected);
					if (context != null) {
						ISaveHandler saveHandler = context.get(ISaveHandler.class);
						if (saveHandler != null) {
							if (saveHandler instanceof WWinPartServiceSaveHandler) {
								try {
									return ((WWinPartServiceSaveHandler) saveHandler).saveParts(parts, confirm, true, true);
								} catch (UnsupportedOperationException e) {
									// do nothing
								}
							}
						}
					}
				}
			}
		}
		// The below code will be called, if handlers are not available to handle saving
		// of E4 parts too.
		return saveAllEditors(confirm, closing);
	}

	private Collection<MPart> getDirtyMParts() {
		Set<MPart> dirtyParts = new HashSet<>();
		for (MWindow window : application.getChildren()) {
			IEclipseContext context = window.getContext();
			if (context != null) {
				IWorkbenchWindow wwindow = context.get(IWorkbenchWindow.class);
				if (wwindow != null) {
					EPartService partService = context.get(EPartService.class);
					if (partService != null) {
						Collection<MPart> parts = null;
						try {
							parts = partService.getDirtyParts();
							dirtyParts.addAll(parts);
						} catch (IllegalStateException e) {
							// This is to handle the case if the partService is instance of
							// ApplicationPartServiceImpl and does not have an active window
							// do nothing
						}
					}
				}
			}
		}
		return dirtyParts;
	}

	@Override
	public boolean close() {
		return close(PlatformUI.RETURN_OK, false);
	}

	/**
	 * Closes the workbench, returning the given return code from the run method. If
	 * forced, the workbench is closed no matter what.
	 *
	 * @param returnCode {@link PlatformUI#RETURN_OK RETURN_OK}for normal exit;
	 *                   {@link PlatformUI#RETURN_RESTART RETURN_RESTART}if the
	 *                   workbench was terminated with a call to
	 *                   {@link IWorkbench#restart IWorkbench.restart};
	 *                   {@link PlatformUI#RETURN_EMERGENCY_CLOSE} for an emergency
	 *                   shutdown {@link PlatformUI#RETURN_UNSTARTABLE
	 *                   RETURN_UNSTARTABLE}if the workbench could not be started;
	 *                   other values reserved for future use
	 *
	 * @param force      true to force the workbench close, and false for a "soft"
	 *                   close that can be canceled
	 * @return true if the close was successful, and false if the close was canceled
	 */
	/* package */
	boolean close(int returnCode, final boolean force) {
		if (returnCode == PlatformUI.RETURN_RESTART && !E4Workbench.canRestart()) {
			informNoRestart();
			return false;
		}

		this.returnCode = returnCode;
		final boolean[] ret = new boolean[1];
		BusyIndicator.showWhile(null, () -> ret[0] = busyClose(force));
		return ret[0];
	}

	@Override
	public IWorkbenchWindow getActiveWorkbenchWindow() {
		// Return null if called from a non-UI thread.
		// This is not spec'ed behaviour and is misleading, however this is how
		// it
		// worked in 2.1 and we cannot change it now.
		// For more details, see [Bug 57384] [RCP] Main window not active on
		// startup
		if (Display.getCurrent() == null || !initializationDone) {
			return null;
		}

		// the source providers try to update again during shutdown
		if (windowsClosed) {
			return null;
		}

		// rendering engine not available, can't make workbench windows, see bug
		// 320932
		if (e4Context.get(IPresentationEngine.class) == null) {
			return null;
		}

		if (windowWhileInit != null) {
			return windowWhileInit;
		}

		MWindow activeWindow = application.getSelectedElement();
		if ((activeWindow == null || activeWindow.getWidget() == null) && !application.getChildren().isEmpty()) {
			activeWindow = application.getChildren().get(0);
		}

		// We can't return a window with no widget...it's in the process
		// of closing...see Bug 379717
		if (activeWindow == null || activeWindow.getWidget() == null) {
			return null;
		}

		// search for existing IWorkbenchWindow
		IWorkbenchWindow iWorkbenchWindow = activeWindow.getContext().get(IWorkbenchWindow.class);
		if (iWorkbenchWindow != null) {
			return iWorkbenchWindow;
		}
		// otherwise create new IWorkbenchWindow instance
		return createWorkbenchWindow(getDefaultPageInput(),
				getPerspectiveRegistry().findPerspectiveWithId(getPerspectiveRegistry().getDefaultPerspective()),
				activeWindow, false);
	}

	IWorkbenchWindow createWorkbenchWindow(IAdaptable input, IPerspectiveDescriptor descriptor, MWindow window,
			boolean newWindow) {

		IEclipseContext windowContext = window.getContext();
		if (windowContext == null) {
			windowContext = E4Workbench.initializeContext(e4Context, window);
		}
		WorkbenchWindow result = (WorkbenchWindow) windowContext.get(IWorkbenchWindow.class);
		if (result == null) {
			if (windowBeingCreated != null)
				return windowBeingCreated;

			try {
				result = new WorkbenchWindow(input, descriptor);
				windowBeingCreated = result;
				windowWhileInit = getActiveWorkbenchWindow();

				if (newWindow) {
					WorkbenchWindowConfigurer windowConfigurer;
					WorkbenchWindow existingWindow = (WorkbenchWindow) windowWhileInit;
					if (existingWindow != null) {
						windowConfigurer = existingWindow.getWindowConfigurer();
					} else {
						windowConfigurer = result.getWindowConfigurer();
					}
					Point size = windowConfigurer.getInitialSize();
					window.setWidth(size.x);
					window.setHeight(size.y);

					placeNearActiveShell(window);

					application.getChildren().add(window);
					application.setSelectedElement(window);
				}

				ContextInjectionFactory.inject(result, windowContext);
				windowContext.set(IWorkbenchWindow.class, result);
			} finally {
				windowBeingCreated = null;
				windowWhileInit = null;
			}

			if (application.getSelectedElement() == window) {
				application.getContext().set(ISources.ACTIVE_WORKBENCH_WINDOW_NAME, result);
				application.getContext().set(ISources.ACTIVE_WORKBENCH_WINDOW_SHELL_NAME, result.getShell());
			}

			fireWindowOpened(result);
			result.fireWindowOpened();
		}
		return result;
	}

	private void placeNearActiveShell(MWindow window) {
		if (getDisplay() == null) {
			return;
		}

		Shell activeShell = getDisplay().getActiveShell();
		if (activeShell == null) {
			return;
		}

		Monitor currentMonitor = findMonitorThatContainsMostOf(activeShell.getBounds());

		final int padding = 20;
		Rectangle paddedMonitorBounds = shrink(currentMonitor.getBounds(), padding);

		final int offsetToExistingShell = 100;
		Rectangle newShellBounds = new Rectangle(activeShell.getBounds().x + offsetToExistingShell,
				activeShell.getBounds().y + offsetToExistingShell, window.getWidth(), window.getHeight());

		moveIntoBounds(newShellBounds, paddedMonitorBounds);

		window.setX(newShellBounds.x);
		window.setY(newShellBounds.y);
	}

	private static Rectangle shrink(Rectangle rectangle, int padding) {
		return new Rectangle(rectangle.x + padding, rectangle.y + padding, rectangle.width - 2 * padding,
				rectangle.height - 2 * padding);
	}

	/**
	 * @param rectangle a rectangle (e.g. the bounds of the shell)
	 * @return The monitor that contains the biggest portion of the rectangle or the
	 *         primary monitor if the rectangle is outside all monitors.
	 */
	private Monitor findMonitorThatContainsMostOf(Rectangle rectangle) {
		Monitor bestFittingMonitor = getDisplay().getPrimaryMonitor();
		int maxIntersectionArea = 0;

		for (Monitor monitor : getDisplay().getMonitors()) {
			Rectangle intersection = new Rectangle(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
			intersection.intersect(monitor.getBounds());

			int insersectionArea = intersection.width * intersection.height;
			if (insersectionArea > maxIntersectionArea) {
				bestFittingMonitor = monitor;
				maxIntersectionArea = insersectionArea;
			}
		}

		return bestFittingMonitor;
	}

	private static void moveIntoBounds(Rectangle rectangleToMove, Rectangle bounds) {
		// move into bounds if it's too far to the right
		if (rectangleToMove.x + rectangleToMove.width > bounds.x + bounds.width) {
			rectangleToMove.x = bounds.x + bounds.width - rectangleToMove.width;
		}

		// move into bounds if it's too far to the left
		if (rectangleToMove.x < bounds.x) {
			rectangleToMove.x = bounds.x;
		}

		// move into bounds if it's too far down
		if (rectangleToMove.y + rectangleToMove.height > bounds.y + bounds.height) {
			rectangleToMove.y = bounds.y + bounds.height - rectangleToMove.height;
		}

		// move into bounds if it's too far up
		if (rectangleToMove.y < bounds.y) {
			rectangleToMove.y = bounds.y;
		}
	}

	/*
	 * Returns the editor history.
	 */
	public EditorHistory getEditorHistory() {
		if (editorHistory == null) {
			editorHistory = new EditorHistory();
		}
		return editorHistory;
	}

	@Override
	public IEditorRegistry getEditorRegistry() {
		return WorkbenchPlugin.getDefault().getEditorRegistry();
	}

	@Override
	public IWorkbenchOperationSupport getOperationSupport() {
		return WorkbenchPlugin.getDefault().getOperationSupport();
	}

	@Override
	public IPerspectiveRegistry getPerspectiveRegistry() {
		return WorkbenchPlugin.getDefault().getPerspectiveRegistry();
	}

	@Override
	public PreferenceManager getPreferenceManager() {
		return WorkbenchPlugin.getDefault().getPreferenceManager();
	}

	@Override
	public IPreferenceStore getPreferenceStore() {
		return WorkbenchPlugin.getDefault().getPreferenceStore();
	}

	@Override
	public ISharedImages getSharedImages() {
		return WorkbenchPlugin.getDefault().getSharedImages();
	}

	@Override
	public int getWorkbenchWindowCount() {
		return getWorkbenchWindows().length;
	}

	@Override
	public IWorkbenchWindow[] getWorkbenchWindows() {
		List<IWorkbenchWindow> windows = new ArrayList<>();
		for (MWindow window : application.getChildren()) {
			IEclipseContext context = window.getContext();
			if (context != null) {
				IWorkbenchWindow wwindow = context.get(IWorkbenchWindow.class);
				if (wwindow != null) {
					windows.add(wwindow);
				}
			}
		}
		return windows.toArray(new IWorkbenchWindow[windows.size()]);
	}

	@Override
	public IWorkingSetManager getWorkingSetManager() {
		return WorkbenchPlugin.getDefault().getWorkingSetManager();
	}

	@Override
	public ILocalWorkingSetManager createLocalWorkingSetManager() {
		return new LocalWorkingSetManager(WorkbenchPlugin.getDefault().getBundleContext());
	}

	/**
	 * Initializes the workbench now that the display is created.
	 *
	 * @return true if init succeeded.
	 */
	private boolean init() {
		// setup debug mode if required.
		if (WorkbenchPlugin.getDefault().isDebugging()) {
			WorkbenchPlugin.DEBUG = true;
			ModalContext.setDebugMode(true);
		}

		// Set up the JFace preference store
		JFaceUtil.initializeJFacePreferences();

		// TODO Correctly order service initialization
		// there needs to be some serious consideration given to
		// the services, and hooking them up in the correct order
		e4Context.set("org.eclipse.core.runtime.Platform", Platform.class); //$NON-NLS-1$
		final EvaluationService evaluationService = new EvaluationService(e4Context);

		StartupThreading.runWithoutExceptions(new StartupRunnable() {

			@Override
			public void runWithException() {
				serviceLocator.registerService(IEvaluationService.class, evaluationService);
			}
		});

		initializeLazyServices();

		// Initialize the activity support.

		activityHelper = ActivityPersistanceHelper.getInstance();
		StartupThreading.runWithoutExceptions(new StartupRunnable() {

			@Override
			public void runWithException() {
				WorkbenchImages.getImageRegistry();
			}
		});
		initializeE4Services();
		IIntroRegistry introRegistry = WorkbenchPlugin.getDefault().getIntroRegistry();
		if (introRegistry.getIntroCount() > 0) {
			IProduct product = Platform.getProduct();
			if (product != null) {
				introDescriptor = (IntroDescriptor) introRegistry.getIntroForProduct(product.getId());
			}
		}
		initializeDefaultServices();
		initializeFonts();
		initializeApplicationColors();

		// now that the workbench is sufficiently initialized, let the advisor
		// have a turn.
		StartupThreading.runWithoutExceptions(new StartupRunnable() {

			@Override
			public void runWithException() {
				advisor.internalBasicInitialize(getWorkbenchConfigurer());
			}
		});

		// configure use of color icons in toolbars
		boolean useColorIcons = PrefUtil.getInternalPreferenceStore().getBoolean(IPreferenceConstants.COLOR_ICONS);
		ActionContributionItem.setUseColorIconsInToolbars(useColorIcons);

		// initialize workbench single-click vs double-click behavior
		initializeSingleClickOption();

		initializeGlobalization();
		initializeNLExtensions();

		initializeWorkbenchImages();

		// hook shortcut visualizer
		StartupThreading.runWithoutExceptions(new StartupRunnable() {
			@Override
			public void runWithException() {
				new ShowKeysListener(Workbench.this, PrefUtil.getInternalPreferenceStore());
			}
		});

		// attempt to restore a previous workbench state
		try {
			UIStats.start(UIStats.RESTORE_WORKBENCH, "Workbench"); //$NON-NLS-1$

			final boolean bail[] = new boolean[1];
			StartupThreading.runWithoutExceptions(new StartupRunnable() {

				@Override
				public void runWithException() throws Throwable {
					advisor.preStartup();
					// TODO compat: open the windows here/instantiate the model
					// TODO compat: instantiate the WW around the model
					initializationDone = true;
					if (isClosing() || !advisor.openWindows()) {
						// if (isClosing()) {
						bail[0] = true;
					}

					restoreWorkbenchState();
				}
			});

			if (bail[0])
				return false;

		} finally {
			UIStats.end(UIStats.RESTORE_WORKBENCH, this, "Workbench"); //$NON-NLS-1$
		}

		return true;
	}

	private void initializeWorkbenchImages() {
		StartupThreading.runWithoutExceptions(new StartupRunnable() {
			@Override
			public void runWithException() {
				WorkbenchImages.getDescriptors();
			}
		});
	}

	/**
	 * Establishes the relationship between JFace actions and the command manager.
	 */
	private void initializeCommandResolver() {
		ExternalActionManager.getInstance()
				.setCallback(new CommandCallback(bindingManager, commandManager,
						commandId -> workbenchActivitySupport.getActivityManager().getIdentifier(commandId).isEnabled(),
						action -> !(action instanceof CommandAction)));
	}

	/**
	 * Initialize colors defined by the new colorDefinitions extension point. Note
	 * this will be rolled into initializeColors() at some point.
	 *
	 * @since 3.0
	 */
	private void initializeApplicationColors() {
		StartupThreading.runWithoutExceptions(new StartupRunnable() {

			@Override
			public void runWithException() {
				ColorDefinition[] colorDefinitions = WorkbenchPlugin.getDefault().getThemeRegistry().getColors();
				ThemeElementHelper.populateRegistry(getThemeManager().getCurrentTheme(), colorDefinitions,
						PrefUtil.getInternalPreferenceStore());
			}
		});
	}

	void initializeSingleClickOption() {
		IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
		boolean openOnSingleClick = store.getBoolean(IPreferenceConstants.OPEN_ON_SINGLE_CLICK);
		boolean selectOnHover = store.getBoolean(IPreferenceConstants.SELECT_ON_HOVER);
		boolean openAfterDelay = store.getBoolean(IPreferenceConstants.OPEN_AFTER_DELAY);
		int singleClickMethod = openOnSingleClick ? OpenStrategy.SINGLE_CLICK : OpenStrategy.DOUBLE_CLICK;
		if (openOnSingleClick) {
			if (selectOnHover) {
				singleClickMethod |= OpenStrategy.SELECT_ON_HOVER;
			}
			if (openAfterDelay) {
				singleClickMethod |= OpenStrategy.ARROW_KEYS_OPEN;
			}
		}
		OpenStrategy.setOpenMethod(singleClickMethod);
	}

	private void initializeGlobalization() {
		IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();

		if (!store.isDefault(IPreferenceConstants.BIDI_SUPPORT)) {
			BidiUtils.setBidiSupport(store.getBoolean(IPreferenceConstants.BIDI_SUPPORT));
		}
		if (!store.isDefault(IPreferenceConstants.TEXT_DIRECTION)) {
			BidiUtils.setTextDirection(store.getString(IPreferenceConstants.TEXT_DIRECTION));
		}
	}

	private void initializeNLExtensions() {
		IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
		if (!store.isDefault(IPreferenceConstants.NL_EXTENSIONS)) {
			String nlExtensions = store.getString(IPreferenceConstants.NL_EXTENSIONS);
			ULocale.setDefault(Category.FORMAT,
					new ULocale(ULocale.getDefault(Category.FORMAT).getBaseName() + nlExtensions));
		}
	}

	/*
	 * Initializes the workbench fonts with the stored values.
	 */
	private void initializeFonts() {
		StartupThreading.runWithoutExceptions(new StartupRunnable() {

			@Override
			public void runWithException() {
				FontDefinition[] fontDefinitions = WorkbenchPlugin.getDefault().getThemeRegistry().getFonts();

				ThemeElementHelper.populateRegistry(getThemeManager().getCurrentTheme(), fontDefinitions,
						PrefUtil.getInternalPreferenceStore());
				final IPropertyChangeListener themeToPreferencesFontSynchronizer = event -> {
					if (event.getNewValue() instanceof FontData[]) {
						FontData[] fontData = (FontData[]) event.getNewValue();
						PrefUtil.getInternalPreferenceStore().setValue(event.getProperty(),
								PreferenceConverter.getStoredRepresentation(fontData));
					}
				};
				getThemeManager().getCurrentTheme().getFontRegistry().addListener(themeToPreferencesFontSynchronizer);
				getThemeManager().addPropertyChangeListener(event -> {
					if (IThemeManager.CHANGE_CURRENT_THEME.equals(event.getProperty())) {
						Object oldValue = event.getOldValue();
						if (oldValue != null && oldValue instanceof ITheme) {
							((ITheme) oldValue).removePropertyChangeListener(themeToPreferencesFontSynchronizer);
						}
						Object newValue = event.getNewValue();
						if (newValue != null && newValue instanceof ITheme) {
							((ITheme) newValue).addPropertyChangeListener(themeToPreferencesFontSynchronizer);
						}
					}
				});
			}
		});
	}

	/*
	 * Initialize the workbench images.
	 *
	 * @param windowImages An array of the descriptors of the images to be used in
	 * the corner of each window, or <code>null</code> if none. It is expected that
	 * the array will contain the same icon, rendered at different sizes.
	 *
	 * @since 3.0
	 */
	private static void initializeImages() {
		ImageDescriptor[] windowImages = WorkbenchPlugin.getDefault().getWindowImages();
		if (windowImages == null) {
			return;
		}

		Image[] images = new Image[windowImages.length];
		for (int i = 0; i < windowImages.length; ++i) {
			images[i] = windowImages[i].createImage();
		}
		Window.setDefaultImages(images);
	}

	/*
	 * Take the workbenches' images out of the shared registry.
	 *
	 * @since 3.0
	 */
	private void uninitializeImages() {
		WorkbenchImages.dispose();
		Image[] images = Window.getDefaultImages();
		Window.setDefaultImage(null);
		for (Image image : images) {
			image.dispose();
		}
	}

	@Override
	public boolean isClosing() {
		return isClosing;
	}

	private void initializeE4Services() {
		eventBroker.subscribe(UIEvents.ElementContainer.TOPIC_CHILDREN, event -> {
			if (application == event.getProperty(UIEvents.EventTags.ELEMENT)) {
				if (UIEvents.isREMOVE(event)) {
					for (Object removed : UIEvents.asIterable(event, UIEvents.EventTags.OLD_VALUE)) {
						MWindow window = (MWindow) removed;
						IEclipseContext windowContext = window.getContext();
						if (windowContext != null) {
							IWorkbenchWindow wwindow = windowContext.get(IWorkbenchWindow.class);
							if (wwindow != null) {
								fireWindowClosed(wwindow);
							}
						}
					}
				}
			}
		});
		eventBroker.subscribe(UIEvents.ElementContainer.TOPIC_SELECTEDELEMENT, event -> {
			if (application == event.getProperty(UIEvents.EventTags.ELEMENT)) {
				if (UIEvents.EventTypes.SET.equals(event.getProperty(UIEvents.EventTags.TYPE))) {
					MWindow window = (MWindow) event.getProperty(UIEvents.EventTags.NEW_VALUE);
					if (window != null) {
						IWorkbenchWindow wwindow = window.getContext().get(IWorkbenchWindow.class);
						if (wwindow != null) {
							e4Context.set(ISources.ACTIVE_WORKBENCH_WINDOW_NAME, wwindow);
							e4Context.set(ISources.ACTIVE_WORKBENCH_WINDOW_SHELL_NAME, wwindow.getShell());
						}
					}
				}
			}
		});

		// watch for parts' "toBeRendered" attribute being flipped to true, if
		// they need to be rendered, then they need a corresponding 3.x
		// reference
		eventBroker.subscribe(UIEvents.UIElement.TOPIC_TOBERENDERED, event -> {
			if (Boolean.TRUE.equals(event.getProperty(UIEvents.EventTags.NEW_VALUE))) {
				Object element = event.getProperty(UIEvents.EventTags.ELEMENT);
				if (element instanceof MPart) {
					MPart part = (MPart) element;
					createReference(part);
				}
			}
		});

		// watch for parts' contexts being set, once they've been set, we need
		// to inject the ViewReference/EditorReference into the context
		eventBroker.subscribe(UIEvents.Context.TOPIC_CONTEXT, event -> {
			Object element = event.getProperty(UIEvents.EventTags.ELEMENT);
			if (element instanceof MPart) {
				MPart part = (MPart) element;
				IEclipseContext context = part.getContext();
				if (context != null) {
					setReference(part, context);
				}
			}
		});

		eventBroker.subscribe(UIEvents.ElementContainer.TOPIC_CHILDREN, event -> {
			Object element = event.getProperty(UIEvents.EventTags.ELEMENT);
			if (!(element instanceof MApplication)) {
				return;
			}
			MApplication app = (MApplication) element;
			if (UIEvents.isREMOVE(event)) {
				if (app.getChildren().isEmpty()) {
					Object oldValue = event.getProperty(UIEvents.EventTags.OLD_VALUE);
					WorkbenchPlugin.log("The final top level window " + oldValue //$NON-NLS-1$
							+ " was just removed", new Exception()); //$NON-NLS-1$
				}
			}
		});

		eventBroker.subscribe(UIEvents.UIModelTopicBase + "/*", event -> { // //$NON-NLS-1$
			applicationModelChanged = true;
		});

		boolean found = false;
		List<MPartDescriptor> currentDescriptors = application.getDescriptors();
		for (MPartDescriptor desc : currentDescriptors) {
			// do we have a matching descriptor?
			if (desc.getElementId().equals(CompatibilityEditor.MODEL_ELEMENT_ID)) {
				// In older versions of the workbench, REMOVE_ON_HIDE_TAG was not set on the
				// descriptor. For migration, ensure that it is set on any model, Bug 527689.
				desc.getTags().add(EPartService.REMOVE_ON_HIDE_TAG);
				found = true;
				break;
			}
		}
		if (!found) {
			EModelService modelService = e4Context.get(EModelService.class);
			MPartDescriptor descriptor = modelService.createModelElement(MPartDescriptor.class);
			descriptor.getTags().add(EDITOR_TAG);
			descriptor.getTags().add(EPartService.REMOVE_ON_HIDE_TAG);
			descriptor.setCloseable(true);
			descriptor.setAllowMultiple(true);
			descriptor.setElementId(CompatibilityEditor.MODEL_ELEMENT_ID);
			descriptor.setContributionURI(CompatibilityPart.COMPATIBILITY_EDITOR_URI);
			descriptor.setCategory("org.eclipse.e4.primaryDataStack"); //$NON-NLS-1$
			application.getDescriptors().add(descriptor);
		}

	}

	/**
	 * Returns a workbench page that will contain the specified part. If no page can
	 * be located, one will be instantiated.
	 *
	 * @param part the model part to query a parent workbench page for
	 * @return the workbench page that contains the specified part
	 */
	private WorkbenchPage getWorkbenchPage(MPart part) {
		IEclipseContext context = getWindowContext(part);
		WorkbenchPage page = (WorkbenchPage) context.get(IWorkbenchPage.class);
		if (page == null) {
			MWindow window = context.get(MWindow.class);
			Workbench workbench = (Workbench) PlatformUI.getWorkbench();
			workbench.openWorkbenchWindow(getDefaultPageInput(),
					getPerspectiveRegistry().findPerspectiveWithId(getDefaultPerspectiveId()), window, false);
			page = (WorkbenchPage) context.get(IWorkbenchPage.class);
		}
		return page;
	}

	/**
	 * Sets the 3.x reference of the specified part into its context.
	 *
	 * @param part    the model part that requires a 3.x part reference
	 * @param context the part's context
	 */
	private void setReference(MPart part, IEclipseContext context) {
		String uri = part.getContributionURI();
		if (CompatibilityPart.COMPATIBILITY_EDITOR_URI.equals(uri)) {
			WorkbenchPage page = getWorkbenchPage(part);
			EditorReference ref = page.getEditorReference(part);
			if (ref == null) {
				// If this editor was cloned from an existing editor (as
				// part of a split...) then re-create a valid EditorReference
				// from the existing editor's ref.
				MPart clonedFrom = (MPart) part.getTransientData().get(EModelService.CLONED_FROM_KEY);
				if (clonedFrom != null && clonedFrom.getContext() != null) {
					EditorReference originalRef = page.getEditorReference(clonedFrom);
					if (originalRef != null) {
						IEditorInput partInput = null;
						String editorId = originalRef.getDescriptor().getId();
						try {
							partInput = originalRef.getEditorInput();
						} catch (PartInitException e) {
							System.out.println("Ooops !!!"); //$NON-NLS-1$
						}
						ref = page.createEditorReferenceForPart(part, partInput, editorId, null);
					}
				}

				// Fallback code
				if (ref == null) {
					ref = createEditorReference(part, page);
				}
			}
			context.set(EditorReference.class, ref);
		} else {
			// Create View References for 'e4' parts as well
			WorkbenchPage page = getWorkbenchPage(part);
			ViewReference ref = page.getViewReference(part);
			if (ref == null) {
				ref = createViewReference(part, page);
			}
			context.set(ViewReference.class, ref);
		}
	}

	private ViewReference createViewReference(MPart part, WorkbenchPage page) {
		WorkbenchWindow window = (WorkbenchWindow) page.getWorkbenchWindow();

		// If the partId contains a ':' then only use the substring before it to
		// fine the descriptor
		String partId = part.getElementId();

		// If the id contains a ':' use the part before it as the descriptor id
		int colonIndex = partId.indexOf(':');
		String descId = colonIndex == -1 ? partId : partId.substring(0, colonIndex);

		IViewDescriptor desc = window.getWorkbench().getViewRegistry().find(descId);
		ViewReference ref = new ViewReference(window.getModel().getContext(), page, part, (ViewDescriptor) desc);
		page.addViewReference(ref);
		return ref;
	}

	private EditorReference createEditorReference(MPart part, WorkbenchPage page) {
		WorkbenchWindow window = (WorkbenchWindow) page.getWorkbenchWindow();
		EditorReference ref = new EditorReference(window.getModel().getContext(), page, part, null, null, null);
		page.addEditorReference(ref);
		return ref;
	}

	/**
	 * Creates a workbench part reference for the specified part if one does not
	 * already exist.
	 *
	 * @param part the model part to create a 3.x part reference for
	 */
	private void createReference(MPart part) {
		String uri = part.getContributionURI();
		if (CompatibilityPart.COMPATIBILITY_VIEW_URI.equals(uri)) {
			WorkbenchPage page = getWorkbenchPage(part);
			ViewReference ref = page.getViewReference(part);
			if (ref == null) {
				createViewReference(part, page);
			}
		} else if (CompatibilityPart.COMPATIBILITY_EDITOR_URI.equals(uri)) {
			WorkbenchPage page = getWorkbenchPage(part);
			EditorReference ref = page.getEditorReference(part);
			if (ref == null) {
				createEditorReference(part, page);
			}
		}
	}

	private IEclipseContext getWindowContext(MPart part) {
		MElementContainer<?> parent = (MElementContainer<?>) ((EObject) part).eContainer();
		while (!(parent instanceof MWindow)) {
			parent = (MElementContainer<?>) ((EObject) parent).eContainer(); // parent.getParent();
		}

		return ((MWindow) parent).getContext();
	}

	private void initializeLazyServices() {
		e4Context.set(IWorkbenchActivitySupport.class.getName(), new ContextFunction() {

			@Override
			public Object compute(IEclipseContext context, String contextKey) {
				if (workbenchActivitySupport == null) {
					workbenchActivitySupport = new WorkbenchActivitySupport();
				}
				return workbenchActivitySupport;
			}
		});
		e4Context.set(IProgressService.class.getName(), new ContextFunction() {
			@Override
			public Object compute(IEclipseContext context, String contextKey) {
				return ProgressManager.getInstance();
			}
		});
		WorkbenchPlugin.getDefault().initializeContext(e4Context);
	}

	private ArrayList<MCommand> commandsToRemove = new ArrayList<>();
	private ArrayList<MCategory> categoriesToRemove = new ArrayList<>();

	private CommandService initializeCommandService(IEclipseContext appContext) {
		CommandService service = new CommandService(commandManager, appContext);
		appContext.set(ICommandService.class, service);
		appContext.set(IUpdateService.class, service);
		service.readRegistry();

		return service;
	}

	private Map<String, MBindingContext> bindingContexts = new HashMap<>();

	public MBindingContext getBindingContext(String id) {
		// cache
		MBindingContext result = bindingContexts.get(id);
		if (result == null) {
			// search
			result = searchContexts(id, application.getRootContext());
			if (result == null) {
				// create
				result = MCommandsFactory.INSTANCE.createBindingContext();
				result.setElementId(id);
				result.setName("Auto::" + id); //$NON-NLS-1$
				application.getRootContext().add(result);
			}
			if (result != null) {
				bindingContexts.put(id, result);
			}
		}
		return result;
	}

	private MBindingContext searchContexts(String id, List<MBindingContext> rootContext) {
		for (MBindingContext context : rootContext) {
			if (context.getElementId().equals(id)) {
				return context;
			}
			MBindingContext result = searchContexts(id, context.getChildren());
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	private void defineBindingTable(String id) {
		List<MBindingTable> bindingTables = application.getBindingTables();
		if (contains(bindingTables, id)) {
			return;
		}
		if (WorkbenchPlugin.getDefault().isDebugging()) {
			WorkbenchPlugin.log("Defining a binding table: " + id); //$NON-NLS-1$
		}
		MBindingTable bt = CommandsFactoryImpl.eINSTANCE.createBindingTable();
		bt.setBindingContext(getBindingContext(id));
		bindingTables.add(bt);
	}

	/**
	 * @return true if this BT already exists
	 */
	private boolean contains(List<MBindingTable> bindingTables, String id) {
		for (MBindingTable bt : bindingTables) {
			if (id.equals(bt.getBindingContext().getElementId())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Initializes all of the default services for the workbench. For initializing
	 * the command-based services, this also parses the registry and hooks up all
	 * the required listeners.
	 */
	private void initializeDefaultServices() {

		final IContributionService contributionService = new ContributionService(getAdvisor());
		serviceLocator.registerService(IContributionService.class, contributionService);

		// TODO Correctly order service initialization
		// there needs to be some serious consideration given to
		// the services, and hooking them up in the correct order
		final IEvaluationService evaluationService = serviceLocator.getService(IEvaluationService.class);

		StartupThreading.runWithoutExceptions(new StartupRunnable() {

			@Override
			public void runWithException() {
				serviceLocator.registerService(ISaveablesLifecycleListener.class, new SaveablesList());
			}
		});

		/*
		 * Phase 1 of the initialization of commands. When this phase completes, all the
		 * services and managers will exist, and be accessible via the
		 * getService(Object) method.
		 */
		StartupThreading.runWithoutExceptions(new StartupRunnable() {

			@Override
			public void runWithException() {
				Command.DEBUG_COMMAND_EXECUTION = Policy.DEBUG_COMMANDS;
				commandManager = e4Context.get(CommandManager.class);
			}
		});

		final CommandService[] commandService = new CommandService[1];
		StartupThreading.runWithoutExceptions(new StartupRunnable() {

			@Override
			public void runWithException() {
				commandService[0] = initializeCommandService(e4Context);

			}
		});

		StartupThreading.runWithoutExceptions(new StartupRunnable() {

			@Override
			public void runWithException() {
				ContextManager.DEBUG = Policy.DEBUG_CONTEXTS;
				contextManager = e4Context.get(ContextManager.class);
			}
		});

		IContextService cxs = ContextInjectionFactory.make(ContextService.class, e4Context);

		final IContextService contextService = cxs;

		StartupThreading.runWithoutExceptions(new StartupRunnable() {

			@Override
			public void runWithException() {
				contextManager.addContextManagerListener(contextManagerEvent -> {
					if (contextManagerEvent.isContextChanged()) {
						String id = contextManagerEvent.getContextId();
						if (id != null) {
							defineBindingTable(id);
						}
					}
				});
				EContextService ecs = e4Context.get(EContextService.class);
				ecs.activateContext(IContextService.CONTEXT_ID_DIALOG_AND_WINDOW);
			}
		});

		serviceLocator.registerService(IContextService.class, contextService);

		final IBindingService[] bindingService = new BindingService[1];

		StartupThreading.runWithoutExceptions(new StartupRunnable() {

			@Override
			public void runWithException() {
				BindingManager.DEBUG = Policy.DEBUG_KEY_BINDINGS;
				bindingManager = e4Context.get(BindingManager.class);
				bindingService[0] = ContextInjectionFactory.make(BindingService.class, e4Context);
			}
		});

		// bindingService[0].readRegistryAndPreferences(commandService[0]);
		serviceLocator.registerService(IBindingService.class, bindingService[0]);

		final CommandImageManager commandImageManager = new CommandImageManager();
		final CommandImageService commandImageService = new CommandImageService(commandImageManager, commandService[0]);
		commandImageService.readRegistry();
		serviceLocator.registerService(ICommandImageService.class, commandImageService);

		final WorkbenchMenuService menuService = new WorkbenchMenuService(serviceLocator, e4Context);

		serviceLocator.registerService(IMenuService.class, menuService);
		// the service must be registered before it is initialized - its
		// initialization uses the service locator to address a dependency on
		// the menu service
		StartupThreading.runWithoutExceptions(new StartupRunnable() {

			@Override
			public void runWithException() {
				menuService.readRegistry();
			}
		});

		/*
		 * Phase 2 of the initialization of commands. The source providers that the
		 * workbench provides are creating and registered with the above services. These
		 * source providers notify the services when particular pieces of workbench
		 * state change.
		 */
		final SourceProviderService sourceProviderService = new SourceProviderService(serviceLocator);
		serviceLocator.registerService(ISourceProviderService.class, sourceProviderService);
		StartupThreading.runWithoutExceptions(new StartupRunnable() {

			@Override
			public void runWithException() {
				// this currently instantiates all players ... sigh
				sourceProviderService.readRegistry();
				ISourceProvider[] sourceproviders = sourceProviderService.getSourceProviders();
				for (ISourceProvider sp : sourceproviders) {
					evaluationService.addSourceProvider(sp);
					if (!(sp instanceof ActiveContextSourceProvider)) {
						contextService.addSourceProvider(sp);
					}
				}
			}
		});

		StartupThreading.runWithoutExceptions(new StartupRunnable() {

			@Override
			public void runWithException() {
				// these guys are need to provide the variables they say
				// they source

				FocusControlSourceProvider focusControl = (FocusControlSourceProvider) sourceProviderService
						.getSourceProvider(ISources.ACTIVE_FOCUS_CONTROL_ID_NAME);
				serviceLocator.registerService(IFocusService.class, focusControl);

				menuSourceProvider = (MenuSourceProvider) sourceProviderService
						.getSourceProvider(ISources.ACTIVE_MENU_NAME);
			}
		});

		/*
		 * Phase 3 of the initialization of commands. This handles the creation of
		 * wrappers for legacy APIs. By the time this phase completes, any code trying
		 * to access commands through legacy APIs should work.
		 */
		final IHandlerService[] handlerService = new IHandlerService[1];
		StartupThreading.runWithoutExceptions(new StartupRunnable() {

			@Override
			public void runWithException() {
				handlerService[0] = new LegacyHandlerService(e4Context);
				e4Context.set(IHandlerService.class, handlerService[0]);
				handlerService[0].readRegistry();
			}
		});
		workbenchContextSupport = new WorkbenchContextSupport(this, contextManager);
		initializeCommandResolver();

		bindingManager.addBindingManagerListener(bindingManagerListener);

		serviceLocator.registerService(ISelectionConversionService.class, new SelectionConversionService());

		backForwardListener = createBackForwardListener();
		StartupThreading.runWithoutExceptions(new StartupRunnable() {
			@Override
			public void runWithException() {
				getDisplay().addFilter(SWT.MouseDown, backForwardListener);
			}
		});
	}

	private Listener createBackForwardListener() {
		return event -> {
			String commandId;
			switch (event.button) {
			case 4:
			case 8:
				commandId = IWorkbenchCommandConstants.NAVIGATE_BACKWARD_HISTORY;
				break;
			case 5:
			case 9:
				commandId = IWorkbenchCommandConstants.NAVIGATE_FORWARD_HISTORY;
				break;
			default:
				return;
			}

			final IHandlerService handlerService = getService(IHandlerService.class);

			try {
				handlerService.executeCommand(commandId, event);
				event.doit = false;
			} catch (NotDefinedException | NotEnabledException | NotHandledException e3) {
				// regular condition; do nothing
			} catch (ExecutionException ex) {
				StatusUtil.handleStatus(ex, StatusManager.SHOW | StatusManager.LOG);
			}
		};
	}

	/**
	 * Returns true if the Workbench is in the process of starting.
	 *
	 * @return <code>true</code> if the Workbench is starting, but not yet running
	 *         the event loop.
	 */
	@Override
	public boolean isStarting() {
		return isStarting && isRunning();
	}

	/**
	 * Opens the initial workbench window.
	 */
	/* package */void openFirstTimeWindow() {
		boolean showProgress = PrefUtil.getAPIPreferenceStore()
				.getBoolean(IWorkbenchPreferenceConstants.SHOW_PROGRESS_ON_STARTUP);

		if (!showProgress) {
			doOpenFirstTimeWindow();
		} else {
			// We don't know how many plug-ins will be loaded,
			// assume we are loading a tenth of the installed plug-ins.
			// (The Eclipse SDK loads 7 of 86 plug-ins at startup as of
			// 2005-5-20)
			runStartupWithProgress(this::doOpenFirstTimeWindow);
		}
	}

	private void doOpenFirstTimeWindow() {
		try {
			final IAdaptable input[] = new IAdaptable[1];
			StartupThreading.runWithoutExceptions(new StartupRunnable() {

				@Override
				public void runWithException() throws Throwable {
					input[0] = getDefaultPageInput();
				}
			});

			openWorkbenchWindow(getDefaultPerspectiveId(), input[0]);
		} catch (final WorkbenchException e) {
			// Don't use the window's shell as the dialog parent,
			// as the window is not open yet (bug 76724).
			StartupThreading.runWithoutExceptions(new StartupRunnable() {

				@Override
				public void runWithException() throws Throwable {
					ErrorDialog.openError(null, WorkbenchMessages.Problems_Opening_Page, e.getMessage(), e.getStatus());
				}
			});
		}
	}

	private void runStartupWithProgress(final Runnable runnable) {

		AbstractSplashHandler handler = getSplash();
		IProgressMonitor progressMonitor = null;
		if (handler != null) {
			progressMonitor = handler.getBundleProgressMonitor();
		}

		if (progressMonitor == null) {
			// cannot report progress (e.g. if the splash screen is not showing)
			// fall back to starting without showing progress.
			runnable.run();
		} else {
			ServiceListener serviceListener = new StartupProgressBundleListener(progressMonitor, display);
			WorkbenchPlugin.getDefault().getBundleContext().addServiceListener(serviceListener);
			try {
				runnable.run();
			} finally {
				WorkbenchPlugin.getDefault().getBundleContext().removeServiceListener(serviceListener);
			}
		}
	}

	@Override
	public IWorkbenchWindow openWorkbenchWindow(IAdaptable input) throws WorkbenchException {
		return openWorkbenchWindow(getDefaultPerspectiveId(), input);
	}

	@Override
	public IWorkbenchWindow openWorkbenchWindow(String perspectiveId, IAdaptable input) throws WorkbenchException {
		IPerspectiveDescriptor descriptor = getPerspectiveRegistry().findPerspectiveWithId(perspectiveId);
		try {
			MWindow window = BasicFactoryImpl.eINSTANCE.createTrimmedWindow();
			return openWorkbenchWindow(input, descriptor, window, true);
		} catch (InjectionException e) {
			throw new WorkbenchException(e.getMessage(), e);
		}
	}

	public WorkbenchWindow openWorkbenchWindow(IAdaptable input, IPerspectiveDescriptor descriptor, MWindow window,
			boolean newWindow) {
		return (WorkbenchWindow) createWorkbenchWindow(input, descriptor, window, newWindow);
	}

	@Override
	public boolean restart() {
		if (!E4Workbench.canRestart()) {
			informNoRestart();
			return false;
		}

		return close(PlatformUI.RETURN_RESTART, false);
	}

	@Override
	public boolean restart(boolean useCurrrentWorkspace) {
		if (!E4Workbench.canRestart()) {
			informNoRestart();
			return false;
		}

		if (Platform.inDevelopmentMode()) {
			// In development mode, command line parameters cannot be changed and restart
			// will always be EXIT_RESTART. Also see setRestartArguments method
			System.setProperty(PROP_EXIT_CODE, IApplication.EXIT_RESTART.toString());
		} else if (useCurrrentWorkspace) {
			URL instanceUrl = Platform.getInstanceLocation().getURL();
			if (instanceUrl != null) {
				try {
					URI uri = instanceUrl.toURI();
					String command_line = buildCommandLine(uri.toString());
					if (command_line != null) {
						System.setProperty(PROP_EXIT_CODE, IApplication.EXIT_RELAUNCH.toString());
						System.setProperty(IApplicationContext.EXIT_DATA_PROPERTY, command_line);
					}
				} catch (URISyntaxException e) {
					// do nothing; workbench will be restarted with the same
					// command line as used for the previous launch
				}
			}
		}
		return close(PlatformUI.RETURN_RESTART, false);
	}

	/**
	 * Inform the user that the workbench can't be restarted. If the workbench UI is
	 * not available, this method does nothing.
	 */
	private void informNoRestart() {
		final Display display = getDisplay();
		if (display != null && !display.isDisposed()) {
			MessageDialog.openError(null, WorkbenchMessages.Workbench_CantRestart_Title,
					WorkbenchMessages.Workbench_CantRestart_Message);
		}
	}

	/**
	 * Create and return a string with command line options for eclipse.exe that
	 * will launch a new workbench that is the same as the currently running one,
	 * but using the argument directory as its workspace.
	 *
	 * @param workspace the directory to use as the new workspace
	 * @return a string of command line options or <code>null</code> if 'eclipse.vm'
	 *         is not set
	 */
	private static String buildCommandLine(String workspace) {
		StringBuilder result = new StringBuilder(512);

		String userData = System.getProperty(IApplicationContext.EXIT_DATA_PROPERTY);
		if (userData != null && !userData.isBlank())
			result.append(userData);

		result.append(CMD_DATA);
		result.append('\n');
		result.append(workspace);
		result.append('\n');

		return result.toString();
	}

	/**
	 * Sets the arguments required to restart the workbench using the specified path
	 * as the workspace location.
	 *
	 * @param workspacePath the new workspace location
	 * @return {@link IApplication#EXIT_OK} or {@link IApplication#EXIT_RELAUNCH}
	 */
	@SuppressWarnings("restriction")
	public static Object setRestartArguments(String workspacePath) {
		if (Platform.inDevelopmentMode()
				&& !Platform.getInstanceLocation().getURL().equals(LocationHelper.buildURL(workspacePath, true))) {
			MessageDialog.openError(null, WorkbenchMessages.Workbench_problemsRestartErrorTitle,
					WorkbenchMessages.Workbench_problemsRestartErrorMessage);
			return null;
		}

		String command_line = Workbench.buildCommandLine(workspacePath);
		if (command_line == null) {
			return IApplication.EXIT_OK;
		}

		System.setProperty(Workbench.PROP_EXIT_CODE, IApplication.EXIT_RELAUNCH.toString());
		System.setProperty(IApplicationContext.EXIT_DATA_PROPERTY, command_line);
		return IApplication.EXIT_RELAUNCH;
	}

	/**
	 * Returns the ids of all plug-ins that extend the
	 * <code>org.eclipse.ui.startup</code> extension point.
	 *
	 * @return the ids of all plug-ins containing 1 or more startup extensions
	 */
	public ContributionInfo[] getEarlyActivatedPlugins() {
		IExtensionPoint point = registry.getExtensionPoint(PlatformUI.PLUGIN_ID,
				IWorkbenchRegistryConstants.PL_STARTUP);
		IExtension[] extensions = point.getExtensions();
		ArrayList<String> pluginIds = new ArrayList<>(extensions.length);
		for (IExtension extension : extensions) {
			String id = extension.getContributor().getName();
			if (!pluginIds.contains(id)) {
				pluginIds.add(id);
			}
		}
		ContributionInfo[] result = new ContributionInfo[pluginIds.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = new ContributionInfo(pluginIds.get(i),
					ContributionInfoMessages.ContributionInfo_EarlyStartupPlugin, null);

		}
		return result;
	}

	/**
	 * Returns the ids of the early activated plug-ins that have been disabled by
	 * the user.
	 *
	 * @return the ids of the early activated plug-ins that have been disabled by
	 *         the user
	 */
	public String[] getDisabledEarlyActivatedPlugins() {
		String pref = PrefUtil.getInternalPreferenceStore()
				.getString(IPreferenceConstants.PLUGINS_NOT_ACTIVATED_ON_STARTUP);
		return pref.split(";"); //$NON-NLS-1$
	}

	/*
	 * Starts all plugins that extend the <code> org.eclipse.ui.startup </code>
	 * extension point, and that the user has not disabled via the preference page.
	 */
	private void startPlugins() {

		Job job = new Job("Executing the early startup extensions") { //$NON-NLS-1$
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				// bug 55901: don't use getConfigElements directly, for pre-3.0
				// compat, make sure to allow both missing class
				// attribute and a missing startup element
				IExtensionPoint point = registry.getExtensionPoint(PlatformUI.PLUGIN_ID,
						IWorkbenchRegistryConstants.PL_STARTUP);

				IExtension[] extensions = point.getExtensions();
				if (extensions.length == 0) {
					return Status.OK_STATUS;
				}
				HashSet<String> disabledPlugins = new HashSet<>(Arrays.asList(getDisabledEarlyActivatedPlugins()));
				SubMonitor subMonitor = SubMonitor.convert(monitor, WorkbenchMessages.Workbench_startingPlugins,
						extensions.length);
				for (IExtension extension : extensions) {
					if (subMonitor.isCanceled() || !isRunning()) {
						return Status.CANCEL_STATUS;
					}

					// if the plugin is not in the set of disabled plugins, then
					// execute the code to start it
					if (!disabledPlugins.contains(extension.getContributor().getName())) {
						subMonitor.setTaskName(extension.getContributor().getName());
						SafeRunner.run(new EarlyStartupRunnable(extension));
					}
					subMonitor.worked(1);
				}
				return Status.OK_STATUS;
			}

			@Override
			public boolean belongsTo(Object family) {
				return EARLY_STARTUP_FAMILY.equals(family);
			}
		};
		job.setSystem(true);
		job.schedule();
	}

	/**
	 * Disable the Workbench Auto-Save job on startup during tests.
	 *
	 * @param b <code>false</code> to disable the tests.
	 */
	public void setEnableAutoSave(boolean b) {
		workbenchAutoSave = b;
	}

	/**
	 * Internal method for running the workbench UI. This entails processing and
	 * dispatching events until the workbench is closed or restarted.
	 *
	 * @return return code {@link PlatformUI#RETURN_OK RETURN_OK}for normal exit;
	 *         {@link PlatformUI#RETURN_RESTART RETURN_RESTART}if the workbench was
	 *         terminated with a call to {@link IWorkbench#restart
	 *         IWorkbench.restart}; {@link PlatformUI#RETURN_UNSTARTABLE
	 *         RETURN_UNSTARTABLE}if the workbench could not be started; other
	 *         values reserved for future use
	 * @since 3.0
	 */
	private int runUI() {
		UIStats.start(UIStats.START_WORKBENCH, "Workbench"); //$NON-NLS-1$

		// deadlock code
		boolean avoidDeadlock = true;

		String[] commandLineArgs = Platform.getCommandLineArgs();
		for (String commandLineArg : commandLineArgs) {
			if (commandLineArg.equalsIgnoreCase("-allowDeadlock")) { //$NON-NLS-1$
				avoidDeadlock = false;
			}
		}

		final UISynchronizer synchronizer;

		if (avoidDeadlock) {
			UILockListener uiLockListener = new UILockListener(display);
			Job.getJobManager().setLockListener(uiLockListener);
			synchronizer = new UISynchronizer(display, uiLockListener);
			display.setSynchronizer(synchronizer);
			// declare the main thread to be a startup thread.
			UISynchronizer.startupThread.set(Boolean.TRUE);
		} else
			synchronizer = null;

		// ModalContext should not spin the event loop (there is no UI yet to block)
		ModalContext.setAllowReadAndDispatch(false);

		// if the -debug command line argument is used and the event loop is being
		// run while starting the Workbench, log a warning.
		if (WorkbenchPlugin.getDefault().isDebugging()) {
			display.asyncExec(() -> {
				if (isStarting()) {
					WorkbenchPlugin.log(StatusUtil.newStatus(IStatus.WARNING,
							"Event loop should not be run while the Workbench is starting.", //$NON-NLS-1$
							new RuntimeException()));
				}
			});
		}

		Listener closeListener = event -> event.doit = close();

		// Initialize an exception handler.
		Window.IExceptionHandler handler = ExceptionHandler.getInstance();

		try {
			// react to display close event by closing workbench nicely
			display.addListener(SWT.Close, closeListener);

			// install backstop to catch exceptions thrown out of event loop
			Window.setExceptionHandler(handler);

			final boolean[] initOK = new boolean[1];

			// initialize workbench and restore or open one window
			initOK[0] = init();

			if (initOK[0] && runEventLoop) {
				// Same registration as in E4Workbench
				Hashtable<String, Object> properties = new Hashtable<>();
				properties.put("id", getId()); //$NON-NLS-1$

				workbenchService = WorkbenchPlugin.getDefault().getBundleContext()
						.registerService(IWorkbench.class.getName(), this, properties);

				e4WorkbenchService = WorkbenchPlugin.getDefault().getBundleContext()
						.registerService(org.eclipse.e4.ui.workbench.IWorkbench.class.getName(), this, properties);

				Runnable earlyStartup = () -> {
					// Let the advisor run its start-up code.
					advisor.postStartup(); // May trigger a close/restart.
					// start eager plug-ins
					startPlugins();
					addStartupRegistryListener();
				};
				e4Context.set(PartRenderingEngine.EARLY_STARTUP_HOOK, earlyStartup);
				// start workspace auto-save
				final int millisecondInterval = getAutoSaveJobTime();
				if (millisecondInterval > 0 && workbenchAutoSave) {
					autoSaveJob = new WorkbenchJob(WORKBENCH_AUTO_SAVE_JOB) {
						@Override
						public IStatus runInUIThread(IProgressMonitor monitor) {
							if (monitor.isCanceled()) {
								return Status.CANCEL_STATUS;
							}
							final int nextDelay = getAutoSaveJobTime();
							try {
								if (applicationModelChanged) {
									persist(false);
									applicationModelChanged = false;

								}
								monitor.done();
							} finally {
								// repeat
								if (nextDelay > 0 && workbenchAutoSave) {
									this.schedule(nextDelay);
								}
							}
							return Status.OK_STATUS;
						}

					};
					autoSaveJob.setSystem(true);
					autoSaveJob.schedule(millisecondInterval);
				}

				display.asyncExec(new Runnable() {
					@Override
					public void run() {
						UIStats.end(UIStats.START_WORKBENCH, this, "Workbench"); //$NON-NLS-1$
						UIStats.startupComplete();
					}
				});

				getWorkbenchTestable().init(display, this);

				// allow ModalContext to spin the event loop
				ModalContext.setAllowReadAndDispatch(true);
				isStarting = false;

				if (synchronizer != null)
					synchronizer.started();
			}
			returnCode = PlatformUI.RETURN_OK;
			if (!initOK[0]) {
				returnCode = PlatformUI.RETURN_UNSTARTABLE;
			}
		} catch (final Exception e) {
			if (!display.isDisposed()) {
				handler.handleException(e);
			} else {
				String msg = "Exception in Workbench.runUI after display was disposed"; //$NON-NLS-1$
				WorkbenchPlugin.log(msg, new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH, 1, msg, e));
			}
		}

		// restart or exit based on returnCode
		return returnCode;
	}

	private int getAutoSaveJobTime() {
		int minuteSaveInterval = getPreferenceStore().getInt(IPreferenceConstants.WORKBENCH_SAVE_INTERVAL);
		return minuteSaveInterval * 60 * 1000;
	}

	@Override
	public IWorkbenchPage showPerspective(String perspectiveId, IWorkbenchWindow window) throws WorkbenchException {
		return showPerspective(perspectiveId, window, advisor.getDefaultPageInput());
	}

	private boolean activate(String perspectiveId, IWorkbenchPage page, IAdaptable input) {
		if (page != null) {
			for (IPerspectiveDescriptor openedPerspective : page.getOpenPerspectives()) {
				if (openedPerspective.getId().equals(perspectiveId)) {
					if (page.getInput() == input) {
						WorkbenchWindow wwindow = (WorkbenchWindow) page.getWorkbenchWindow();
						MWindow model = wwindow.getModel();
						application.setSelectedElement(model);
						page.setPerspective(openedPerspective);
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public IWorkbenchPage showPerspective(String perspectiveId, IWorkbenchWindow targetWindow, IAdaptable input)
			throws WorkbenchException {
		Assert.isNotNull(perspectiveId);
		final Object[] ret = new Object[1];
		BusyIndicator.showWhile(null, () -> {
			try {
				ret[0] = busyShowPerspective(perspectiveId, targetWindow, input);
			} catch (WorkbenchException e) {
				ret[0] = e;
			}
		});
		if (ret[0] instanceof IWorkbenchPage) {
			return (IWorkbenchPage) ret[0];
		} else if (ret[0] instanceof WorkbenchException) {
			throw ((WorkbenchException) ret[0]);
		} else {
			throw new WorkbenchException(WorkbenchMessages.WorkbenchPage_AbnormalWorkbenchCondition);
		}
	}

	private IWorkbenchPage busyShowPerspective(String perspectiveId, IWorkbenchWindow targetWindow, IAdaptable input)
			throws WorkbenchException {
		Assert.isNotNull(perspectiveId);
		IPerspectiveDescriptor targetPerspective = getPerspectiveRegistry().findPerspectiveWithId(perspectiveId);
		if (targetPerspective == null) {
			throw new WorkbenchException(
					NLS.bind(WorkbenchMessages.WorkbenchPage_ErrorCreatingPerspective, perspectiveId));
		}

		if (targetWindow != null) {
			IWorkbenchPage page = targetWindow.getActivePage();
			if (activate(perspectiveId, page, input)) {
				return page;
			}
		}

		for (IWorkbenchWindow window : getWorkbenchWindows()) {
			IWorkbenchPage page = window.getActivePage();
			if (activate(perspectiveId, page, input)) {
				return page;
			}
		}

		if (targetWindow != null) {
			IWorkbenchPage page = targetWindow.getActivePage();
			IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
			int mode = store.getInt(IPreferenceConstants.OPEN_PERSP_MODE);

			if (IPreferenceConstants.OPM_NEW_WINDOW != mode) {
				targetWindow.getShell().open();
				if (page == null) {
					page = targetWindow.openPage(perspectiveId, input);
				} else {
					page.setPerspective(targetPerspective);
				}
				return page;
			}
		}

		return openWorkbenchWindow(perspectiveId, input).getActivePage();
	}

	/*
	 * Shuts down the application.
	 */
	private void shutdown() {
		// shutdown application-specific portions first
		StatusManager statusManager = StatusManager.getManager();
		try {
			advisor.postShutdown();
		} catch (Exception ex) {
			statusManager.handle(StatusUtil.newStatus(WorkbenchPlugin.PI_WORKBENCH, "Exceptions during shutdown", ex)); //$NON-NLS-1$
		}

		// notify regular workbench clients of shutdown, and clear the list when done
		firePostShutdown();
		workbenchListeners.clear();

		cancelEarlyStartup();
		if (workbenchService != null) {
			workbenchService.unregister();
		}
		workbenchService = null;

		if (e4WorkbenchService != null) {
			e4WorkbenchService.unregister();
		}
		e4WorkbenchService = null;

		// for dynamic UI
		registry.removeRegistryChangeListener(extensionEventHandler);
		registry.removeRegistryChangeListener(startupRegistryListener);

		// shut down activity helper before disposing workbench activity support;
		// dispose activity support before disposing service locator to avoid
		// unnecessary activity disablement processing
		activityHelper.shutdown();
		workbenchActivitySupport.dispose();
		WorkbenchHelpSystem.disposeIfNecessary();

		// Bring down all of the services.
		serviceLocator.dispose();
		application.getCommands().removeAll(commandsToRemove);
		application.getCategories().removeAll(categoriesToRemove);
		getDisplay().removeFilter(SWT.MouseDown, backForwardListener);
		backForwardListener = null;

		// shutdown the rest of the workbench
		uninitializeImages();
		if (WorkbenchPlugin.getDefault() != null) {
			WorkbenchPlugin.getDefault().reset();
		}
		WorkbenchThemeManager.disposeManager();
		PropertyPageContributorManager.disposeManager();
		ObjectActionContributorManager.disposeManager();
		statusManager.unregister();
	}

	/**
	 * Cancels the early startup job, if it's still running.
	 */
	private void cancelEarlyStartup() {
		Job.getJobManager().cancel(EARLY_STARTUP_FAMILY);
		// We do not currently wait for any plug-in currently being started to
		// complete
		// (e.g. by doing a join on EARLY_STARTUP_FAMILY), since they may do a
		// syncExec,
		// which would hang. See bug 94537 for rationale.
	}

	@Override
	public IDecoratorManager getDecoratorManager() {
		return WorkbenchPlugin.getDefault().getDecoratorManager();
	}

	/**
	 * Returns the unique object that applications use to configure the workbench.
	 * <p>
	 * IMPORTANT This method is declared package-private to prevent regular plug-ins
	 * from downcasting IWorkbench to Workbench and getting hold of the workbench
	 * configurer that would allow them to tamper with the workbench. The workbench
	 * configurer is available only to the application.
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
	 * IMPORTANT This method is declared package-private to prevent regular plug-ins
	 * from downcasting IWorkbench to Workbench and getting hold of the workbench
	 * advisor that would allow them to tamper with the workbench. The workbench
	 * advisor is internal to the application.
	 * </p>
	 */
	/* package */
	WorkbenchAdvisor getAdvisor() {
		return advisor;
	}

	@Override
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
		return getAdvisor().getMainPreferencePageId();
	}

	@Override
	public IElementFactory getElementFactory(String factoryId) {
		Assert.isNotNull(factoryId);
		return WorkbenchPlugin.getDefault().getElementFactory(factoryId);
	}

	@Override
	public IProgressService getProgressService() {
		return e4Context.get(IProgressService.class);
	}

	private WorkbenchActivitySupport workbenchActivitySupport;

	private WorkbenchContextSupport workbenchContextSupport;

	/**
	 * The single instance of the binding manager used by the workbench. This is
	 * initialized in <code>Workbench.init(Display)</code> and then never changed.
	 * This value will only be <code>null</code> if the initialization call has not
	 * yet completed.
	 *
	 * @since 3.1
	 */
	private BindingManager bindingManager;

	/**
	 * The single instance of the command manager used by the workbench. This is
	 * initialized in <code>Workbench.init(Display)</code> and then never changed.
	 * This value will only be <code>null</code> if the initialization call has not
	 * yet completed.
	 *
	 * @since 3.1
	 */
	private CommandManager commandManager;

	/**
	 * The single instance of the context manager used by the workbench. This is
	 * initialized in <code>Workbench.init(Display)</code> and then never changed.
	 * This value will only be <code>null</code> if the initialization call has not
	 * yet completed.
	 *
	 * @since 3.1
	 */
	private ContextManager contextManager;

	@Override
	public IWorkbenchActivitySupport getActivitySupport() {
		return e4Context.get(IWorkbenchActivitySupport.class);
	}

	@Override
	public IWorkbenchContextSupport getContextSupport() {
		return workbenchContextSupport;
	}

	private final IBindingManagerListener bindingManagerListener = bindingManagerEvent -> {
		if (bindingManagerEvent.isActiveBindingsChanged()) {
			updateActiveWorkbenchWindowMenuManager(true);
		}
	};

	private void updateActiveWorkbenchWindowMenuManager(boolean textOnly) {

		final IWorkbenchWindow workbenchWindow = getActiveWorkbenchWindow();

		if (workbenchWindow instanceof WorkbenchWindow) {
			WorkbenchWindow activeWorkbenchWindow = (WorkbenchWindow) workbenchWindow;
			if (activeWorkbenchWindow.isClosing()) {
				return;
			}

			// Update the action sets.
			final MenuManager menuManager = activeWorkbenchWindow.getMenuManager();

			if (textOnly) {
				menuManager.update(IAction.TEXT);
			} else {
				menuManager.update(true);
			}
		}
	}

	private ActivityPersistanceHelper activityHelper;

	@Override
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
	 * @param descriptor The intro descriptor to use.
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

	private IRegistryChangeListener startupRegistryListener = event -> {
		final IExtensionDelta[] deltas = event.getExtensionDeltas(PlatformUI.PLUGIN_ID,
				IWorkbenchRegistryConstants.PL_STARTUP);
		if (deltas.length == 0) {
			return;
		}
		final String disabledPlugins = PrefUtil.getInternalPreferenceStore()
				.getString(IPreferenceConstants.PLUGINS_NOT_ACTIVATED_ON_STARTUP);

		for (IExtensionDelta delta : deltas) {
			IExtension extension = delta.getExtension();
			if (delta.getKind() == IExtensionDelta.REMOVED) {
				continue;
			}

			// if the plugin is not in the set of disabled plugins,
			// then
			// execute the code to start it
			if (!disabledPlugins.contains(extension.getContributor().getName())) {
				SafeRunner.run(new EarlyStartupRunnable(extension));
			}
		}

	};

	@Override
	public IThemeManager getThemeManager() {
		return WorkbenchThemeManager.getInstance();
	}

	/**
	 * Returns <code>true</code> if the workbench is running, <code>false</code> if
	 * it has been terminated.
	 *
	 * @return <code>true</code> if the workbench is running, <code>false</code> if
	 *         it has been terminated.
	 */
	public boolean isRunning() {
		return runEventLoop;
	}

	/**
	 * <p>
	 * Indicates the start of a large update within the workbench. This is used to
	 * disable CPU-intensive, change-sensitive services that were temporarily
	 * disabled in the midst of large changes. This method should always be called
	 * in tandem with <code>largeUpdateEnd</code>, and the event loop should not be
	 * allowed to spin before that method is called.
	 * </p>
	 * <p>
	 * Important: always use with <code>largeUpdateEnd</code>!
	 * </p>
	 */
	public void largeUpdateStart() {
		if (largeUpdates++ == 0) {
			final IWorkbenchWindow[] windows = getWorkbenchWindows();
			for (IWorkbenchWindow window : windows) {
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
	 * <code>largeUpdateStart</code>, and the event loop should not be allowed to
	 * spin before this method is called.
	 * </p>
	 * <p>
	 * Important: always protect this call by using <code>finally</code>!
	 * </p>
	 */
	public void largeUpdateEnd() {
		if (--largeUpdates == 0) {

			// Perform window-specific blocking.
			final IWorkbenchWindow[] windows = getWorkbenchWindows();
			for (IWorkbenchWindow window : windows) {
				if (window instanceof WorkbenchWindow) {
					((WorkbenchWindow) window).largeUpdateEnd();
				}
			}
		}
	}

	@Override
	public IExtensionTracker getExtensionTracker() {
		return e4Context.get(IExtensionTracker.class);
	}

	/**
	 * Adds the listener that handles startup plugins
	 *
	 * @since 3.1
	 */
	private void addStartupRegistryListener() {
		registry.addRegistryChangeListener(startupRegistryListener);
	}

	@Override
	public IWorkbenchHelpSystem getHelpSystem() {
		return WorkbenchHelpSystem.getInstance();
	}

	@Override
	public IWorkbenchBrowserSupport getBrowserSupport() {
		return WorkbenchBrowserSupport.getInstance();
	}

	@Override
	public IViewRegistry getViewRegistry() {
		return WorkbenchPlugin.getDefault().getViewRegistry();
	}

	@Override
	public IWizardRegistry getNewWizardRegistry() {
		return WorkbenchPlugin.getDefault().getNewWizardRegistry();
	}

	@Override
	public IWizardRegistry getImportWizardRegistry() {
		return WorkbenchPlugin.getDefault().getImportWizardRegistry();
	}

	@Override
	public IWizardRegistry getExportWizardRegistry() {
		return WorkbenchPlugin.getDefault().getExportWizardRegistry();
	}

	@Override
	public <T> T getAdapter(final Class<T> key) {
		return key.cast(serviceLocator.getService(key));
	}

	@Override
	public <T> T getService(final Class<T> key) {
		return serviceLocator.getService(key);
	}

	@Override
	public boolean hasService(final Class<?> key) {
		return serviceLocator.hasService(key);
	}

	/**
	 * Registers a service with this locator. If there is an existing service
	 * matching the same <code>api</code> and it implements {@link IDisposable}, it
	 * will be disposed.
	 *
	 * @param api     This is the interface that the service implements. Must not be
	 *                <code>null</code>.
	 * @param service The service to register. This must be some implementation of
	 *                <code>api</code>. This value must not be <code>null</code>.
	 */
	public void registerService(final Class api, final Object service) {
		serviceLocator.registerService(api, service);
	}

	/**
	 * The source provider that tracks which context menus (i.e., menus with target
	 * identifiers) are now showing. This value is <code>null</code> until
	 * {@link #initializeDefaultServices()} is called.
	 */
	private MenuSourceProvider menuSourceProvider;

	/**
	 * Adds the ids of a menu that is now showing to the menu source provider. This
	 * is used for legacy action-based handlers which need to become active only for
	 * the duration of a menu being visible.
	 *
	 * @param menuIds          The identifiers of the menu that is now showing; must
	 *                         not be <code>null</code>.
	 */
	public void addShowingMenus(final Set menuIds, final ISelection localSelection, final ISelection localEditorInput) {
		menuSourceProvider.addShowingMenus(menuIds, localSelection, localEditorInput);
		Map currentState = menuSourceProvider.getCurrentState();
		for (String key : menuSourceProvider.getProvidedSourceNames()) {
			e4Context.set(key, currentState.get(key));
		}
	}

	/**
	 * Removes the ids of a menu that is now hidden from the menu source provider.
	 * This is used for legacy action-based handlers which need to become active
	 * only for the duration of a menu being visible.
	 *
	 * @param menuIds          The identifiers of the menu that is now hidden; must
	 *                         not be <code>null</code>.
	 */
	public void removeShowingMenus(final Set menuIds, final ISelection localSelection,
			final ISelection localEditorInput) {
		menuSourceProvider.removeShowingMenus(menuIds, localSelection, localEditorInput);
		for (String key : menuSourceProvider.getProvidedSourceNames()) {
			e4Context.remove(key);
		}
	}

	@Override
	public boolean saveAll(final IShellProvider shellProvider, final IRunnableContext runnableContext,
			final ISaveableFilter filter, boolean confirm) {
		SaveablesList saveablesList = (SaveablesList) getService(ISaveablesLifecycleListener.class);
		Saveable[] saveables = saveablesList.getOpenModels();
		List<Saveable> toSave = getFilteredSaveables(filter, saveables);
		if (toSave.isEmpty()) {
			return true;
		}

		if (!confirm) {
			return !saveablesList.saveModels(toSave, shellProvider, runnableContext);
		}

		// We must negate the result since false is cancel saveAll
		return !saveablesList.promptForSaving(toSave, shellProvider, runnableContext, true, false);
	}

	/*
	 * Apply the given filter to the list of saveables
	 */
	private List<Saveable> getFilteredSaveables(ISaveableFilter filter, Saveable[] saveables) {
		List<Saveable> toSave = new ArrayList<>();
		if (filter == null) {
			for (Saveable saveable : saveables) {
				if (saveable.isDirty()) {
					toSave.add(saveable);
				}
			}
		} else {
			SaveablesList saveablesList = (SaveablesList) getService(ISaveablesLifecycleListener.class);
			for (Saveable saveable : saveables) {
				if (saveable.isDirty()) {
					IWorkbenchPart[] parts = saveablesList.getPartsForSaveable(saveable);
					if (matchesFilter(filter, saveable, parts)) {
						toSave.add(saveable);
					}
				}
			}
		}
		return toSave;
	}

	/*
	 * Test whether the given filter matches the saveable
	 */
	private boolean matchesFilter(ISaveableFilter filter, Saveable saveable, IWorkbenchPart[] parts) {
		return filter == null || filter.select(saveable, parts);
	}

	@Override
	public IShellProvider getModalDialogShellProvider() {
		return ProgressManagerUtil::getDefaultParent;
	}

	public IEclipseContext getContext() {
		return e4Context;
	}

	@Override
	public MApplication getApplication() {
		return application;
	}

	/*
	 * Record the workbench UI in a document
	 */
	private void persistWorkbenchState() {
		try {
			XMLMemento memento = XMLMemento.createWriteRoot(IWorkbenchConstants.TAG_WORKBENCH);
			IStatus status = saveWorkbenchState(memento);

			if (status.getSeverity() == IStatus.OK) {
				StringWriter writer = new StringWriter();
				memento.save(writer);
				application.getPersistedState().put(MEMENTO_KEY, writer.toString());
			} else {
				WorkbenchPlugin.log(new Status(status.getSeverity(), PlatformUI.PLUGIN_ID,
						WorkbenchMessages.Workbench_problemsSavingMsg));
			}
		} catch (IOException e) {
			WorkbenchPlugin.log(new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, 0,
					WorkbenchMessages.Workbench_problemsSavingMsg, e));
		}
	}

	/*
	 * Saves the current state of the workbench so it can be restored later on
	 */
	private IStatus saveWorkbenchState(IMemento memento) {
		MultiStatus result = new MultiStatus(PlatformUI.PLUGIN_ID, IStatus.OK,
				WorkbenchMessages.Workbench_problemsSaving);

		// TODO: Currently we store the editors history only. Add more if needed

		result.add(getEditorHistory().saveState(memento.createChild(IWorkbenchConstants.TAG_MRU_LIST)));
		return result;
	}

	private void restoreWorkbenchState() {
		try {
			String persistedState = application.getPersistedState().get(MEMENTO_KEY);
			if (persistedState != null) {
				XMLMemento memento = XMLMemento.createReadRoot(new StringReader(persistedState));
				IStatus status = readWorkbenchState(memento);

				if (status.getSeverity() != IStatus.OK) {
					WorkbenchPlugin.log(new Status(status.getSeverity(), PlatformUI.PLUGIN_ID,
							WorkbenchMessages.Workbench_problemsRestoring));
				}
			}
		} catch (Exception e) {
			WorkbenchPlugin.log(new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, 0,
					WorkbenchMessages.Workbench_problemsRestoring, e));
		}
	}

	private IStatus readWorkbenchState(IMemento memento) {
		MultiStatus result = new MultiStatus(PlatformUI.PLUGIN_ID, IStatus.OK,
				WorkbenchMessages.Workbench_problemsRestoring, null);

		try {
			UIStats.start(UIStats.RESTORE_WORKBENCH, "MRUList"); //$NON-NLS-1$
			IMemento mruMemento = memento.getChild(IWorkbenchConstants.TAG_MRU_LIST);
			if (mruMemento != null) {
				result.add(getEditorHistory().restoreState(mruMemento));
			}
		} finally {
			UIStats.end(UIStats.RESTORE_WORKBENCH, this, "MRUList"); //$NON-NLS-1$
		}
		return result;
	}

	@Override
	public String getId() {
		return id;
	}

	protected String createId() {
		return UUID.randomUUID().toString();
	}
}
