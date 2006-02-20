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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;

/**
 * Dialog to edit a comment of a refactoring.
 * 
 * @since 3.2
 */
public final class EditCommentDialog extends Dialog {

	/** The comment text */
	private String fComment= "";//$NON-NLS-1$

	/** The comment text field */
	private Text fCommentField;

	/** The message to display */
	private final String fMessage;

	/** The dialog title */
	private final String fTitle;

	/**
	 * Creates a new edit comment dialog.
	 * 
	 * @param shell
	 *            the parent shell, or <code>null</code>
	 * @param title
	 *            the dialog title, or <code>null</code> if none
	 * @param message
	 *            the dialog message, or <code>null</code> if none
	 * @param comment
	 *            the initial comment, or <code>null</code> if none
	 */
	public EditCommentDialog(final Shell shell, final String title, final String message, final String comment) {
		super(shell);
		fTitle= title;
		fMessage= message;
		if (comment == null)
			fComment= "";//$NON-NLS-1$
		else
			fComment= comment;
	}

	/**
	 * {@inheritDoc}
	 */
	protected void buttonPressed(final int id) {
		if (id == IDialogConstants.OK_ID)
			fComment= fCommentField.getText();
		else
			fComment= null;
		super.buttonPressed(id);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void configureShell(final Shell shell) {
		super.configureShell(shell);
		if (fTitle != null)
			shell.setText(fTitle);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void createButtonsForButtonBar(final Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
		fCommentField.setFocus();
		if (fComment != null) {
			fCommentField.setText(fComment);
			fCommentField.selectAll();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	protected Control createDialogArea(final Composite parent) {
		initializeDialogUnits(parent);
		final Composite composite= (Composite) super.createDialogArea(parent);
		if (fMessage != null) {
			final Label label= new Label(composite, SWT.WRAP);
			label.setText(fMessage);
			final GridData data= new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER);
			data.widthHint= convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
			label.setLayoutData(data);
			label.setFont(parent.getFont());
		}
		fCommentField= new Text(composite, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
		final GridData data= new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
		data.heightHint= convertHeightInCharsToPixels(3);
		fCommentField.setLayoutData(data);
		applyDialogFont(composite);
		return composite;
	}

	/**
	 * Returns the current comment.
	 * 
	 * @return the current comment
	 */
	public String getComment() {
		return fComment;
	}
}