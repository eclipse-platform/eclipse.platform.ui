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
package org.eclipse.team.internal.ccvs.ui.wizards;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.team.internal.ccvs.ui.CommitCommentArea;
import org.eclipse.team.internal.ccvs.ui.IHelpContextIds;
import org.eclipse.ui.help.WorkbenchHelp;

public class KSubstWizardCommitCommentPage extends CVSWizardPage {

	private CommitCommentArea commitCommentArea;

	/**
	 * Constructor for KSubstWizardCommitCommentPage.
	 * @param pageName
	 * @param title
	 * @param titleImage
	 * @param description
	 */
	public KSubstWizardCommitCommentPage(
		Dialog parentDialog,
		String pageName,
		String title,
		ImageDescriptor titleImage,
		String description) {
			
		super(pageName, title, titleImage, description);
		commitCommentArea = new CommitCommentArea();
	}

	/**
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Composite top = new Composite(parent, SWT.NONE);
		top.setLayout(new GridLayout());
		setControl(top);
		// set F1 help
		WorkbenchHelp.setHelp(top, IHelpContextIds.KEYWORD_SUBSTITUTION_COMMIT_COMMENT_PAGE);
		commitCommentArea.createArea(top);
        Dialog.applyDialogFont(parent);
	}

	/**
	 * Method getComment.
	 * @return String
	 */
	public String getComment() {
		return commitCommentArea.getComment();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			commitCommentArea.setFocus();
		}
	}
	
}
