/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.dialogs;

/**
 * For validating selections in some selection dialogs.
 * <p>
 * Clients should implement this interface to define specialized selection
 * validators. 
 * </p>
 */
public interface ISelectionValidator {
    /**
     * Returns a string indicating whether the given selection is valid. If the
     * result is <code>null</code>, the selection is considered to be valid; if the result is
     * non-empty, it contains the error message to be displayed to the user.
     *
     * @param selection the selection to be validated
     * @return the error message, or <code>null</code> indicating
     *	that the value is valid
     */
    public String isValid(Object selection);
}
