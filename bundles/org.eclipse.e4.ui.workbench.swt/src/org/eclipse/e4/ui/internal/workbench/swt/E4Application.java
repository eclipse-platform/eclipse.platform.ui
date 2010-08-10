/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.internal.workbench.swt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Properties;
import org.eclipse.core.commands.contexts.ContextManager;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IContextConstants;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.contexts.RunAndTrack;
import org.eclipse.e4.core.internal.services.EclipseAdapter;
import org.eclipse.e4.core.services.adapter.Adapter;
import org.eclipse.e4.core.services.contributions.IContributionFactory;
import org.eclipse.e4.core.services.log.ILoggerProvider;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.core.services.statusreporter.StatusReporter;
import org.eclipse.e4.ui.internal.services.ActiveContextsFunction;
import org.eclipse.e4.ui.internal.workbench.ActiveChildLookupFunction;
import org.eclipse.e4.ui.internal.workbench.ActivePartLookupFunction;
import org.eclipse.e4.ui.internal.workbench.DefaultLoggerProvider;
import org.eclipse.e4.ui.internal.workbench.E4Workbench;
import org.eclipse.e4.ui.internal.workbench.ExceptionHandler;
import org.eclipse.e4.ui.internal.workbench.ReflectionContributionFactory;
import org.eclipse.e4.ui.internal.workbench.ResourceHandler;
import org.eclipse.e4.ui.internal.workbench.WorkbenchLogger;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MContribution;
import org.eclipse.e4.ui.model.application.ui.MContext;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.e4.ui.workbench.IExceptionHandler;
import org.eclipse.e4.ui.workbench.IModelResourceHandler;
import org.eclipse.e4.ui.workbench.lifecycle.PostContextCreate;
import org.eclipse.e4.ui.workbench.lifecycle.PreSave;
import org.eclipse.e4.ui.workbench.lifecycle.ProcessAdditions;
import org.eclipse.e4.ui.workbench.lifecycle.ProcessRemovals;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.swt.internal.copy.WorkbenchSWTMessages;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.css.CSSStyleDeclaration;

/**
 *
 */
public class E4Application implements IApplication {

	private static final String PLUGIN_ID = "org.eclipse.e4.ui.workbench.swt"; //$NON-NLS-1$

	// Copied from IDEApplication
	public static final String METADATA_FOLDER = ".metadata"; //$NON-NLS-1$
	private static final String VERSION_FILENAME = "version.ini"; //$NON-NLS-1$
	private static final String WORKSPACE_VERSION_KEY = "org.eclipse.core.runtime"; //$NON-NLS-1$
	private static final String WORKSPACE_VERSION_VALUE = "2"; //$NON-NLS-1$

	private String[] args;

	private IModelResourceHandler handler;
	private Display display = null;

	public static final String THEME_ID = "cssTheme";

	private Object lcManager;

	public Display getApplicationDisplay() {
		if (display == null) {
			display = Display.getDefault();
		}
		return display;
	}

	public Object start(IApplicationContext applicationContext)
			throws Exception {
		Display display = getApplicationDisplay();
		E4Workbench workbench = createE4Workbench(applicationContext, display);

		Location instanceLocation = (Location) workbench.getContext().get(
				E4Workbench.INSTANCE_LOCATION);
		Shell shell = display.getActiveShell();
		if (shell == null)
			shell = new Shell();
		try {
			if (!checkInstanceLocation(instanceLocation, shell))
				return EXIT_OK;

			IEclipseContext workbenchContext = workbench.getContext();
			workbenchContext.set(Display.class, display);

			// Create and run the UI (if any)
			workbench.createAndRunUI(workbench.getApplication());

			// Save the model into the targetURI
			if (lcManager != null) {
				ContextInjectionFactory.invoke(lcManager, PreSave.class,
						workbenchContext, null);
			}
			saveModel();
			workbench.close();
			return EXIT_OK;
		} finally {
			if (display != null)
				display.dispose();
			if (instanceLocation != null)
				instanceLocation.release();
		}
	}

