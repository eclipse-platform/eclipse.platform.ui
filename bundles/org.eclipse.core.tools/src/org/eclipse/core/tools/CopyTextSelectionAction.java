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

import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelectionProvider;

/**
 * A concrete implementation for <code>AbstractCopySelectionAction</code> that 
 * supports text selections.
 * 
 * @see org.eclipse.jface.text.ITextSelection
 */
public class CopyTextSelectionAction extends AbstractCopySelectionAction {

	/**
	 * @see AbstractCopySelectionAction#AbstractCopySelectionAction
	 * (ISelectionProvider)
	 */
	public CopyTextSelectionAction(ISelectionProvider selectionProvider) {
		super(selectionProvider);
	}

	/**
	 * Returns the current text selection.
	 * 
	 * @return a string containing the currently selected text
	 * @see org.eclipse.core.tools.AbstractCopySelectionAction#getContents()
	 */
	protected String getContents() {
		return ((ITextSelection) selectionProvider.getSelection()).getText();
	}

}