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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.e4.core.services.IContributionFactory;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.ContextInjectionFactory;
import org.eclipse.e4.ui.model.application.ApplicationPackage;
import org.eclipse.e4.ui.model.application.MHandler;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.workbench.swt.Activator;
import org.eclipse.e4.workbench.ui.IPresentationEngine;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.emf.ecore.EObject;

public class PartRenderingEngine implements IPresentationEngine {
	public static final String engineURI = "platform:/plugin/org.eclipse.e4.ui.workbench.swt/"
			+ "org.eclipse.e4.ui.workbench.swt.internal.PartRenderingEngine";

	private final List partFactories = new ArrayList();

	// SWT property ids containing the currently rendered part (and factory) for
	// a given widget
	public static final String FACTORY = "partFactory"; //$NON-NLS-1$

	// Life Cycle listeners
	private AdapterImpl visibilityListener = new AdapterImpl() {
		@Override
		public void notifyChanged(Notification msg) {
			if (ApplicationPackage.Literals.MPART__VISIBLE.equals(msg
					.getFeature())) {
				// skip no-ops
				if (msg.getOldBooleanValue() == msg.getNewBooleanValue())
					return;

				MPart<?> changedPart = (MPart<?>) msg.getNotifier();

				// If the parent isn't displayed who cares?
				MPart<?> parent = changedPart.getParent();
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
			if (ApplicationPackage.Literals.MPART__CHILDREN.equals(msg
					.getFeature())) {
				MPart<?> changedPart = (MPart<?>) msg.getNotifier();
				AbstractPartRenderer factory = getFactoryFor(changedPart);

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
						factory.childAdded(changedPart, added);
					}
				} else if (msg.getEventType() == Notification.REMOVE) {
					Activator.trace(Policy.DEBUG_RENDERER,
							"Child Removed", null); //$NON-NLS-1$
					MPart removed = (MPart) msg.getOldValue();
					// Removing invisible elements is a NO-OP as far as the
					// renderer is concerned
					if (!removed.isVisible())
						return;

					factory.childRemoved(changedPart, removed);
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
				.getConfigurationElementsFor("org.eclipse.e4.workbench.partfactory"); //$NON-NLS-1$

		// Sort the factories based on their dependence
		// This is a hack, should be based on plug-in dependencies
		int offset = 0;
		for (int i = 0; i < factories.length; i++) {
			String clsSpec = factories[i].getAttribute("class"); //$NON-NLS-1$
			if (clsSpec.indexOf("Legacy") >= 0 //$NON-NLS-1$
					|| clsSpec.indexOf("PartSash") >= 0) { //$NON-NLS-1$
				IConfigurationElement tmp = factories[offset];
				factories[offset++] = factories[i];
				factories[i] = tmp;
			}
		}

		for (int i = 0; i < factories.length; i++) {
			AbstractPartRenderer factory = null;
			try {
				factory = (AbstractPartRenderer) factories[i]
						.createExecutableExtension("class"); //$NON-NLS-1$
			} catch (CoreException e) {
				e.printStackTrace();
			}
			if (factory != null) {
				factory.init(this, context, contributionFactory);
				ContextInjectionFactory.inject(factory, context);
				addPartFactory(factory);
			}
		}

		// Add the renderer to the context
		context.set(PartRenderingEngine.SERVICE_NAME, this);
	}

	public void addPartFactory(AbstractPartRenderer factory) {
		partFactories.add(factory);
	}

	public Object createGui(MPart element, Object parent) {
		// Life-cycle hooks
		installLifeCycleHooks(element);

		if (!element.isVisible())
			return null;

		// Create a control appropriate to the part
		Object newWidget = createWidget(element, parent);

		// Remember that we've created the control
		if (newWidget != null) {
			// Bind the widget to its model element
			element.setWidget(newWidget);

			// Process its internal structure through the factory that created
			// it
			AbstractPartRenderer factory = getFactoryFor(element);
			factory.bindWidget(element, newWidget);
			hookControllerLogic(element);
			factory.processContents(element);
			factory.postProcess(element);

			// Now that we have a widget let the parent know
			if (element.getParent() instanceof MPart) {
				MPart parentElement = (MPart) element.getParent();
				AbstractPartRenderer parentFactory = getFactoryFor(parentElement);
				parentFactory.childAdded(parentElement, element);
			}
		}

		return newWidget;
	}

	public Object createGui(MPart element) {
		// Obtain the necessary parent and context
		Object parent = null;
		MPart parentME = element.getParent();
		if (parentME != null) {
			parent = parentME.getWidget();
		}

		return createGui(element, parent);
	}

	/**
	 * @param element
	 */
	public void removeGui(MPart<?> element) {
		AbstractPartRenderer factory = getFactoryFor(element);
		assert (factory != null);

		MPart<?> parent = element.getParent();
		AbstractPartRenderer parentFactory = parent != null ? getFactoryFor(parent)
				: null;
		if (parentFactory == null)
			return;

		// Remove the child from its current parent's -Composite- this does NOT
		// mean removing the model element from its parent's model
		parentFactory.childRemoved(element.getParent(), element);

		factory.disposeWidget(element);
	}

	/**
	 * @param element
	 *            an element that's been seen by createGui
	 */
	private void installLifeCycleHooks(MPart<?> element) {
		// Handle visibility changes
		if (!((EObject) element).eAdapters().contains(visibilityListener))
			((EObject) element).eAdapters().add(visibilityListener);

		// Handle children
		if (!((EObject) element).eAdapters().contains(childrenListener))
			((EObject) element).eAdapters().add(childrenListener);
	}

	protected Object createWidget(MPart<?> element, Object parent) {
		// Iterate through the factories until one actually creates the widget
		for (Iterator iterator = partFactories.iterator(); iterator.hasNext();) {
			AbstractPartRenderer factory = (AbstractPartRenderer) iterator
					.next();

			// *** Put any declarative tests here to prevent aggressive loading
			// For example, test whether this factory handles a particular model
			// type ('StackModel'...)

			Object newWidget = factory.createWidget(element, parent);
			if (newWidget != null) {
				// Remember which factory created the widget
				setFactoryFor(element, factory);

				processHandlers(element);

				return newWidget;
			}
		}

		return null;
	}

	protected void processHandlers(MPart<?> element) {
		for (MHandler contributedHandler : element.getHandlers()) {
			if (contributedHandler.getURI() != null
					&& contributedHandler.getObject() == null) {
				contributedHandler.setObject(contributionFactory.create(
						contributedHandler.getURI(), context));
			}
		}
	}

	/**
	 * Manages the relationship between a MPart<?> and its rendered Widget.
	 * 
	 * MPart<?>.getWidget().getData(OWNING_ME) == MPart<?>
	 * 
	 * @param element
	 *            The UI element
	 * @param widget
	 *            The widget
	 */
	// public void bindWidget(MPart<?> me, Widget widget) {
	// me.setWidget(widget);
	// widget.setData(OWNING_ME, me);
	//		
	// hookControllerLogic(me, widget);
	// }

	private void hookControllerLogic(final MPart<?> element) {
		// Delegate widget specific hook-up to the creating factory
		AbstractPartRenderer factory = (AbstractPartRenderer) getFactoryFor(element);
		if (factory != null)
			factory.hookControllerLogic(element);
	}

	protected void setFactoryFor(MPart element, AbstractPartRenderer factory) {
		element.setOwner(factory);
	}

	protected AbstractPartRenderer getFactoryFor(MPart element) {
		return (AbstractPartRenderer) element.getOwner();
	}
}
