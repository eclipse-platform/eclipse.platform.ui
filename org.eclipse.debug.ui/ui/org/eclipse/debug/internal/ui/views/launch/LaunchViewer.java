/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.launch;


import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.internal.ui.views.RemoteTreeViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

/**
 * The launch viewer displays a tree of launches.
 */
public class LaunchViewer extends RemoteTreeViewer {
    /**
	 * Overridden to fix bug 39709 - duplicate items in launch viewer. The
	 * workaround is required since debug creation events (which result in
	 * additions to the tree) are processed asynchrnously with the expanding
	 * of a launch/debug target in the tree. 
	 * 
	 * @see org.eclipse.jface.viewers.AbstractTreeViewer#add(java.lang.Object, java.lang.Object)
	 */
	public synchronized void add(Object parentElement, Object childElement) {
		if (findItem(childElement) == null) {
			super.add(parentElement, childElement);
		}
	}
	
    public LaunchViewer(Composite parent) {
		super(new Tree(parent, SWT.MULTI));
		setUseHashlookup(true);
	}
			
	/**
	 * Update the images for all stack frame children of the given thread.
	 * 
	 * @param parentThread the thread whose frames should be updated
	 */	
	protected void updateStackFrameImages(IThread parentThread) {
		Widget parentItem= findItem(parentThread);
		if (parentItem != null) {
			Item[] items= getItems((Item)parentItem);
			for (int i = 0; i < items.length; i++) {
				updateTreeItemImage((TreeItem)items[i]);
			}
		}
	}
	
	/**
	 * Updates the image of the given tree item.
	 * 
	 * @param treeItem the item
	 */
	protected void updateTreeItemImage(TreeItem treeItem) {
		ILabelProvider provider = (ILabelProvider) getLabelProvider();
		Image image = provider.getImage(treeItem.getData());
		if (image != null) {
			treeItem.setImage(image);
		}			
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.StructuredViewer#refresh(java.lang.Object)
	 */
	public void refresh(Object element) {
		super.refresh(element);
	}
 
}
