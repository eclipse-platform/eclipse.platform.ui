package org.eclipse.ui;

/************************************************************************
Copyright (c) 2000, 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

import org.eclipse.jface.viewers.ISelection;

/**
 * Interface for listening to selection changes.
 * <p>
 * This interface may be implemented by clients.
 * </p>
 *
 * @see ISelectionService#addSelectionListener
 * @see org.eclipse.ui.INullSelectionListener
 */
public interface ISelectionListener {
	/**
	 * Notifies this listener that the selection has changed.
	 * <p>
	 * This method is called when the selection changes from one to a 
	 * <code>non-null</code> value, but not when the selection changes to 
	 * <code>null</code>. If there is a requirement to be notified in the latter 
	 * scenario, implement <code>INullSelectionListener</code>. The event will
	 * be posted through this method.
	 * </p>
	 *
	 * @param part the workbench part containing the selection
	 * @param selection the current selection. This may be <code>null</code> 
	 * 		if <code>INullSelectionListener</code> is implemented.
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection selection);
}
