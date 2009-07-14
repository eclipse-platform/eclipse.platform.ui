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

import java.util.Iterator;
import java.util.List;
import org.eclipse.e4.ui.model.application.ApplicationPackage;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.application.MSashForm;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.emf.common.util.EList;
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

	public Widget createWidget(MPart<?> part, Object parent) {
		if (!(part instanceof MSashForm<?>) || !(parent instanceof Composite))
			return null;

		Widget parentWidget = (Widget) parent;

		int orientation = (part.getPolicy() != null && part.getPolicy()
				.startsWith("Horizontal")) ? SWT.HORIZONTAL //$NON-NLS-1$
				: SWT.VERTICAL;
		SashForm newSash = new SashForm((Composite) parentWidget, SWT.SMOOTH
				| orientation);
		bindWidget(part, newSash);
		newSash.setVisible(true);

		return newSash;
	}

	public void postProcess(MPart<?> part) {
		if (part instanceof org.eclipse.e4.ui.model.application.MSashForm<?>) {
			// do we have any children ?
			EList<MPart<?>> kids = (EList<MPart<?>>) part.getChildren();
			if (kids.size() == 0)
				return;

			// Cound the -visible- children
			int visCount = 0;
			for (Iterator iterator = kids.iterator(); iterator.hasNext();) {
				MPart<?> mPart = (MPart<?>) iterator.next();
				if (mPart.getWidget() != null)
					visCount++;
			}

			// set the weights of the sashes
			final SashForm sashForm = (SashForm) part.getWidget();
			org.eclipse.e4.ui.model.application.MSashForm<?> sashPart = (org.eclipse.e4.ui.model.application.MSashForm<?>) part;
			List<Integer> weightList = sashPart.getWeights();

			// If it's not already initialized the set them all ==
			if (weightList.size() != visCount) {
				weightList.clear();
				for (int i = weightList.size(); i < visCount; i++) {
					weightList.add(new Integer(100));
				}
			}

			// Set the weights in the control
			if (weightList.size() > 0) {
				int count = 0;
				int[] weights = new int[weightList.size()];
				for (Iterator iterator = weightList.iterator(); iterator
						.hasNext();) {
					Integer integer = (Integer) iterator.next();
					weights[count++] = integer;
				}
				sashForm.setWeights(weights);
			}

			// add an adapter to the model sash form so we can respond to model
			// changes and apply the weight changes to the widget
			sashPart.eAdapters().add(new AdapterImpl() {
				@Override
				public void notifyChanged(Notification msg) {
					if (ApplicationPackage.Literals.MSASH_FORM__WEIGHTS
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
		MSashForm<?> sfm = (MSashForm<?>) sf.getData(OWNING_ME);
		// get the weights of the widget and the model model
		int[] ctrlWeights = sf.getWeights();
		EList<Integer> modelWeights = sfm.getWeights();

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
		org.eclipse.e4.ui.model.application.MSashForm<?> sfm = (org.eclipse.e4.ui.model.application.MSashForm<?>) sf
				.getData(OWNING_ME);
		int[] ctrlWeights = sf.getWeights();
		EList<Integer> modelWeights = sfm.getWeights();

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
