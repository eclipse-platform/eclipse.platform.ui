/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search2.internal.ui.basic.views;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

public class TreeViewerNavigator implements INavigate {
	private TreeViewer fViewer;
	
	public TreeViewerNavigator(TreeViewer viewer) {
		fViewer= viewer;
	}
	
	
	private TreeItem getNextItem(TreeItem item, boolean forward) {
		while (true) {
			if (item == null)
				return null;
			TreeItem[] siblings= getSiblings(item);
			for (int i= 0; i < siblings.length; i++) {
				if (item.getData().equals(siblings[i].getData())) {
					if (forward) {
						if (i < siblings.length-1)
							return siblings[i+1];
						else
							break;
					} else {
						if (i > 0) {
							return siblings[i-1];
						} else {
							break;
						}
					}
				}
			}
			item= item.getParentItem();
		}
	}

	private TreeItem[] getSiblings(TreeItem item) {
		TreeItem parentItem= item.getParentItem();
		if (parentItem != null)
			return parentItem.getItems();
		Tree tree= item.getParent();
		return tree.getItems();
	}
	
	private TreeItem getLeaf(TreeItem item, boolean forward) {
		while (true) {
			fViewer.setExpandedState(item.getData(), true);
			TreeItem[] children= item.getItems();
			if (children.length == 0)
				return item;
			if (forward)
				item= children[0];
			else
				item= children[children.length-1];
		}
	}
	
	private TreeItem getNextLeaf(TreeItem item, boolean forward) {
		if (item != null && forward && item.getItemCount() > 0)
			return getLeaf(item, forward);
		TreeItem nextItem= getNextItem(item, forward);
		if (nextItem == null) {
			TreeItem[] roots= fViewer.getTree().getItems();
			if (roots.length > 0) {
				if (forward)
					nextItem= roots[0];
				else
					nextItem= roots[roots.length-1];
			}
		}
		if (nextItem != null)
			return getLeaf(nextItem, forward);
		return null;
	}
	
	public void navigateNext(boolean forward) {
		Item[] selection= fViewer.getTree().getSelection();
		TreeItem nextItem= null;
		if (selection.length > 0) {
			nextItem= getNextLeaf((TreeItem) (forward?selection[0]:selection[selection.length-1]), forward);
		} else {
			nextItem= getNextLeaf(null, forward);
		}
		if (nextItem != null) {
			internalSetSelection(nextItem);
		}
	}
	
	private void internalSetSelection(TreeItem ti) {
		if (ti != null) {
			Object data= ti.getData();
			if (data != null) {
				ISelection selection= new StructuredSelection(data);
				fViewer.setSelection(selection, true);
			}
		}
	}
}
