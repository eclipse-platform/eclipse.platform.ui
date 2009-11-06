/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.workbench.swt.internal;

import java.io.IOException;
import java.net.URLConnection;
import java.util.Collections;
import java.util.Map;
import org.eclipse.core.commands.CommandManager;
import org.eclipse.core.commands.contexts.ContextManager;
import org.eclipse.core.internal.runtime.PlatformURLPluginConnection;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.commands.internal.CommandServiceImpl;
import org.eclipse.e4.core.commands.internal.HandlerServiceCreationFunction;
import org.eclipse.e4.core.services.IContributionFactory;
import org.eclipse.e4.core.services.Logger;
import org.eclipse.e4.core.services.context.EclipseContextFactory;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.ContextInjectionFactory;
import org.eclipse.e4.core.services.context.spi.IContextConstants;
import org.eclipse.e4.ui.bindings.EBindingService;
import org.eclipse.e4.ui.bindings.internal.BindingServiceCreationFunction;
import org.eclipse.e4.ui.internal.services.ActiveContextsFunction;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.e4.ui.services.events.EventBrokerFactory;
import org.eclipse.e4.ui.services.events.IEventBroker;
import org.eclipse.e4.ui.workbench.swt.Activator;
import org.eclipse.e4.workbench.ui.IExceptionHandler;
import org.eclipse.e4.workbench.ui.internal.ActiveChildLookupFunction;
import org.eclipse.e4.workbench.ui.internal.ActivePartLookupFunction;
import org.eclipse.e4.workbench.ui.internal.E4Workbench;
import org.eclipse.e4.workbench.ui.internal.ExceptionHandler;
import org.eclipse.e4.workbench.ui.internal.ReflectionContributionFactory;
import org.eclipse.e4.workbench.ui.internal.ResourceHandler;
import org.eclipse.e4.workbench.ui.internal.UISchedulerStrategy;
import org.eclipse.e4.workbench.ui.internal.WorkbenchLogger;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.osgi.service.datalocation.Location;
import org.osgi.framework.Bundle;

/**
 *
 */
public class E4Application implements IApplication {
	private String[] args;

	private ResourceHandler handler;

	public Object start(IApplicationContext applicationContext)
			throws Exception {

		args = (String[]) applicationContext.getArguments().get(
				"application.args"); //$NON-NLS-1$

		// Create the app model and its context
		MApplication appModel = loadApplicationModel();
		IEclipseContext appContext = createDefaultContext();

		// Set the app's context after adding itself
		appContext.set(MApplication.class.getName(), appModel);
		appModel.setContext(appContext);

		// Parse out parameters from both the command line and/or the product
		// definition (if any) and put them in the context
		String xmiURI = getArgValue(E4Workbench.XMI_URI_ARG);
		appContext.set(E4Workbench.XMI_URI_ARG, xmiURI);
		String cssURI = getArgValue(E4Workbench.CSS_URI_ARG);
		appContext.set(E4Workbench.CSS_URI_ARG, cssURI);
		String cssResourcesURI = getArgValue(E4Workbench.CSS_RESOURCE_URI_ARG);
		appContext.set(E4Workbench.CSS_RESOURCE_URI_ARG, cssResourcesURI);

		// Instantiate the Workbench (which is responsible for
		// 'running' the UI (if any)...
		E4Workbench workbench = new E4Workbench(appModel, appContext);

		// Save the model into the targetURI
		saveModel();

		return workbench.getReturnValue();
	}

