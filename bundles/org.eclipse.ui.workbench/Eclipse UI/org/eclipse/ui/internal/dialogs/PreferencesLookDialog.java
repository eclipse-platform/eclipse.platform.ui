/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;

import org.eclipse.ui.internal.WorkbenchMessages;

/**
 * The PreferencesLookDialog is the dialog for selecting 
 * whether or not there are icons shown, what size of icons 
 * and whether or not there is text.
 */
public class PreferencesLookDialog extends Dialog {

	WorkbenchPreferenceDialog preferenceDialog;

	int iconMode;

	boolean textShowing;
	
//	Minimum dialog width (in dialog units)
	private static final int MIN_DIALOG_WIDTH = 150;


	/**
	 * Create a new instance of the receiver parented
	 * by the parent Shell of dialog.
	 * @param dialog
	 */
	public PreferencesLookDialog(WorkbenchPreferenceDialog dialog) {
		super(dialog.getShell());
		preferenceDialog = dialog;
		iconMode = WorkbenchPreferenceDialog.ICON_MODE;
		textShowing = WorkbenchPreferenceDialog.TEXT_SHOWING;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(WorkbenchMessages.getString("PreferencesLookDialog.GroupSettingsTitle")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#getInitialSize()
	 */
	protected Point getInitialSize() {
		Point shellSize = super.getInitialSize();
		return new Point(Math.max(convertHorizontalDLUsToPixels(MIN_DIALOG_WIDTH * 5 / 4),
				shellSize.x), shellSize.y);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		Button okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);
		getShell().setDefaultButton(okButton);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		Composite dialogArea = (Composite) super.createDialogArea(parent);

		createIconsGroup(dialogArea);

		createTextRadio(dialogArea);

		return dialogArea;
	}

	/**
	 * Create the radio group for the text.
	 * @param parent
	 */
	private void createTextRadio(Composite parent) {
		final Button showText = new Button(parent, SWT.CHECK);
		showText.setText(WorkbenchMessages.getString("PreferencesLookDialog.ShowTextButton")); //$NON-NLS-1$
		showText.setSelection(textShowing);
		showText.addSelectionListener(new SelectionAdapter() {
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			public void widgetSelected(SelectionEvent e) {
				textShowing = showText.getSelection();
			}
		});

	}

	/**
	 * Create the icons combo for the receiver.
	 * @param parent
	 */
	private void createIconsGroup(Composite parent) {

		Group iconsGroup = new Group(parent, SWT.NONE);
		iconsGroup.setText(WorkbenchMessages.getString("PreferencesLookDialog.IconsGroup")); //$NON-NLS-1$

		iconsGroup.setLayout(new GridLayout());
		iconsGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));

		Button noButton = new Button(iconsGroup, SWT.RADIO);
		noButton.setText(WorkbenchMessages.getString("PreferencesLookDialog.NoneButton")); //$NON-NLS-1$
		noButton.addSelectionListener(new SelectionAdapter() {
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			public void widgetSelected(SelectionEvent e) {
				iconMode = SWT.NONE;
			}
		});
		noButton.setSelection(iconMode == SWT.NONE);

		Button smallButton = new Button(iconsGroup, SWT.RADIO);
		smallButton.setText(WorkbenchMessages.getString("PreferencesLookDialog.SmallButton")); //$NON-NLS-1$
		smallButton.addSelectionListener(new SelectionAdapter() {
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			public void widgetSelected(SelectionEvent e) {
				iconMode = SWT.MIN;
			}
		});
		smallButton.setSelection(iconMode == SWT.MIN);

		Button largeButton = new Button(iconsGroup, SWT.RADIO);
		largeButton.setText(WorkbenchMessages.getString("PreferencesLookDialog.LargeButton")); //$NON-NLS-1$
		largeButton.addSelectionListener(new SelectionAdapter() {
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			public void widgetSelected(SelectionEvent e) {
				iconMode = SWT.MAX;
			}
		});
		largeButton.setSelection(iconMode == SWT.MAX);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		WorkbenchPreferenceDialog.ICON_MODE = iconMode;
		WorkbenchPreferenceDialog.TEXT_SHOWING = textShowing;
		preferenceDialog.updateForToolbarChange();
		super.okPressed();
	}

}
