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
package org.eclipse.core.commands.operations;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.internal.commands.util.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

/**
 * <p>
 * Abstract implementation for an operation
 * </p>
 * <p>
 * Note: This class/interface is part of a new API under development. It has
 * been added to builds so that clients can start using the new features.
 * However, it may change significantly before reaching stability. It is being
 * made available at this early stage to solicit feedback with the understanding
 * that any code that uses this API may be broken as the API evolves.
 * </p>
 * 
 * @since 3.1
 * @experimental
 */
public abstract class AbstractOperation implements IUndoableOperation {
	private List fContexts = new ArrayList();

	private String fLabel = ""; //$NON-NLS-1$

	/**
	 * Construct an operation that has the specified label.
	 * 
	 * @param label -
	 *            the label to be used for the operation.
	 */
	public AbstractOperation(String label) {
		fLabel = label;
	}

	public void addContext(IUndoContext context) {
		if (!fContexts.contains(context)) {
			fContexts.add(context);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.runtime.operations.IOperation#canExecute()
	 */
	public boolean canExecute() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.runtime.operations.IOperation#canRedo()
	 */
	public boolean canRedo() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.runtime.operations.IOperation#canUndo()
	 */
	public boolean canUndo() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.runtime.operations.IOperation#dispose()
	 */
	public void dispose() {
		// nothing to dispose.
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.runtime.operations.IOperation#execute(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public abstract IStatus execute(IProgressMonitor monitor, IAdaptable info);

	public IUndoContext[] getContexts() {
		return (IUndoContext[]) fContexts.toArray(new IUndoContext[fContexts
				.size()]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.runtime.operations.IOperation#getDescription()
	 */
	public String getDescription() {
		return ""; //$NON-NLS-1$
	}

	public String getLabel() {
		return fLabel;
	}

	/**
	 * Set the label of the operation to the specified name.
	 * 
	 * @param name -
	 *            the string to be used for the label.
	 */
	public void setLabel(String name) {
		fLabel = name;
	}

	public boolean hasContext(IUndoContext context) {
		Assert.isNotNull(context);
		for (int i = 0; i< fContexts.size(); i++) {
			IUndoContext otherContext = (IUndoContext)fContexts.get(i);
			// have to check both ways because one context may be more general in
			// its matching rules than another.
			if (context.matches(otherContext) || otherContext.matches(context)) 
				return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.runtime.operations.IOperation#redo(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public abstract IStatus redo(IProgressMonitor monitor, IAdaptable info);

	public void removeContext(IUndoContext context) {
		fContexts.remove(context);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.runtime.operations.IOperation#undo(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public abstract IStatus undo(IProgressMonitor monitor, IAdaptable info);

}
