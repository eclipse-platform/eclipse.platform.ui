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

 
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.debug.internal.ui.views.IRemoteTreeViewerUpdateListener;
import org.eclipse.debug.internal.ui.views.RemoteTreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.Widget;

/**
 * Variables viewer. As the user steps through code, this
 * we ensure that newly added varibles are visible.
 */
public class VariablesViewer extends RemoteTreeViewer {
    
    private ArrayList fUpdateListeners = new ArrayList();
	
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
	 * @see AbstractTreeViewer#newItem(Widget, int, int)
	 */
	protected Item newItem(Widget parent, int style, int index) {
		Item item = super.newItem(parent, style, index);
		if (index != -1 && getSelection(getControl()).length == 0) {
			//ignore the dummy items
			showItem(item);
		} 
		return item;
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
     * @see org.eclipse.debug.internal.ui.views.RemoteTreeViewer#restoreExpansionState()
     */
    protected synchronized void restoreExpansionState() {
        cancelJobs();
        for (Iterator i = fUpdateListeners.iterator(); i.hasNext();) {
            IRemoteTreeViewerUpdateListener listener = (IRemoteTreeViewerUpdateListener) i.next();
            listener.treeUpdated();
        }
    }
    
    public void addUpdateListener(IRemoteTreeViewerUpdateListener listener) {
        fUpdateListeners.add(listener);
    }
    public void removeUpdateListener(IRemoteTreeViewerUpdateListener listener) {
        fUpdateListeners.remove(listener);
    }
}
