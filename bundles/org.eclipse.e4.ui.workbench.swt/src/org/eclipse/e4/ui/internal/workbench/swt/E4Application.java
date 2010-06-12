/*******************************************************************************
 * Copyright (c) 2009. 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.internal.workbench.swt;

import java.io.IOException;
import org.eclipse.core.commands.contexts.ContextManager;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IContextConstants;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.contexts.RunAndTrack;
import org.eclipse.e4.core.internal.services.EclipseAdapter;
import org.eclipse.e4.core.services.adapter.Adapter;
import org.eclipse.e4.core.services.contributions.IContributionFactory;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.internal.services.ActiveContextsFunction;
import org.eclipse.e4.ui.internal.workbench.ActiveChildLookupFunction;
import org.eclipse.e4.ui.internal.workbench.ActivePartLookupFunction;
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
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.swt.WorkbenchSWTActivator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 *
 */
public class E4Application implements IApplication {
	private String[] args;

	private IModelResourceHandler handler;
	private Display display = null;

	public static final String THEME_ID = "cssTheme";

	public Display getApplicationDisplay() {
		if (display == null) {
			display = Display.getDefault();
		}
		return display;
	}

	public Object start(IApplicationContext applicationContext)
			throws Exception {

		Display display = getApplicationDisplay();

		E4Workbench workbench = createE4Workbench(applicationContext);

		// Create and run the UI (if any)
		workbench.createAndRunUI(workbench.getApplication());

		// Save the model into the targetURI
		saveModel();

		workbench.close();

		return 0;
	}

	public void saveModel() {
		try {
			handler.save();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public E4Workbench createE4Workbench(IApplicationContext applicationContext) {
		args = (String[]) applicationContext.getArguments().get(
				IApplicationContext.APPLICATION_ARGS);

		IEclipseContext appContext = createDefaultContext();
		appContext.set(IApplicationContext.class, applicationContext);

		// Get the factory to create DI instances with
		IContributionFactory factory = (IContributionFactory) appContext
				.get(IContributionFactory.class.getName());

		// Install the life-cycle manager for this session if there's one
		// defined
		String lifeCycleURI = getArgValue(E4Workbench.LIFE_CYCLE_URI_ARG,
				applicationContext);
		Object lcManager = null;
		if (lifeCycleURI != null) {
			lcManager = factory.create(lifeCycleURI, appContext);
			if (lcManager != null) {
				// Let the manager manipulate the appContext if desired
				factory.call(lcManager, null, "postContextCreate", appContext,
						null);
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
			factory.call(lcManager, null, "processAdditions", appContext, null);
			factory.call(lcManager, null, "processRemovals", appContext, null);
		}

		// Create the addons
		for (MContribution addon : appModel.getAddons()) {
			Object obj = factory.create(addon.getContributionURI(), appContext);
			addon.setObject(obj);
		}

		// Parse out parameters from both the command line and/or the product
		// definition (if any) and put them in the context
		String xmiURI = getArgValue(E4Workbench.XMI_URI_ARG, applicationContext);
		appContext.set(E4Workbench.XMI_URI_ARG, xmiURI);

		String themeId = getArgValue(E4Application.THEME_ID, applicationContext);
		appContext.set(E4Application.THEME_ID, themeId);

		String cssURI = getArgValue(E4Workbench.CSS_URI_ARG, applicationContext);
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
				applicationContext);
		appContext.set(E4Workbench.CSS_RESOURCE_URI_ARG, cssResourcesURI);
		appContext.set(
				E4Workbench.RENDERER_FACTORY_URI,
				getArgValue(E4Workbench.RENDERER_FACTORY_URI,
						applicationContext));

		// This is a default arg, if missing we use the default rendering engine
		String presentationURI = getArgValue(E4Workbench.PRESENTATION_URI_ARG,
				applicationContext);
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

		String appModelPath = getArgValue(E4Workbench.XMI_URI_ARG, appContext);
		Assert.isNotNull(appModelPath, E4Workbench.XMI_URI_ARG
				+ " argument missing"); //$NON-NLS-1$
		final URI initialWorkbenchDefinitionInstance = URI
				.createPlatformPluginURI(appModelPath, true);

		boolean saveAndRestore;
		String value = getArgValue(E4Workbench.SAVE_AND_RESTORE, appContext);

		saveAndRestore = value == null || Boolean.getBoolean(value);

		eclipseContext.set(E4Workbench.SAVE_AND_RESTORE,
				Boolean.valueOf(saveAndRestore));
		eclipseContext.set(E4Workbench.INITIAL_WORKBENCH_MODEL_URI,
				initialWorkbenchDefinitionInstance);
		eclipseContext.set(E4Workbench.INSTANCE_LOCATION, instanceLocation);

		String resourceHandler = getArgValue(
				E4Workbench.MODEL_RESOURCE_HANDLER, appContext);

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

	private String getArgValue(String argName, IApplicationContext appContext) {
		// Is it in the arg list ?
		if (argName == null || argName.length() == 0)
			return null;

		for (int i = 0; i < args.length; i += 2) {
			if (argName.equals(args[i]))
				return args[i + 1];
		}

		return appContext.getBrandingProperty(argName);
	}

	public void stop() {
	}

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
			@Override
			public Object compute(IEclipseContext context) {
				MContext perceivedRoot = (MContext) context.get(MWindow.class
						.getName());
				if (perceivedRoot == null) {
					perceivedRoot = (MContext) context.get(MApplication.class
							.getName());
					if (perceivedRoot == null) {
						return null;
					}
				}

				IEclipseContext current = perceivedRoot.getContext();
				if (current == null) {
					return null;
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
				if (window == null)
					return null;
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
}
