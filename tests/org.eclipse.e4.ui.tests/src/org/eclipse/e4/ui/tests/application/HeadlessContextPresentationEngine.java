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

package org.eclipse.e4.ui.tests.application;

import javax.inject.Inject;

import org.eclipse.e4.core.services.IDisposable;
import org.eclipse.e4.core.services.annotations.PostConstruct;
import org.eclipse.e4.core.services.context.EclipseContextFactory;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.IContextConstants;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.MContext;
import org.eclipse.e4.ui.model.application.MElementContainer;
import org.eclipse.e4.ui.model.application.MUIElement;
import org.eclipse.e4.ui.services.events.IEventBroker;
import org.eclipse.e4.workbench.ui.IPresentationEngine;
import org.eclipse.e4.workbench.ui.UIEvents;
import org.eclipse.e4.workbench.ui.internal.Activator;
import org.eclipse.e4.workbench.ui.internal.Policy;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

public class HeadlessContextPresentationEngine implements IPresentationEngine {

	@Inject
	private IEventBroker eventBroker;

	private static IEclipseContext getParentContext(MUIElement element) {
		MElementContainer<MUIElement> parent = element.getParent();
		IEclipseContext context = null;
		while (parent != null) {
			if (parent instanceof MContext) {
				return ((MContext) parent).getContext();
			}
			parent = parent.getParent();
		}

		return context;
	}

	private static void populateModelInterfaces(MContext contextModel,
			IEclipseContext context, Class<?>[] interfaces) {
		for (Class<?> intf : interfaces) {
			Activator.trace(Policy.DEBUG_CONTEXTS,
					"Adding " + intf.getName() + " for " //$NON-NLS-1$ //$NON-NLS-2$
							+ contextModel.getClass().getName(), null);
			context.set(intf.getName(), contextModel);

			populateModelInterfaces(contextModel, context, intf.getInterfaces());
		}
	}

	@PostConstruct
	void postConstruct() {
		eventBroker.subscribe(UIEvents.buildTopic(
				UIEvents.ElementContainer.TOPIC,
				UIEvents.ElementContainer.CHILDREN), new EventHandler() {
			public void handleEvent(Event event) {
				Object element = event
						.getProperty(UIEvents.EventTags.NEW_VALUE);
				if (element instanceof MUIElement) {
					Object parent = event
							.getProperty(UIEvents.EventTags.ELEMENT);
					createGui((MUIElement) element, parent);
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.workbench.ui.IPresentationEngine#createGui(org.eclipse
	 * .e4.ui.model.application.MUIElement, java.lang.Object)
	 */
	public Object createGui(MUIElement element, Object parent) {
		if (element instanceof MContext) {
			final IEclipseContext parentContext = getParentContext(element);
			MContext mcontext = (MContext) element;
			final IEclipseContext createdContext = EclipseContextFactory
					.create(parentContext, null);

			createdContext.set(IContextConstants.DEBUG_STRING, element
					.getClass().getInterfaces()[0].getName()
					+ " eclipse context"); //$NON-NLS-1$
			populateModelInterfaces(mcontext, createdContext, element
					.getClass().getInterfaces());

			for (String variable : mcontext.getVariables()) {
				createdContext.declareModifiable(variable);
			}

			mcontext.setContext(createdContext);
		}
		if (element instanceof MElementContainer<?>) {
			boolean active = false;
			for (Object child : ((MElementContainer<?>) element).getChildren()) {
				if (child instanceof MUIElement) {
					createGui((MUIElement) child, element);
					if (!active) {
						active = true;
						((MElementContainer) element)
								.setActiveChild((MUIElement) child);
						if (child instanceof MContext) {
							IEclipseContext childContext = ((MContext) child)
									.getContext();
							IEclipseContext parentContext = getParentContext((MUIElement) child);
							parentContext.set(IContextConstants.ACTIVE_CHILD,
									childContext);
						}
					}
				}
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.workbench.ui.IPresentationEngine#createGui(org.eclipse
	 * .e4.ui.model.application.MUIElement)
	 */
	public Object createGui(MUIElement element) {
		return createGui(element, null);
	}

	public void removeGui(MUIElement element) {
		if (element instanceof MElementContainer<?>) {
			for (Object child : ((MElementContainer<?>) element).getChildren()) {
				if (child instanceof MUIElement) {
					removeGui((MUIElement) child);
				}
			}
		}

		if (element instanceof MContext) {
			MContext mcontext = (MContext) element;
			IEclipseContext context = mcontext.getContext();

			mcontext.setContext(null);
			if (context instanceof IDisposable) {
				((IDisposable) context).dispose();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.workbench.ui.IPresentationEngine#run(org.eclipse.e4.ui
	 * .model.application.MApplicationElement)
	 */
	public Object run(MApplicationElement uiRoot, IEclipseContext appContext) {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.workbench.ui.IPresentationEngine#stop()
	 */
	public void stop() {
	}
}
