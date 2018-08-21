/*******************************************************************************
 * Copyright (c) 2003, 2013 IBM Corporation and others.
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
package org.eclipse.ant.internal.ui.model;

import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class AntModelContentProvider implements ITreeContentProvider {

	protected static final Object[] EMPTY_ARRAY = new Object[0];

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	@Override
	public void dispose() {
		// do nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// do nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(Object)
	 */
	@Override
	public Object[] getChildren(Object parentNode) {
		if (parentNode instanceof AntElementNode) {
			AntElementNode parentElement = (AntElementNode) parentNode;
			if (parentElement.hasChildren()) {
				List<IAntElement> children = parentElement.getChildNodes();
				return children.toArray();
			}
		} else if (parentNode instanceof IAntModel) {
			return new Object[] { ((IAntModel) parentNode).getProjectNode() };
		}
		return EMPTY_ARRAY;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(Object)
	 */
	@Override
	public Object getParent(Object aNode) {
		AntElementNode tempElement = (AntElementNode) aNode;
		return tempElement.getParentNode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(Object)
	 */
	@Override
	public boolean hasChildren(Object aNode) {
		return ((AntElementNode) aNode).hasChildren();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(Object)
	 */
	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof IAntModel) {
			AntProjectNode projectNode = ((IAntModel) inputElement).getProjectNode();
			if (projectNode == null) {
				return new AntElementNode[0];
			}

			return new Object[] { projectNode };
		}
		if (inputElement instanceof List) {
			return ((List<?>) inputElement).toArray();
		}
		if (inputElement instanceof Object[]) {
			return (Object[]) inputElement;
		}
		return EMPTY_ARRAY;
	}
}
