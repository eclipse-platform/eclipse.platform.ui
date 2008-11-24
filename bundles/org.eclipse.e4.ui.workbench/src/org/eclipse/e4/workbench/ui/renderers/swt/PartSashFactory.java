package org.eclipse.e4.workbench.ui.renderers.swt;

import java.util.Iterator;
import java.util.List;

import org.eclipse.e4.ui.model.application.Part;
import org.eclipse.emf.common.util.EList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Widget;

public class PartSashFactory extends SWTPartFactory {

	public PartSashFactory() {
		super();
	}

	public Widget createWidget(Part<?> part) {
		Widget parentWidget = getParentWidget(part);

		if (part instanceof org.eclipse.e4.ui.model.application.SashForm<?>) {
			org.eclipse.e4.ui.model.application.SashForm<?> sashModel = (org.eclipse.e4.ui.model.application.SashForm<?>) part;
			int orientation = (sashModel.getPolicy() != null && sashModel
					.getPolicy().startsWith("Horizontal")) ? SWT.HORIZONTAL
					: SWT.VERTICAL;
			SashForm newSash = new SashForm((Composite) parentWidget,
					SWT.SMOOTH | orientation);
			// newSash.setSashWidth(1);
			newSash.setVisible(true);
			return newSash;
		}

		return null;
	}

	public void postProcess(Part<?> part) {
		if (part instanceof org.eclipse.e4.ui.model.application.SashForm<?>) {
			// do we have any children ?
			EList<?> kids = part.getChildren();
			if (kids.size() == 0)
				return;
			
			// set the weights of the sashes
			SashForm sashForm = (SashForm) part.getWidget();
			org.eclipse.e4.ui.model.application.SashForm<?> sashPart =
				(org.eclipse.e4.ui.model.application.SashForm<?>) part;
			List<Integer> weightList = sashPart.getWeights();
			
			// If it's not already initialized the set them all ==
			if (weightList.size() != kids.size()) {
				for (int i = 0; i < kids.size(); i++) {
					weightList.add(new Integer(100+i));
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
		}
	}
}
