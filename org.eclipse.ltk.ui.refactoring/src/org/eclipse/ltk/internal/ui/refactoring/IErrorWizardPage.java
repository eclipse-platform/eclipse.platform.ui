/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring;

import org.eclipse.jface.wizard.IWizardPage;

import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * Interface for a refactoring error wizard page.
 *
 * @since 3.2
 */
public interface IErrorWizardPage extends IWizardPage {

	/** The page's name */
	public static final String PAGE_NAME= "ErrorPage"; //$NON-NLS-1$

	/**
	 * Returns the page's refactoring status.
	 *
	 * @return the refactoring status
	 */
	public RefactoringStatus getStatus();

	/**
	 * Sets the page's refactoring status to the given value.
	 *
	 * @param status
	 *            the refactoring status
	 */
	public void setStatus(RefactoringStatus status);
}