/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search2.internal.ui.basic.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;

/**
 * @author Thomas Mäder
 *
 */
public class SearchResultsTreeViewer extends TreeViewer implements INavigate {

	/**
	 * @param parent
	 * @param style
	 */
	public SearchResultsTreeViewer(Composite parent, int style) {
		super(parent, style);
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
			createChildren(item);
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
			TreeItem[] roots= getTree().getItems();
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
		Item[] selection= getSelection(getTree());
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
				setSelection(selection, true);
			}
		}
	}
	
	
	// reimplementation of AbstractTreeViewer methods to avoid flickering.
	public void add(Object parentElement, Object[] childElements) {
		Assert.isNotNull(parentElement);
		Assert.isNotNull(childElements);
		Widget widget = findItem(parentElement);
		// If parent hasn't been realized yet, just ignore the add.
		if (widget == null)
			return;

		// optimization!
		// if the widget is not expanded we just invalidate the subtree
		if (widget instanceof Item) {
			Item ti = (Item) widget;
			if (!getExpanded(ti)) {
				boolean needDummy = isExpandable(parentElement);
				// remove all children
				Item[] items = getItems(ti);
				boolean haveDummy = false;
				for (int i = 1; i < items.length; i++) {
					if (items[i].getData() != null) {
						disassociate(items[i]);
						items[i].dispose();
					} else {
						if (needDummy && !haveDummy) {
							haveDummy = true;
						} else {
							items[i].dispose();
						}
					}
				}
				if (items.length > 0) {
					if (items[0].getData() != null) {
						disassociate(items[0]);
						items[0].setData(null);
					}
					haveDummy= true;
				}
				// append a dummy if necessary
				if (needDummy && !haveDummy) {
					newItem(ti, SWT.NULL, -1);
				} else {
					// XXX: Workaround (PR missing)
					//tree.redraw();
				}

				return;
			}
		}

		if (childElements.length > 0) {
			Object[] filtered = filter(childElements);
			for (int i = 0; i < filtered.length; i++) {
				createAddedElement(widget,filtered[i]);				
			}
		}
	}
	/**
	 * Create the new element in the parent widget. If the
	 * child already exists do nothing.
	 * @param widget
	 * @param element
	 */
	private void createAddedElement(Widget widget, Object element){
		
		if(equals(element,widget.getData()))
			return;
		
		Item[] items = getChildren(widget); 
		for(int i = 0; i < items.length; i++){
			if(items[i].getData().equals(element))
				return;
		}				
		
		int index = indexForElement(widget, element);
		createTreeItem(widget, element, index);
	}
	
}
