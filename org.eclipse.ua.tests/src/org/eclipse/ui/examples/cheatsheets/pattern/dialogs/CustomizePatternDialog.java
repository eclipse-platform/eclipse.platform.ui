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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.cheatsheets.*;

public class CustomizePatternDialog extends Dialog {
	private ICheatSheetManager csmanager;
	private Text t1;
	private Text t2;
	private Text t3;
	private Text t4;
	private Text t5;
	private String pname;
	private String cname;
	/**
	* @param parentShell
	*/
	public CustomizePatternDialog(Shell parentShell) {
		super(parentShell);
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
		String data = null;
		if (csmanager != null)
			data = csmanager.getData("pattern");//$NON-NLS-1$
		if (data != null) {
			dl.setText("Customize your " + data + " pattern.");//$NON-NLS-1$//$NON-NLS-2$
			if (data.equals("Singleton"))//$NON-NLS-1$
				addSingletonStuff(bComposite);
			else if (data.equals("Visitor"))//$NON-NLS-1$
				addVisitorStuff(bComposite);
			else if (data.equals("Factory"))//$NON-NLS-1$
				addFactoryStuff(bComposite);
		} else
			dl.setText("Customize your pattern.");//$NON-NLS-1$

		return composite;
	}

	private void addSingletonStuff(Composite c) {
		Label l = new Label(c, SWT.NULL);
		l.setText("Java Project Name:");//$NON-NLS-1$
		t1 = new Text(c, SWT.BORDER);

		l = new Label(c, SWT.NULL);
		l.setText("Singleton Class Name:");//$NON-NLS-1$
		t2 = new Text(c, SWT.BORDER);
	}

	public void addFactoryStuff(Composite c) {
		Label l = new Label(c, SWT.NULL);
		l.setText("Java Project Name:");//$NON-NLS-1$
		t1 = new Text(c, SWT.BORDER);

		l = new Label(c, SWT.NULL);
		l.setText("Factory Class Name:");//$NON-NLS-1$
		t2 = new Text(c, SWT.BORDER);

		l = new Label(c, SWT.NULL);
		l.setText("Base Derived Class Name:");//$NON-NLS-1$
		t3 = new Text(c, SWT.BORDER);

		l = new Label(c, SWT.NULL);
		l.setText("First Derived Class Name:");//$NON-NLS-1$
		t4 = new Text(c, SWT.BORDER);

		l = new Label(c, SWT.NULL);
		l.setText("Second Derived Class Name:");//$NON-NLS-1$
		t5 = new Text(c, SWT.BORDER);
	}

	public void addVisitorStuff(Composite c) {

		Label l = new Label(c, SWT.NULL);
		l.setText("Java Project Name:");//$NON-NLS-1$
		t1 = new Text(c, SWT.BORDER);

		l = new Label(c, SWT.NULL);
		l.setText("Visitor Class Name:");//$NON-NLS-1$
		t2 = new Text(c, SWT.BORDER);

		l = new Label(c, SWT.NULL);
		l.setText("Class to visit Name:");//$NON-NLS-1$
		t3 = new Text(c, SWT.BORDER);

	}

	protected void okPressed() {
		pname = t1.getText();
		cname = t2.getText();
		csmanager.setData("project", pname);//$NON-NLS-1$
		String pattern = csmanager.getData("pattern");//$NON-NLS-1$
		if (pattern.equals("Factory")) {//$NON-NLS-1$
			StringBuffer buffer = new StringBuffer();
			buffer.append(t2.getText());
			buffer.append(',');
			buffer.append(t3.getText());
			buffer.append(',');
			buffer.append(t4.getText());
			buffer.append(',');
			buffer.append(t5.getText());

			csmanager.setData("files", buffer.toString());//$NON-NLS-1$
		} else if (pattern.equals("Visitor")) {//$NON-NLS-1$
			StringBuffer buffer = new StringBuffer();
			buffer.append(t2.getText());
			buffer.append(',');
			buffer.append(t3.getText());

			csmanager.setData("files", buffer.toString());//$NON-NLS-1$
		} else if (pattern.equals("Singleton")) {//$NON-NLS-1$
			StringBuffer buffer = new StringBuffer();
			buffer.append(t2.getText());

			csmanager.setData("files", buffer.toString());//$NON-NLS-1$
		}

		super.okPressed();
	}

	public void setCSM(ICheatSheetManager csm) {
		csmanager = csm;
	}

}
