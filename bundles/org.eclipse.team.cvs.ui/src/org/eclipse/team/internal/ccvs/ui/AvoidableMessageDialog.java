/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class AvoidableMessageDialog extends MessageDialog {
	Button dontShowAgain;
	boolean dontShow;
	boolean showOption;
	
	public AvoidableMessageDialog(Shell shell, String dialogTitle, Image dialogTitleImage, String dialogMessage, int dialogImageType, String[] dialogButtonLabels, int defaultIndex) {
		this(shell, dialogTitle, dialogTitleImage, dialogMessage, dialogImageType, dialogButtonLabels, defaultIndex, true);
	}
	
	public AvoidableMessageDialog(Shell shell, String dialogTitle, Image dialogTitleImage, String dialogMessage, int dialogImageType, String[] dialogButtonLabels, int defaultIndex, boolean showOption) {
		super(shell, dialogTitle, dialogTitleImage, dialogMessage, dialogImageType, dialogButtonLabels, defaultIndex);
		this.showOption = showOption;
	}
	
	protected Control createCustomArea(Composite composite) {
		if ( ! showOption) return null;
		dontShow = false;
		dontShowAgain = new Button(composite, SWT.CHECK);
		GridData data = new GridData();
		data.horizontalIndent = 50;
		dontShowAgain.setLayoutData(data);
		dontShowAgain.setText(Policy.bind("AvoidableMessageDialog.dontShowAgain")); //$NON-NLS-1$
		dontShowAgain.setSelection(dontShow);
		dontShowAgain.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				dontShow = dontShowAgain.getSelection();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

		});
		return dontShowAgain;
	}
	
	public boolean isDontShowAgain() {
		return dontShow;
	}

}
