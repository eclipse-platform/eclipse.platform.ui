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
package org.eclipse.jface.tests.viewers.interactive;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * The LazyVirtualTableView is the VirtualTableView with
 * lazy content.
 */
public class LazyVirtualTableView extends VirtualTableView {

	/**
	 * Create a new instance of the receiver.
	 */
	public LazyVirtualTableView() {
		super();
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
		        viewer.replace("Element " + String.valueOf(index), index);
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
	 * @see org.eclipse.jface.tests.viewers.interactive.VirtualTableView#resetInput()
	 */
	protected void resetInput() {
		viewer.setItemCount(itemCount);
		super.resetInput();
	}
}
