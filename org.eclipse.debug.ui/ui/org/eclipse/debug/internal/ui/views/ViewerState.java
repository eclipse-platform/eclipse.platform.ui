package org.eclipse.debug.internal.ui.views;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import javax.swing.text.View;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeViewer;

/**
 * Memento of the expanded and selected items in a tree
 * viewer for.
 * 
 * @since 2.1
 */
public class ViewerState {

	private Object[] fExpandedElements = null;	
	private ISelection fSelection = null;
	
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
		fExpandedElements = viewer.getExpandedElements();
		fSelection = viewer.getSelection();
	}
	
	/**
	 * Restores the state of the given viewer to this mementos
	 * saved state.
	 * 
	 * @param viewer viewer to which state is restored
	 */
	public void restoreState(TreeViewer viewer) {
		if (fExpandedElements != null) {
			viewer.setExpandedElements(fExpandedElements);
		}
		if (fSelection != null) {
			viewer.setSelection(fSelection);
		}
	}
}
