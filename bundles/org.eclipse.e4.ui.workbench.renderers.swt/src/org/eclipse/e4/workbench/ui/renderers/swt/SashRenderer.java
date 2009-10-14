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

import org.eclipse.e4.ui.model.application.MApplicationPackage;
import org.eclipse.e4.ui.model.application.MElementContainer;
import org.eclipse.e4.ui.model.application.MPartSashContainer;
import org.eclipse.e4.ui.model.application.MUIElement;

import java.util.Iterator;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;

public class SashRenderer extends SWTPartRenderer {

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

		// Ensure that the model has *at least* enough weights
		int[] sfWeights = sashForm.getWeights();
		int sfCount = sfWeights.length;

		EList<Integer> modelWeights = psc.getWeights();
		int mwCount = modelWeights.size();
		if (mwCount < sfCount) {
			for (int i = mwCount; i < sfCount; i++) {
				int newVal = i < sfCount ? sfWeights[i] : 100;
				modelWeights.add(newVal);
			}
		}

		// Set the model's weights into the sash form
		int[] newWeights = new int[sfCount];
		for (int i = 0; i < sfCount; i++)
			newWeights[i] = modelWeights.get(i);
		sashForm.setWeights(newWeights);

		// add an adapter to the model sash form so we can respond to model
		// changes and apply the weight changes to the widget
		// IEclipseContext lclContext = getContext(psc);
		// IEventBroker eb = (IEventBroker)
		// lclContext.get(IEventBroker.class.getName());
		// eb.subscribe(topic, filter, eventReceiver);
		((EObject) psc).eAdapters().add(new AdapterImpl() {
			@Override
			public void notifyChanged(Notification msg) {
				if (MApplicationPackage.Literals.GENERIC_TILE__WEIGHTS
						.equals(msg.getFeature())
						&& msg.getNewValue() != null) {
					synchWeightsToModel(sashForm);
				}
			}
		});

		// add a size change listener to each child so we can recalculate
		// the
		// weights on a change...
		Control[] childCtrls = sashForm.getChildren();
		for (int i = 0; i < childCtrls.length; i++) {
			childCtrls[i].addControlListener(new ControlListener() {
				public void controlMoved(ControlEvent e) {
				}

				public void controlResized(ControlEvent e) {
					// See if we need to re do the weights
					synchWeights((Control) e.widget);
				}
			});
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

		// get the weights of the widget and the model model
		int[] ctrlWeights = sf.getWeights();
		EList<Integer> modelWeights = psc.getWeights();

		// check that the size is in the same, when the model is modified,
		// values may be added individually, in this case, we want to change
		// only when the final value has been added because if the number of
		// weights do not match the underlying number of child controls, an
		// exception is thrown by SWT
		if (ctrlWeights.length == modelWeights.size()) {
			// only overwrite if the values are different, or else we will have
			// an infinite loop
			boolean overWrite = false;
			int i = 0;
			for (Iterator<Integer> weightIter = modelWeights.iterator(); weightIter
					.hasNext();) {
				Integer integer = weightIter.next();
				if (integer.intValue() != ctrlWeights[i++]) {
					overWrite = true;
					break;
				}
			}

			if (overWrite) {
				// reset the control's weights to match the weight
				int[] weights = new int[modelWeights.size()];
				for (i = 0; i < weights.length; i++) {
					weights[i] = modelWeights.get(i);
				}
				sf.setWeights(weights);
			}
		}
	}

	protected void synchWeights(Control ctrl) {
		if (!(ctrl.getParent() instanceof SashForm))
			return;

		SashForm sf = (SashForm) ctrl.getParent();
		MPartSashContainer psc = (MPartSashContainer) sf.getData(OWNING_ME);
		int[] ctrlWeights = sf.getWeights();
		EList<Integer> modelWeights = psc.getWeights();

		boolean overWrite = false;
		if (ctrlWeights.length != modelWeights.size())
			overWrite = true;
		else {
			int i = 0;
			for (Iterator weightIter = modelWeights.iterator(); weightIter
					.hasNext();) {
				Integer integer = (Integer) weightIter.next();
				if (integer.intValue() != ctrlWeights[i++]) {
					overWrite = true;
					break;
				}
			}
		}

		// reset the model's weights to match the control
		if (overWrite) {
			modelWeights.clear();
			for (int i = 0; i < ctrlWeights.length; i++) {
				modelWeights.add(ctrlWeights[i]);
			}
		}
	}
}
