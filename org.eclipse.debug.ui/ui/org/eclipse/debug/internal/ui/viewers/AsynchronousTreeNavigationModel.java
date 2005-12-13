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
package org.eclipse.debug.internal.ui.viewers;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.TreeItem;

/**
 * A navigation model is used with a nagivation dialog to find and select an
 * element in an async tree viewer.
 * 
 * @since 3.2
 *
 */
public class AsynchronousTreeNavigationModel extends LabelProvider {
	
	private Map fElementsToItems = new HashMap();
	private AsynchronousTreeViewer fViewer = null;
	
	public AsynchronousTreeNavigationModel(AsynchronousTreeViewer viewer) {
		init(viewer);
	}

	/**
	 * initialize cache of 
	 * @param viewer
	 */
	private void init(AsynchronousTreeViewer viewer) {
		fViewer = viewer;
		TreeItem[] items = viewer.getTree().getItems();
		traverse(items);
	}
	
	private void traverse(TreeItem[] items) {
		for (int i = 0; i < items.length; i++) {
			TreeItem item = items[i];
			Object data = item.getData();
			if (data != null) {
				fElementsToItems.put(data, item);
				traverse(item.getItems());
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
	 */
	public Image getImage(Object element) {
		TreeItem item = getItem(element);
		if (item != null) {
			return item.getImage();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object element) {
		TreeItem item = getItem(element);
		if (item != null) {
			return item.getText();
		}
		return ""; //$NON-NLS-1$
	}
	
	/**
	 * Returns tree item or <code>null</code> if none.
	 * 
	 * @param element
	 * @return
	 */
	TreeItem getItem(Object element) {
		return (TreeItem) fElementsToItems.get(element);
	}
	
	/**
	 * Returns all the elements in the tree.
	 * 
	 * @return
	 */
	Object[] getElements() {
		return fElementsToItems.keySet().toArray();
	}
	
	/**
	 * Returns the viewer this navigation model is for.
	 * 
	 * @return
	 */
	AsynchronousTreeViewer getViewer() {
		return fViewer;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#dispose()
	 */
	public void dispose() {
		super.dispose();
		fElementsToItems.clear();
		fViewer = null;
	}
	
	
	
}
