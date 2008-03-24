/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.commands.operations;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

/**
 * <p>
 * Abstract implementation for an undoable operation. At a minimum, subclasses
 * should implement behavior for
 * {@link IUndoableOperation#execute(org.eclipse.core.runtime.IProgressMonitor, org.eclipse.core.runtime.IAdaptable)},
 * {@link IUndoableOperation#redo(org.eclipse.core.runtime.IProgressMonitor, org.eclipse.core.runtime.IAdaptable)},
 * and
 * {@link IUndoableOperation#undo(org.eclipse.core.runtime.IProgressMonitor, org.eclipse.core.runtime.IAdaptable)}.
 * </p>
 * 
 * @see org.eclipse.core.commands.operations.IUndoableOperation
 * 
 * @since 3.1
 */
public abstract class AbstractOperation implements IUndoableOperation {
	List contexts = new ArrayList();

	private String label = ""; //$NON-NLS-1$

	/**
	 * Construct an operation that has the specified label.
	 * 
	 * @param label
	 *            the label to be used for the operation. Should never be
	 *            <code>null</code>.
	 */
	public AbstractOperation(String label) {
		Assert.isNotNull(label);
		this.label = label;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IUndoableOperation#addContext(org.eclipse.core.commands.operations.IUndoContext)
	 * 
	 * <p> Subclasses may override this method. </p>
	 */
	public void addContext(IUndoContext context) {
		if (!contexts.contains(context)) {
			contexts.add(context);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IUndoableOperation#canExecute()
	 *      <p> Default implementation. Subclasses may override this method.
	 *      </p>
	 * 
	 */
	public boolean canExecute() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IUndoableOperation#canRedo()
	 *      <p> Default implementation. Subclasses may override this method.
	 *      </p>
	 */
	public boolean canRedo() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IUndoableOperation#canUndo()
	 *      <p> Default implementation. Subclasses may override this method.
	 *      </p>
	 */
	public boolean canUndo() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IUndoableOperation#dispose()
	 *      <p> Default implementation. Subclasses may override this method.
	 *      </p>
	 */
	public void dispose() {
		// nothing to dispose.
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IUndoableOperation#execute(org.eclipse.core.runtime.IProgressMonitor,
	 *      org.eclipse.core.runtime.IAdaptable)
	 */
	public abstract IStatus execute(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException;

	public final IUndoContext[] getContexts() {
		return (IUndoContext[]) contexts.toArray(new IUndoContext[contexts
				.size()]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IUndoableOperation#getLabel()
	 *      <p> Default implementation. Subclasses may override this method.
	 *      </p>
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Set the label of the operation to the specified name.
	 * 
	 * @param name
	 *            the string to be used for the label. Should never be
	 *            <code>null</code>.
	 */
	public void setLabel(String name) {
		label = name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IUndoableOperation#hasContext(org.eclipse.core.commands.operations.IUndoContext)
	 */
	public final boolean hasContext(IUndoContext context) {
		Assert.isNotNull(context);
		for (int i = 0; i < contexts.size(); i++) {
			IUndoContext otherContext = (IUndoContext) contexts.get(i);
			// have to check both ways because one context may be more general
			// in
			// its matching rules than another.
			if (context.matches(otherContext) || otherContext.matches(context)) {
				return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IUndoableOperation#redo(org.eclipse.core.runtime.IProgressMonitor,
	 *      org.eclipse.core.runtime.IAdaptable)
	 */
	public abstract IStatus redo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IUndoableOperation#removeContext(org.eclipse.core.commands.operations.IUndoContext)
	 *      <p> Default implementation. Subclasses may override this method.
	 *      </p>
	 */

	public void removeContext(IUndoContext context) {
		contexts.remove(context);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IUndoableOperation#undo(org.eclipse.core.runtime.IProgressMonitor,
	 *      org.eclipse.core.runtime.IAdaptable)
	 */
	public abstract IStatus undo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException;

	/**
	 * The string representation of this operation. Used for debugging purposes
	 * only. This string should not be shown to an end user.
	 * 
	 * @return The string representation.
	 */
	public String toString() {
		final StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append(getLabel());
		stringBuffer.append("("); //$NON-NLS-1$
		IUndoContext[] contexts = getContexts();
		for (int i = 0; i < contexts.length; i++) {
			stringBuffer.append(contexts[i].toString());
			if (i != contexts.length - 1) {
				stringBuffer.append(',');
			}
		}
		stringBuffer.append(')');
		return stringBuffer.toString();
	}
}
