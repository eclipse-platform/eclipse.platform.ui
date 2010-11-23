/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

	/**
	 * @param parentShell
	 */
	public ViewerSettingsAndStatusDialog(Shell parentShell) {
		super(parentShell);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets
	 * .Shell)
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Window#getShellStyle()
	 */
	protected int getShellStyle() {
		return super.getShellStyle() | SWT.RESIZE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#getInitialSize()
	 */
	protected Point getInitialSize() {
		Point size = super.getInitialSize();
		size.y += convertHeightInCharsToPixels(3);
		return size;
	}

	protected Control createDialogArea(Composite parent) {

		Composite dialogArea = (Composite) super.createDialogArea(parent);

		dialogArea.setLayout(new GridLayout(1, true));

		initializeDialogUnits(dialogArea);

		createMessageArea(dialogArea).setLayoutData(
				new GridData(SWT.FILL, SWT.NONE, true, false));

		createDialogContentArea(dialogArea).setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, true, true));

		applyDialogFont(dialogArea);

		initializeDialog();

		return dialogArea;
	}

	/**
	 * @param dialogArea
	 */
	protected abstract Control createDialogContentArea(Composite dialogArea);

	/**
	 * 
	 */
	protected void initializeDialog() {
		handleStatusUdpate(IStatus.INFO, getDefaultMessage());
	}

	/**
	 * Create message area.
	 * 
	 * @param parent
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
	 * 
	 * @param status
	 */
	protected void handleStatusUdpate(IStatus status) {
		handleStatusUdpate(status.getSeverity(), status.getMessage());
	}

	/**
	 * Display the message and an appropriate icon.
	 * 
	 * @param messgage
	 * @param severity
	 */
	protected void handleStatusUdpate(int severity, String messgage) {
		Image image = null;
		Button okBttn = getButton(OK);
		switch (severity) {
		case IStatus.ERROR: {
			if (messgage == null) {
				messgage = getErrorMessage();
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
		if (messgage == null) {
			messgage = getDefaultMessage();
		}
		if (messgage.equals(MarkerSupportInternalUtilities.EMPTY_STRING)) {
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
			setMessageText(messgage);
			msgParent.layout();
		}
	}

	/**
	 * @param image
	 */
	protected void setMessageImage(Image image) {
		if (imageLabel != null) {
			imageLabel.setImage(image);
		}
	}

	/**
	 * @param messgage
	 */
	protected void setMessageText(String messgage) {
		if (messageArea != null) {
			messageArea.setText(messgage);
		}
	}

	/**
	 * 
	 */
	protected Image getMessageImage() {
		if (imageLabel != null) {
			imageLabel.getImage();
		}
		return null;
	}

	/**
	 */
	protected String getMessageText() {
		if (messageArea != null) {
			return messageArea.getText();
		}
		return null;
	}

	/**
	 * 
	 * @param visible
	 */
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

	/**
	 */
	protected Image getInfoImage() {
		return JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_INFO);
	}

	/**
	 */
	protected Image getWarningImage() {
		return JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_WARNING);
	}

	/**
	 */
	protected Image getErrorImage() {
		return JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_ERROR);
	}

	protected void performDefaults() {
		super.performDefaults();
	}

	protected boolean isResizable() {
		return true;
	}

	protected void okPressed() {
		super.okPressed();
	}

}