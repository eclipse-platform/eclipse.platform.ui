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
package org.eclipse.ltk.ui.refactoring;

/**
 * Interface to access special methods provided by refactoring wizard
 * dialogs.
 * 
 * @since 3.0 
 */
public interface IRefactoringWizardDialog {

	/**
	 * Changes the wizard dialogs default button to be the 
	 * next button.
	 */
	public void makeNextButtonDefault();
}
