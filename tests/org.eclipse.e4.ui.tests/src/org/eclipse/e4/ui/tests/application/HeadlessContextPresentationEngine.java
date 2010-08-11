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

package org.eclipse.e4.ui.tests.application;

import java.util.List;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IContextConstants;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.IDisposable;
import org.eclipse.e4.core.services.contributions.IContributionFactory;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.internal.workbench.Activator;
import org.eclipse.e4.ui.internal.workbench.Policy;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.MContribution;
import org.eclipse.e4.ui.model.application.ui.MContext;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MStackElement;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

public class HeadlessContextPresentationEngine implements IPresentationEngine {

	@Inject
	private IEventBroker eventBroker;

	@Inject
	private IContributionFactory contributionFactory;

	private EventHandler childHandler;
	private EventHandler activeChildHandler;
	private EventHandler toBeRenderedHandler;

	private boolean createContributions = true;

	protected IEclipseContext getParentContext(MUIElement element) {
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
		childHandler = new EventHandler() {
			public void handleEvent(Event event) {
				if (UIEvents.EventTypes.ADD.equals(event
						.getProperty(UIEvents.EventTags.TYPE))) {
					Object element = event
							.getProperty(UIEvents.EventTags.NEW_VALUE);
					if (element instanceof MUIElement) {
						Object parent = event
								.getProperty(UIEvents.EventTags.ELEMENT);
						createGui((MUIElement) element, parent,
								getParentContext((MUIElement) element));

						if (parent instanceof MPartStack) {
							MPartStack stack = (MPartStack) parent;
							List<MStackElement> children = stack.getChildren();
							if (children.size() == 1) {
								stack.setSelectedElement((MStackElement) element);
							}
						}
					}
				}
			}
		};

		eventBroker.subscribe(UIEvents.buildTopic(
				UIEvents.ElementContainer.TOPIC,
				UIEvents.ElementContainer.CHILDREN), childHandler);

		activeChildHandler = new EventHandler() {
			public void handleEvent(Event event) {
				Object element = event
						.getProperty(UIEvents.EventTags.NEW_VALUE);
				if (element instanceof MUIElement) {
					Object parent = event
							.getProperty(UIEvents.EventTags.ELEMENT);
					createGui((MUIElement) element, parent,
							getParentContext((MUIElement) element));
				}
			}
		};

		eventBroker.subscribe(UIEvents.buildTopic(
				UIEvents.ElementContainer.TOPIC,
				UIEvents.ElementContainer.SELECTEDELEMENT), activeChildHandler);

		toBeRenderedHandler = new EventHandler() {
			public void handleEvent(Event event) {
				MUIElement element = (MUIElement) event
						.getProperty(UIEvents.EventTags.ELEMENT);
				Boolean value = (Boolean) event
						.getProperty(UIEvents.EventTags.NEW_VALUE);
				if (value.booleanValue()) {
					createGui(element, element.getParent(),
							getParentContext(element));
				} else {
					removeGui(element);
				}
			}
		};

		eventBroker.subscribe(UIEvents.buildTopic(UIEvents.UIElement.TOPIC,
				UIEvents.UIElement.TOBERENDERED), toBeRenderedHandler);
	}

	public void setCreateContributions(boolean createContributions) {
		this.createContributions = createContributions;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.workbench.IPresentationEngine#createGui(org.eclipse
	 * .e4.ui.model.application.MUIElement, java.lang.Object)
	 */
	public Object createGui(MUIElement element, Object parentWidget,
			IEclipseContext parentContext) {
		if (!element.isToBeRendered()) {
			return null;
		}

		if (element instanceof MContext) {
			MContext mcontext = (MContext) element;
			if (mcontext.getContext() != null) {
				return null;
			}

			String contextName = element.getClass().getInterfaces()[0]
					.getName() + " eclipse context"; //$NON-NLS-1$
			final IEclipseContext createdContext = (parentContext != null) ? parentContext
					.createChild(contextName) : EclipseContextFactory
					.create(contextName);

			populateModelInterfaces(mcontext, createdContext, element
					.getClass().getInterfaces());

			for (String variable : mcontext.getVariables()) {
				createdContext.declareModifiable(variable);
			}

			mcontext.setContext(createdContext);

			if (element instanceof MContribution && createContributions) {
				MContribution contribution = (MContribution) element;
				String uri = contribution.getContributionURI();
				if (uri != null) {
					Object clientObject = contributionFactory.create(uri,
							createdContext);
					contribution.setObject(clientObject);
				}
			}

			if (parentContext != null
					&& parentContext.getLocal(IContextConstants.ACTIVE_CHILD) == null) {
				parentContext.set(IContextConstants.ACTIVE_CHILD,
						createdContext);
			}
		}

		if (element instanceof MPartStack) {
			MPartStack container = (MPartStack) element;
			MPart active = (MPart) container.getSelectedElement();
			if (active != null) {
				createGui(active, container, getParentContext(active));
			} else {
				List<MStackElement> children = container.getChildren();
				if (!children.isEmpty()) {
					container.setSelectedElement(children.get(0));
				}
			}
		} else if (element instanceof MElementContainer<?>) {
			for (Object child : ((MElementContainer<?>) element).getChildren()) {
				if (child instanceof MUIElement) {
					createGui((MUIElement) child, element,
							getParentContext((MUIElement) child));
					if (child instanceof MContext) {
						IEclipseContext childContext = ((MContext) child)
								.getContext();
						IEclipseContext pContext = getParentContext((MUIElement) child);
						if (pContext.getLocal(IContextConstants.ACTIVE_CHILD) == null) {
							pContext.set(IContextConstants.ACTIVE_CHILD,
									childContext);
						}
					}
				}
			}

			if (element instanceof MWindow) {
				MWindow window = (MWindow) element;
				for (MWindow childWindow : window.getWindows()) {
					createGui(childWindow, element, window.getContext());
				}
			}

			if (element instanceof MPerspective) {
				MPerspective perspective = (MPerspective) element;
				for (MWindow childWindow : perspective.getWindows()) {
					createGui(childWindow, element, perspective.getContext());
				}
			}
		} else if (element instanceof MPlaceholder) {
			MPlaceholder placeholder = (MPlaceholder) element;
			MUIElement ref = placeholder.getRef();
			if (ref != null) {
				ref.setToBeRendered(true);
				createGui(ref);
				ref.setCurSharedRef(placeholder);
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.workbench.IPresentationEngine#createGui(org.eclipse
	 * .e4.ui.model.application.MUIElement)
	 */
	public Object createGui(MUIElement element) {
		return createGui(element, null, getParentContext(element));
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
	 * org.eclipse.e4.ui.workbench.IPresentationEngine#run(org.eclipse.e4.ui
	 * .model.application.MApplicationElement)
	 */
	public Object run(MApplicationElement uiRoot, IEclipseContext appContext) {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.workbench.IPresentationEngine#stop()
	 */
	public void stop() {
	}
}
