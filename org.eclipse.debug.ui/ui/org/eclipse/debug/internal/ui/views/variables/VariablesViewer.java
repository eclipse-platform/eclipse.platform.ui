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
package org.eclipse.debug.internal.ui.views.variables;

 
import org.eclipse.debug.internal.ui.views.DebugViewInterimLabelProvider;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

/**
 * Variables viewer. As the user steps through code, this
 * we ensure that newly added varibles are visible.
 */
public class VariablesViewer extends TreeViewer {

	private Item fNewItem;
	
	/**
	 * Constructor for VariablesViewer.
	 * @param parent
	 */
	public VariablesViewer(Composite parent) {
		super(parent);
	}

	/**
	 * Constructor for VariablesViewer.
	 * @param parent
	 * @param style
	 */
	public VariablesViewer(Composite parent, int style) {
		super(parent, style);
	}

	/**
	 * Constructor for VariablesViewer.
	 * @param tree
	 */
	public VariablesViewer(Tree tree) {
		super(tree);
	}
	
	/**
	 * Refresh the view, and then do another pass to
	 * update the foreground color for values that have changed
	 * since the last refresh. Values that have not
	 * changed are drawn with the default system foreground color.
	 * If the viewer has no selection, ensure that new items
	 * are visible.
	 * 
	 * @see Viewer#refresh()
	 */
	public void refresh() {
		super.refresh();
		
		ISelection selection = getSelection();
        if (selection.isEmpty()) {
		    if (getNewItem() != null) {
				if (!getNewItem().isDisposed()) {
					//ensure that new items are visible
					showItem(getNewItem());
				}
				setNewItem(null);
		    }
		} else {
		    // Force a selection change to update the details pane
		    setSelection(selection);
		}
	}
	
	/**
	 * @see AbstractTreeViewer#newItem(Widget, int, int)
	 */
	protected Item newItem(Widget parent, int style, int index) {
		if (index != -1) {
			//ignore the dummy items
			setNewItem(super.newItem(parent, style, index));
			return getNewItem();
		} 
		return	super.newItem(parent, style, index);
	}
	
	protected Item getNewItem() {
		return fNewItem;
	}

	protected void setNewItem(Item newItem) {
		fNewItem = newItem;
	}
	
	/**
	 * @see org.eclipse.jface.viewers.AbstractTreeViewer#setExpandedElements(Object[])
	 */
	public void setExpandedElements(Object[] elements) {
		getControl().setRedraw(false);
		super.setExpandedElements(elements);
		getControl().setRedraw(true);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.AbstractTreeViewer#collapseAll()
	 */
	public void collapseAll() {
		//see https://bugs.eclipse.org/bugs/show_bug.cgi?id=39449
		if (getRoot() != null) {
			super.collapseAll();
		}
	}
	
	/* (non-Javadoc)
	 * Method declared in AbstractTreeViewer.
	 */
	protected void doUpdateItem(Item item, Object element) {
		// update icon and label
		ILabelProvider provider= (ILabelProvider) getLabelProvider();
		String text= provider.getText(element);
		if ("".equals(item.getText()) || !DebugViewInterimLabelProvider.PENDING_LABEL.equals(text)) { //$NON-NLS-1$
			// If an element already has a label, don't set the label to
			// the pending label. This avoids labels flashing when they're
			// updated.
			item.setText(text);
		}
		Image image = provider.getImage(element);
		if (item.getImage() != image) {
			item.setImage(image);
		}
		if (provider instanceof IColorProvider) {
			IColorProvider cp = (IColorProvider) provider;
			TreeItem treeItem = (TreeItem) item;
			treeItem.setForeground(cp.getForeground(element));
			treeItem.setBackground(cp.getBackground(element));
		}
	}

}
