/*******************************************************************************
 * Copyright (c) 2010 Oakland Software Incorporated and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Francis Upton IV, Oakland Software - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.tests.navigator.extension;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * Provides some children for a given resource.
 */
public class TestSimpleChildrenContentProvider implements ITreeContentProvider {

	public static final int NUM_ITEMS = 4;

	public String _name;

	private Object[] _children;

	public class SimpleChild {
		public String _name;
		public Object _parent;

		public String toString() {
			return _name;
		}
	}

	public TestSimpleChildrenContentProvider() {
	}

	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof IResource) {
			if (_children == null) {
				List l = new ArrayList();
				for (int i = 0; i < NUM_ITEMS; i++) {
					SimpleChild child = new SimpleChild();
					child._parent = parentElement;
					child._name = _name + i;
					l.add(child);
				}
				_children = l.toArray();
			}
			return _children;
		}
		return new Object[] {};
	}

	public Object getParent(Object element) {
		SimpleChild child = (SimpleChild) element;
		return child._parent;
	}

	public boolean hasChildren(Object element) {
		return true;
	}

	public void inputChanged(Viewer aViewer, Object oldInput, Object newInput) {
		_children = null;
	}

	public void dispose() {
	}
}
