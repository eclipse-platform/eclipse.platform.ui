/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring.history;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;

import org.eclipse.ltk.internal.ui.refactoring.PreviewWizardPage;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIMessages;

/**
 * Preview page for refactoring history wizards.
 * 
 * @since 3.2
 */
public final class RefactoringHistoryPreviewPage extends PreviewWizardPage {

	/**
	 * Creates a new refactoring history preview page.
	 */
	public RefactoringHistoryPreviewPage() {
		super(true);
		setTitle(RefactoringUIMessages.RefactoringHistoryOverviewPage_title);
		setDescription(RefactoringUIMessages.RefactoringHistoryPreviewPage_description);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean canFlipToNextPage() {
		return true;
	}

	/**
	 * Sets the title of the page according to the refactoring.
	 * 
	 * @param descriptor
	 *            the refactoring descriptor, or <code>null</code>
	 */
	public void setTitle(final RefactoringDescriptorProxy descriptor) {
		if (descriptor != null)
			setTitle(descriptor.getDescription());
		else
			setTitle(RefactoringUIMessages.RefactoringHistoryOverviewPage_title);
	}
}