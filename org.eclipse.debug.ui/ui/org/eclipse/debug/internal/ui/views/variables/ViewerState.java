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
package org.eclipse.debug.internal.ui.views.variables;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.TreeItem;

/**
 * Memento of the expanded and selected items in a variables viewer.
 * 
 * @since 2.1
 */
public class ViewerState {

	// paths to expanded variables
	private List fExpandedElements = null;
	// paths to selected variables	
	private IPath[] fSelection = null;
	
	/**
	 * Constructs a memento for the given viewer.
	 */
	public ViewerState(TreeViewer viewer) {
		saveState(viewer);
	}

	/**
	 * Saves the current state of the given viewer into
	 * this memento.
	 * 
	 * @param viewer viewer of which to save the state
	 */
	public void saveState(TreeViewer viewer) {
		List expanded = new ArrayList();
		fExpandedElements = null;
		TreeItem[] items = viewer.getTree().getItems();
		try {
			for (int i = 0; i < items.length; i++) {
				collectExandedItesm(items[i], expanded);
			}
			if (expanded.size() > 0) {
				fExpandedElements = expanded;
			}
		} catch (DebugException e) {
			fExpandedElements = null;
		}
		
		TreeItem[] selection = viewer.getTree().getSelection();
		fSelection = new IPath[selection.length];
		try {
			for (int i = 0; i < selection.length; i++) {
				fSelection[i] = encodeVariable(selection[i]);
			}
		} catch (DebugException e) {
			fSelection = null;
		}
	}
	
	protected void collectExandedItesm(TreeItem item, List expanded) throws DebugException {
		if (item.getExpanded()) {
			expanded.add(encodeVariable(item));
			TreeItem[] items = item.getItems();
			for (int i = 0; i < items.length; i++) {
				collectExandedItesm(items[i], expanded);
			}
		}
	}
	
	/**
	 * Restores the state of the given viewer to this mementos
	 * saved state.
	 * 
	 * @param viewer viewer to which state is restored
	 */
	public void restoreState(TreeViewer viewer) {
		if (fExpandedElements != null) {
			List expansion = new ArrayList(fExpandedElements.size());
			for (int i = 0; i < fExpandedElements.size(); i++) {
				IPath path = (IPath) fExpandedElements.get(i);
				if (path != null) {
					IVariable var;
					try {
						var = decodePath(path, viewer);
						if (var != null) {
							expansion.add(var);
						}
					} catch (DebugException e) {
					}
				}
			}
			viewer.setExpandedElements(expansion.toArray());
		}
		if (fSelection != null) {
			List selection = new ArrayList(fSelection.length);
			for (int i = 0; i < fSelection.length; i++) {
				IPath path = fSelection[i];
				IVariable var;
				try {
					var = decodePath(path, viewer);
					if (var != null) {
						selection.add(var);
					}
				} catch (DebugException e) {
				}
			}			
			
			viewer.setSelection(new StructuredSelection(selection));
		}
	}
	
	/**
	 * Constructs a path representing the given variable. The segments in the
	 * path denote parent variable names, and the last segment is the name of
	 * the given variable.
	 *   
	 * @param item tree item containing the variable to encode
	 * @return path encoding the given variable
	 * @throws DebugException if unable to generate a path
	 */
	protected IPath encodeVariable(TreeItem item) throws DebugException {
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
	 * Returns a variable in the given viewer that corresponds to the given
	 * path, or <code>null</code> if none.
	 * 
	 * @param path encoded variable path
	 * @param viewer viewer to search for the variable in
	 * @return variable represented by the path, or <code>null</code> if none
	 * @throws DebugException if unable to locate a variable
	 */
	protected IVariable decodePath(IPath path, TreeViewer viewer) throws DebugException {
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
			} else {
				parent = variable;
			}
		}
		return variable;
	}
}
