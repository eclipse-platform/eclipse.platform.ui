/*******************************************************************************
 * Copyright (c) 2010, 2015 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.ui.internal.views.markers;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.preferences.ViewSettingsDialog;

/**
 * @since 3.7
 * @author Hitesh Soliwal
 * @noextend This class is not intended to be subclassed by clients.
 */
public abstract class ViewerSettingsAndStatusDialog extends ViewSettingsDialog {

	private Label imageLabel;
	/** The message area */
	private Text messageArea;
	private Composite msgParent;

	public ViewerSettingsAndStatusDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
	}

	@Override
	protected int getShellStyle() {
		return super.getShellStyle() | SWT.RESIZE;
	}

	@Override
	protected Point getInitialSize() {
		Point size = super.getInitialSize();
		size.y += convertHeightInCharsToPixels(3);
		return size;
	}

	@Override
	protected Control createDialogArea(Composite parent) {

		Composite dialogAreaComposite = (Composite) super.createDialogArea(parent);

		dialogAreaComposite.setLayout(new GridLayout(1, true));

		initializeDialogUnits(dialogAreaComposite);

		createMessageArea(dialogAreaComposite).setLayoutData(
				new GridData(SWT.FILL, SWT.NONE, true, false));

		createDialogContentArea(dialogAreaComposite).setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, true, true));

		applyDialogFont(dialogAreaComposite);

		initializeDialog();

		return dialogAreaComposite;
	}

	protected abstract Control createDialogContentArea(Composite dialogAreaComposite);

	protected void initializeDialog() {
		handleStatusUdpate(IStatus.INFO, getDefaultMessage());
	}

	/**
	 * Create message area.
	 */
	Control createMessageArea(Composite parent) {
		msgParent = new Composite(parent, SWT.BORDER);
		msgParent.setBackground(getMessageBackground());
		msgParent.setLayout(new GridLayout(2, false));

		imageLabel = new Label(msgParent, SWT.NONE);
		imageLabel.setBackground(msgParent.getBackground());
		imageLabel.setImage(JFaceResources
				.getImage(Dialog.DLG_IMG_MESSAGE_INFO));
		imageLabel
				.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		messageArea = new Text(msgParent, SWT.READ_ONLY | SWT.NONE | SWT.WRAP
				| SWT.MULTI | SWT.V_SCROLL);
		messageArea.setEditable(false);
		messageArea.setBackground(msgParent.getBackground());
		messageArea
				.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		return msgParent;
	}

	/**
	 * Display the message and an appropriate icon.
	 */
	protected void handleStatusUdpate(IStatus status) {
		handleStatusUdpate(status.getSeverity(), status.getMessage());
	}

	/**
	 * Display the message and an appropriate icon.
	 */
	protected void handleStatusUdpate(int severity, String message) {
		Image image = null;
		Button okBttn = getButton(OK);
		switch (severity) {
		case IStatus.ERROR: {
			if (message == null) {
				message = getErrorMessage();
			}
			image = getErrorImage();
			break;
		}
		case IStatus.WARNING: {
			image = getWarningImage();
			break;
		}
		case IStatus.OK:
		case IStatus.INFO:
		default:
			image = getInfoImage();
		}
		if (message == null) {
			message = getDefaultMessage();
		}
		if (message.equals(MarkerSupportInternalUtilities.EMPTY_STRING)) {
			handleMessageAreaVisibility(false);
			image = null;
		} else {
			handleMessageAreaVisibility(true);
		}
		if (okBttn != null) {
			okBttn.setEnabled(severity == IStatus.OK
					|| severity == IStatus.INFO || severity == IStatus.WARNING);
		}
		if (msgParent != null) {
			setMessageImage(image);
			setMessageText(message);
			msgParent.layout();
		}
	}

	protected void setMessageImage(Image image) {
		if (imageLabel != null) {
			imageLabel.setImage(image);
		}
	}

	protected void setMessageText(String messgage) {
		if (messageArea != null) {
			messageArea.setText(messgage);
		}
	}

	protected Image getMessageImage() {
		if (imageLabel != null) {
			imageLabel.getImage();
		}
		return null;
	}

	protected String getMessageText() {
		if (messageArea != null) {
			return messageArea.getText();
		}
		return null;
	}

	protected void handleMessageAreaVisibility(boolean visible) {
		if (msgParent == null || msgParent.isDisposed()) {
			return;
		}
		GridData data = (GridData) msgParent.getLayoutData();
		if (data.exclude == visible) {
			data.exclude = !visible;
			msgParent.setVisible(visible);
			msgParent.getParent().layout();
		}
	}

	/**
	 * Return the Color to display when dialog is opened.
	 */
	protected Color getMessageBackground() {
		return getShell().getDisplay()
				.getSystemColor(SWT.COLOR_LIST_BACKGROUND);
	}

	/**
	 * Return the message to display when dialog is opened.
	 */
	protected String getDefaultMessage() {
		return MarkerSupportInternalUtilities.EMPTY_STRING;
	}

	/**
	 * @return Returns the error message to display for a wrong limit value.
	 */
	protected String getErrorMessage() {
		return JFaceResources.getString("StringFieldEditor.errorMessage"); //$NON-NLS-1$
	}

	protected Image getInfoImage() {
		return JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_INFO);
	}

	protected Image getWarningImage() {
		return JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_WARNING);
	}

	protected Image getErrorImage() {
		return JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_ERROR);
	}

	@Override
	protected void performDefaults() {
		super.performDefaults();
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected void okPressed() {
		super.okPressed();
	}

}