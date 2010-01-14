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
package org.eclipse.e4.workbench.ui.renderers.swt;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.inject.Inject;
import org.eclipse.e4.core.services.annotations.PostConstruct;
import org.eclipse.e4.core.services.annotations.PreDestroy;
import org.eclipse.e4.ui.model.application.MElementContainer;
import org.eclipse.e4.ui.model.application.MPartSashContainer;
import org.eclipse.e4.ui.model.application.MUIElement;
import org.eclipse.e4.ui.services.events.IEventBroker;
import org.eclipse.e4.workbench.ui.UIEvents;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

public class SashRenderer extends SWTPartRenderer {

	private class ModelUpdateJob implements Runnable {
		public List<MPartSashContainer> sashModelsToUpdate = new ArrayList<MPartSashContainer>();

		public void run() {
			clearModelUpdate();
			while (!sashModelsToUpdate.isEmpty()) {
				MPartSashContainer psc = sashModelsToUpdate.remove(0);

				// prevent recursive updating
				ignoreWeightUpdates = true;
				synchSashToModel(psc);
				ignoreWeightUpdates = false;
			}
		}
	}

	private class SashUpdateJob implements Runnable {
		public List<SashForm> sashesToUpdate = new ArrayList<SashForm>();

		public void run() {
			clearSashUpdate();
			while (!sashesToUpdate.isEmpty()) {
				SashForm sf = sashesToUpdate.remove(0);

				// prevent recursive updating
				ignoreWeightUpdates = true;
				synchModelToSash(sf);
				ignoreWeightUpdates = false;
			}
		}
	}

	@Inject
	private IEventBroker eventBroker;

	private static final int UNDEFINED_WEIGHT = -1;
	private static final int DEFAULT_WEIGHT = 100;

	private EventHandler sashOrientationHandler;
	private EventHandler sashWeightHandler;

	ModelUpdateJob modelUpdateJob;
	SashUpdateJob sashUpdateJob;

	protected boolean ignoreWeightUpdates = false;

	public SashRenderer() {
		super();
	}

	void clearModelUpdate() {
		modelUpdateJob = null;
	}

