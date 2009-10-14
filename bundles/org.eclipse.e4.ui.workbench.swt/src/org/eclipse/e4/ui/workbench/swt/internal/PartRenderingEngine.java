/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.swt.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.e4.core.services.IContributionFactory;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplicationPackage;
import org.eclipse.e4.ui.model.application.MElementContainer;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.application.MUIElement;
import org.eclipse.e4.ui.workbench.swt.factories.IRendererFactory;
import org.eclipse.e4.workbench.ui.IPresentationEngine;
import org.eclipse.e4.workbench.ui.internal.Activator;
import org.eclipse.e4.workbench.ui.internal.Policy;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.emf.ecore.EObject;

public class PartRenderingEngine implements IPresentationEngine {
	public static final String engineURI = "platform:/plugin/org.eclipse.e4.ui.workbench.swt/"
			+ "org.eclipse.e4.ui.workbench.swt.internal.PartRenderingEngine";

	private String defaultRenderingFactoryId = "org.eclipse.e4.ui.workbench.renderers.default";
	private String curFactoryId = defaultRenderingFactoryId;
	IRendererFactory curFactory = null;

	// Life Cycle listeners
	private AdapterImpl visibilityListener = new AdapterImpl() {
		@Override
		public void notifyChanged(Notification msg) {
			if (MApplicationPackage.Literals.UI_ELEMENT__VISIBLE.equals(msg
					.getFeature())) {
				// skip no-ops
				if (msg.getOldBooleanValue() == msg.getNewBooleanValue())
					return;

				MUIElement changedPart = (MUIElement) msg.getNotifier();

				// If the parent isn't displayed who cares?
				MElementContainer<?> parent = changedPart.getParent();
				AbstractPartRenderer parentFactory = parent != null ? getFactoryFor(parent)
						: null;
				if (parentFactory == null)
					return;

				if (changedPart.isVisible()) {
					Activator.trace(Policy.DEBUG_RENDERER,
							"visible -> true", null); //$NON-NLS-1$

					// Note that the 'createGui' protocol calls 'childAdded'
					createGui(changedPart);
				} else {
					Activator.trace(Policy.DEBUG_RENDERER,
							"visible -> false", null); //$NON-NLS-1$

					// Note that the 'createGui' protocol calls 'childRemoved'
					removeGui(changedPart);
				}
			}
		}
	};

	private AdapterImpl childrenListener = new AdapterImpl() {
		@Override
		public void notifyChanged(Notification msg) {
			if (MApplicationPackage.Literals.ELEMENT_CONTAINER__CHILDREN
					.equals(msg.getFeature())) {
				MElementContainer<MUIElement> changedElement = (MElementContainer<MUIElement>) msg
						.getNotifier();
				AbstractPartRenderer factory = getFactoryFor(changedElement);

				// If the parent isn't in the UI then who cares?
				if (factory == null)
					return;

				if (msg.getEventType() == Notification.ADD) {
					Activator.trace(Policy.DEBUG_RENDERER, "Child Added", null); //$NON-NLS-1$
					MPart added = (MPart) msg.getNewValue();
					// Adding invisible elements is a NO-OP as far as the
					// renderer is concerned
					if (!added.isVisible()) {
						installLifeCycleHooks(added);
						return;
					}

					// OK, we have a new -visible- part we either have to create
					// it or host it under the correct parent
					if (added.getWidget() == null)
						// NOTE: createGui will call 'childAdded' if successful
						createGui(added);
					else {
						factory.childRendered(changedElement, added);
					}
				} else if (msg.getEventType() == Notification.REMOVE) {
					Activator.trace(Policy.DEBUG_RENDERER,
							"Child Removed", null); //$NON-NLS-1$
					MPart removed = (MPart) msg.getOldValue();
					// Removing invisible elements is a NO-OP as far as the
					// renderer is concerned
					if (!removed.isVisible())
						return;

					factory.hideChild(changedElement, removed);
				}
			}
		}
	};

	private IContributionFactory contributionFactory;
	private IEclipseContext context;

	public PartRenderingEngine(IContributionFactory factory,
			IEclipseContext context, IExtensionRegistry registry) {
		initialize(factory, context, registry);
	}

