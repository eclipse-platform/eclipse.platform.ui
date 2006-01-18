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
import org.eclipse.jface.databinding.SelectionAwareUpdatableCollection;
import org.eclipse.jface.internal.databinding.swt.AsyncRunnable;
import org.eclipse.jface.internal.databinding.swt.SyncRunnable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

/**
 * @since 3.2
 * TODO: rather than maintaining its own collection of elements, it should use the model side of the binding directly as the viewer's model
 * (though I'm assuming the viewer collection is being used as the target in this case).  
 * Note that CollectionBinding copies one at a time from the model to the target as well.
 */
public class TableViewerUpdatableCollection extends SelectionAwareUpdatableCollection {

	protected final TableViewer viewer;

	private List elements = new ArrayList();

	/**
	 * @param viewer
	 */
	public TableViewerUpdatableCollection(TableViewer viewer) {
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
	}

	public int getSize() {
		return elements.size();
	}
	
	public int addElement(final Object element, final int index){
		SyncRunnable runnable = new SyncRunnable(){
			public Object run(){
				return new Integer(internalAddElement(element,index));
			}
		};
		return ((Integer)runnable.runOn(viewer.getControl().getDisplay())).intValue();
	}

	private int internalAddElement(Object element, int index) {
		int position = primAddElement(element, index);
		if (position == elements.size() - 1 || viewer.getSorter() != null)
			viewer.add(element);
		else
			viewer.refresh();
		return position;
	}

	private int primAddElement(Object element, int index) {
		int position;
		if (index < 0 || index > elements.size()) {
			position = elements.size();
			elements.add(element);
		} else {
			elements.add(index, element);
			position = index;
		}
		return position;
	}
	
	public void removeElement(final int index){
		// Even though this could be run Async because we don't need a result, we must run it sync otherwise
		// it races against the sync code in addElement(...)
		SyncRunnable runnable = new SyncRunnable(){
			public Object run(){
				internalRemoveElement(index);
				return null;
			}
		};
		runnable.runOn(viewer.getControl().getDisplay());
	}

	public void internalRemoveElement(int index) {
		Object element = elements.remove(index);
		viewer.remove(element);
	}

	public void setElement(final int index, final Object element) {
		AsyncRunnable runnable = new AsyncRunnable(){
			public void run(){
				if (elements.get(index).equals(element)) {
					viewer.update(element, null);
				} else {
					internalRemoveElement(index);
					internalAddElement(element, index);
				}				
			}
		};
		runnable.runOn(viewer.getControl().getDisplay());
	}

	public Object getElement(final int index) {
		SyncRunnable runnable = new SyncRunnable(){
			public Object run(){
				return elements.get(index);
			}
		};
		return runnable.runOn(viewer.getControl().getDisplay());
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

	public Class getElementType() {
		return Object.class;
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
}
