/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software Systems - Mikhail Khodjaiants - Registers View (Bug 53640)
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.variables;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.views.AbstractViewerState;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
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
	public ViewerState(TreeViewer viewer) {
		super(viewer);
	}

	/**
	 * @see org.eclipse.debug.internal.ui.views.AbstractViewerState#encodeElement(org.eclipse.swt.widgets.TreeItem)
	 */
	protected IPath encodeElement(TreeItem item) throws DebugException {
		IVariable variable = (IVariable)item.getData();
		IPath path = new Path(variable.getName());
		TreeItem parent = item.getParentItem();
		while (parent != null) {
			variable = (IVariable)parent.getData();
			path = new Path(variable.getName()).append(path);
			parent = parent.getParentItem();
		}
		return path;
	}

	/**
	 * @see org.eclipse.debug.internal.ui.views.AbstractViewerState#decodePath(org.eclipse.core.runtime.IPath, org.eclipse.jface.viewers.TreeViewer)
	 */
	protected Object decodePath(IPath path, TreeViewer viewer) throws DebugException {
		ITreeContentProvider contentProvider = (ITreeContentProvider)viewer.getContentProvider();
		String[] names = path.segments();
		Object parent = viewer.getInput();
		IVariable variable = null;
		for (int i = 0; i < names.length; i++) {
			variable = null;
			Object[] children = contentProvider.getChildren(parent);
			String name = names[i];
			for (int j = 0; j < children.length; j++) {
				IVariable var = (IVariable)children[j];
				if (var.getName().equals(name)) {
					variable = var;
					break;
				}
			}
			if (variable == null) {
				return null;
			} 
			parent = variable;
		}
		return variable;
	}
}