	/**
	 * Initialize a part renderer from the extension point.
	 * 
	 * @param registry
	 *            the registry for the EP
	 * @param r
	 *            the created renderer
	 * @param context
	 *            the context for the part factories
	 * @param f
	 *            the IContributionFactory already provided to <code>r</code>
	 */
	public void initialize(IContributionFactory factoryIn,
			IEclipseContext context, IExtensionRegistry registry) {
		this.contributionFactory = factoryIn;
		this.context = context;

		// add the factories from the extension point, sort by dependency
		// * Need to make the EP more declarative to avoid aggressive
		// loading
		IConfigurationElement[] factories = registry
				.getConfigurationElementsFor("org.eclipse.e4.workbench.rendererfactory"); //$NON-NLS-1$
		for (int i = 0; i < factories.length; i++) {
			String id = factories[i].getAttribute("id");
			if (!curFactoryId.equals(id))
				continue;

			IRendererFactory factory = null;
			try {
				factory = (IRendererFactory) factories[i]
						.createExecutableExtension("class"); //$NON-NLS-1$
			} catch (CoreException e) {
				e.printStackTrace();
			}

			if (factory != null) {
				factory.init(this, context, contributionFactory);
				curFactory = factory;
			}
		}

		// Add the renderer to the context
		context.set(PartRenderingEngine.SERVICE_NAME, this);
	}

	public Object createGui(MUIElement element, Object parent) {
		// Life-cycle hooks
		installLifeCycleHooks(element);

		if (!element.isVisible())
			return null;

		// Create a control appropriate to the part
		Object newWidget = createWidget(element, parent);

		// Remember that we've created the control
		if (newWidget != null) {
			// Process its internal structure through the factory that created
			// it
			AbstractPartRenderer factory = getFactoryFor(element);

			factory.hookControllerLogic(element);

			if (element instanceof MElementContainer) {
				factory
						.processContents((MElementContainer<MUIElement>) element);
			}

			factory.postProcess(element);

			// Now that we have a widget let the parent (if any) know
			if (element.getParent() instanceof MUIElement) {
				MElementContainer<MUIElement> parentElement = element
						.getParent();
				AbstractPartRenderer parentFactory = getFactoryFor(parentElement);
				if (parentFactory != null)
					parentFactory.childRendered(parentElement, element);
			}
		}

		return newWidget;
	}

	public Object createGui(MUIElement element) {
		// Obtain the necessary parent and context
		Object parent = null;
		MUIElement parentME = element.getParent();
		if (parentME != null) {
			AbstractPartRenderer factory = getFactoryFor(parentME);
			if (factory != null) {
				parent = factory.getUIContainer(element);
			}
		}

		return createGui(element, parent);
	}

	/**
	 * @param element
	 */
	public void removeGui(MUIElement element) {
		AbstractPartRenderer factory = getFactoryFor(element);
		assert (factory != null);

		MUIElement parent = element.getParent();
		AbstractPartRenderer parentFactory = parent != null ? getFactoryFor(parent)
				: null;
		if (parentFactory == null)
			return;

		// Remove the child from its current parent's -Composite- this does NOT
		// mean removing the model element from its parent's model
		parentFactory.hideChild(element.getParent(), element);

		factory.disposeWidget(element);
	}

	/**
	 * @param element
	 *            an element that's been seen by createGui
	 */
	private void installLifeCycleHooks(MUIElement element) {
		// Handle visibility changes
		if (!((EObject) element).eAdapters().contains(visibilityListener))
			((EObject) element).eAdapters().add(visibilityListener);

		// Handle children
		if (!((EObject) element).eAdapters().contains(childrenListener))
			((EObject) element).eAdapters().add(childrenListener);
	}

	protected Object createWidget(MUIElement element, Object parent) {
		AbstractPartRenderer renderer = getRenderer(element, parent);
		if (renderer != null) {
			Object newWidget = renderer.createWidget(element, parent);
			if (newWidget != null) {
				renderer.bindWidget(element, newWidget);
				processHandlers(element);
				return newWidget;
			}
		}

		return null;
	}

	private AbstractPartRenderer getRenderer(MUIElement uiElement, Object parent) {
		return curFactory.getRenderer(uiElement, parent);
	}

	protected void processHandlers(MUIElement element) {
		// for (MHandler contributedHandler : element.getHandlers()) {
		// if (contributedHandler.getURI() != null &&
		// contributedHandler.getObject() == null) {
		// contributedHandler.setObject(contributionFactory.create(
		// contributedHandler.getURI(), context));
		// }
		// }
	}

	protected void setFactoryFor(MUIElement element,
			AbstractPartRenderer factory) {
		element.setFactory(factory);
	}

	protected AbstractPartRenderer getFactoryFor(MUIElement element) {
		return (AbstractPartRenderer) element.getFactory();
	}
}
