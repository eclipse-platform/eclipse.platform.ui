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
 * 
 * @deprecated moved to org.eclipse.ui.part
 */
public class ShowInContext extends org.eclipse.ui.part.ShowInContext {
	
    /**
     * Constructs a new <code>ShowInContext</code> with the given input and 
     * selection.
     * 
     * @param input the input or <code>null</code> 
     * @param selection the selection or <code>null</code>
     */
    public ShowInContext(Object input, ISelection selection) {
    	super(input, selection);
    }
    
}
