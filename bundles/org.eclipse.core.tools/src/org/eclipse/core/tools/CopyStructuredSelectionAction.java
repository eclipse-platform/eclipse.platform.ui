/**********************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.tools;

import java.util.Iterator;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * A concrete implementation for <code>AbstractCopySelectionAction</code> that 
 * supports structured selections.
 * 
 * @see org.eclipse.jface.viewers.IStructuredSelection
 */
public class CopyStructuredSelectionAction extends AbstractCopySelectionAction {

	/** 
	 * @see org.eclipse.core.tools.AbstractCopySelectionAction#AbstractCopySelectionAction(ISelectionProvider)
	 */
	public CopyStructuredSelectionAction(ISelectionProvider selectionProvider) {
		super(selectionProvider);
	}

	/**
	 * Returns the current structured selection as a string object where each 
	 * node is followed by a line terminator char.  This method depends on the
	 * toString() method of each node to define a reasonably formatted string
	 * for display.
	 * 
	 * @return a string containing the currently selected elements separated by
	 * line terminators 
	 * @see org.eclipse.core.tools.AbstractCopySelectionAction#getContents()
	 */
	public String getContents() {
		// retrieves the selected contents from the selection provider
		IStructuredSelection selection = (IStructuredSelection) selectionProvider.getSelection();
		StringBuffer content = new StringBuffer();
		for (Iterator selectionIter = selection.iterator(); selectionIter.hasNext();) {
			content.append(selectionIter.next());
			content.append('\n');
		}
		return content.toString();
	}
}