/*******************************************************************************
 * Copyright (c) 2005, 2017 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.tests.viewers;


import org.eclipse.jface.viewers.ILazyTreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.junit.Assert;

public class TestModelLazyTreeContentProvider extends TestModelContentProvider
		implements ILazyTreeContentProvider {

	private final TreeViewer treeViewer;

	public TestModelLazyTreeContentProvider(TreeViewer treeViewer) {
		this.treeViewer = treeViewer;
	}

	@Override
	public void updateElement(Object parent, int index) {
		TestElement parentElement = (TestElement) parent;
		if(parentElement.getChildCount() > index) {
			TestElement childElement = parentElement.getChildAt(index);
			treeViewer.replace(parent, index, childElement);
			treeViewer.setChildCount(childElement, childElement.getChildCount());
		}
	}

	@Override
	public Object[] getChildren(Object element) {
		Assert.fail("should not be called on a LazyTreeContentProvider");
		return null;
	}

	@Override
	public Object[] getElements(Object element) {
		Assert.fail("should not be called on a LazyTreeContentProvider");
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		Assert.fail("should not be called on a LazyTreeContentProvider");
		return false;
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput,
			final Object newInput) {
		super.inputChanged(viewer, oldInput, newInput);
	}

	@Override
	public void updateChildCount(Object element, int currentChildCount) {
		treeViewer.setChildCount(element, ((TestElement) element).getChildCount());
	}

}
