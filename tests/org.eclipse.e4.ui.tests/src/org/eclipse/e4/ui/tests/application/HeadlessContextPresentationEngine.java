/*******************************************************************************
 * Copyright (c) 2009, 2023 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.e4.ui.tests.application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import org.eclipse.core.runtime.Assert;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.contributions.IContributionFactory;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.internal.workbench.Activator;
import org.eclipse.e4.ui.internal.workbench.Policy;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.MContribution;
import org.eclipse.e4.ui.model.application.ui.MContext;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MGenericStack;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MStackElement;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.emf.ecore.EObject;
import org.osgi.service.event.EventHandler;

public class HeadlessContextPresentationEngine implements IPresentationEngine {

	@Inject
	private IEventBroker eventBroker;

	@Inject
	private IContributionFactory contributionFactory;

	private EventHandler childHandler;
	private EventHandler activeChildHandler;
	private EventHandler toBeRenderedHandler;

	private Map<MUIElement, List<MPlaceholder>> renderedPlaceholders = new HashMap<>();

	private boolean createContributions = true;

	@Inject
	private EModelService modelService;

	protected IEclipseContext getParentContext(MUIElement element) {
		return modelService.getContainingContext(element);
	}

	private static void populateModelInterfaces(MContext contextModel,
			IEclipseContext context, Class<?>[] interfaces) {
		for (Class<?> intf : interfaces) {
			if (Policy.DEBUG_CONTEXTS) {
				Activator.trace(Policy.DEBUG_CONTEXTS_FLAG, "Adding " + intf.getName() + " for " //$NON-NLS-1$ //$NON-NLS-2$
						+ contextModel.getClass().getName(), null);
			}
			context.set(intf.getName(), contextModel);

			populateModelInterfaces(contextModel, context, intf.getInterfaces());
		}
	}

	@PostConstruct
	void postConstruct() {
		childHandler = event -> {
			if (UIEvents.isADD(event)) {
				for (Object element : UIEvents.asIterable(event,
						UIEvents.EventTags.NEW_VALUE)) {
					if (element instanceof MUIElement e) {
						Object parent = event
								.getProperty(UIEvents.EventTags.ELEMENT);
						IEclipseContext parentContext = getParentContext(e);
						if (element instanceof MContext c) {
							IEclipseContext context = c.getContext();
							if (context != null
									&& context.getParent() != parentContext) {
								context.deactivate();
							}
						}
						createGui(e, parent, parentContext);

						if (parent instanceof MPartStack stack) {
							List<MStackElement> children = stack
									.getChildren();
							MStackElement stackElement = (MStackElement) element;
							if (children.size() == 1
									&& stackElement.isVisible()
									&& stackElement.isToBeRendered()) {
								stack.setSelectedElement(stackElement);
							}
						}
					}
				}
			}
		};

		eventBroker.subscribe(UIEvents.ElementContainer.TOPIC_CHILDREN,
				childHandler);

		activeChildHandler = event -> {
			Object element = event
					.getProperty(UIEvents.EventTags.NEW_VALUE);
			if (element instanceof MUIElement uiElement) {
				Object parent = event
						.getProperty(UIEvents.EventTags.ELEMENT);
				if (parent instanceof MGenericStack) {
					IEclipseContext parentContext = getParentContext(uiElement);
					createGui(uiElement, parent, parentContext);

					if (parent instanceof MPerspectiveStack) {
						MPerspective perspective = (MPerspective) uiElement;
						adjustPlaceholders(perspective);
						parentContext.get(EPartService.class)
								.switchPerspective(perspective);
					}
				}
			}
		};

		eventBroker.subscribe(UIEvents.ElementContainer.TOPIC_SELECTEDELEMENT,
				activeChildHandler);

		toBeRenderedHandler = event -> {
			MUIElement element = (MUIElement) event
					.getProperty(UIEvents.EventTags.ELEMENT);
			Boolean value = (Boolean) event
					.getProperty(UIEvents.EventTags.NEW_VALUE);
			if (value.booleanValue()) {
				createGui(element);
			} else {
				removeGui(element);
			}
		};

		eventBroker.subscribe(UIEvents.UIElement.TOPIC_TOBERENDERED,
				toBeRenderedHandler);
	}

	@PreDestroy
	void preDestroy() {
		eventBroker.unsubscribe(childHandler);
		eventBroker.unsubscribe(activeChildHandler);
		eventBroker.unsubscribe(toBeRenderedHandler);
	}

	private void adjustPlaceholders(MUIElement element) {
		if (element.isToBeRendered()) {
			if (element instanceof MPlaceholder placeholder) {
				MUIElement ref = placeholder.getRef();
				if (ref != null) {
					ref.setCurSharedRef(placeholder);
					element = ref;
				}
			}

			if (element instanceof MGenericStack<?> stack) {
				Object selectedElement = stack.getSelectedElement();
				if (selectedElement != null) {
					adjustPlaceholders((MUIElement) selectedElement);
				}
			} else if (element instanceof MElementContainer<?> container) {
				for (Object child : container.getChildren()) {
					adjustPlaceholders((MUIElement) child);
				}
			}
		}
	}

	public void setCreateContributions(boolean createContributions) {
		this.createContributions = createContributions;
	}

	@Override
	public Object createGui(MUIElement element, Object parentWidget,
			IEclipseContext parentContext) {
		MUIElement current = element;
		while (current != null) {
			if (!current.isToBeRendered()) {
				return null;
			}
			if (current.getCurSharedRef() != null) {
				current = current.getCurSharedRef();
			} else {
				current = current.getParent();
			}
		}

		MUIElement parent = element.getParent();
		if (element.getCurSharedRef() != null) {
			parent = element.getCurSharedRef().getParent();
		}

		if (!(parent instanceof MApplication)) {
			// if the element is not under the application, it should have a
			// parent widget
			Assert.isNotNull(parentWidget);
		}

		if (element.getWidget() != null) {
			if (element instanceof MContext c) {
				IEclipseContext context = c.getContext();
				if (context.getParent() != parentContext) {
					context.setParent(parentContext);
				}
			}
			return element.getWidget();
		}

		element.setRenderer(this);

		Object widget = new Object();
		element.setWidget(widget);

		if (element instanceof MContext mcontext) {
			IEclipseContext createdContext = mcontext.getContext();
			if (createdContext == null) {
				String contextName = element.getClass().getInterfaces()[0]
						.getName() + " eclipse context"; //$NON-NLS-1$
				createdContext = (parentContext != null) ? parentContext
						.createChild(contextName) : EclipseContextFactory
						.create(contextName);

				populateModelInterfaces(mcontext, createdContext, element
						.getClass().getInterfaces());

				for (String variable : mcontext.getVariables()) {
					createdContext.declareModifiable(variable);
				}

				mcontext.setContext(createdContext);

				if (element instanceof MContribution contribution && createContributions) {
					String uri = contribution.getContributionURI();
					if (uri != null) {
						Object clientObject = contributionFactory.create(uri,
								createdContext);
						contribution.setObject(clientObject);
					}
				}

				if (parentContext != null
						&& parentContext.getActiveChild() == null) {
					createdContext.activate();
				}
			} else if (createdContext.getParent() != parentContext) {
				createdContext.setParent(parentContext);
			}
		}

		if (element instanceof MGenericStack container) {
			MUIElement active = container.getSelectedElement();
			if (active != null) {
				createGui(active, container, getParentContext(active));
			} else {
				List<?> children = container.getChildren();
				if (!children.isEmpty()) {
					((MElementContainer) element)
							.setSelectedElement((MUIElement) children.get(0));
				}
			}
		} else if (element instanceof MElementContainer<?> c) {
			for (Object child : c.getChildren()) {
				if (child instanceof MUIElement e) {
					createGui(e, element, getParentContext(e));
					if (child instanceof MContext mc) {
						IEclipseContext childContext = mc.getContext();
						IEclipseContext pContext = getParentContext(e);
						if (childContext != null
								&& pContext.getActiveChild() == null) {
							childContext.activate();
						}
					}
				}
			}

			if (element instanceof MWindow window) {
				for (MWindow childWindow : window.getWindows()) {
					createGui(childWindow, element, window.getContext());
				}
			}

			if (element instanceof MPerspective perspective) {
				for (MWindow childWindow : perspective.getWindows()) {
					createGui(childWindow, element, perspective.getContext());
				}
			}
		} else if (element instanceof MPlaceholder placeholder) {
			MUIElement ref = placeholder.getRef();
			if (ref != null) {
				ref.setCurSharedRef(placeholder);
				ref.setToBeRendered(true);
				createGui(ref);

				List<MPlaceholder> placeholders = renderedPlaceholders.get(ref);
				if (placeholders == null) {
					placeholders = new ArrayList<>();
					renderedPlaceholders.put(ref, placeholders);
				} else if (placeholders.contains(placeholder)) {
					return null;
				}

				placeholders.add(placeholder);
			}
		}
		return widget;
	}

	@Override
	public Object createGui(MUIElement element) {
		MUIElement placeholder = element.getCurSharedRef();
		if (placeholder != null) {
			return createGui(element, placeholder.getWidget(),
					getParentContext(element));
		}

		MUIElement parent = element.getParent();
		if (parent == null) {
			parent = (MUIElement) ((EObject) element).eContainer();
		}
		return createGui(element, parent.getWidget(), getParentContext(element));
	}

	@Override
	public void removeGui(MUIElement element) {
		if (element instanceof MElementContainer<?> c) {
			for (Object child : c.getChildren()) {
				if (child instanceof MUIElement e) {
					removeGui(e);
				}
			}
		}

		if (element instanceof MPlaceholder p) {
			removePlaceholder(p);
		}

		if (element instanceof MContext mcontext) {
			IEclipseContext context = mcontext.getContext();
			if (context != null) {
				IEclipseContext parentContext = context.getParent();
				if (parentContext != null
						&& parentContext.getActiveChild() == context) {
					context.deactivate();
				}
			}

			mcontext.setContext(null);
			if (context != null) {
				context.dispose();
			}
		}

		element.setRenderer(null);
		element.setWidget(null);
	}

	private void removePlaceholder(MPlaceholder placeholder) {
		MUIElement ref = placeholder.getRef();
		List<MPlaceholder> placeholders = renderedPlaceholders.get(placeholder
				.getRef());
		placeholders.remove(placeholder);

		if (placeholders.isEmpty()) {
			// no other placeholders around, unrender the element
			removeGui(ref);
			renderedPlaceholders.remove(ref);
		} else {
			IEclipseContext currentContext = modelService
					.getContainingContext(placeholder);
			// find another placeholder to put the element under
			for (MPlaceholder other : placeholders) {
				IEclipseContext newParentContext = modelService
						.getContainingContext(other);
				if (newParentContext != null) {
					List<MContext> contextElements = modelService.findElements(ref, null, MContext.class);
					for (MContext contextElement : contextElements) {
						IEclipseContext context = contextElement.getContext();
						// this is currently pointing at the placeholder's
						// containing context, reparent it
						if (context.getParent() == currentContext) {
							context.setParent(newParentContext);
						}
					}
					ref.setCurSharedRef(other);
					break;
				}
			}
		}
	}

	@Override
	public Object run(MApplicationElement uiRoot, IEclipseContext appContext) {
		return 0;
	}

	@Override
	public void stop() {
	}

	@Override
	public void focusGui(MUIElement element) {
		Object implementation = element instanceof MContribution c ? c.getObject() : null;
		if (implementation != null) {
			IEclipseContext context = getContext(element);
			Object defaultValue = new Object();
			Object returnValue = ContextInjectionFactory.invoke(implementation,
					Focus.class, context, defaultValue);
			if (returnValue == defaultValue) {
				System.err.println("No @Focus method");
			}
		}
	}

	private IEclipseContext getContext(MUIElement parent) {
		if (parent instanceof MContext c) {
			return c.getContext();
		}
		return modelService.getContainingContext(parent);
	}
}
