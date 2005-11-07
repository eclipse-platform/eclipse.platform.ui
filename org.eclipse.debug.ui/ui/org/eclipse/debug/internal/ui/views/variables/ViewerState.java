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
import org.eclipse.debug.internal.ui.viewers.TreePath;
import org.eclipse.debug.internal.ui.views.AbstractViewerState;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * Memento of the expanded and selected items in a variables viewer.
 * 
 * @since 2.1
 */
public class ViewerState extends AbstractViewerState {

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
		StringBuffer path = new StringBuffer(item.getText());
		TreeItem parent = item.getParentItem();
		while (parent != null) {
			path.insert(0, parent.getText()+'/');
			parent = parent.getParentItem();
		}
		return new Path(path.toString());
	}

	/**
	 * @see org.eclipse.debug.internal.ui.views.AbstractViewerState#decodePath(org.eclipse.core.runtime.IPath,
	 *      org.eclipse.jface.viewers.TreeViewer)
	 */
	protected TreePath decodePath(IPath path, AsynchronousTreeViewer viewer) throws DebugException {
		String[] names = path.segments();
		Tree tree = viewer.getTree();
		TreeItem[] items = tree.getItems();
		
		List elements = new ArrayList();
		elements.add(viewer.getInput());
		
		boolean pathFound = false;
		
		for (int i = 0; i < names.length; i++) {
			String name = names[i];
			TreeItem item = findItem(name, items);
			if (item != null) {
				pathFound = true;
				elements.add(item.getData());
				items = item.getItems();
			}
		}
		
		if (pathFound) {
			return new TreePath(elements.toArray());
		}
		
		return null;
	}

	private TreeItem findItem(String name, TreeItem[] items) {
		for (int i = 0; i < items.length; i++) {
			TreeItem item = items[i];
			if (item.getText().equals(name)) {
				return item;
			}
		}
		return null;
	}

}
