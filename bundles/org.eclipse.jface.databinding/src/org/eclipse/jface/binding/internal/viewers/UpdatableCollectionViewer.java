/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.binding.internal.viewers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.binding.IUpdatableCollection;
import org.eclipse.jface.binding.Updatable;
import org.eclipse.jface.viewers.AbstractListViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * @since 3.2
 *
 */
public class UpdatableCollectionViewer extends Updatable implements
		IUpdatableCollection {

	private final AbstractListViewer viewer;

	private List elements = new ArrayList();

	/**
	 * @param viewer
	 */
	// TODO for ComboViewer, sometimes you want to add an "empty element" so
	// that the user can null the current selection...
	public UpdatableCollectionViewer(AbstractListViewer viewer) {
		this.viewer = viewer;
		viewer.setContentProvider(new IStructuredContentProvider() {

			public Object[] getElements(Object inputElement) {
				return elements.toArray();
			}

			public void dispose() {
			}

			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
			}
		});
		viewer.setInput(this);
	}

	public int getSize() {
		return elements.size();
	}

	public int addElement(Object element, int index) {
		int position = primAddElement(element, index);
		if (position == elements.size() - 1 || viewer.getSorter() != null)
			viewer.add(element);
		else
			viewer.refresh();
		return position;
	}

	private int primAddElement(Object element, int index) {
		int position = elements.size();
		if (index < 0 || index > elements.size()) {
			position = elements.size();
			elements.add(element);
		} else {
			elements.add(index, element);
			position = index;
		}
		return position;
	}

	public void removeElement(int index) {
		Object element = elements.remove(index);
		viewer.remove(element);
	}

	public void setElement(int index, Object element) {
		if (elements.get(index).equals(element)) {
			viewer.update(element, null);
		} else {
			removeElement(index);
			addElement(element, index);
		}
	}

	public Object getElement(int index) {
		return elements.get(index);
	}

	public Class getElementType() {
		return Object.class;
	}

}
