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
package org.eclipse.ltk.internal.ui.refactoring;

import org.eclipse.ltk.core.refactoring.Change;

import org.eclipse.jface.wizard.IWizardPage;

/**
 * Interface for a refactoring preview wizard page.
 */
public interface IPreviewWizardPage extends IWizardPage {

	/** The page's name */
	public static final String PAGE_NAME= "PreviewPage"; //$NON-NLS-1$

	/**
	 * Returns the change that is displayed.
	 * 
	 * @return the change, or <code>null</code>
	 * @since 3.2
	 */
	public Change getChange();

	/**
	 * Sets that change for which the page is supposed to display a preview.
	 * 
	 * @param change
	 *            the new change, or <code>null</code>
	 */
	public void setChange(Change change);
}
