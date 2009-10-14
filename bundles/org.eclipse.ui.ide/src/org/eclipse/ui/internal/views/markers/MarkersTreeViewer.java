/*******************************************************************************
 * Copyright (c) 2007, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.views.markers;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

/**
 * The MarkersTreeViewer is a viewer that optimizes the expandToLevel method.
 * 
 * @since 3.4
 * 
 */

// TODO Delete this class if Bug 201135 is fixed.
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=201135
public class MarkersTreeViewer extends TreeViewer {

	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param parent
	 * @param style
	 */
	public MarkersTreeViewer(Composite parent, int style) {
		super(parent, style);

	}

	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param parent
	 */
	public MarkersTreeViewer(Composite parent) {
		super(parent);
	}

	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param tree
	 */
	public MarkersTreeViewer(Tree tree) {
		super(tree);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.AbstractTreeViewer#expandToLevel(java.lang.Object,
	 *      int)
	 */
	public void expandToLevel(Object elementOrTreePath, int level) {
		if (level == 1) {
			Widget widget = findItem(elementOrTreePath);
			if (widget != null && widget instanceof TreeItem) {
				((TreeItem) widget).setExpanded(true);
				return;
			}
		}
		super.expandToLevel(elementOrTreePath, level);
	}

	/**
	 * Remove all of the entries and unmap all of the elements.
	 */
	public void removeAndClearAll() {
		removeAll(getControl());
		unmapAllElements();		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.AbstractTreeViewer#doUpdateItem(org.eclipse
	 * .swt.widgets.Item, java.lang.Object)
	 */
	protected void doUpdateItem(Item item, Object element) {
		super.doUpdateItem(item, element);
		/*
		 * For performance reasons clear cache of the item used in updating UI.
		 */
		MarkerSupportItem cellItem=(MarkerSupportItem) element;
		if(cellItem.isConcrete())cellItem.clearCache();
	}

}
