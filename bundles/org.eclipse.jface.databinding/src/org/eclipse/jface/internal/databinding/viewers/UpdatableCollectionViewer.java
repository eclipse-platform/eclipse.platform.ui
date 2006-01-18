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
package org.eclipse.jface.internal.databinding.viewers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.databinding.BindingException;
import org.eclipse.jface.databinding.ChangeEvent;
import org.eclipse.jface.databinding.SelectionAwareUpdatableCollection;
import org.eclipse.jface.internal.databinding.swt.SyncRunnable;
import org.eclipse.jface.viewers.AbstractListViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;

/**
 * @since 3.2
 *
 */
public class UpdatableCollectionViewer extends SelectionAwareUpdatableCollection {

	private final AbstractListViewer viewer;

	private List elements = new ArrayList();
	
	private boolean updating=false;

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
		if (!updating) {
			try {
				updating = true;
				int position = primAddElement(element, index);
				if (position == elements.size() - 1
						|| viewer.getSorter() != null)
					viewer.add(element);
				else
					viewer.refresh();
				fireChangeEvent(ChangeEvent.ADD, null, element, position);
				return position;
			} finally {
				updating = false;
			}
		}
		return index;
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
		if (!updating) {
			try {
				updating=true;
				Object element = elements.remove(index);
				viewer.remove(element);
				fireChangeEvent(ChangeEvent.REMOVE, element, null, index);
			} finally  {
				updating=false;
			}
		}
	}

	public void setElement(int index, Object element) {
		
		if (!updating) {            
			try {
				updating=true;
				Object old;
				if (elements.get(index).equals(element)) {
					viewer.update(element, null);
					old = element;
				} else {
					old = getElement(index);
					removeElement(index);
					addElement(element, index);
				}
				fireChangeEvent(ChangeEvent.CHANGE, old, element, index);
			} finally {
				updating=false;
			}
		}
	}

	public Object getElement(int index) {
		return elements.get(index);
	}

	public Class getElementType() {
		return Object.class;
	}

	public Object getSelectedObject() {
		SyncRunnable runnable = new SyncRunnable(){
			public Object run(){
				StructuredSelection selection = (StructuredSelection) viewer.getSelection();
				if (selection.isEmpty()) {
					return null;
				}
				return selection.getFirstElement();
			}
		};
		return runnable.runOn(viewer.getControl().getDisplay());
	}
	
	public void setSelectedObject(Object object) {
	    ISelection selection;
		if (object instanceof ISelection) {
			throw new BindingException("Selected object can not be an ISelection."); //$NON-NLS-1$
		}
		if (object == null) {
			selection = new StructuredSelection();
		} else {
			if (getElements().contains(object)) {
				selection = new StructuredSelection(object);
			} else { 
				return;
			}
		}
		final ISelection selectedObject = selection;
		SyncRunnable runnable = new SyncRunnable(){
			public Object run(){
				viewer.setSelection(selectedObject);
				return null;
			}
		};
		runnable.runOn(viewer.getControl().getDisplay());
	}


	public void setElements(List elements) {
		Object selection = getSelectedObject();
		super.setElements(elements);
		setSelectedObject(selection);
	}
}
