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
package org.eclipse.ltk.ui.refactoring;

import org.eclipse.ltk.internal.ui.refactoring.RefactoringPreferences;

/**
 * Central access point to access resources managed by the refactoring
 * core plug-in.
 * 
 * <p> 
 * Note: this class is not intented to be subclassed by clients.
 * </p>
 * 
 * @since 3.0
 */
public class RefactoringUI {
	
	private RefactoringUI() {
		// no instance
	}
	
	/**
	 * When condition checking is performed for a refactoring the the
	 * condition check is interpreted as failed if refactoring status
	 * severity return from the condition checking operation is equal
	 * or greater than the value returned by this method. 
	 * 
	 * @return the condition checking failed severity
	 */
	public static int getConditionCheckingFailedSeverity() {
		return RefactoringPreferences.getStopSeverity();
	}
}
