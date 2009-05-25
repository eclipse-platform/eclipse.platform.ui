/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.tests.viewers.interactive;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * The LazyVirtualTableView is the VirtualTableView with
 * lazy content.
 */
public class LazyVirtualTableView extends VirtualTableView {

	private List elements;

	/**
	 * Create a new instance of the receiver.
	 */
	public LazyVirtualTableView() {
		super();
		initElements();
	}

	/**
	 * 
	 */
	private void initElements() {
		elements = new ArrayList();
		for (int i = 0; i < itemCount; i++) {
			elements.add("Element " + String.valueOf(i));
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.tests.viewers.interactive.VirtualTableView#getContentProvider()
	 */
	protected IContentProvider getContentProvider() {
		return new ILazyContentProvider() {
			
			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.ILazyContentProvider#updateElements(int, int)
			 */
			public void updateElement(int index) {
		        viewer.replace(elements.get(index), index);
			}
			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
			 */
			public void dispose() {
				//Do Nothing
			}

			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
			 */
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				// Do nothing.
			}
		};
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.tests.viewers.interactive.VirtualTableView#doRemove(java.lang.Object[])
	 */
	protected void doRemove(Object[] selection, int[] selectionIndices) {
		for (int i = 0; i < selectionIndices.length; i++) {
			int index = selectionIndices[i];
			elements.remove(index);
		}
		super.doRemove(selection, selectionIndices);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.tests.viewers.interactive.VirtualTableView#resetInput()
	 */
	protected void resetInput() {
		viewer.setItemCount(itemCount);
		super.resetInput();
	}
}
