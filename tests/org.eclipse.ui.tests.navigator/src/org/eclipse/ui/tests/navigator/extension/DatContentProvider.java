package org.eclipse.ui.tests.navigator.extension;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class DatContentProvider implements ITreeContentProvider {
	
	private TestExtensionTreeData child = new TestExtensionTreeData(null, "Child", null, null);

	public Object[] getChildren(Object parentElement) { 
		return new Object[] { child } ;
	}

	public Object getParent(Object element) { 
		return null;
	}

	public boolean hasChildren(Object element) { 
		return false;
	}

	public Object[] getElements(Object inputElement) { 
		return null;
	}

	public void dispose() { 

	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) { 

	}

}
