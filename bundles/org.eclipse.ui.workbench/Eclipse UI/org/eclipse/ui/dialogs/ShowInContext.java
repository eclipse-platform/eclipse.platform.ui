/************************************************************************
Copyright (c) 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM - Initial implementation
************************************************************************/

package org.eclipse.ui.dialogs;

import org.eclipse.jface.viewers.ISelection;

/**
 * Carries the context for the Show In... action.
 * The default implementation carries an input and a selection.
 * Subclasses may extend.
 *
 * @see IShowInSource
 * @see IShowInTarget
 */
public class ShowInContext {
	
	private Object input;
	private ISelection selection;
	
    /**
     * Constructs a new <code>ShowInContext</code> with the given input and 
     * selection.
     * 
     * @param input the input or <code>null</code> 
     * @param selection the selection or <code>null</code>
     */
    public ShowInContext(Object input, ISelection selection) {
    	setInput(input);
    	setSelection(selection);
    }

    /**
     * Returns the input, or <code>null</code> to indicate no input
     * 
     * @return the input or <code>null</code>.
     */
    public Object getInput() {
    	return input;
    }

    /**
     * Returns the selection, or <code>null</code> to indicate no selection.
     * 
     * @return the selection or <code>null</code>
     */
    public ISelection getSelection() {
    	return selection;
    }
    
    /**
     * Sets the input, or <code>null</code> to indicate no input.
     * 
     * @param input the input or <code>null</code> 
     */
    public void setInput(Object input) {
    	this.input = input;
    }

    /**
     * Sets the selection, or <code>null</code> to indicate no selection.
     * 
     * @param selection the selection or <code>null</code>
     */
    public void setSelection(ISelection selection) {
    	this.selection = selection;
    }

}
