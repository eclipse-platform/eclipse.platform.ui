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
package org.eclipse.ui.examples.cheatsheets.pattern.dialogs;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.cheatsheets.ICheatSheetManager;


public class PatternSelectionDialog extends Dialog {
	Button f;
	Button v;
	Button s;
	String selected;
	private static final String factory = "Factory"; //$NON-NLS-1$
	private static final String singleton = "Singleton"; //$NON-NLS-1$
	private static final String visitor = "Visitor"; //$NON-NLS-1$
	private ICheatSheetManager csmanager;
	
	public PatternSelectionDialog(Shell parentShell) {
		super(parentShell);
	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Pattern Selection"); //$NON-NLS-1$

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

		Composite bComposite = new Composite(composite, SWT.NULL);
		GridLayout bg = new GridLayout(2, false);
		bComposite.setLayout(bg);

		Label dl = new Label(descComposite, SWT.NULL);
		dl.setText("Please select a design pattern."); //$NON-NLS-1$

		f = new Button(bComposite, SWT.RADIO);
		f.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
					selected = factory;		
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				return;	
			}
		}
		);
		Label fl = new Label(bComposite, SWT.NULL);
		fl.setText(factory);

		s = new Button(bComposite, SWT.RADIO);
		s.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				selected = singleton;
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				return;
			}
		});
		Label sl = new Label(bComposite, SWT.NULL);
		sl.setText(singleton);

		v = new Button(bComposite, SWT.RADIO);
		v.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				selected = visitor;
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				return;
			}
		});
		Label vl = new Label(bComposite, SWT.NULL);
		vl.setText(visitor);

		return composite;
	}

	protected void okPressed() {

		//System.out.println(selected);
		csmanager.setData("pattern", selected); //$NON-NLS-1$
		super.okPressed();
	}
	
	public void setCSM(ICheatSheetManager csm){
		csmanager = csm;	
	}
	
}
