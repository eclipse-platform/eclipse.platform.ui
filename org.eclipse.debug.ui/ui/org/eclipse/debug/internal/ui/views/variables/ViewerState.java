/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software Systems - Mikhail Khodjaiants - Registers View (Bug 53640)
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.variables;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.internal.ui.viewers.AsynchronousTreeViewer;
import org.eclipse.debug.internal.ui.views.AbstractViewerState;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * Memento of the expanded and selected items in a variables viewer.
 * 
 * @since 2.1
 */
public class ViewerState extends AbstractViewerState {

	public ViewerState() {
	}
	
	/**
	 * Constructs a memento for the given viewer.
	 */
	public ViewerState(AsynchronousTreeViewer viewer) {
		super(viewer);
	}

	/**
	 * @see org.eclipse.debug.internal.ui.views.AbstractViewerState#encodeElement(org.eclipse.swt.widgets.TreeItem)
	 */
	protected IPath encodeElement(TreeItem item) throws DebugException {
		StringBuffer path = new StringBuffer();
		TreeItem parent = item.getParentItem();
		while (parent != null) {
			int i = parent.indexOf(item);
			path.insert(0, i);
			path.insert(0, '/');
			item = parent;
			parent = item.getParentItem();
		}
		Tree tree = item.getParent();
		int i = tree.indexOf(item);
		path.insert(0, i);
		return new Path(path.toString());
	}

	/**
	 * @see org.eclipse.debug.internal.ui.views.AbstractViewerState#decodePath(org.eclipse.core.runtime.IPath,
	 *      org.eclipse.jface.viewers.TreeViewer)
	 */
	protected TreePath decodePath(IPath path, AsynchronousTreeViewer viewer) throws DebugException {
		String[] indicies = path.segments();
		if (indicies.length == 0) {
			return null;
		}
		Tree tree = viewer.getTree();
		int index = -1;
		try {
			index = Integer.parseInt(indicies[0]);
		} catch (NumberFormatException e) {
			return null;
		}
		TreeItem item = null;
		if (index < tree.getItemCount()) {
			item = tree.getItem(index);
		} else {
			return null;
		}
		
		List elements = new ArrayList();
		elements.add(viewer.getInput());
		Object element = item.getData();
		if (element != null) {
			elements.add(element);
		} else {
			return null;
		}
		
		for (int i = 1; i < indicies.length; i++) {
			try {
				index = Integer.parseInt(indicies[i]);
			} catch (NumberFormatException e) {
				return null;
			}	
			if (index < item.getItemCount()) {
				item = item.getItem(index);
			} else {
				return null;
			}		
			element = item.getData();
			if (element != null) {
				elements.add(element);
			} else {
				return null;
			}			
		}
		
		return new TreePath(elements.toArray());
	}


}
