package org.eclipse.e4.workbench.modeling;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

public class ModeledLabelProvider extends LabelProvider {
	private ModelService modelSvc;

	public ModeledLabelProvider(ModelService modelSvc) {
		this.modelSvc = modelSvc;
	}

	@Override
	public Image getImage(Object element) {
		return null;
	}

	@Override
	public String getText(Object element) {
		String label = (String) modelSvc.getProperty(element, "Label"); //$NON-NLS-1$
		if (label == null)
			label = super.getText(element);
		
		return label;
	}

}
