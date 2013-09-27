/*******************************************************************************
 * Copyright (c) 2008, 2010 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.ui.internal.workbench;

import java.util.Iterator;
import java.util.List;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.contributions.IContributionFactory;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MHandler;
import org.eclipse.e4.ui.model.application.commands.MHandlerContainer;
import org.eclipse.e4.ui.model.application.ui.MContext;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.e4.ui.workbench.modeling.ExpressionContext;
import org.eclipse.emf.common.notify.Notifier;

public class E4Workbench implements IWorkbench {
	public static final String LOCAL_ACTIVE_SHELL = "localActiveShell"; //$NON-NLS-1$
	public static final String XMI_URI_ARG = "applicationXMI"; //$NON-NLS-1$
	public static final String CSS_URI_ARG = "applicationCSS"; //$NON-NLS-1$
	public static final String CSS_RESOURCE_URI_ARG = "applicationCSSResources"; //$NON-NLS-1$
	public static final String PRESENTATION_URI_ARG = "presentationURI"; //$NON-NLS-1$
	public static final String LIFE_CYCLE_URI_ARG = "lifeCycleURI"; //$NON-NLS-1$
	public static final String PERSIST_STATE = "persistState"; //$NON-NLS-1$
	public static final String INITIAL_WORKBENCH_MODEL_URI = "initialWorkbenchModelURI"; //$NON-NLS-1$
	public static final String INSTANCE_LOCATION = "instanceLocation"; //$NON-NLS-1$
	public static final String MODEL_RESOURCE_HANDLER = "modelResourceHandler"; //$NON-NLS-1$
	public static final String RENDERER_FACTORY_URI = "rendererFactoryUri"; //$NON-NLS-1$

	public static final String CLEAR_PERSISTED_STATE = "clearPersistedState"; //$NON-NLS-1$
	public static final String DELTA_RESTORE = "deltaRestore"; //$NON-NLS-1$

	public static final String RTL_MODE = "dir"; //$NON-NLS-1$
	public static final String NO_SAVED_MODEL_FOUND = "NO_SAVED_MODEL_FOUND"; //$NON-NLS-1$


	IEclipseContext appContext;
	IPresentationEngine renderer;
	MApplication appModel = null;
	private UIEventPublisher uiEventPublisher;

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

		uiEventPublisher = new UIEventPublisher(appContext);
		((Notifier) uiRoot).eAdapters().add(uiEventPublisher);
	}

	/**
	 * @param renderingEngineURI
	 * @param cssURI
	 * @param cssResourcesURI
	 */
	public void createAndRunUI(MApplicationElement uiRoot) {
		// Has someone already created one ?
		instantiateRenderer();

		if (renderer != null) {
			renderer.run(uiRoot, appContext);
		}
	}

	/**
	 * 
	 */
	public void instantiateRenderer() {
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
	}

	private void init(MApplication appElement) {
		Activator.trace(Policy.DEBUG_WORKBENCH, "init() workbench", null); //$NON-NLS-1$

		IEclipseContext context = appElement.getContext();
		if (context != null) {
			context.set(ExpressionContext.ALLOW_ACTIVATION, Boolean.TRUE);
		}
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
		if (uiEventPublisher != null && appModel != null) {
			((Notifier) appModel).eAdapters().remove(uiEventPublisher);
			uiEventPublisher = null;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.workbench.IWorkbench#run()
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
					MCommand command = handler.getCommand();
					if (command == null)
						continue;
					String commandId = command.getElementId();
					if (handler.getObject() == null) {
						handler.setObject(cf.create(handler.getContributionURI(), context));
					}
					hs.activateHandler(commandId, handler.getObject());
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
			context = parentContext.createChild("PartContext(" + contextModel + ')'); //$NON-NLS-1$
		}

		Activator.trace(Policy.DEBUG_CONTEXTS, "initializeContext(" //$NON-NLS-1$
				+ parentContext.toString() + ", " + contextModel + ")", null); //$NON-NLS-1$ //$NON-NLS-2$
		// fill in the interfaces, so MContributedPart.class.getName() will
		// return the model element, for example.
		ContributionsAnalyzer.populateModelInterfaces(contextModel, context, contextModel
				.getClass().getInterfaces());

		// declares modifiable variables from the model
		List<String> containedProperties = contextModel.getVariables();
		for (String name : containedProperties) {
			context.declareModifiable(name);
		}

		contextModel.setContext(context);
		return context;
	}

}
