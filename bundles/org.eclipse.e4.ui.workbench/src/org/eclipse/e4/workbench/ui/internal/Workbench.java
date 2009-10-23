/*******************************************************************************
 * Copyright (c) 2008, 2009 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.workbench.ui.internal;

import java.io.IOException;
import java.net.URLConnection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import org.eclipse.core.commands.Category;
import org.eclipse.core.commands.contexts.ContextManager;
import org.eclipse.core.internal.runtime.PlatformURLPluginConnection;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.e4.core.commands.ContextUtil;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.services.IContributionFactory;
import org.eclipse.e4.core.services.Logger;
import org.eclipse.e4.core.services.context.EclipseContextFactory;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.ContextFunction;
import org.eclipse.e4.core.services.context.spi.ContextInjectionFactory;
import org.eclipse.e4.core.services.context.spi.IContextConstants;
import org.eclipse.e4.ui.internal.services.ActiveContextsFunction;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.MApplicationFactory;
import org.eclipse.e4.ui.model.application.MCommand;
import org.eclipse.e4.ui.model.application.MContext;
import org.eclipse.e4.ui.model.application.MElementContainer;
import org.eclipse.e4.ui.model.application.MHandler;
import org.eclipse.e4.ui.model.application.MHandlerContainer;
import org.eclipse.e4.ui.model.application.MPSCElement;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.application.MUIElement;
import org.eclipse.e4.ui.model.application.MWindow;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.e4.workbench.ui.IExceptionHandler;
import org.eclipse.e4.workbench.ui.IPresentationEngine;
import org.eclipse.e4.workbench.ui.IWorkbench;
import org.eclipse.e4.workbench.ui.IWorkbenchWindowHandler;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.osgi.service.datalocation.Location;
import org.osgi.framework.Bundle;
import org.osgi.service.packageadmin.PackageAdmin;

public class Workbench implements IWorkbench {
	public static final String LOCAL_ACTIVE_SHELL = "localActiveShell"; //$NON-NLS-1$
	public static final String ID = "org.eclipse.e4.workbench.fakedWBWindow"; //$NON-NLS-1$
	private MApplication workbench;
	private static final boolean saveAndRestore = true;
	private IWorkbenchWindowHandler windowHandler;

	public IEclipseContext getContext() {
		return workbenchContext;
	}

	// UI Construction...
	private IPresentationEngine renderer;
	private int rv;

	private ExceptionHandler exceptionHandler;
	private IEclipseContext workbenchContext;
	private ReflectionContributionFactory contributionFactory;
	private String renderingEngineURI;
	private ResourceHandler handler;
	private Location instanceLocation;

	public Workbench(Location instanceLocation, IExtensionRegistry registry,
			PackageAdmin packageAdmin, IEclipseContext applicationContext,
			IWorkbenchWindowHandler windowHandler, String renderingEngineURI) {
		//		System.err.println("NEw Workbenc"); //$NON-NLS-1$
		this.windowHandler = windowHandler;
		this.renderingEngineURI = renderingEngineURI;
		this.instanceLocation = instanceLocation;

		exceptionHandler = new ExceptionHandler();
		contributionFactory = new ReflectionContributionFactory(registry);

		workbenchContext = createWorkbenchContext(applicationContext, registry, exceptionHandler,
				contributionFactory);
		workbenchContext.set(Workbench.class.getName(), this);
		workbenchContext.set(IWorkbench.class.getName(), this);
		workbenchContext.set(IExtensionRegistry.class.getName(), registry);
		workbenchContext.set(IContributionFactory.class.getName(), contributionFactory);
		workbenchContext.set(IEclipseContext.class.getName(), workbenchContext);
	}

	public void setWorkbenchModel(MApplication model) {
		workbench = model;
		init();
	}

	// this could be it
	// and that's all, folks

	public void setWorkbenchModelURI(URI workbenchXmiURI) {
		createWorkbenchModel(workbenchXmiURI);
	}

	public static IEclipseContext createWorkbenchContext(final IEclipseContext applicationContext,
			IExtensionRegistry registry, IExceptionHandler exceptionHandler,
			IContributionFactory contributionFactory) {
		Activator
				.trace(
						Policy.DEBUG_CONTEXTS,
						"createWorkbenchContext: initialize the workbench context with needed services", null); //$NON-NLS-1$
		final IEclipseContext mainContext = EclipseContextFactory.create(applicationContext,
				UISchedulerStrategy.getInstance());
		mainContext.set(Logger.class.getName(), ContextInjectionFactory.inject(
				new WorkbenchLogger(), mainContext));
		mainContext.set(IContextConstants.DEBUG_STRING, "WorkbenchContext"); //$NON-NLS-1$

		// setup for commands and handlers
		if (contributionFactory != null) {
			mainContext.set(IContributionFactory.class.getName(), contributionFactory);
		}
		mainContext.set(ContextManager.class.getName(), new ContextManager());
		ContextUtil.commandSetup(mainContext);
		ContextUtil.handlerSetup(mainContext);
		mainContext.set(IServiceConstants.ACTIVE_CONTEXTS, new ActiveContextsFunction());
		mainContext.set(IServiceConstants.ACTIVE_PART, new ActivePartLookupFunction());
		mainContext.runAndTrack(new Runnable() {
			public void run() {
				Object o = mainContext.get(IServiceConstants.ACTIVE_PART);
				if (o instanceof MPart) {
					mainContext.set(IServiceConstants.ACTIVE_PART_ID, ((MPart) o).getId());
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
		mainContext.set(IExceptionHandler.class.getName(), exceptionHandler);
		mainContext.set(IExtensionRegistry.class.getName(), registry);
		mainContext.set(IServiceConstants.SELECTION, new ActiveChildOutputFunction(
				IServiceConstants.SELECTION));
		mainContext.set(IServiceConstants.INPUT, new ContextFunction() {
			public Object compute(IEclipseContext context, Object[] arguments) {
				Class adapterType = null;
				if (arguments.length > 0 && arguments[0] instanceof Class) {
					adapterType = (Class) arguments[0];
				}
				Object newInput = null;
				Object newValue = context.get(IServiceConstants.SELECTION);
				if (adapterType == null || adapterType.isInstance(newValue)) {
					newInput = newValue;
				} else if (newValue != null && adapterType != null) {
					IAdapterManager adapters = (IAdapterManager) applicationContext
							.get(IAdapterManager.class.getName());
					if (adapters != null) {
						Object adapted = adapters.loadAdapter(newValue, adapterType.getName());
						if (adapted != null) {
							newInput = adapted;
						}
					}
				}
				return newInput;
			}
		});
		mainContext.set(IServiceConstants.ACTIVE_SHELL, new ActiveChildLookupFunction(
				IServiceConstants.ACTIVE_SHELL, LOCAL_ACTIVE_SHELL));

		initializeNullStyling(mainContext);

		return mainContext;
	}

	private MApplication createWorkbenchModel(URI applicationDefinitionInstance) {
		handler = new ResourceHandler(instanceLocation, applicationDefinitionInstance,
				saveAndRestore);

		long restoreLastModified = handler.getLastStoreDatetime();
		long lastApplicationModification = getLastApplicationModification(applicationDefinitionInstance);

		boolean restore = restoreLastModified > lastApplicationModification;

		Resource resource;
		if (restore) {
			resource = handler.loadRestoredModel();
			workbench = (MApplication) resource.getContents().get(0);
		} else {
			resource = handler.loadBaseModel();
			MApplication app = (MApplication) resource.getContents().get(0);

			final EList<MWindow> windows = app.getChildren();
			for (MWindow window : windows) {
				processPartContributions(resource, window);
			}

			workbench = app;
		}

		init();

		return workbench;
	}

	private long getLastApplicationModification(URI applicationDefinitionInstance) {
		long appLastModified = 0L;
		ResourceSetImpl resourceSetImpl = new ResourceSetImpl();

		Map<String, ?> attributes = resourceSetImpl.getURIConverter().getAttributes(
				applicationDefinitionInstance,
				Collections.singletonMap(URIConverter.OPTION_REQUESTED_ATTRIBUTES, Collections
						.singleton(URIConverter.ATTRIBUTE_TIME_STAMP)));

		Object timestamp = attributes.get(URIConverter.ATTRIBUTE_TIME_STAMP);
		if (timestamp instanceof Long) {
			appLastModified = ((Long) timestamp).longValue();
		} else if (applicationDefinitionInstance.isPlatformPlugin()) {
			try {
				java.net.URL url = new java.net.URL(applicationDefinitionInstance.toString());
				Object[] obj = PlatformURLPluginConnection.parse(url.getFile().trim(), url);
				Bundle b = (Bundle) obj[0];
				URLConnection openConnection = b.getResource((String) obj[1]).openConnection();
				appLastModified = openConnection.getLastModified();
			} catch (Exception e) {
				// ignore
			}
		}

		return appLastModified;
	}

	private void processPartContributions(Resource resource, MWindow mWindow) {
		IExtensionRegistry registry = RegistryFactory.getRegistry();
		String extId = "org.eclipse.e4.workbench.parts"; //$NON-NLS-1$
		IConfigurationElement[] parts = registry.getConfigurationElementsFor(extId);

		for (int i = 0; i < parts.length; i++) {
			MPart part = MApplicationFactory.eINSTANCE.createPart();
			part.setName(parts[i].getAttribute("label")); //$NON-NLS-1$
			part.setIconURI("platform:/plugin/" //$NON-NLS-1$
					+ parts[i].getContributor().getName() + "/" //$NON-NLS-1$
					+ parts[i].getAttribute("icon")); //$NON-NLS-1$
			part.setURI("platform:/plugin/" //$NON-NLS-1$
					+ parts[i].getContributor().getName() + "/" //$NON-NLS-1$
					+ parts[i].getAttribute("class")); //$NON-NLS-1$
			String parentId = parts[i].getAttribute("parentId"); //$NON-NLS-1$

			Object parent = findObject(resource.getAllContents(), parentId);
			if (parent instanceof MElementContainer<?>) {
				((MElementContainer<MPSCElement>) parent).getChildren().add(part);
			}
		}

	}

	private EObject findObject(TreeIterator<EObject> it, String id) {
		while (it.hasNext()) {
			EObject el = it.next();
			if (el instanceof MApplicationElement) {
				if (el.eResource().getURIFragment(el).equals(id)) {
					return el;
				}
			}
		}

		return null;
	}

	private void init() {
		Activator.trace(Policy.DEBUG_WORKBENCH, "init() workbench", null); //$NON-NLS-1$
		// Capture the MApplication into the context
		workbenchContext.set(MApplication.class.getName(), workbench);
		workbench.setContext(workbenchContext);

		// fill in commands
		Activator.trace(Policy.DEBUG_CMDS, "Initialize service from model", null); //$NON-NLS-1$
		ECommandService cs = (ECommandService) workbenchContext
				.get(ECommandService.class.getName());
		Category cat = cs
				.defineCategory(MApplication.class.getName(), "Application Category", null); //$NON-NLS-1$
		EList<MCommand> commands = workbench.getCommands();
		for (MCommand cmd : commands) {
			String id = cmd.getId();
			String name = cmd.getCommandName();
			cs.defineCommand(id, name, null, cat, null);
		}

		// take care of generating the contexts.
		EList<MWindow> windows = workbench.getChildren();
		for (MWindow window : windows) {
			initializeContext(workbenchContext, window);
		}

		// Hook the global notifications
		((Notifier) workbench).eAdapters().add(new UIEventPublisher(workbench.getContext()));

		// NMH: how do we do this now?
		// workbench.eAdapters().add(new AdapterImpl() {
		// @Override
		// public void notifyChanged(Notification msg) {
		// if (ApplicationPackage.Literals.MAPPLICATION__WINDOWS.equals(msg.getFeature())
		// && msg.getEventType() == Notification.ADD) {
		// MPart<?> added = (MPart<?>) msg.getNewValue();
		// initializeContext(workbenchContext, added);
		// }
		// }
		// });
	}

	/**
	 * Create the context chain. It both creates the chain for the current model, and adds eAdapters
	 * so it can add new contexts when new model items are added.
	 * 
	 * @param parentContext
	 *            The parent context
	 * @param contextModel
	 *            needs a context created
	 */
	public static void initializeContext(IEclipseContext parentContext, MContext contextModel) {
		final IEclipseContext context;
		if (contextModel.getContext() != null) {
			context = contextModel.getContext();
		} else {
			context = EclipseContextFactory
					.create(parentContext, UISchedulerStrategy.getInstance());
			context.set(IContextConstants.DEBUG_STRING, "PartContext(" + contextModel + ')'); //$NON-NLS-1$
		}

		Activator.trace(Policy.DEBUG_CONTEXTS, "initializeContext(" //$NON-NLS-1$
				+ parentContext.toString() + ", " + contextModel + ")", null); //$NON-NLS-1$ //$NON-NLS-2$
		// fill in the interfaces, so MContributedPart.class.getName() will
		// return the model element, for example.
		final Class[] interfaces = contextModel.getClass().getInterfaces();
		for (Class intf : interfaces) {
			Activator.trace(Policy.DEBUG_CONTEXTS, "Adding " + intf.getName() + " for " //$NON-NLS-1$ //$NON-NLS-2$
					+ contextModel.getClass().getName(), null);
			context.set(intf.getName(), contextModel);
		}

		// declares modifiable variables from the model
		EList<String> containedProperties = contextModel.getVariables();
		for (String name : containedProperties) {
			context.declareModifiable(name);
		}

		contextModel.setContext(context);
		processHandlers(contextModel);

		// NMH: how do we do this now?
		// take care of generating the contexts.
		// for (MPart child : (EList<MPart>) contextModel.getChildren()) {
		// initializeContext(context, child);
		// }
		// contextModel.eAdapters().add(new AdapterImpl() {
		// @Override
		// public void notifyChanged(Notification msg) {
		// if (ApplicationPackage.Literals.MPART__CHILDREN.equals(msg.getFeature())
		// && msg.getEventType() == Notification.ADD) {
		// MPart added = (MPart) msg.getNewValue();
		// initializeContext(context, added);
		// }
		// }
		// });
	}

	/**
	 * Should be called prior to running the e4 workench.
	 */
	public void createUIFromModel() {
		EList<MWindow> windows = workbench.getChildren();
		for (MWindow wbw : windows) {
			createGUI(wbw);
		}
	}

	public int run() {
		Activator.trace(Policy.DEBUG_WORKBENCH, "running event loop", null); //$NON-NLS-1$
		windowHandler.runEvenLoop(workbench.getChildren().get(0).getWidget());

		if (handler != null && saveAndRestore && workbench != null) {
			try {
				Activator.trace(Policy.DEBUG_WORKBENCH, "Saving workbench: " //$NON-NLS-1$
						+ ((EObject) workbench).eResource().getURI(), null);
				handler.save();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return rv;
	}

	// NMH: how do we do this now?
	private static void processHandlers(Object me) {

		if (me instanceof MHandlerContainer) {
			MContext contextModel = (MContext) me;
			MHandlerContainer container = (MHandlerContainer) contextModel;
			IEclipseContext context = contextModel.getContext();
			if (context == null) {
				return;
			}
			IContributionFactory cf = (IContributionFactory) context.get(IContributionFactory.class
					.getName());
			if (context != null) {
				EHandlerService hs = (EHandlerService) context.get(EHandlerService.class.getName());
				EList<MHandler> handlers = container.getHandlers();
				for (MHandler handler : handlers) {
					String commandId = handler.getCommand().getId();
					if (handler.getObject() == null) {
						handler.setObject(cf.create(handler.getURI(), context));
					}
					hs.activateHandler(commandId, handler.getObject());
				}
			}
		}
		if (me instanceof MElementContainer<?>) {
			EList children = ((MElementContainer) me).getChildren();
			Iterator i = children.iterator();
			while (i.hasNext()) {
				MUIElement e = (MUIElement) i.next();
				processHandlers(e);
			}
		}
	}

	public void createGUI(MUIElement uiRoot) {
		if (renderer == null) {
			Object newEngine = contributionFactory.create(renderingEngineURI, workbenchContext);
			if (newEngine != null) {
				renderer = (IPresentationEngine) newEngine;
			}
		}

		renderer.createGui(uiRoot);
		if (uiRoot instanceof MWindow) {
			MWindow wbw = (MWindow) uiRoot;
			Object appWindow = wbw.getWidget();
			rv = 0;

			// TODO get access to IApplicationContext to call
			// applicationRunning()
			// Platform.endSplash();

			// A position of 0 is not possible on OS-X because then the
			// title-bar is
			// hidden
			// below the MMenu-Bar
			windowHandler.setBounds(appWindow, wbw.getX(), wbw.getY(), wbw.getWidth(), wbw
					.getHeight());
			windowHandler.layout(appWindow);

			windowHandler.open(appWindow);

			// NMH: how do we do this now ?
			// processHandlers(wbw);
		}
	}

	public void close() {
		EList<MWindow> windows = workbench.getChildren();
		for (MWindow window : windows) {
			windowHandler.dispose(window.getWidget());
		}
	}

	// public Display getDisplay() {
	// return appWindow.getDisplay();
	// }

	public void closeWindow(MWindow workbenchWindow) {
		windowHandler.close(workbenchWindow.getWidget());
	}

	public Object getWindow() {
		return workbench.getChildren().get(0).getWidget();
	}

	/*
	 * For use when there is no real styling engine present. Has no behaviour but conforms to
	 * IStylingEngine API.
	 * 
	 * @param appContext
	 */
	private static void initializeNullStyling(IEclipseContext appContext) {
		appContext.set(IStylingEngine.SERVICE_NAME, new IStylingEngine() {
			public void setClassname(Object widget, String classname) {
			}

			public void setId(Object widget, String id) {
			}

			public void style(Object widget) {
			}
		});
	}

	public MApplication getModel() {
		return workbench;
	}
}
