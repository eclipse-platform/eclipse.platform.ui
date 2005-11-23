/*******************************************************************************
 * Copyright (c) 2005 Tobias Widmer and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Tobias Widmer - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring.history;

import org.eclipse.core.resources.IProject;

import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIMessages;

import org.eclipse.ltk.ui.refactoring.history.RefactoringHistoryControlConfiguration;

/**
 * Configuration object for a refactoring history dialog.
 * 
 * @since 3.2
 */
public class RefactoringHistoryDialogConfiguration extends RefactoringHistoryControlConfiguration {

	/**
	 * Creates a new refactoring history dialog configuration.
	 * 
	 * @param project
	 *            the project, or <code>null</code>
	 * @param time
	 *            <code>true</code> to display time information,
	 *            <code>false</code> otherwise
	 */
	public RefactoringHistoryDialogConfiguration(final IProject project, final boolean time) {
		super(project, time);
	}

	/**
	 * Returns the label of the commit button of the dialog.
	 * 
	 * @return the button label
	 */
	public String getButtonLabel() {
		return RefactoringUIMessages.RefactoringHistoryDialogConfiguration_commit_button_label;
	}

	/**
	 * Returns the default height of the dialog.
	 * 
	 * @return the default height
	 */
	public int getDefaultHeight() {
		return 200;
	}

	/**
	 * Returns the default width of the dialog.
	 * 
	 * @return the default width
	 */
	public int getDefaultWidth() {
		return 240;
	}

	/**
	 * Returns the dialog title of the dialog.
	 * 
	 * @return the dialog title
	 */
	public String getDialogTitle() {
		return RefactoringUIMessages.RefactoringHistoryDialogConfiguration_dialog_title;
	}
}