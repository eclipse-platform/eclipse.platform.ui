package org.eclipse.jface.tests.viewers;

import junit.framework.Assert;

import org.eclipse.jface.viewers.ILazyTreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;

public class TestModelLazyTreeContentProvider extends TestModelContentProvider
		implements ILazyTreeContentProvider {

	private final TreeViewer treeViewer;

	public TestModelLazyTreeContentProvider(TreeViewer treeViewer) {
		this.treeViewer = treeViewer;
	}

	public void updateElement(Object parent, int index) {
		TestElement parentElement = (TestElement) parent;
		TestElement childElement = parentElement.getChildAt(index);
		treeViewer.replace(parent, index, childElement);
		treeViewer.setChildCount(childElement, childElement.getChildCount());
	}

	public Object[] getChildren(Object element) {
		Assert.fail("should not be called on a LazyTreeContentProvider");
		return null;
	}

	public Object[] getElements(Object element) {
		Assert.fail("should not be called on a LazyTreeContentProvider");
		return null;
	}

	public Object getParent(Object element) {
		Assert.fail("should not be called on a LazyTreeContentProvider");
		return null;
	}

	public boolean hasChildren(Object element) {
		Assert.fail("should not be called on a LazyTreeContentProvider");
		return false;
	}

	public void inputChanged(Viewer viewer, Object oldInput,
			final Object newInput) {
		super.inputChanged(viewer, oldInput, newInput);
		treeViewer.getTree().setItemCount(0);
		if (newInput != null) {
			final TestElement testElement = (TestElement) newInput;
			Display.getCurrent().asyncExec(new Runnable() {
				public void run() {
					treeViewer.setChildCount(newInput, testElement
							.getChildCount());
				};
			});
		}
	}

}
