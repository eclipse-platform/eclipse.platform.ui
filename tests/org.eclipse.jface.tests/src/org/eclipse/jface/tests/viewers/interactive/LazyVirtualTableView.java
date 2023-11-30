/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
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
 *     Jeanderson Candido <http://jeandersonbc.github.io> - Bug 433608
 *******************************************************************************/
package org.eclipse.jface.tests.viewers.interactive;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * The LazyVirtualTableView is the VirtualTableView with lazy content.
 */
public class LazyVirtualTableView extends VirtualTableView {

	private List<String> elements;

	/**
	 * Create a new instance of the receiver.
	 */
	public LazyVirtualTableView() {
		super();
		initElements();
	}

	private void initElements() {
		elements = new ArrayList<>();
		for (int i = 0; i < itemCount; i++) {
			elements.add("Element " + i);
		}
	}

	@Override
	protected IContentProvider getContentProvider() {
		return new ILazyContentProvider() {

			@Override
			public void updateElement(int index) {
				viewer.replace(elements.get(index), index);
			}

			@Override
			public void dispose() {
				// Do Nothing
			}

			@Override
			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
				// Do nothing.
			}
		};
	}

	protected void doRemove(Object[] selection, int[] selectionIndices) {
		for (int index : selectionIndices) {
			elements.remove(index);
		}
		super.doRemove(selection);
	}

	@Override
	protected void resetInput() {
		viewer.setItemCount(itemCount);
		super.resetInput();
	}
}
