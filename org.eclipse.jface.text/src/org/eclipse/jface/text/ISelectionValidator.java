/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.text;

import org.eclipse.jface.viewers.ISelection;

/**
 * A selection validator allows clients to test
 * whether the selection they received during selection
 * changed notification is still valid.
 * <p>
 * For example selection and document changes cause the
 * selection to be invalid.
 * </p>
 * 
 * @since 3.0
 */
public interface ISelectionValidator {
	
	/**
	 * Tests whether the given post selection is still valid.
	 *
	 * @param selection
	 * @return <code>true</code> if the selection is still valid
	 */
	boolean isValid(ISelection selection);
}
