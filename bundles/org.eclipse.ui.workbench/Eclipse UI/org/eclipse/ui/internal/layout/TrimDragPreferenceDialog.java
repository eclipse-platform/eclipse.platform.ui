/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.layout;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.internal.TrimDragPreferences;

/**
 * This dialog allows the User to modify the <code>TrimDragPreferences</code>.
 * 
 * <p><b>
 * NOTE: this is a test harness at this time. This class may be removed
 * before the release of 3.2.
 * </b></p>
 * 
 * @since 3.2
 *
 */
public class TrimDragPreferenceDialog extends Dialog {

	private Text   thresholdValue;
	private Button raggedTrimButton;
	
	/**
	 * @param parentShell
	 */
	public TrimDragPreferenceDialog(Shell parentShell) {
		super(parentShell);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		
		Label disclaimer = new Label(composite, SWT.BORDER | SWT.WRAP);
		disclaimer.setText("NOTE: This dialog is for testing purposes -only- and "+ //$NON-NLS-1$
				" will be removed from the code before release."); //$NON-NLS-1$
		disclaimer.setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_DARK_RED));

		// Filler to leave a blank line
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);
		
		GridData dgd = new GridData();
		dgd.grabExcessHorizontalSpace = true;
		dgd.horizontalSpan = 2;
		dgd.minimumWidth = 50;
		disclaimer.setLayoutData(dgd);
		
		// Create a control to change the threshold value
		Label tLabel = new Label(composite, SWT.NONE);
		tLabel.setText("Drag Threshold"); //$NON-NLS-1$
		
		thresholdValue = new Text(composite, SWT.SINGLE | SWT.BORDER);		
		thresholdValue.setText(Integer.toString(TrimDragPreferences.getThreshold()));
		thresholdValue.setToolTipText("The minimum distance to snap to"); //$NON-NLS-1$
		
		GridData tgd = new GridData();
		tgd.grabExcessHorizontalSpace = true;
		tgd.minimumWidth = 50;
		thresholdValue.setLayoutData(tgd);
		
		// Create a control to change the layout to show 'ragged' trim
		raggedTrimButton = new Button(composite, SWT.CHECK);
		raggedTrimButton.setText("Ragged Trim"); //$NON-NLS-1$
		raggedTrimButton.setSelection(TrimDragPreferences.showRaggedTrim());
		raggedTrimButton.setToolTipText("Allows trim in the same area to have different heights if checked"); //$NON-NLS-1$
		
		GridData rgd = new GridData();
		rgd.horizontalSpan = 2;
		raggedTrimButton.setLayoutData(rgd);
			
		return composite;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		// Update the 'threshold' pref
		try {
			TrimDragPreferences.setThreshold(Integer.parseInt(thresholdValue.getText()));
		} catch (NumberFormatException e) {
			// If it fails...just leave it...
		}
		
		// Update the 'ragged trim' pref
		boolean val = raggedTrimButton.getSelection();
		TrimDragPreferences.setRaggedTrim(val);
		
		super.okPressed();
	}

}
