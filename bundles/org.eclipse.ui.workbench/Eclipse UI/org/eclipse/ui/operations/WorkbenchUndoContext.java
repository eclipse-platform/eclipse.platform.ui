/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.operations;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.ObjectUndoContext;
import org.eclipse.ui.internal.Workbench;

/**
 * @since 3.1
 */
public class WorkbenchUndoContext extends ObjectUndoContext {
    
    private String fLabel = ""; //$NON-NLS-1$
	private List fChildren = new ArrayList();
 
    /**
     * Create an operation context for the workbench
     */
    public WorkbenchUndoContext() {
        super(Workbench.getInstance());
    }
    /**
     * Create an operation context for the workbench using the specified label.
     * @param label - the label for the context
     */
    public WorkbenchUndoContext(String label) {
        super(Workbench.getInstance());
        fLabel = label;
    }
    
	/**
	 * Add the specified context as a match of this context.  Contexts added as
	 * matches of this context will be interpreted as a match of this context 
	 * when the history is filtered for a particular context.  Adding a match
	 * allows components to create their own contexts for implementing specialized 
	 * behavior, yet have their operations appear in the workbench filtered operations list.
	 *  
	 * @param context -
	 *            the context to be added as a match of this context
	 */
	public void addMatch(IUndoContext context) {
		fChildren.add(context);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.operations.IUndoContext#getLabel()
	 */
	public String getLabel() {
		return fLabel; //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.operations.IUndoContext#matches(IUndoContext context)
	 */
	public boolean matches(IUndoContext context) {
		if (fChildren.contains(context)) return true;
		return super.matches(context);
	}

}
