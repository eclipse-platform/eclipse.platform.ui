/*******************************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.cheatsheets.parameters;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.cheatsheets.ICheatSheetManager;


public class GatherDataDialog extends Dialog {
	private Text t1;
	private Text t2;
	private Text t3;

	private ICheatSheetManager csmanager;
	
	
	public GatherDataDialog(Shell parentShell, ICheatSheetManager csm) {
		super(parentShell);
		csmanager = csm;	
	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Data Entry"); //$NON-NLS-1$

	}

	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		GridLayout g = new GridLayout();
		g.numColumns = 1;
		composite.setLayout(g);

		Composite descComposite = new Composite(composite, SWT.NULL);
		GridLayout dg = new GridLayout();
		dg.numColumns = 1;
		descComposite.setLayout(dg);

		Label dl = new Label(descComposite, SWT.NULL);
		dl.setText("Enter some data to be analysed."); //$NON-NLS-1$

		String data = null;
		if (csmanager != null)
			data = csmanager.getData("pattern"); //$NON-NLS-1$

		Composite bComposite = new Composite(composite, SWT.NULL);
		GridLayout bg = new GridLayout(2, false);
		bComposite.setLayout(bg);

		Label l = new Label(bComposite, SWT.NULL);
		l.setText("Please enter you name:"); //$NON-NLS-1$
		t1 = new Text(bComposite, SWT.BORDER);

		l = new Label(bComposite, SWT.NULL);
		l.setText("What is your favorite color:"); //$NON-NLS-1$
		t2 = new Text(bComposite, SWT.BORDER);

		l = new Label(bComposite, SWT.NULL);
		l.setText("Which animal would you most like to own:"); //$NON-NLS-1$
		t3 = new Text(bComposite, SWT.BORDER);
		return composite;
	}

	protected void okPressed() {

		csmanager.setData("name", t1.getText()); //$NON-NLS-1$
		csmanager.setData("color", t2.getText()); //$NON-NLS-1$
		csmanager.setData("animal", t3.getText()); //$NON-NLS-1$
		super.okPressed();
	}
	
}
