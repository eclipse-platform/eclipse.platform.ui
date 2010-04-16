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

import java.util.Iterator;
import java.util.List;
import org.eclipse.core.commands.contexts.ContextManager;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.ContextChangeEvent;
import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IContextConstants;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.contexts.IRunAndTrack;
import org.eclipse.e4.core.services.contributions.IContributionFactory;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.internal.services.ActiveContextsFunction;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.commands.MBindings;
import org.eclipse.e4.ui.model.application.commands.MHandler;
import org.eclipse.e4.ui.model.application.commands.MHandlerContainer;
import org.eclipse.e4.ui.model.application.ui.MContext;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.EContextService;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.e4.workbench.ui.IExceptionHandler;
import org.eclipse.e4.workbench.ui.IPresentationEngine;
import org.eclipse.e4.workbench.ui.IWorkbench;
import org.eclipse.emf.common.notify.Notifier;

public class E4Workbench implements IWorkbench {
	public static final String LOCAL_ACTIVE_SHELL = "localActiveShell"; //$NON-NLS-1$
	public static final String XMI_URI_ARG = "applicationXMI"; //$NON-NLS-1$
	public static final String CSS_URI_ARG = "applicationCSS"; //$NON-NLS-1$
	public static final String CSS_RESOURCE_URI_ARG = "applicationCSSResources"; //$NON-NLS-1$
	public static final String PRESENTATION_URI_ARG = "presentationURI"; //$NON-NLS-1$
	public static final String LIFE_CYCLE_URI_ARG = "lifeCycleURI"; //$NON-NLS-1$

	IEclipseContext appContext;
	IPresentationEngine renderer;
	MApplication appModel = null;

	public IEclipseContext getContext() {
		return appContext;
	}

	public E4Workbench(MApplicationElement uiRoot, IEclipseContext applicationContext) {
		appContext = applicationContext;
		appContext.set(IWorkbench.class.getName(), this);
		if (uiRoot instanceof MApplication) {
			appModel = (MApplication) uiRoot;
		}

		if (uiRoot instanceof MApplication) {
			init((MApplication) uiRoot);
		}

		// Hook the global notifications
		((Notifier) uiRoot).eAdapters().add(new UIEventPublisher(appContext));
	}

	/**
	 * @param renderingEngineURI
	 * @param cssURI
	 * @param cssResourcesURI
	 */
	public void createAndRunUI(MApplicationElement uiRoot) {
		// Has someone already created one ?
		renderer = (IPresentationEngine) appContext.get(IPresentationEngine.class.getName());
		if (renderer == null) {
			String presentationURI = (String) appContext.get(PRESENTATION_URI_ARG);
			if (presentationURI != null) {
				IContributionFactory factory = (IContributionFactory) appContext
						.get(IContributionFactory.class.getName());
				renderer = (IPresentationEngine) factory.create(presentationURI, appContext);
				appContext.set(IPresentationEngine.class.getName(), renderer);
			}
			if (renderer == null) {
				Logger logger = (Logger) appContext.get(Logger.class.getName());
				logger.error("Failed to create the presentation engine for URI: " + presentationURI); //$NON-NLS-1$
			}
		}

		if (renderer != null) {
			renderer.run(uiRoot, appContext);
		}
	}

	private void init(MApplication appElement) {
		Activator.trace(Policy.DEBUG_WORKBENCH, "init() workbench", null); //$NON-NLS-1$

		E4CommandProcessor.processCommands(appElement.getContext(), appElement.getCommands());
		E4CommandProcessor.processBindings(appElement.getContext(), appElement);

		// Do a top level processHierarchy for the application?
		processHierarchy(appElement);
	}

