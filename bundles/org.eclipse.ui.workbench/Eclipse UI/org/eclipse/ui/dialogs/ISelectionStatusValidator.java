/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.dialogs;

import org.eclipse.core.runtime.IStatus;

/**
 * Used in selection dialogs to validate selections
 * 
 * @since 2.0
 */
public interface ISelectionStatusValidator {
	
	/**
 	 * Validates an array of elements and returns the resulting status.
 	 * @param selection The elements to validate
 	 * @return The resulting status
	 */	
	IStatus validate(Object[] selection);
	
}
