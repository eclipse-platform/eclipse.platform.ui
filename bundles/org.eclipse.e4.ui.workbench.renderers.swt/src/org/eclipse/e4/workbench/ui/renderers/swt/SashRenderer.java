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
import org.eclipse.e4.ui.model.application.MElementContainer;
import org.eclipse.e4.ui.model.application.MPSCElement;
import org.eclipse.e4.ui.model.application.MPartSashContainer;
import org.eclipse.e4.ui.model.application.MUIElement;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;

public class SashRenderer extends SWTPartRenderer {

	private static final int UNDEFINED_WEIGHT = -1;
	private static final int DEFAULT_WEIGHT = 100;

	public SashRenderer() {
		super();
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

		synchWeightsToModel(sashForm);

		// add a size change listener to each child so we can recalculate
		// the weights on a change...
		Control[] childCtrls = sashForm.getChildren();
		for (int i = 0; i < childCtrls.length; i++) {
			childCtrls[i].addControlListener(new ControlListener() {
				public void controlMoved(ControlEvent e) {
				}

				public void controlResized(ControlEvent e) {
					// See if we need to re do the weights
					synchModelToWeights((Control) e.widget);
				}
			});
		}
	}

	/**
	 * @param element
	 * @return
	 */
	private int getWeight(MUIElement element) {
		String info = element.getContainerData();
		try {
			int value = Integer.parseInt(info);
			return value;
		} catch (NumberFormatException e) {
			return UNDEFINED_WEIGHT;
		}
	}

	/**
	 * Synchronizes the weights of the specified sash form to the underlying
	 * model.
	 * 
	 * @param sf
	 *            the sash form to synchronize
	 */
	private void synchWeightsToModel(SashForm sf) {
		if (sf.isDisposed())
			return;

		// retrieve the model
		MPartSashContainer psc = (MPartSashContainer) sf.getData(OWNING_ME);

		// Gather up the weights from the rendered elements and
		// apply then to the SashForm
		List<Integer> weights = new ArrayList<Integer>();
		for (MPSCElement pscElement : psc.getChildren()) {
			if (pscElement.getWidget() instanceof Control) {
				weights.add(getWeight(pscElement));
			}
		}

		int[] sashWeights = new int[weights.size()];
		int swCount = 0;
		for (int sw : weights) {
			sashWeights[swCount++] = sw;
		}

		sf.setWeights(sashWeights);
	}

	protected void synchModelToWeights(Control ctrl) {
		if (!(ctrl.getParent() instanceof SashForm))
			return;

		SashForm sf = (SashForm) ctrl.getParent();
		MPartSashContainer psc = (MPartSashContainer) sf.getData(OWNING_ME);
		int[] ctrlWeights = sf.getWeights();

		int weightIndex = 0;
		for (MPSCElement pscElement : psc.getChildren()) {
			if (pscElement.getWidget() instanceof Control) {
				pscElement.setContainerData(Integer
						.toString(ctrlWeights[weightIndex++]));
			}
		}
	}
}
