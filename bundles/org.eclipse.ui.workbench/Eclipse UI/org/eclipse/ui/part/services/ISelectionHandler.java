/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.part.services;

import org.eclipse.jface.viewers.ISelection;

/**
 * Interface implemented by objects that can process changes in selection.
 * Parts can use an ISelectionHandler as a service fto change
 * their selection. 
 * 
 * @since 3.1
 */
public interface ISelectionHandler {
    /**
     * Sets the current selection. A null value indicates that
     * no selection is available. This is not the same as an empty
     * selection
     * 
     * @param newSelection new selection or null if no selection is available
     */
	public void setSelection(ISelection newSelection);
}
