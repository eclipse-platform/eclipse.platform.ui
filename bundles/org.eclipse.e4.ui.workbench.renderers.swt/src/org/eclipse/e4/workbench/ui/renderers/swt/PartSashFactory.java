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

import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.emf.common.util.EList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;

public class PartSashFactory extends SWTPartFactory {

	public PartSashFactory() {
		super();
	}

	public Widget createWidget(MPart<?> part) {

		Widget parentWidget = getParentWidget(part);

		if (part instanceof org.eclipse.e4.ui.model.application.MSashForm<?>) {
			org.eclipse.e4.ui.model.application.MSashForm<?> sashModel = (org.eclipse.e4.ui.model.application.MSashForm<?>) part;
			int orientation = (sashModel.getPolicy() != null && sashModel
					.getPolicy().startsWith("Horizontal")) ? SWT.HORIZONTAL //$NON-NLS-1$
					: SWT.VERTICAL;
			SashForm newSash = new SashForm((Composite) parentWidget,
					SWT.SMOOTH | orientation);
			bindWidget(part, newSash);
			// newSash.setSashWidth(1);
			newSash.setVisible(true);
			return newSash;
		}

		return null;
	}

	public void postProcess(MPart<?> part) {
		if (part instanceof org.eclipse.e4.ui.model.application.MSashForm<?>) {
			// do we have any children ?
			EList<?> kids = part.getChildren();
			if (kids.size() == 0)
				return;

			// set the weights of the sashes
			SashForm sashForm = (SashForm) part.getWidget();
			org.eclipse.e4.ui.model.application.MSashForm<?> sashPart = (org.eclipse.e4.ui.model.application.MSashForm<?>) part;
			List<Integer> weightList = sashPart.getWeights();

			// If it's not already initialized the set them all ==
			if (weightList.size() != kids.size()) {
				for (int i = 0; i < kids.size(); i++) {
					weightList.add(new Integer(100 + i));
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
