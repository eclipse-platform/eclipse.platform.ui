/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.decorators;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * A content provider with a dummy tree structure.
 */
public class TestTreeContentProvider implements ITreeContentProvider {


	@Override
	public Object[] getChildren(Object parentElement) {
		TreeElement parent = (TreeElement) parentElement;
		TreeElement[] children = new TreeElement[10];
		for (int i = 0; i < 10; i++) {
			children[i] = new TreeElement(parent, i);
		}
		return children;

	}

	@Override
	public Object getParent(Object element) {
		return ((TreeElement) element).parent;
	}

	@Override
	public boolean hasChildren(Object element) {
		return true;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		TreeElement root = new TreeElement(null, 0);
		return new Object[] { root };
	}

	@Override
	public void dispose() {
		//No dispose behavior

	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {


	}

}
