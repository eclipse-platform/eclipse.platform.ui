/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.viewers;

import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.util.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

/**
 * A concrete viewer based on an SWT <code>Tree</code> control.
 * <p>
 * This class is not intended to be subclassed outside the viewer framework. 
 * It is designed to be instantiated with a pre-existing SWT tree control and configured
 * with a domain-specific content provider, label provider, element filter (optional),
 * and element sorter (optional).
 * </p>
 * <p>
 * Content providers for tree viewers must implement the <code>ITreeContentProvider</code>
 * interface.
 * </p>
 */
public class TreeViewer extends AbstractTreeViewer {
	
	/**
	 * This viewer's control.
	 */
	private Tree tree;
/**
 * Creates a tree viewer on a newly-created tree control under the given parent.
 * The tree control is created using the SWT style bits <code>MULTI, H_SCROLL, V_SCROLL,</code> and <code>BORDER</code>.
 * The viewer has no input, no content provider, a default label provider, 
 * no sorter, and no filters.
 *
 * @param parent the parent control
 */
public TreeViewer(Composite parent) {
	this(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
}
/**
 * Creates a tree viewer on a newly-created tree control under the given parent.
 * The tree control is created using the given SWT style bits.
 * The viewer has no input, no content provider, a default label provider, 
 * no sorter, and no filters.
 *
 * @param parent the parent control
 * @param style the SWT style bits used to create the tree.
 */
public TreeViewer(Composite parent, int style) {
	this(new Tree(parent, style));
}
/**
 * Creates a tree viewer on the given tree control.
 * The viewer has no input, no content provider, a default label provider, 
 * no sorter, and no filters.
 *
 * @param tree the tree control
 */
public TreeViewer(Tree tree) {
	super();
	this.tree = tree;
	hookControl(tree);
}
/* (non-Javadoc)
 * Method declared in AbstractTreeViewer.
 */
protected void addTreeListener(Control c, TreeListener listener) {
	((Tree)c).addTreeListener(listener);
}
/* (non-Javadoc)
 * Method declared in AbstractTreeViewer.
 */
protected void doUpdateItem(final Item item, Object element) {
	
	if(item.isDisposed()){
		unmapElement(element);
		Assert.isTrue(!item.isDisposed(),"Update to disposed element " + element.toString());//$NON-NLS-1$
		return;
	}
	
	// update icon and label
	IBaseLabelProvider baseProvider = getLabelProvider();
	if (baseProvider instanceof IViewerLabelProvider) {
		IViewerLabelProvider provider = (IViewerLabelProvider)baseProvider;
		
		ViewerLabel updateLabel = new ViewerLabel(item.getText(), item.getImage());
		provider.updateLabel(updateLabel, element);
		
		if(updateLabel.hasNewImage())
			item.setImage(updateLabel.getImage());
		if(updateLabel.hasNewText())
			item.setText(updateLabel.getText());
		
	} else {
		if(baseProvider instanceof ILabelProvider){
			ILabelProvider provider = (ILabelProvider)baseProvider;
			item.setText(provider.getText(element));
			Image image = provider.getImage(element);
			if (item.getImage() != image) 
				item.setImage(image);
		}
	}
	if (baseProvider instanceof IColorProvider) {
		IColorProvider cp = (IColorProvider) baseProvider;
		TreeItem treeItem = (TreeItem) item;
		treeItem.setForeground(cp.getForeground(element));
		treeItem.setBackground(cp.getBackground(element));
	}
	if (baseProvider instanceof IFontProvider) {
	    IFontProvider fprov = (IFontProvider) baseProvider;
	    TreeItem treeItem = (TreeItem) item;
	    treeItem.setFont(fprov.getFont(element));
	}			
}
/* (non-Javadoc)
 * Method declared in AbstractTreeViewer.
 */
protected Item[] getChildren(Widget o) {
	if (o instanceof TreeItem)
		return ((TreeItem) o).getItems();
	if (o instanceof Tree)
		return ((Tree) o).getItems();
	return null;
}
/* (non-Javadoc)
 * Method declared in Viewer.
 */
public Control getControl() {
	return tree;
}
/* (non-Javadoc)
 * Method declared in AbstractTreeViewer.
 */
protected boolean getExpanded(Item item) {
	return ((TreeItem) item).getExpanded();
}
/* (non-Javadoc)
 * Method declared in StructuredViewer.
 */
protected Item getItem(int x, int y) {
	return getTree().getItem(getTree().toControl(new Point(x, y)));
}
/* (non-Javadoc)
 * Method declared in AbstractTreeViewer.
 */
protected int getItemCount(Control widget) {
	return ((Tree) widget).getItemCount();
}
/* (non-Javadoc)
 * Method declared in AbstractTreeViewer.
 */
protected int getItemCount(Item item) {
	return ((TreeItem) item).getItemCount();
}
/* (non-Javadoc)
 * Method declared in AbstractTreeViewer.
 */
protected Item[] getItems(Item item) {
	return ((TreeItem) item).getItems();
}
/**
 * The tree viewer implementation of this <code>Viewer</code> framework
 * method returns the label provider, which in the case of tree
 * viewers will be an instance of <code>ILabelProvider</code>.
 */
public IBaseLabelProvider getLabelProvider() {
	return super.getLabelProvider();
}
/* (non-Javadoc)
 * Method declared in AbstractTreeViewer.
 */
protected Item getParentItem(Item item) {
	return ((TreeItem)item).getParentItem();
}
/* (non-Javadoc)
 * Method declared in AbstractTreeViewer.
 */
protected Item[] getSelection(Control widget) {
	return ((Tree) widget).getSelection();
}
/**
 * Returns this tree viewer's tree control.
 *
 * @return the tree control
 */
public Tree getTree() {
	return tree;
}
/* (non-Javadoc)
 * Method declared in AbstractTreeViewer.
 */
protected Item newItem(Widget parent, int flags, int ix) {
	TreeItem item;
	if (ix >= 0) {
		if (parent instanceof TreeItem)
			item = new TreeItem((TreeItem) parent, flags, ix);
		else
			item = new TreeItem((Tree) parent, flags, ix);
	} else {
		if (parent instanceof TreeItem)
			item = new TreeItem((TreeItem) parent, flags);
		else
			item = new TreeItem((Tree) parent, flags);
	}
	return item;
}
/* (non-Javadoc)
 * Method declared in AbstractTreeViewer.
 */
protected void removeAll(Control widget) {
	((Tree) widget).removeAll();
}
/* (non-Javadoc)
 * Method declared in AbstractTreeViewer.
 */
protected void setExpanded(Item node, boolean expand) {
	((TreeItem) node).setExpanded(expand);
}
/**
 * The tree viewer implementation of this <code>Viewer</code> framework
 * method ensures that the given label provider is an instance
 * of <code>ILabelProvider</code>.
 */
public void setLabelProvider(IBaseLabelProvider labelProvider) {
	Assert.isTrue(labelProvider instanceof ILabelProvider);
	super.setLabelProvider(labelProvider);
}
/* (non-Javadoc)
 * Method declared in AbstractTreeViewer.
 */
protected void setSelection(List items) {
	
	Item[] current = getSelection(getTree());
	
	//Don't bother resetting the same selection
	if(haveSameData(items,current))
		return;
	 
	TreeItem[] newItems = new TreeItem[items.size()];
	items.toArray(newItems);
	getTree().setSelection(newItems);
}

/**
 * Returns <code>true</code> if the given list and array of items
 * refer to the same model elements.  Order is unimportant.
 * 
 * @param items the list of items
 * @param current the array of items
 * @return <code>true</code> if the refer to the same elements, <code>false</code> otherwise
 */
private boolean haveSameData(List items, Item[] current){
	//If they are not the same size then they are not equivalent
    int n = items.size();
	if (n != current.length)
		return false;
	
	CustomHashtable itemSet = newHashtable(n*2+1);
	for (Iterator i = items.iterator(); i.hasNext();) {
        Item item = (Item) i.next();
        Object element = item.getData();
		itemSet.put(element, element);
	}
	
	//Go through the items of the current collection
	//If there is a mismatch return false
	for (int i = 0; i < current.length; i++) {
		if (!itemSet.containsKey(current[i].getData()))
			return false;
	}
	
	return true;
}
/* (non-Javadoc)
 * Method declared in AbstractTreeViewer.
 */
protected void showItem(Item item) {
	getTree().showItem((TreeItem)item);
}
}
