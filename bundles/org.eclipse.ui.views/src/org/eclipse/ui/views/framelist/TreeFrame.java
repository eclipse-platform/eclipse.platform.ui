package org.eclipse.ui.views.framelist;

/**********************************************************************
Copyright (c) 2000, 2001, 2002, International Business Machines Corp and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/

import org.eclipse.jface.viewers.*;

/**
 * Frame for tree viewers.  This capture the viewer's input element, selection,
 * and expanded elements.
 */
public class TreeFrame extends Frame {
	
	private AbstractTreeViewer viewer;
	private Object input;
	private ISelection selection;
	private Object[] expandedElements;
	
	/**
	 * Constructs a frame for the specified tree viewer.
	 * The frame's input, name and tool tip text are not set.
	 * 
	 * @param viewer the tree viewer
	 */
	public TreeFrame(AbstractTreeViewer viewer) {
		this.viewer = viewer;
	}

	/**
	 * Constructs a frame for the specified tree viewer.
	 * The frame's input element is set to the specified input element.
	 * The frame's name and tool tip text are set to the text for the input 
	 * element, as provided by the viewer's label provider.
	 * 
	 * @param viewer the tree viewer
	 * @param input the input element
	 */
	public TreeFrame(AbstractTreeViewer viewer, Object input) {
		this(viewer);
		setInput(input);
		ILabelProvider provider = (ILabelProvider) viewer.getLabelProvider();
		String name = provider.getText(input);
		setName(name);
		setToolTipText(name);
	}
	
	/**
	 * Returns the expanded elements.
	 * 
	 * @return the expanded elements
	 */
	public Object[] getExpandedElements() {
		return expandedElements;
	}
	
	/**
	 * Returns the input element.
	 * 
	 * @return the input element
	 */
	public Object getInput() {
		return input;
	}
	
	/**
	 * Returns the selection.
	 * 
	 * @return the selection
	 */
	public ISelection getSelection() {
		return selection;
	}
	
	/**
	 * Returns the tree viewer.
	 * 
	 * @return the tree viewer
	 */
	public AbstractTreeViewer getViewer() {
		return viewer;
	}
	
	/**
	 * Sets the input element.
	 * 
	 * @param input the input element
	 */
	public void setInput(Object input) {
		this.input = input;
	}
	
	/**
	 * Sets the expanded elements.
	 * 
	 * @param expandedElements the expanded elements
	 */
	public void setExpandedElements(Object[] expandedElements) {
		this.expandedElements = expandedElements;
	}
	
	/**
	 * Sets the selection.
	 * 
	 * @param selection the selection
	 */
	public void setSelection(ISelection selection) {
		this.selection = selection;
	}
}