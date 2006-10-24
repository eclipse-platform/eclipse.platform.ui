/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.tests.viewers;

import junit.framework.Assert;

import org.eclipse.jface.viewers.ILazyTreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

public class TestModelLazyTreeContentProvider extends TestModelContentProvider
		implements ILazyTreeContentProvider {

	private final TreeViewer treeViewer;

	public TestModelLazyTreeContentProvider(TreeViewer treeViewer) {
		this.treeViewer = treeViewer;
	}

	public void updateElement(Object parent, int index) {
		TestElement parentElement = (TestElement) parent;
		if(parentElement.getChildCount() > index) {
			TestElement childElement = parentElement.getChildAt(index);
			treeViewer.replace(parent, index, childElement);
			treeViewer.setChildCount(childElement, childElement.getChildCount());
		}
	}

	public Object[] getChildren(Object element) {
		Assert.fail("should not be called on a LazyTreeContentProvider");
		return null;
	}

	public Object[] getElements(Object element) {
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
	}

	public void updateChildCount(Object element, int currentChildCount) {
		treeViewer.setChildCount(element, ((TestElement) element).getChildCount());
	}

}
