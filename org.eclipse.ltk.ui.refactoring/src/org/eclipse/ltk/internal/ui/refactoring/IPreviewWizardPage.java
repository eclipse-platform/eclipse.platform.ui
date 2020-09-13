/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring;

import org.eclipse.jface.wizard.IWizardPage;

import org.eclipse.ltk.core.refactoring.Change;

/**
 * Interface for a refactoring preview wizard page.
 */
public interface IPreviewWizardPage extends IWizardPage {

	/** The page's name */
	String PAGE_NAME= "PreviewPage"; //$NON-NLS-1$

	/**
	 * Returns the change that is displayed.
	 *
	 * @return the change, or <code>null</code>
	 * @since 3.2
	 */
	Change getChange();

	/**
	 * Sets that change for which the page is supposed to display a preview.
	 *
	 * @param change
	 *            the new change, or <code>null</code>
	 */
	void setChange(Change change);
}
