package org.eclipse.debug.internal.ui.views.launch;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
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
public class LaunchViewer extends TreeViewer {
		
	public LaunchViewer(Composite parent) {
		super(new Tree(parent, SWT.MULTI));
		setUseHashlookup(true);
	}
			
	/**
	 * Update the icons for all stack frame children of the given thread.
	 */	
	protected void updateStackFrameIcons(IThread parentThread) {
		Widget parentItem= findItem(parentThread);
		if (parentItem != null) {
			Item[] items= getItems((Item)parentItem);
			for (int i = 0; i < items.length; i++) {
				TreeItem treeItem = (TreeItem)items[i];
				updateOneStackFrameIcon(treeItem, (IStackFrame)treeItem.getData());
			}
		}
	}
	
	/**
	 * For the given stack frame and associated TreeItem, update the icon on the
	 * TreeItem.
	 */
	protected void updateOneStackFrameIcon(TreeItem treeItem, IStackFrame stackFrame) {
		ILabelProvider provider = (ILabelProvider) getLabelProvider();
		Image image = provider.getImage(stackFrame);
		if (image != null) {
			treeItem.setImage(image);
		}			
	}
	
	/**
	 * @see StructuredViewer#refresh(Object)
	 */
	public void refresh(Object element) {
		//@see bug 7965 - Debug view refresh flicker
		getControl().setRedraw(false);
		super.refresh(element);
		getControl().setRedraw(true);
	}
}

