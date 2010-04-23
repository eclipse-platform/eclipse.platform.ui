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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.eclipse.e4.core.di.annotations.PostConstruct;
import org.eclipse.e4.core.di.annotations.PreDestroy;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.workbench.ui.UIEvents;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

public class SashRenderer extends SWTPartRenderer {

	private ControlListener resizeListener = new ControlListener() {
		public void controlMoved(ControlEvent e) {
		}

		public void controlResized(ControlEvent e) {
			Control ctrl = (Control) e.widget;
			if (ctrl.isDisposed() || !(ctrl.getParent() instanceof SashForm))
				return;

			SashForm sf = (SashForm) ctrl.getParent();
			if (weightsChanged(sf)) {
				// Cache the new values
				weightsMap.put(sf, sf.getWeights());
				addSashToUpdate(sf);
			}
		}

		// determine if the weights in the SashForm have actually changed
		private boolean weightsChanged(SashForm sf) {
			int[] oldW = (int[]) weightsMap.get(sf);
			int[] newW = sf.getWeights();
			if (oldW == null || oldW.length != newW.length)
				return true;
			for (int j = 0; j < oldW.length; j++) {
				if (oldW[j] != newW[j])
					return true;
			}
			return false;
		}
	};

	private class SashUpdateJob implements Runnable {
		public List<SashForm> sashesToUpdate = new ArrayList<SashForm>();

		public void run() {
			clearSashUpdate();
			while (!sashesToUpdate.isEmpty()) {
				SashForm sf = sashesToUpdate.remove(0);
				if (sf.isDisposed())
					continue;

				// prevent recursive updating
				ignoreWeightUpdates = true;
				synchModelToSash(sf);
				ignoreWeightUpdates = false;
			}
		}
	}

	private void addSashToUpdate(SashForm sf) {
		MElementContainer<MUIElement> psc = (MElementContainer<MUIElement>) sf
				.getData(OWNING_ME);
		if (modelUpdateJob != null
				&& modelUpdateJob.sashModelsToUpdate.contains(psc)) {
			return;
		}
		if (sashUpdateJob == null) {
			sashUpdateJob = new SashUpdateJob();
			sashUpdateJob.sashesToUpdate.add(sf);
			sf.getDisplay().asyncExec(sashUpdateJob);
		} else {
			if (!sashUpdateJob.sashesToUpdate.contains(sf))
				sashUpdateJob.sashesToUpdate.add(sf);
		}
	}

	private class ModelUpdateJob implements Runnable {
		public List<MElementContainer<MUIElement>> sashModelsToUpdate = new ArrayList<MElementContainer<MUIElement>>();

		public void run() {
			clearModelUpdate();
			while (!sashModelsToUpdate.isEmpty()) {
				MElementContainer<MUIElement> psc = sashModelsToUpdate
						.remove(0);

				// prevent recursive updating
				ignoreWeightUpdates = true;
				synchSashToModel(psc);
				ignoreWeightUpdates = false;
			}
		}
	}

	private void addModelToUpdate(MElementContainer<MUIElement> pscModel) {
		Control sf = (Control) pscModel.getWidget();
		if (sf == null || sf.isDisposed())
			return;

		if (modelUpdateJob == null) {
			modelUpdateJob = new ModelUpdateJob();
			modelUpdateJob.sashModelsToUpdate.add(pscModel);
			sf.getDisplay().asyncExec(modelUpdateJob);
		} else {
			if (!modelUpdateJob.sashModelsToUpdate.contains(pscModel))
				modelUpdateJob.sashModelsToUpdate.add(pscModel);
		}
	}

	@Inject
	private IEventBroker eventBroker;

	private static final int UNDEFINED_WEIGHT = -1;
	private static final int DEFAULT_WEIGHT = 100;