	public void saveModel() {
		try {
			handler.save();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public E4Workbench createE4Workbench(
			IApplicationContext applicationContext, Display display) {
		args = (String[]) applicationContext.getArguments().get(
				IApplicationContext.APPLICATION_ARGS);

		IEclipseContext appContext = createDefaultContext();
		appContext.set(Realm.class, SWTObservables.getRealm(display));
		appContext.set(IApplicationContext.class, applicationContext);

		// Check if DS is running
		if (!appContext
				.containsKey("org.eclipse.e4.ui.workbench.modeling.EModelService")) {
			throw new IllegalStateException(
					"Core services not available. Please make sure that a declarative service implementation (such as the bundle 'org.eclipse.equinox.ds') is available!");
		}

		// Get the factory to create DI instances with
		IContributionFactory factory = (IContributionFactory) appContext
				.get(IContributionFactory.class.getName());

		// Install the life-cycle manager for this session if there's one
		// defined
		String lifeCycleURI = getArgValue(E4Workbench.LIFE_CYCLE_URI_ARG,
				applicationContext, false);
		if (lifeCycleURI != null) {
			lcManager = factory.create(lifeCycleURI, appContext);
			if (lcManager != null) {
				// Let the manager manipulate the appContext if desired
				ContextInjectionFactory.invoke(lcManager,
						PostContextCreate.class, appContext, null);
			}
		}
		// Create the app model and its context
		MApplication appModel = loadApplicationModel(applicationContext,
				appContext);
		appModel.setContext(appContext);

		// for compatibility layer: set the application in the OSGi service
		// context (see Workbench#getInstance())
		if (!E4Workbench.getServiceContext().containsKey(
				MApplication.class.getName())) {
			// first one wins.
			E4Workbench.getServiceContext().set(MApplication.class.getName(),
					appModel);
		}

		// Set the app's context after adding itself
		appContext.set(MApplication.class.getName(), appModel);

		// let the life cycle manager add to the model
		if (lcManager != null) {
			ContextInjectionFactory.invoke(lcManager, ProcessAdditions.class,
					appContext, null);
			ContextInjectionFactory.invoke(lcManager, ProcessRemovals.class,
					appContext, null);
		}

		// Create the addons
		for (MContribution addon : appModel.getAddons()) {
			Object obj = factory.create(addon.getContributionURI(), appContext);
			addon.setObject(obj);
		}

		// Parse out parameters from both the command line and/or the product
		// definition (if any) and put them in the context
		String xmiURI = getArgValue(E4Workbench.XMI_URI_ARG,
				applicationContext, false);
		appContext.set(E4Workbench.XMI_URI_ARG, xmiURI);

		String themeId = getArgValue(E4Application.THEME_ID,
				applicationContext, false);
		appContext.set(E4Application.THEME_ID, themeId);

		String cssURI = getArgValue(E4Workbench.CSS_URI_ARG,
				applicationContext, false);
		if (cssURI != null) {
			appContext.set(E4Workbench.CSS_URI_ARG, cssURI);
		}

		// Temporary to support old property as well
		if (cssURI != null && !cssURI.startsWith("platform:")) {
			System.err
					.println("Warning "
							+ cssURI
							+ " changed its meaning it is used now to run without theme support");
			appContext.set(E4Application.THEME_ID, cssURI);
		}

		String cssResourcesURI = getArgValue(E4Workbench.CSS_RESOURCE_URI_ARG,
				applicationContext, false);
		appContext.set(E4Workbench.CSS_RESOURCE_URI_ARG, cssResourcesURI);
		appContext.set(
				E4Workbench.RENDERER_FACTORY_URI,
				getArgValue(E4Workbench.RENDERER_FACTORY_URI,
						applicationContext, false));

		// This is a default arg, if missing we use the default rendering engine
		String presentationURI = getArgValue(E4Workbench.PRESENTATION_URI_ARG,
				applicationContext, false);
		if (presentationURI == null) {
			presentationURI = PartRenderingEngine.engineURI;
		}
		appContext.set(E4Workbench.PRESENTATION_URI_ARG, presentationURI);

		// Instantiate the Workbench (which is responsible for
		// 'running' the UI (if any)...
		E4Workbench workbench = new E4Workbench(appModel, appContext);
		return workbench;
	}

	private MApplication loadApplicationModel(IApplicationContext appContext,
			IEclipseContext eclipseContext) {
		MApplication theApp = null;

		Location instanceLocation = WorkbenchSWTActivator.getDefault()
				.getInstanceLocation();

		String appModelPath = getArgValue(E4Workbench.XMI_URI_ARG, appContext,
				false);
		Assert.isNotNull(appModelPath, E4Workbench.XMI_URI_ARG
				+ " argument missing"); //$NON-NLS-1$
		final URI initialWorkbenchDefinitionInstance = URI
				.createPlatformPluginURI(appModelPath, true);

		eclipseContext.set(E4Workbench.INITIAL_WORKBENCH_MODEL_URI,
				initialWorkbenchDefinitionInstance);
		eclipseContext.set(E4Workbench.INSTANCE_LOCATION, instanceLocation);

		// Save and restore
		boolean saveAndRestore;
		String value = getArgValue(E4Workbench.PERSIST_STATE, appContext, false);

		saveAndRestore = value == null || Boolean.parseBoolean(value);

		eclipseContext.set(E4Workbench.PERSIST_STATE,
				Boolean.valueOf(saveAndRestore));

		// Persisted state
		boolean clearPersistedState;
		value = getArgValue(E4Workbench.CLEAR_PERSISTED_STATE, appContext, true);
		clearPersistedState = value != null && Boolean.parseBoolean(value);
		eclipseContext.set(E4Workbench.CLEAR_PERSISTED_STATE,
				Boolean.valueOf(clearPersistedState));

		// Delta save and restore
		boolean deltaRestore;
		value = getArgValue(E4Workbench.DELTA_RESTORE, appContext, true);
		deltaRestore = value == null || Boolean.parseBoolean(value);
		eclipseContext.set(E4Workbench.DELTA_RESTORE,
				Boolean.valueOf(deltaRestore));

		String resourceHandler = getArgValue(
				E4Workbench.MODEL_RESOURCE_HANDLER, appContext, false);

		if (resourceHandler == null) {
			resourceHandler = "platform:/plugin/org.eclipse.e4.ui.workbench/"
					+ ResourceHandler.class.getName();
		}

		IContributionFactory factory = eclipseContext
				.get(IContributionFactory.class);

		handler = (IModelResourceHandler) factory.create(resourceHandler,
				eclipseContext);

		Resource resource = handler.loadMostRecentModel();
		theApp = (MApplication) resource.getContents().get(0);

		return theApp;
	}

	private String getArgValue(String argName, IApplicationContext appContext,
			boolean singledCmdArgValue) {
		// Is it in the arg list ?
		if (argName == null || argName.length() == 0)
			return null;

		if (singledCmdArgValue) {
			for (int i = 0; i < args.length; i++) {
				if (("-" + argName).equals(args[i]))
					return "true";
			}
		} else {
			for (int i = 0; i < args.length; i++) {
				if (("-" + argName).equals(args[i]) && i + 1 < args.length)
					return args[i + 1];
			}
		}

		return appContext.getBrandingProperty(argName);
	}

	public void stop() {
	}

	// FIXME We should have one place to setup the generic context stuff (see
	// E4Application#createDefaultContext())
	public static IEclipseContext createDefaultContext() {
		// FROM: WorkbenchApplication
		// parent of the global workbench context is an OSGi service
		// context that can provide OSGi services
		IEclipseContext serviceContext = E4Workbench.getServiceContext();
		final IEclipseContext appContext = serviceContext
				.createChild("WorkbenchContext"); //$NON-NLS-1$

		// FROM: Workbench#createWorkbenchContext
		IExtensionRegistry registry = RegistryFactory.getRegistry();
		ExceptionHandler exceptionHandler = new ExceptionHandler();
		ReflectionContributionFactory contributionFactory = new ReflectionContributionFactory(
				registry);
		appContext.set(IContributionFactory.class.getName(),
				contributionFactory);

		appContext
				.set(Logger.class.getName(), ContextInjectionFactory.make(
						WorkbenchLogger.class, appContext));
		appContext.set(Adapter.class.getName(),
				ContextInjectionFactory.make(EclipseAdapter.class, appContext));

		// No default log provider available
		if (appContext.get(ILoggerProvider.class) == null) {
			appContext.set(ILoggerProvider.class, ContextInjectionFactory.make(
					DefaultLoggerProvider.class, appContext));
		}

		// setup for commands and handlers
		appContext.set(ContextManager.class.getName(), new ContextManager());

		// FROM: Workbench#createWorkbenchContext
		appContext.set(IServiceConstants.ACTIVE_CONTEXTS,
				new ActiveContextsFunction());
		appContext.set(IServiceConstants.ACTIVE_PART,
				new ActivePartLookupFunction());
		appContext.runAndTrack(new RunAndTrack() {
			public boolean changed(IEclipseContext context) {
				Object o = appContext.get(IServiceConstants.ACTIVE_PART);
				if (o instanceof MPart) {
					appContext.set(IServiceConstants.ACTIVE_PART_ID,
							((MPart) o).getElementId());
				}
				return true;
			}

			/*
			 * For debugging purposes only
			 */
			@Override
			public String toString() {
				return IServiceConstants.ACTIVE_PART_ID;
			}
		});
		appContext.set(EPartService.PART_SERVICE_ROOT, new ContextFunction() {
			private void log() {
				StatusReporter statusReporter = (StatusReporter) appContext
						.get(StatusReporter.class.getName());
				statusReporter.report(new Status(IStatus.ERROR,
						WorkbenchSWTActivator.PI_RENDERERS,
						"Internal error, please post the trace to bug 315270",
						new Exception()), StatusReporter.LOG);
			}

			@Override
			public Object compute(IEclipseContext context) {
				MContext perceivedRoot = (MContext) context.get(MWindow.class
						.getName());
				if (perceivedRoot == null) {
					perceivedRoot = (MContext) context.get(MApplication.class
							.getName());
					if (perceivedRoot == null) {
						IEclipseContext ctxt = (IEclipseContext) appContext
								.getLocal(IContextConstants.ACTIVE_CHILD);
						if (ctxt == null) {
							return null;
						}
						log();
						return ctxt.get(MWindow.class);
					}
				}

				IEclipseContext current = perceivedRoot.getContext();
				if (current == null) {
					IEclipseContext ctxt = (IEclipseContext) appContext
							.getLocal(IContextConstants.ACTIVE_CHILD);
					if (ctxt == null) {
						return null;
					}
					log();
					return ctxt.get(MWindow.class);
				}

				IEclipseContext next = (IEclipseContext) current
						.getLocal(IContextConstants.ACTIVE_CHILD);
				MPerspective candidate = null;
				while (next != null) {
					current = next;
					MPerspective perspective = current.get(MPerspective.class);
					if (perspective != null) {
						candidate = perspective;
					}
					next = (IEclipseContext) current
							.getLocal(IContextConstants.ACTIVE_CHILD);
				}

				if (candidate != null) {
					return candidate;
				}

				// we need to consider detached windows
				MUIElement window = (MUIElement) current.get(MWindow.class
						.getName());
				if (window == null) {
					IEclipseContext ctxt = (IEclipseContext) appContext
							.getLocal(IContextConstants.ACTIVE_CHILD);
					if (ctxt == null) {
						return null;
					}
					log();
					return ctxt.get(MWindow.class);
				}
				MElementContainer<?> parent = window.getParent();
				while (parent != null && !(parent instanceof MApplication)) {
					window = parent;
					parent = parent.getParent();
				}
				return window;
			}
		});

		// EHandlerService comes from a ContextFunction
		// EContextService comes from a ContextFunction
		appContext.set(IExceptionHandler.class.getName(), exceptionHandler);
		appContext.set(IExtensionRegistry.class.getName(), registry);
		// appContext.set(IServiceConstants.SELECTION,
		// new ActiveChildOutputFunction(IServiceConstants.SELECTION));

		// appContext.set(IServiceConstants.INPUT, new ContextFunction() {
		// public Object compute(IEclipseContext context, Object[] arguments) {
		// Class adapterType = null;
		// if (arguments.length > 0 && arguments[0] instanceof Class) {
		// adapterType = (Class) arguments[0];
		// }
		// Object newInput = null;
		// Object newValue = context.get(IServiceConstants.SELECTION);
		// if (adapterType == null || adapterType.isInstance(newValue)) {
		// newInput = newValue;
		// } else if (newValue != null && adapterType != null) {
		// IAdapterManager adapters = (IAdapterManager) appContext
		// .get(IAdapterManager.class.getName());
		// if (adapters != null) {
		// Object adapted = adapters.loadAdapter(newValue,
		// adapterType.getName());
		// if (adapted != null) {
		// newInput = adapted;
		// }
		// }
		// }
		// return newInput;
		// }
		// });
		appContext.set(IServiceConstants.ACTIVE_SHELL,
				new ActiveChildLookupFunction(IServiceConstants.ACTIVE_SHELL,
						E4Workbench.LOCAL_ACTIVE_SHELL));

		// FROM: Workbench#initializeNullStyling
		appContext.set(IStylingEngine.SERVICE_NAME, new IStylingEngine() {
			public void setClassname(Object widget, String classname) {
			}

			public void setId(Object widget, String id) {
			}

			public void style(Object widget) {
			}

			public CSSStyleDeclaration getStyle(Object widget) {
				return null;
			}

			public void setClassnameAndId(Object widget, String classname,
					String id) {
			}
		});

		// FROM: Workbench constructor
		// workbenchContext.set(Workbench.class.getName(), this);
		// workbenchContext.set(IWorkbench.class.getName(), this);
		appContext.set(IExtensionRegistry.class.getName(), registry);
		appContext.set(IContributionFactory.class.getName(),
				contributionFactory);
		appContext.set(IEclipseContext.class.getName(), appContext);
		appContext.set(IShellProvider.class.getName(), new IShellProvider() {
			public Shell getShell() {
				return null;
			}
		});

		return appContext;
	}

	/**
	 * Simplified copy of IDEAplication processing that does not offer to choose
	 * a workspace location.
	 */
	private boolean checkInstanceLocation(Location instanceLocation, Shell shell) {
		if (instanceLocation == null) {
			MessageDialog
					.openError(
							shell,
							WorkbenchSWTMessages.IDEApplication_workspaceMandatoryTitle,
							WorkbenchSWTMessages.IDEApplication_workspaceMandatoryMessage);
			return false;
		}

		// -data "/valid/path", workspace already set
		if (instanceLocation.isSet()) {
			// make sure the meta data version is compatible (or the user has
			// chosen to overwrite it).
			if (!checkValidWorkspace(shell, instanceLocation.getURL())) {
				return false;
			}

			// at this point its valid, so try to lock it and update the
			// metadata version information if successful
			try {
				if (instanceLocation.lock()) {
					writeWorkspaceVersion();
					return true;
				}

				// we failed to create the directory.
				// Two possibilities:
				// 1. directory is already in use
				// 2. directory could not be created
				File workspaceDirectory = new File(instanceLocation.getURL()
						.getFile());
				if (workspaceDirectory.exists()) {
					MessageDialog
							.openError(
									shell,
									WorkbenchSWTMessages.IDEApplication_workspaceCannotLockTitle,
									WorkbenchSWTMessages.IDEApplication_workspaceCannotLockMessage);
				} else {
					MessageDialog
							.openError(
									shell,
									WorkbenchSWTMessages.IDEApplication_workspaceCannotBeSetTitle,
									WorkbenchSWTMessages.IDEApplication_workspaceCannotBeSetMessage);
				}
			} catch (IOException e) {
				Logger logger = new WorkbenchLogger(PLUGIN_ID);
				logger.error(e);
				MessageDialog.openError(shell,
						WorkbenchSWTMessages.InternalError, e.getMessage());
			}
			return false;
		}
		/*
		 * // -data @noDefault or -data not specified, prompt and set
		 * ChooseWorkspaceData launchData = new ChooseWorkspaceData(instanceLoc
		 * .getDefault());
		 * 
		 * boolean force = false; while (true) { URL workspaceUrl =
		 * promptForWorkspace(shell, launchData, force); if (workspaceUrl ==
		 * null) { return false; }
		 * 
		 * // if there is an error with the first selection, then force the //
		 * dialog to open to give the user a chance to correct force = true;
		 * 
		 * try { // the operation will fail if the url is not a valid //
		 * instance data area, so other checking is unneeded if
		 * (instanceLocation.setURL(workspaceUrl, true)) {
		 * launchData.writePersistedData(); writeWorkspaceVersion(); return
		 * true; } } catch (IllegalStateException e) { MessageDialog .openError(
		 * shell, IDEWorkbenchMessages.IDEApplication_workspaceCannotBeSetTitle,
		 * IDEWorkbenchMessages.IDEApplication_workspaceCannotBeSetMessage);
		 * return false; }
		 * 
		 * // by this point it has been determined that the workspace is //
		 * already in use -- force the user to choose again
		 * MessageDialog.openError(shell,
		 * IDEWorkbenchMessages.IDEApplication_workspaceInUseTitle,
		 * IDEWorkbenchMessages.IDEApplication_workspaceInUseMessage); }
		 */
		return false;
	}

	/**
	 * Return true if the argument directory is ok to use as a workspace and
	 * false otherwise. A version check will be performed, and a confirmation
	 * box may be displayed on the argument shell if an older version is
	 * detected.
	 * 
	 * @return true if the argument URL is ok to use as a workspace and false
	 *         otherwise.
	 */
	private boolean checkValidWorkspace(Shell shell, URL url) {
		// a null url is not a valid workspace
		if (url == null) {
			return false;
		}

		String version = readWorkspaceVersion(url);

		// if the version could not be read, then there is not any existing
		// workspace data to trample, e.g., perhaps its a new directory that
		// is just starting to be used as a workspace
		if (version == null) {
			return true;
		}

		final int ide_version = Integer.parseInt(WORKSPACE_VERSION_VALUE);
		int workspace_version = Integer.parseInt(version);

		// equality test is required since any version difference (newer
		// or older) may result in data being trampled
		if (workspace_version == ide_version) {
			return true;
		}

		// At this point workspace has been detected to be from a version
		// other than the current ide version -- find out if the user wants
		// to use it anyhow.
		String title = WorkbenchSWTMessages.IDEApplication_versionTitle;
		String message = NLS.bind(
				WorkbenchSWTMessages.IDEApplication_versionMessage,
				url.getFile());

		MessageBox mbox = new MessageBox(shell, SWT.OK | SWT.CANCEL
				| SWT.ICON_WARNING | SWT.APPLICATION_MODAL);
		mbox.setText(title);
		mbox.setMessage(message);
		return mbox.open() == SWT.OK;
	}

	/**
	 * Look at the argument URL for the workspace's version information. Return
	 * that version if found and null otherwise.
	 */
	private static String readWorkspaceVersion(URL workspace) {
		File versionFile = getVersionFile(workspace, false);
		if (versionFile == null || !versionFile.exists()) {
			return null;
		}

		try {
			// Although the version file is not spec'ed to be a Java properties
			// file, it happens to follow the same format currently, so using
			// Properties to read it is convenient.
			Properties props = new Properties();
			FileInputStream is = new FileInputStream(versionFile);
			try {
				props.load(is);
			} finally {
				is.close();
			}

			return props.getProperty(WORKSPACE_VERSION_KEY);
		} catch (IOException e) {
			Logger logger = new WorkbenchLogger(PLUGIN_ID);
			logger.error(e);
			return null;
		}
	}

	/**
	 * Write the version of the metadata into a known file overwriting any
	 * existing file contents. Writing the version file isn't really crucial, so
	 * the function is silent about failure
	 */
	private static void writeWorkspaceVersion() {
		Location instanceLoc = Platform.getInstanceLocation();
		if (instanceLoc == null || instanceLoc.isReadOnly()) {
			return;
		}

		File versionFile = getVersionFile(instanceLoc.getURL(), true);
		if (versionFile == null) {
			return;
		}

		OutputStream output = null;
		try {
			String versionLine = WORKSPACE_VERSION_KEY + '='
					+ WORKSPACE_VERSION_VALUE;

			output = new FileOutputStream(versionFile);
			output.write(versionLine.getBytes("UTF-8")); //$NON-NLS-1$
		} catch (IOException e) {
			Logger logger = new WorkbenchLogger(PLUGIN_ID);
			logger.error(e);
		} finally {
			try {
				if (output != null) {
					output.close();
				}
			} catch (IOException e) {
				// do nothing
			}
		}
	}

	/**
	 * The version file is stored in the metadata area of the workspace. This
	 * method returns an URL to the file or null if the directory or file does
	 * not exist (and the create parameter is false).
	 * 
	 * @param create
	 *            If the directory and file does not exist this parameter
	 *            controls whether it will be created.
	 * @return An url to the file or null if the version file does not exist or
	 *         could not be created.
	 */
	private static File getVersionFile(URL workspaceUrl, boolean create) {
		if (workspaceUrl == null) {
			return null;
		}

		try {
			// make sure the directory exists
			File metaDir = new File(workspaceUrl.getPath(), METADATA_FOLDER);
			if (!metaDir.exists() && (!create || !metaDir.mkdir())) {
				return null;
			}

			// make sure the file exists
			File versionFile = new File(metaDir, VERSION_FILENAME);
			if (!versionFile.exists()
					&& (!create || !versionFile.createNewFile())) {
				return null;
			}

			return versionFile;
		} catch (IOException e) {
			// cannot log because instance area has not been set
			return null;
		}
	}

}