	void clearSashUpdate() {
		sashUpdateJob = null;
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
					sashForm
							.setOrientation(horizontal.booleanValue() ? SWT.HORIZONTAL
									: SWT.VERTICAL);
				}
			}
		};

		eventBroker.subscribe(UIEvents.buildTopic(UIEvents.GenericTile.TOPIC,
				UIEvents.GenericTile.HORIZONTAL), sashOrientationHandler);

		sashWeightHandler = new EventHandler() {
			public void handleEvent(Event event) {
				if (ignoreWeightUpdates)
					return;

				// Ensure that this event is for a MPartSashContainer
				MUIElement element = (MUIElement) event
						.getProperty(UIEvents.EventTags.ELEMENT);
				MUIElement parent = element.getParent();
				if (!(parent instanceof MPartSashContainer)
						|| parent.getRenderer() != SashRenderer.this)
					return;

				MPartSashContainer pscModel = (MPartSashContainer) parent;
				SashForm sf = (SashForm) pscModel.getWidget();
				if (UIEvents.UIElement.CONTAINERDATA.equals(event
						.getProperty(UIEvents.EventTags.ATTNAME))) {
					if (modelUpdateJob == null) {
						modelUpdateJob = new ModelUpdateJob();
						modelUpdateJob.sashModelsToUpdate.add(pscModel);
						sf.getDisplay().asyncExec(modelUpdateJob);
					} else {
						if (!modelUpdateJob.sashModelsToUpdate
								.contains(pscModel))
							modelUpdateJob.sashModelsToUpdate.add(pscModel);
					}
				}
			}
		};

		eventBroker.subscribe(UIEvents.buildTopic(UIEvents.UIElement.TOPIC,
				UIEvents.UIElement.CONTAINERDATA), sashWeightHandler);
	}

	@PreDestroy
	void preDestroy() {
		eventBroker.unsubscribe(sashOrientationHandler);
		eventBroker.unsubscribe(sashWeightHandler);
	}

	public Widget createWidget(MUIElement element, Object parent) {
		if (!(element instanceof MPartSashContainer)
				|| !(parent instanceof Composite))
			return null;

		Widget parentWidget = (Widget) parent;

		MPartSashContainer psc = (MPartSashContainer) element;
		int orientation = psc.isHorizontal() ? SWT.HORIZONTAL : SWT.VERTICAL;
		SashForm newSash = new SashForm((Composite) parentWidget, SWT.SMOOTH
				| orientation);
		bindWidget(element, newSash);
		newSash.setVisible(true);

		return newSash;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.workbench.ui.renderers.swt.SWTPartRenderer#childAdded(
	 * org.eclipse.e4.ui.model.application.MPart,
	 * org.eclipse.e4.ui.model.application.MPart)
	 */
	@Override
	public void childRendered(MElementContainer<MUIElement> parentElement,
			MUIElement element) {
		super.childRendered(parentElement, element);

		if (!(parentElement.getWidget() instanceof SashForm))
			return;

		// Ensure that the element's 'containerInfo' is initialized
		int weight = getWeight(element);
		if (weight == UNDEFINED_WEIGHT) {
			element.setContainerData(Integer.toString(DEFAULT_WEIGHT));
		}

		// Ensure the Z-order of the contained controls matches the model order
		for (Iterator kidIter = (parentElement.getChildren()).iterator(); kidIter
				.hasNext();) {
			MUIElement part = (MUIElement) kidIter.next();
			Control partCtrl = (Control) part.getWidget();
			if (partCtrl != null) {
				Control outerMost = getOutermost(partCtrl);
				outerMost.moveBelow(null);
			}
		}
	}

	public void postProcess(MUIElement element) {
		if (!(element instanceof MPartSashContainer))
			return;

		MPartSashContainer psc = (MPartSashContainer) element;
		final SashForm sashForm = (SashForm) psc.getWidget();

		synchSashToModel(psc);

		// add a size change listener to each child so we can recalculate
		// the weights on a change...
		Control[] childCtrls = sashForm.getChildren();
		for (int i = 0; i < childCtrls.length; i++) {
			childCtrls[i].addControlListener(new ControlListener() {
				public void controlMoved(ControlEvent e) {
				}

				public void controlResized(ControlEvent e) {
					Control ctrl = (Control) e.widget;
					if (ctrl.isDisposed()
							|| !(ctrl.getParent() instanceof SashForm))
						return;

					SashForm sf = (SashForm) ctrl.getParent();
					if (sashUpdateJob == null) {
						sashUpdateJob = new SashUpdateJob();
						sashUpdateJob.sashesToUpdate.add(sf);
						sf.getDisplay().asyncExec(sashUpdateJob);
					} else {
						if (!sashUpdateJob.sashesToUpdate.contains(sf))
							sashUpdateJob.sashesToUpdate.add(sf);
					}
				}
			});
		}
	}

	/**
	 * @param element
	 * @return
	 */
	private static int getWeight(MUIElement element) {
		String info = element.getContainerData();
		if (info == null || info.length() == 0)
			return DEFAULT_WEIGHT;

		try {
			int value = Integer.parseInt(info);
			return value;
		} catch (NumberFormatException e) {
			return UNDEFINED_WEIGHT;
		}
	}

	private static int[] getModelWeights(MPartSashContainer psc) {
		SashForm sf = (SashForm) psc.getWidget();
		int[] modelWeights = sf.getWeights();
		int index = 0;
		for (MUIElement element : psc.getChildren()) {
			if (element.getWidget() != null)
				modelWeights[index++] = getWeight(element);
		}
		return modelWeights;
	}

	private static MUIElement[] getModelElements(SashForm sf) {
		Object me = sf.getData(OWNING_ME);
		if (!(me instanceof MPartSashContainer))
			return null;

		MPartSashContainer psc = (MPartSashContainer) me;
		MUIElement[] modelElements = new MUIElement[sf.getWeights().length];
		int index = 0;
		for (MUIElement element : psc.getChildren()) {
			if (element.getWidget() != null)
				modelElements[index++] = element;
		}
		return modelElements;
	}

	/**
	 * @param psc
	 */
	private void synchSashToModel(MPartSashContainer psc) {
		if (!(psc.getWidget() instanceof SashForm))
			return;

		SashForm sf = (SashForm) psc.getWidget();
		if (sf.isDisposed())
			return;

		int[] curWeights = sf.getWeights();
		assert (curWeights.length > 0);
		int[] newWeights = getModelWeights(psc);
		sf.setWeights(newWeights);
		curWeights = sf.getWeights();
	}

	/**
	 * @param psc
	 */
	private void synchModelToSash(SashForm sf) {
		int[] w = sf.getWeights();
		MUIElement[] elements = SashRenderer.getModelElements(sf);
		for (int i = 0; i < w.length; i++) {
			elements[i].setContainerData(Integer.toString(w[i]));
		}
	}
}
