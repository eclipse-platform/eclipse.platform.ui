/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.renderers.swt;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MArea;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

public class SashRenderer extends SWTPartRenderer {

	@Inject
	private IEventBroker eventBroker;

	private static final int UNDEFINED_WEIGHT = -1;
	private static final int DEFAULT_WEIGHT = 100;

	private EventHandler sashOrientationHandler;
	private EventHandler sashWeightHandler;
	private EventHandler visibilityHandler;

	public SashRenderer() {
		super();
	}

	@PostConstruct
	void postConstruct() {
		sashOrientationHandler = new EventHandler() {
			public void handleEvent(Event event) {
				// Ensure that this event is for a MPartSashContainer
				Object element = event.getProperty(UIEvents.EventTags.ELEMENT);
				if (!(element instanceof MPartSashContainer)) {
					return;
				}

				if (UIEvents.GenericTile.HORIZONTAL.equals(event
						.getProperty(UIEvents.EventTags.ATTNAME))) {
					Boolean horizontal = (Boolean) event
							.getProperty(UIEvents.EventTags.NEW_VALUE);
					MPartSashContainer container = (MPartSashContainer) element;
					SashForm sashForm = (SashForm) container.getWidget();
					sashForm.setOrientation(horizontal.booleanValue() ? SWT.HORIZONTAL
							: SWT.VERTICAL);
				}
			}
		};

		eventBroker.subscribe(UIEvents.buildTopic(UIEvents.GenericTile.TOPIC,
				UIEvents.GenericTile.HORIZONTAL), sashOrientationHandler);

		sashWeightHandler = new EventHandler() {
			public void handleEvent(Event event) {
				// Ensure that this event is for a MPartSashContainer
				MUIElement element = (MUIElement) event
						.getProperty(UIEvents.EventTags.ELEMENT);
				MUIElement parent = element.getParent();
				if (!(parent instanceof MPartSashContainer)
						|| parent.getRenderer() != SashRenderer.this)
					return;

				MElementContainer<MUIElement> pscModel = (MElementContainer<MUIElement>) parent;
				if (UIEvents.UIElement.CONTAINERDATA.equals(event
						.getProperty(UIEvents.EventTags.ATTNAME))) {
					forceLayout(pscModel);
				}
			}
		};

		visibilityHandler = new EventHandler() {
			public void handleEvent(Event event) {
				// Ensure that this event is for a MPartSashContainer
				MUIElement element = (MUIElement) event
						.getProperty(UIEvents.EventTags.ELEMENT);
				MUIElement parent = element.getParent();
				if (!(parent instanceof MPartSashContainer)
						|| parent.getRenderer() != SashRenderer.this)
					return;

				MElementContainer<MUIElement> pscModel = (MElementContainer<MUIElement>) parent;
				if (UIEvents.UIElement.VISIBLE.equals(event
						.getProperty(UIEvents.EventTags.ATTNAME))) {
					if (element.isVisible()) {
						forceLayout(pscModel);
					}
				}
			}
		};

		eventBroker.subscribe(UIEvents.buildTopic(UIEvents.UIElement.TOPIC,
				UIEvents.UIElement.VISIBLE), visibilityHandler);
	}

	/**
	 * @param pscModel
	 */
	protected void forceLayout(MElementContainer<MUIElement> pscModel) {
		// layout the containing Composite
		while (!(pscModel.getWidget() instanceof Control))
			pscModel = pscModel.getParent();
		Control ctrl = (Control) pscModel.getWidget();
		ctrl.getParent().layout(true, true);
	}

	@PreDestroy
	void preDestroy() {
		eventBroker.unsubscribe(sashOrientationHandler);
		eventBroker.unsubscribe(sashWeightHandler);
		eventBroker.unsubscribe(visibilityHandler);
	}

	public Object createWidget(MUIElement element, Object parent) {
		if (element.getParent() != null) {
			Rectangle newRect = new Rectangle(0, 0, 0, 0);
			return newRect;
		}

		// Special case...a null parent indicates that this is a 'shared'
		// element
		// This means that it *must* have a Composite so it can be reparented
		Composite sashComposite = new Composite((Composite) parent, SWT.NONE);
		sashComposite.setLayout(new SashLayout(sashComposite, null));
		return sashComposite;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.workbench.renderers.swt.SWTPartRenderer#childAdded(
	 * org.eclipse.e4.ui.model.application.MPart,
	 * org.eclipse.e4.ui.model.application.MPart)
	 */
	@Override
	public void childRendered(MElementContainer<MUIElement> parentElement,
			MUIElement element) {
		super.childRendered(parentElement, element);

		// If this is a 'shared' sash then use its child as the 'root'
		if (parentElement.getWidget() instanceof Composite) {
			Composite c = (Composite) parentElement.getWidget();
			SashLayout sl = (SashLayout) c.getLayout();
			sl.setRootElemenr(element);
		}

		// Ensure that the element's 'containerInfo' is initialized
		int weight = getWeight(element);
		if (weight == UNDEFINED_WEIGHT) {
			element.setContainerData(Integer.toString(DEFAULT_WEIGHT));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer#getUIContainer
	 * (org.eclipse.e4.ui.model.application.ui.MUIElement)
	 */
	@Override
	public Object getUIContainer(MUIElement element) {
		// OK, find the first parent that is *not* a sash container
		MUIElement parentElement = element.getParent();
		if (parentElement.getWidget() instanceof Composite)
			return parentElement.getWidget();

		MUIElement prevParent = null;
		while (parentElement instanceof MPartSashContainer
				&& !(parentElement.getWidget() instanceof Composite)
				&& !(parentElement instanceof MArea)) {
			prevParent = parentElement;
			parentElement = parentElement.getParent();
		}

		if (parentElement.getRenderer() instanceof AbstractPartRenderer) {
			AbstractPartRenderer renderer = (AbstractPartRenderer) parentElement
					.getRenderer();
			return renderer.getUIContainer(prevParent);
		}
		return null;
	}

	/**
	 * @param element
	 * @return
	 */
	private static int getWeight(MUIElement element) {
		String info = element.getContainerData();
		if (info == null || info.length() == 0) {
			element.setContainerData(Integer.toString(100));
			info = element.getContainerData();
		}

		try {
			int value = Integer.parseInt(info);
			return value;
		} catch (NumberFormatException e) {
			return UNDEFINED_WEIGHT;
		}
	}
}
