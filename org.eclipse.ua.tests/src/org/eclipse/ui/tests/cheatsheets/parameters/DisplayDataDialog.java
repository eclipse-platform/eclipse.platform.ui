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


public class DisplayDataDialog extends Dialog {

	private ICheatSheetManager csmanager;
	private String[] parameters;
	
	
	public DisplayDataDialog(Shell parentShell, ICheatSheetManager csm, String[] parameters) {
		super(parentShell);
		csmanager = csm;
		this.parameters = parameters;
	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Data Analysis"); //$NON-NLS-1$

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
		dl.setText("Result of your data analysis."); //$NON-NLS-1$

		Composite bComposite = new Composite(composite, SWT.NULL);
		GridLayout bg = new GridLayout(1, false);
		bComposite.setLayout(bg);

		Label l = new Label(bComposite, SWT.NULL);
		l.setText("Name: " + parameters[1]); //$NON-NLS-1$

		l = new Label(bComposite, SWT.NULL);
		l.setText("Favorite color: " + parameters[2]); //$NON-NLS-1$

		l = new Label(bComposite, SWT.NULL);
		l.setText("Animal: " + parameters[3]); //$NON-NLS-1$

		l = new Label(bComposite, SWT.NULL);
		l.setText("Extra 1: " + parameters[4]); //$NON-NLS-1$
		
		l = new Label(bComposite, SWT.NULL);
		l.setText("Extra 2: " + parameters[5]); //$NON-NLS-1$

		return composite;
	}
	
}
