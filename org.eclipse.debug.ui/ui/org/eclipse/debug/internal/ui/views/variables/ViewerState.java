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
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;

/**
 * Memento of the expanded and selected items in a variables viewer.
 * 
 * @since 2.1
 */
public class ViewerState {

	// paths to expanded variables
	private IPath[] fExpandedElements = null;
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
		Object[] expansion = viewer.getExpandedElements();
		fExpandedElements = new IPath[expansion.length];
		for (int i = 0; i < expansion.length; i++) {
			IVariable variable = (IVariable)expansion[i];
			try {
				fExpandedElements[i] = encodeVariable(variable, viewer);
			} catch (DebugException e1) {
			}
		}
		
		IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
		fSelection = new IPath[selection.size()];
		Iterator elements = selection.iterator();
		int i = 0;
		try {
			while (elements.hasNext()) {
				fSelection[i] = encodeVariable((IVariable)elements.next(), viewer);
				i++;
			}
		} catch (DebugException e) {
			fSelection = null;
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
			List expansion = new ArrayList(fExpandedElements.length);
			for (int i = 0; i < fExpandedElements.length; i++) {
				IPath path = fExpandedElements[i];
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
	 * @param variable variable to encode
	 * @param viewer viewer the variable is contained in
	 * @return path encoding the given variable
	 * @throws DebugException if unable to generate a path
	 */
	protected IPath encodeVariable(IVariable variable, TreeViewer viewer) throws DebugException {
		ITreeContentProvider contentProvider = (ITreeContentProvider)viewer.getContentProvider();
		IPath path = new Path(variable.getName());
		Object parent = contentProvider.getParent(variable);
		while (parent instanceof IVariable) {
			IVariable parentVar = (IVariable)parent;
			path = new Path(parentVar.getName()).append(path);
			parent = contentProvider.getParent(parentVar);
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
