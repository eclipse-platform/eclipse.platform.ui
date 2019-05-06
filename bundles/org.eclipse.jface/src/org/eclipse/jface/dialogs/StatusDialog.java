/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
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
 *     Rüdiger Herrmann - 395426: [JFace] StatusDialog should escape ampersand in status message
 *     Alexander Fedorov <alexander.fedorov@arsysop.ru> - Bug 546991
 *******************************************************************************/
package org.eclipse.jface.dialogs;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.util.Policy;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * An abstract base class for dialogs with a status bar and OK/CANCEL buttons.
 * The status message is specified in an IStatus which can be of severity ERROR,
 * WARNING, INFO or OK. The OK button is enabled or disabled depending on the
 * status.
 *
 * @since 3.1
 */
public abstract class StatusDialog extends TrayDialog {

	private Button fOkButton;

	private MessageLine fStatusLine;

	private IStatus fLastStatus;

	private String fTitle;

	private Image fImage;

	private boolean fStatusLineAboveButtons = true;

	/**
	 * Creates an instance of a status dialog.
	 *
	 * @param parent
	 *            the parent Shell of the dialog
	 */
	public StatusDialog(Shell parent) {
		super(parent);
		fLastStatus = new Status(IStatus.OK, Policy.JFACE, IStatus.OK,
				Util.ZERO_LENGTH_STRING, null);
	}

	/**
	 * Specifies whether status line appears to the left of the buttons
	 * (default) or above them.
	 *
	 * @param aboveButtons
	 *            if <code>true</code> status line is placed above buttons; if
	 *            <code>false</code> to the right
	 */
	public void setStatusLineAboveButtons(boolean aboveButtons) {
		fStatusLineAboveButtons = aboveButtons;
	}

	/**
	 * Update the dialog's status line to reflect the given status. It is safe
	 * to call this method before the dialog has been opened.
	 *
	 * @param status
	 *            the status to set
	 */
	protected void updateStatus(IStatus status) {
		fLastStatus = status;
		if (fStatusLine != null && !fStatusLine.isDisposed()) {
			updateButtonsEnableState(status);
			fStatusLine.setErrorStatus(status);
		}
	}

	/**
	 * Returns the last status.
	 *
	 * @return IStatus
	 */
	public IStatus getStatus() {
		return fLastStatus;
	}

	/**
	 * Updates the status of the ok button to reflect the given status.
	 * Subclasses may override this method to update additional buttons.
	 *
	 * @param status
	 *            the status.
	 */
	protected void updateButtonsEnableState(IStatus status) {
		if (fOkButton != null && !fOkButton.isDisposed()) {
			fOkButton.setEnabled(!status.matches(IStatus.ERROR));
		}
	}

	/*
	 * @see Window#create(Shell)
	 */
	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		if (fTitle != null) {
			shell.setText(fTitle);
		}
	}

	/*
	 * @see Window#create()
	 */
	@Override
	public void create() {
		super.create();
		if (fLastStatus != null) {
			// policy: dialogs are not allowed to come up with an error message
			if (fLastStatus.matches(IStatus.ERROR)) {
				// remove the message
				fLastStatus = new Status(IStatus.ERROR,
						fLastStatus.getPlugin(), fLastStatus.getCode(),
						"", fLastStatus.getException()); //$NON-NLS-1$
			}
			updateStatus(fLastStatus);
		}
	}

	/*
	 * @see Dialog#createButtonsForButtonBar(Composite)
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		fOkButton = createButton(parent, IDialogConstants.OK_ID,
				IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
	}

	/*
	 * @see Dialog#createButtonBar(Composite)
	 */
	@Override
	protected Control createButtonBar(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();

		if (fStatusLineAboveButtons) {
			layout.numColumns = 1;
		} else {
			layout.numColumns = 2;
		}

		layout.marginHeight = 0;
		layout.marginLeft = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.marginWidth = 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		if (!fStatusLineAboveButtons && isHelpAvailable()) {
			createHelpControl(composite);
		}
		fStatusLine = new MessageLine(composite);
		fStatusLine.setAlignment(SWT.LEFT);
		GridData statusData = new GridData(GridData.FILL_HORIZONTAL);
		fStatusLine.setErrorStatus(null);
		if (fStatusLineAboveButtons && isHelpAvailable()) {
			statusData.horizontalSpan = 2;
			createHelpControl(composite);
		}
		fStatusLine.setLayoutData(statusData);
		applyDialogFont(composite);

		/*
		 * Create the rest of the button bar, but tell it not to create a help
		 * button (we've already created it).
		 */
		boolean helpAvailable = isHelpAvailable();
		setHelpAvailable(false);
		super.createButtonBar(composite);
		setHelpAvailable(helpAvailable);
		return composite;
	}

	/**
	 * Sets the title for this dialog.
	 *
	 * @param title
	 *            the title.
	 */
	public void setTitle(String title) {
		fTitle = title != null ? title : ""; //$NON-NLS-1$
		Shell shell = getShell();
		if ((shell != null) && !shell.isDisposed()) {
			shell.setText(fTitle);
		}
	}

	/**
	 * Sets the image for this dialog.
	 *
	 * @param image
	 *            the image.
	 */
	public void setImage(Image image) {
		fImage = image;
		Shell shell = getShell();
		if ((shell != null) && !shell.isDisposed()) {
			shell.setImage(fImage);
		}
	}

}
