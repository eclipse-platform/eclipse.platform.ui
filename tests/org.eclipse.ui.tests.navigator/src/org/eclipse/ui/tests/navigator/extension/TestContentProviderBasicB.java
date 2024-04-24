package org.eclipse.ui.tests.navigator.extension;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * @since 3.3
 */
public class TestContentProviderBasicB implements ITreeContentProvider {

	private final Object[] children = new Object[] { new TreeContentA("child4"), new TreeContentA("child2") };

	@Override
	public void dispose() {
		// nothing
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// nothing
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		return children;
	}

	@Override
	public Object getParent(Object element) {
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		return true;
	}

}

class TreeContentB {
	private final String name;

	public TreeContentB(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}
