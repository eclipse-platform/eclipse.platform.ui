package org.eclipse.e4.workbench.ui.renderers.swt;

import java.util.Iterator;
import java.util.List;

import org.eclipse.e4.ui.model.application.Part;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Widget;

public class PartSashFactory extends PartFactory {

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
			// set the weights of the sashes
			SashForm sashForm = (SashForm) part.getWidget();
			List<Integer> weightList = ((org.eclipse.e4.ui.model.application.SashForm<?>) part)
					.getWeights();
			if (weightList.size() > 0) {
				int count = 0;
				int[] weights = new int[weightList.size()];
				for (Iterator iterator = weightList.iterator(); iterator
						.hasNext();) {
					Integer integer = (Integer) iterator.next();
					weights[count++] = integer;
				}
				sashForm.setWeights(weights);
			} else {
				// set all the weights equally
				int nKids = sashForm.getChildren().length;
				int[] weights = new int[nKids];
				for (int i = 0; i < nKids; i++)
					weights[i] = 100;
				sashForm.setWeights(weights);
			}
		}
	}
}