	/**
	 * @return
	 * @return
	 */
	public boolean close() {
		if (renderer != null) {
			renderer.stop();
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.workbench.ui.IWorkbench#run()
	 */
	public int run() {
		// TODO Auto-generated method stub
		return 0;
	}

	public static IEclipseContext getServiceContext() {
		return EclipseContextFactory.getServiceContext(Activator.getDefault().getContext());
	}

	public MApplication getApplication() {
		return appModel;
	}

	public static IEclipseContext createWorkbenchContext(final IEclipseContext applicationContext,
			IExtensionRegistry registry, IExceptionHandler exceptionHandler,
			IContributionFactory contributionFactory) {
		Activator
				.trace(
						Policy.DEBUG_CONTEXTS,
						"createWorkbenchContext: initialize the workbench context with needed services", null); //$NON-NLS-1$
		final IEclipseContext mainContext = EclipseContextFactory.create(applicationContext, null);
		mainContext.set(Logger.class.getName(), ContextInjectionFactory.inject(
				new WorkbenchLogger(), mainContext));
		mainContext.set(IContextConstants.DEBUG_STRING, "WorkbenchContext"); //$NON-NLS-1$

		// setup for commands and handlers
		if (contributionFactory != null) {
			mainContext.set(IContributionFactory.class.getName(), contributionFactory);
		}
		mainContext.set(ContextManager.class.getName(), new ContextManager());

		mainContext.set(IServiceConstants.ACTIVE_CONTEXTS, new ActiveContextsFunction());
		mainContext.set(IServiceConstants.ACTIVE_PART, new ActivePartLookupFunction());
		mainContext.runAndTrack(new IRunAndTrack() {
			public boolean notify(ContextChangeEvent event) {
				Object o = mainContext.get(IServiceConstants.ACTIVE_PART);
				if (o instanceof MPart) {
					mainContext.set(IServiceConstants.ACTIVE_PART_ID, ((MPart) o).getElementId());
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
		}, null);
		// EHandlerService comes from a ContextFunction
		// EContextService comes from a ContextFunction
		mainContext.set(IExceptionHandler.class.getName(), exceptionHandler);
		mainContext.set(IExtensionRegistry.class.getName(), registry);
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
				IServiceConstants.ACTIVE_SHELL, null));

		initializeNullStyling(mainContext);

		return mainContext;
	}

	public static void processHierarchy(Object me) {
		if (me instanceof MHandlerContainer) {
			MContext contextModel = (MContext) me;
			MHandlerContainer container = (MHandlerContainer) contextModel;
			IEclipseContext context = contextModel.getContext();
			if (context != null) {
				IContributionFactory cf = (IContributionFactory) context
						.get(IContributionFactory.class.getName());
				EHandlerService hs = (EHandlerService) context.get(EHandlerService.class.getName());
				List<MHandler> handlers = container.getHandlers();
				for (MHandler handler : handlers) {
					String commandId = handler.getCommand().getElementId();
					if (handler.getObject() == null) {
						handler.setObject(cf.create(handler.getContributionURI(), context));
					}
					hs.activateHandler(commandId, handler.getObject());
				}
			}
		}
		if (me instanceof MBindings) {
			MContext contextModel = (MContext) me;
			MBindings container = (MBindings) me;
			List<String> bindingContexts = container.getBindingContexts();
			IEclipseContext context = contextModel.getContext();
			if (context != null && !bindingContexts.isEmpty()) {
				EContextService cs = (EContextService) context.get(EContextService.class.getName());
				for (String id : bindingContexts) {
					cs.activateContext(id);
				}
			}
		}
		if (me instanceof MElementContainer<?>) {
			List<MUIElement> children = ((MElementContainer) me).getChildren();
			Iterator<MUIElement> i = children.iterator();
			while (i.hasNext()) {
				MUIElement e = i.next();
				processHierarchy(e);
			}
		}
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
	public static IEclipseContext initializeContext(IEclipseContext parentContext,
			MContext contextModel) {
		final IEclipseContext context;
		if (contextModel.getContext() != null) {
			context = contextModel.getContext();
		} else {
			context = EclipseContextFactory.create(parentContext, null);
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
		List<String> containedProperties = contextModel.getVariables();
		for (String name : containedProperties) {
			context.declareModifiable(name);
		}

		contextModel.setContext(context);
		return context;
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
}
