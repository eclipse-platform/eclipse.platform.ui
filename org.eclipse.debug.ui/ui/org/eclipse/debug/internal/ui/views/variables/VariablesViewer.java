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
		
		if (getSelection().isEmpty() && getNewItem() != null) {
			if (!getNewItem().isDisposed()) {
				//ensure that new items are visible
				showItem(getNewItem());
			}
			setNewItem(null);
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

	/**
	 * Returns a memento for the current state of expanded and
	 * selected elements in this viewer.
	 * 
	 * @return a memento for the current state of expanded and
	 *  selected elements in this viewer
	 */
	public ViewerState saveState() {
		Object[] expansion = getExpandedElements();
		IPath[] expandedElements = new IPath[expansion.length];
		for (int i = 0; i < expansion.length; i++) {
			IVariable variable = (IVariable)expansion[i];
			try {
				expandedElements[i] = encodeVariable(variable);
			} catch (DebugException e1) {
			}
		}
		
		IStructuredSelection selection = (IStructuredSelection)getSelection();
		IPath[] sel = new IPath[selection.size()];
		Iterator elements = selection.iterator();
		int i = 0;
		try {
			while (elements.hasNext()) {
				sel[i] = encodeVariable((IVariable)elements.next());
				i++;
			}
		} catch (DebugException e) {
			selection = null;
		}
		return new ViewerState(expandedElements, sel);
	}
	
	/**
	 * Restores the state of expanded and selected elements as described
	 * by the given memento.
	 * 
	 * @param state viewer state
	 */
	public void restoreState(ViewerState state) {
		IPath[] expandedElements = state.getExpandedElements();
		if (expandedElements != null) {
			List expansion = new ArrayList(expandedElements.length);
			for (int i = 0; i < expandedElements.length; i++) {
				IPath path = expandedElements[i];
				if (path != null) {
					IVariable var;
					try {
						var = decodePath(path);
						if (var != null) {
							expansion.add(var);
						}
					} catch (DebugException e) {
					}
				}
			}
			setExpandedElements(expansion.toArray());
		}
		IPath[] selection = state.getSelection();
		if (selection != null) {
			List sel = new ArrayList(selection.length);
			for (int i = 0; i < selection.length; i++) {
				IPath path = selection[i];
				IVariable var;
				try {
					var = decodePath(path);
					if (var != null) {
						sel.add(var);
					}
				} catch (DebugException e) {
				}
			}			
			
			setSelection(new StructuredSelection(sel));
		}
	}
	
	/**
	 * Constructs a path representing the given variable. The segments in the
	 * path denote parent variable names, and the last segment is the name of
	 * the given variable.
	 *   
	 * @param variable variable to encode
	 * @return path encoding the given variable
	 * @throws DebugException if unable to generate a path
	 */
	protected IPath encodeVariable(IVariable variable) throws DebugException {
		IPath path = new Path(variable.getName());
		TreeItem treeItem= (TreeItem) findItem(variable);
		TreeItem parent = treeItem.getParentItem();
		while (parent != null) {
			IVariable var = (IVariable)parent.getData(); 
			path = new Path(var.getName()).append(path);
			parent = parent.getParentItem();
		}
		return path;
	}
	
	/**
	 * Returns a variable in the given viewer that corresponds to the given
	 * path, or <code>null</code> if none.
	 * 
	 * @param path encoded variable path
	 * @return variable represented by the path, or <code>null</code> if none
	 * @throws DebugException if unable to locate a variable
	 */
	protected IVariable decodePath(IPath path) throws DebugException {
		ITreeContentProvider contentProvider = (ITreeContentProvider)getContentProvider();
		String[] names = path.segments();
		Object parent = getInput();
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
