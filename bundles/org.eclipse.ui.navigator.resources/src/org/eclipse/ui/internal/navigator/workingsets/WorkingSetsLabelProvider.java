package org.eclipse.ui.internal.navigator.workingsets;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * Provides a text label and icon for Working Sets.
 * 
 */
public class WorkingSetsLabelProvider implements ILabelProvider {

	private WorkbenchLabelProvider labelProvider = new WorkbenchLabelProvider();

	public Image getImage(Object element) {
		if (element instanceof IWorkingSet)
			return labelProvider.getImage(element);
		return null;
	}

	public String getText(Object element) {
		if (element instanceof IWorkingSet)
			return ((IWorkingSet) element).getLabel();
		return null;
	}

	public void addListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub

	}

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public boolean isLabelProperty(Object element, String property) {
		// TODO Auto-generated method stub
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub

	}

}
