/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
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