	private Map weightsMap = new HashMap();

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
					sashForm.setOrientation(horizontal.booleanValue() ? SWT.HORIZONTAL
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

				MElementContainer<MUIElement> pscModel = (MElementContainer<MUIElement>) parent;
				if (UIEvents.UIElement.CONTAINERDATA.equals(event
						.getProperty(UIEvents.EventTags.ATTNAME))) {
					addModelToUpdate(pscModel);
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
		final MElementContainer<MUIElement> psc = parentElement;
		for (MUIElement part : psc.getChildren()) {
			Control partCtrl = (Control) part.getWidget();
			if (partCtrl != null) {
				partCtrl.moveBelow(null);
			}
		}

		// Set up the size listeners
		Control newCtrl = (Control) element.getWidget();
		newCtrl.addControlListener(resizeListener);
		newCtrl.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				((Control) e.widget).removeControlListener(resizeListener);
			}
		});

		// synch this sash after the dust settles
		addModelToUpdate(psc);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.workbench.swt.internal.AbstractPartRenderer#hideChild
	 * (org.eclipse.e4.ui.model.application.MElementContainer,
	 * org.eclipse.e4.ui.model.application.MUIElement)
	 */
	@Override
	public void hideChild(MElementContainer<MUIElement> parentElement,
			MUIElement child) {
		super.hideChild(parentElement, child);

		((Control) child.getWidget()).removeControlListener(resizeListener);

		// synch this sash after the dust settles
		addModelToUpdate(parentElement);
	}

	public void postProcess(MUIElement element) {
		if (!(element instanceof MPartSashContainer))
			return;

		MElementContainer<MUIElement> psc = (MElementContainer<MUIElement>) element;
		final SashForm sashForm = (SashForm) psc.getWidget();

		synchSashToModel(psc);

		// Clean up the cache entry on dispose
		sashForm.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				weightsMap.remove(e.widget);
			}
		});
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

	private static int[] getModelWeights(MElementContainer<MUIElement> psc) {
		int count = 0;
		for (MUIElement element : psc.getChildren()) {
			if (element.getWidget() != null)
				count++;
		}

		int[] modelWeights = new int[count];
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
		List<MUIElement> modelElements = new ArrayList<MUIElement>();
		for (MUIElement element : psc.getChildren()) {
			if (element.getWidget() != null)
				modelElements.add(element);
		}

		return modelElements.toArray(new MUIElement[modelElements.size()]);
	}

	/**
	 * @param psc
	 */
	private void synchSashToModel(MElementContainer<MUIElement> psc) {
		if (!(psc.getWidget() instanceof SashForm))
			return;

		SashForm sf = (SashForm) psc.getWidget();
		if (sf.isDisposed())
			return;

		int[] curWeights = sf.getWeights();
		assert (curWeights.length > 0);
		int[] newWeights = getModelWeights(psc);

		// Put the new weights in the map first
		if (newWeights.length > 0)
			sf.setWeights(newWeights);
		sf.layout();
		weightsMap.put(sf, newWeights);
		curWeights = sf.getWeights();
	}

	/**
	 * @param psc
	 */
	private void synchModelToSash(SashForm sf) {
		// Calculate the total amount of 'containerData' weights for the visible
		// components
		MUIElement[] elements = SashRenderer.getModelElements(sf);
		if (elements.length == 0)
			return;

		int totalModelWeight = 0;
		for (MUIElement element : elements) {
			totalModelWeight += getWeight(element);
		}

		int[] w = sf.getWeights();
		int totalSashWeight = 0;
		for (int weight : w) {
			totalSashWeight += weight;
		}

		// Ensure that the new containerData weights add up to what they used to
		double ratio = (double) totalModelWeight / totalSashWeight;
		int totalAdded = 0;
		for (int i = 0; i < w.length; i++) {
			int newWeight = (int) (w[i] * ratio);

			// The last element will use up all the leftover 'weight' to avoid
			// roundoff errors
			if (i == (w.length - 1))
				newWeight = totalModelWeight - totalAdded;
			elements[i].setContainerData(Integer.toString(newWeight));
			totalAdded += newWeight;
		}
		System.out
				.println("Model Weights changed: " + totalModelWeight + " " + totalAdded); //$NON-NLS-1$//$NON-NLS-2$
	}
}
