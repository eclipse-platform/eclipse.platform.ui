/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

/**
 * The IconAndMessageDialog is the abstract superclass of dialogs that
 * have an icon and a message as the first two widgets. In this dialog
 * the icon and message are direct children of the shell in order that
 * they can be read by accessibility tools more easily.
 */
public abstract class IconAndMessageDialog extends Dialog {

	/**
	 * Message (a localized string).
	 */
	protected String message;

	/**
	 * Message label is the label the message is shown on.
	 */
	protected Label messageLabel;

	/**
	 * Constructor for IconAndMessageDialog.
	 *
	 * @param parentShell the parent shell, or <code>null</code> to create a top-level shell
	 */
	public IconAndMessageDialog(Shell parentShell) {
		super(parentShell);
	}

	/**
	 * Create the area the message will be shown in.
	 */
	protected Control createMessageArea(Composite composite) {
		// create composite

		// create image
		Image image = getImage();
		if (image != null) {
			Label label = new Label(composite, SWT.NULL);
			image.setBackground(label.getBackground());
			label.setImage(image);
			label.setLayoutData(
				new GridData(
					GridData.HORIZONTAL_ALIGN_CENTER
						| GridData.VERTICAL_ALIGN_BEGINNING));
		}

		// create message
		if (message != null) {
			messageLabel = new Label(composite, SWT.WRAP);
			messageLabel.setText(message);
			GridData data =
				new GridData(
					GridData.GRAB_HORIZONTAL
						| GridData.HORIZONTAL_ALIGN_FILL
						| GridData.VERTICAL_ALIGN_BEGINNING);
			data.widthHint =
				convertHorizontalDLUsToPixels(
					IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
			messageLabel.setLayoutData(data);
		}
		return composite;
	}

	/*
	 * @see Dialog.createButtonBar()
	 */
	protected Control createButtonBar(Composite parent) {

		Composite composite = new Composite(parent, SWT.NONE);

		// create a layout with spacing and margins appropriate for the font size.
		GridLayout layout = new GridLayout();
		layout.numColumns = 0; // this is incremented by createButton
		layout.makeColumnsEqualWidth = true;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.horizontalSpacing =
			convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		layout.verticalSpacing =
			convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);

		composite.setLayout(layout);

		GridData data =
			new GridData(
				GridData.HORIZONTAL_ALIGN_END | GridData.VERTICAL_ALIGN_CENTER);
		data.horizontalSpan = 2;
		composite.setLayoutData(data);

		composite.setFont(parent.getFont());

		// Add the buttons to the button bar.
		createButtonsForButtonBar(composite);

		return composite;

	}

	/**
	* Returns the image to display beside the message in this dialog.
	* <p>
	* Subclasses may override.
	* </p>
	* 
	* @return the image to display beside the message
	* @since 2.0
	*/
	protected abstract Image getImage();

	/*
	 * @see Dialog.createContents(Composite)
	 */
	protected Control createContents(Composite parent) {

		// initialize the dialog units
		initializeDialogUnits(parent);

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight =
			convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN)
				* 3
				/ 2;
		layout.marginWidth =
			convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.verticalSpacing =
			convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		layout.horizontalSpacing =
			convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING)
				* 2;
		layout.makeColumnsEqualWidth = false;
		parent.setLayout(layout);
		parent.setLayoutData(new GridData(GridData.FILL_BOTH));

		createDialogAndButtonArea(parent);

		return parent;
	}

	/**
	 * Create the dialog area and the button bar for 
	 * the receiver.
	 * @param parent
	 */
	protected void createDialogAndButtonArea(Composite parent) {
		// create the dialog area and button bar
		dialogArea = createDialogArea(parent);
		buttonBar = createButtonBar(parent);

		//Apply to the parent so that the message gets it too.
		applyDialogFont(parent);
	}

}