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

package org.eclipse.ltk.internal.core.refactoring;

import org.eclipse.ltk.core.refactoring.RefactoringStatus;

public class RefactoringCorePreferences {

	// private static final String CONDITION_CHECKING_FAILED_SEVERITY= "condidtionCheckingFailed"; //$NON-NLS-1$
	
	public static int getCheckPassedSeverity() {
		/*
		String value= RefactoringUIPlugin.getDefault().getPreferenceStore().getString(CONDITION_CHECKING_FAILED_SEVERITY);
		try {
			return Integer.valueOf(value).intValue() - 1;
		} catch (NumberFormatException e) {
			return RefactoringStatus.WARNING;
		}
		*/
		return RefactoringStatus.INFO;
	}
	
	public static int getStopSeverity() {
		switch (getCheckPassedSeverity()) {
			case RefactoringStatus.OK:
				return RefactoringStatus.INFO;
			case RefactoringStatus.INFO:
				return RefactoringStatus.WARNING;
			case RefactoringStatus.WARNING:
				return RefactoringStatus.ERROR;
		}
		return RefactoringStatus.FATAL;
	}
}
