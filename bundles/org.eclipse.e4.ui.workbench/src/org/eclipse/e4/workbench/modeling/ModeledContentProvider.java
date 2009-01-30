package org.eclipse.e4.workbench.modeling;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class ModeledContentProvider implements ITreeContentProvider {

	private ModelService modelSvc;

	public ModeledContentProvider(ModelService modelSvc) {
		this.modelSvc = modelSvc;
	}
	
	public Object[] getChildren(Object parentElement) {
		return modelSvc.getChildren(parentElement, "Children"); //$NON-NLS-1$
	}

	public Object getParent(Object element) {
		return modelSvc.getProperty(element, "Parent"); //$NON-NLS-1$
	}

	public boolean hasChildren(Object element) {
		return getChildren(element).length > 0;
	}

	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	public void dispose() {
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

}
