/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.provisional.forms;

import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.ManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.internal.forms.Messages;

/**
 * A general-purpose dialog that hosts a form. Clients should
 * extend the class and override <code>createFormContent(IManagedForm)</code>
 * protected method.
 * <p>
 * Since forms with wrapped text typically don't have a preferred
 * size, it is important to set the initial dialog size upon creation:
 * <p>
 * <pre>
 * MyFormDialog dialog = new MyFormDialog(shell);
 * dialog.create();
 * dialog.getShell().setSize(500, 500);
 * dialog.open();
 * </pre>
 * <p>
 * Otherwise, the dialog may open very wide.
 * <p>
 * @since 3.3
 */

public class FormDialog extends TrayDialog {
	private FormToolkit toolkit;

	public FormDialog(Shell shell) {
		super(shell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	public FormDialog(IShellProvider parentShell) {
		super(parentShell);
	}
	
	public boolean close() {
		boolean rcode = super.close();
		toolkit.dispose();
		return rcode;
	}

	protected Control createDialogArea(Composite parent) {
		toolkit = new FormToolkit(parent.getDisplay());
		ScrolledForm sform = toolkit.createScrolledForm(parent);
		sform.setLayoutData(new GridData(GridData.FILL_BOTH));
		ManagedForm mform = new ManagedForm(toolkit, sform);
		createFormContent(mform);
		applyDialogFont(sform.getBody());
		return sform;
	}
	
	protected Control createButtonBar(Composite parent) {
		Control sep = toolkit.createCompositeSeparator(parent);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.heightHint =1;
		sep.setLayoutData(gd);
		Control bar = super.createButtonBar(parent);
		recursiveAdapt((Composite)bar);
		return bar;
	}
	
	private void recursiveAdapt(Composite parent) {
		toolkit.adapt(parent);
		Control [] children = parent.getChildren();
		for (int i=0; i<children.length; i++) {
			Control c = children[i];
			if (c instanceof Composite)
				recursiveAdapt((Composite)c);
			else
				toolkit.adapt(c, false, false);
		}
	}
	
	protected void createFormContent(IManagedForm mform) {
		mform.getForm().setText(Messages.FormDialog_defaultTitle);
	}
}