	private void saveModel() {
		try {
			handler.save();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private MApplication loadApplicationModel() {
		MApplication theApp = null;

		Location instanceLocation = Activator.getDefault()
				.getInstanceLocation();

		String appModelPath = getArgValue(E4Workbench.XMI_URI_ARG);
		Assert.isNotNull(appModelPath, E4Workbench.XMI_URI_ARG
				+ " argument missing"); //$NON-NLS-1$
		final URI initialWorkbenchDefinitionInstance = URI
				.createPlatformPluginURI(appModelPath, true);

		boolean saveAndRestore = true;
		handler = new ResourceHandler(instanceLocation,
				initialWorkbenchDefinitionInstance, saveAndRestore);

		long restoreLastModified = handler.getLastStoreDatetime();
		long lastApplicationModification = getLastApplicationModification(initialWorkbenchDefinitionInstance);

		boolean restore = restoreLastModified > lastApplicationModification;

		Resource resource;
		if (restore) {
			resource = handler.loadRestoredModel();
			theApp = (MApplication) resource.getContents().get(0);
		} else {
			resource = handler.loadBaseModel();
			theApp = (MApplication) resource.getContents().get(0);

			// final EList<MWindow> windows = app.getChildren();
			// for (MWindow window : windows) {
			// processPartContributions(resource, window);
			// }
		}

		// init();

		return theApp;
	}

	private long getLastApplicationModification(
			URI applicationDefinitionInstance) {
		long appLastModified = 0L;
		ResourceSetImpl resourceSetImpl = new ResourceSetImpl();

		Map<String, ?> attributes = resourceSetImpl
				.getURIConverter()
				.getAttributes(
						applicationDefinitionInstance,
						Collections
								.singletonMap(
										URIConverter.OPTION_REQUESTED_ATTRIBUTES,
										Collections
												.singleton(URIConverter.ATTRIBUTE_TIME_STAMP)));

		Object timestamp = attributes.get(URIConverter.ATTRIBUTE_TIME_STAMP);
		if (timestamp instanceof Long) {
			appLastModified = ((Long) timestamp).longValue();
		} else if (applicationDefinitionInstance.isPlatformPlugin()) {
			try {
				java.net.URL url = new java.net.URL(
						applicationDefinitionInstance.toString());
				Object[] obj = PlatformURLPluginConnection.parse(url.getFile()
						.trim(), url);
				Bundle b = (Bundle) obj[0];
				URLConnection openConnection = b.getResource((String) obj[1])
						.openConnection();
				appLastModified = openConnection.getLastModified();
			} catch (Exception e) {
				// ignore
			}
		}

		return appLastModified;
	}

	private String getArgValue(String argName) {
		// Is it in the arg list ?
		if (argName == null || argName.length() == 0)
			return null;

		for (int i = 0; i < args.length; i += 2) {
			if (argName.equals(args[i]))
				return args[i + 1];
		}

		// No, if we're a product is it in the product's definition?
		IProduct product = Platform.getProduct();
		if (product != null) {
			return product.getProperty(argName);
		}

		return null;
	}

	public void stop() {
	}

	public static IEclipseContext createDefaultContext() {
		// FROM: WorkbenchApplication
		// parent of the global workbench context is an OSGi service
		// context that can provide OSGi services
		IEclipseContext serviceContext = EclipseContextFactory
				.createServiceContext(Activator.getDefault().getContext());
		final IEclipseContext appContext = EclipseContextFactory.create(
				serviceContext, UISchedulerStrategy.getInstance());
		appContext.set(IContextConstants.DEBUG_STRING, "WorkbenchAppContext"); //$NON-NLS-1$
		appContext.set(IEventBroker.class.getName(), EventBrokerFactory
				.newEventBroker());

		// FROM: Workbench#createWorkbenchContext
		IExtensionRegistry registry = RegistryFactory.getRegistry();
		ExceptionHandler exceptionHandler = new ExceptionHandler();
		ReflectionContributionFactory contributionFactory = new ReflectionContributionFactory(
				registry);

		appContext.set(Logger.class.getName(), ContextInjectionFactory.inject(
				new WorkbenchLogger(), appContext));
		appContext.set(IContextConstants.DEBUG_STRING, "WorkbenchContext"); //$NON-NLS-1$

		// setup for commands and handlers
		if (contributionFactory != null) {
			appContext.set(IContributionFactory.class.getName(),
					contributionFactory);
		}
		appContext.set(ContextManager.class.getName(), new ContextManager());

		// FROM: ContextUtil
		// ContextUtil.commandSetup(mainContext);
		CommandManager commandManager = new CommandManager();
		appContext.set(CommandManager.class.getName(), commandManager);
		CommandServiceImpl csi = new CommandServiceImpl();
		ContextInjectionFactory.inject(csi, appContext);
		appContext.set(ECommandService.class.getName(), csi);

		// ContextUtil.handlerSetup(mainContext);
		appContext.set(IContextConstants.ROOT_CONTEXT, appContext);
		appContext.set(EHandlerService.class.getName(),
				new HandlerServiceCreationFunction());

		// bindings.ContextUtil
		appContext.set(IContextConstants.ROOT_CONTEXT, appContext);
		appContext.set(EBindingService.class.getName(),
				new BindingServiceCreationFunction());

		// FROM: Workbench#createWorkbenchContext
		appContext.set(IServiceConstants.ACTIVE_CONTEXTS,
				new ActiveContextsFunction());
		appContext.set(IServiceConstants.ACTIVE_PART,
				new ActivePartLookupFunction());
		appContext.runAndTrack(new Runnable() {
			public void run() {
				Object o = appContext.get(IServiceConstants.ACTIVE_PART);
				if (o instanceof MPart) {
					appContext.set(IServiceConstants.ACTIVE_PART_ID,
							((MPart) o).getId());
				}
			}

			/*
			 * For debugging purposes only
			 */
			@Override
			public String toString() {
				return IServiceConstants.ACTIVE_PART_ID;
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

		return appContext;
	}
}
