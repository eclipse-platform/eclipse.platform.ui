/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.debug.internal.ui.views.memory;

import java.util.ArrayList;
import java.util.Hashtable;

import org.eclipse.debug.internal.ui.viewers.AsynchronousTreeViewer;
import org.eclipse.swt.widgets.TreeItem;

public class MemoryBlockNavigationModel  {

	private ArrayList fElements;
	private AsynchronousTreeViewer fViewer = null;
	private Hashtable fLabels;
	
	public MemoryBlockNavigationModel(AsynchronousTreeViewer viewer) {
		init(viewer);
	}

	/**
	 * initialize cache of 
	 * @param viewer
	 */
	private void init(AsynchronousTreeViewer viewer) {
		fViewer = viewer;
		fElements = new ArrayList();
		fLabels = new Hashtable();
		TreeItem[] items = viewer.getTree().getItems();
		traverse(items);
	}
	
	private void traverse(TreeItem[] items) {
		for (int i = 0; i < items.length; i++) {
			TreeItem item = items[i];
			Object data = item.getData();
			if (data != null) {
				fElements.add(data);
				fLabels.put(data, item.getText());
				traverse(item.getItems());
			}
		}
	}

	/**
	 * Returns all the elements in the tree.
	 * 
	 * @return
	 */
	public Object[] getElements() {
		return fElements.toArray();
	}
	
	/**
	 * Returns the viewer this navigation model is for.
	 * 
	 * @return
	 */
	AsynchronousTreeViewer getViewer() {
		return fViewer;
	}
	
	public String getLabel(Object item)
	{
		return (String)fLabels.get(item);
	}
}
