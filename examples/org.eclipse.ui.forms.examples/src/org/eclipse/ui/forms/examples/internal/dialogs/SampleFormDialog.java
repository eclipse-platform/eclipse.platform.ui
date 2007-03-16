/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.forms.examples.internal.dialogs;

import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.FormDialog;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.examples.internal.rcp.FreeFormPage;

public class SampleFormDialog extends FormDialog {

	public SampleFormDialog(Shell shell) {
		super(shell);
	}

	public SampleFormDialog(IShellProvider parentShell) {
		super(parentShell);
	}

	protected void createFormContent(IManagedForm mform) {
		mform.getForm().setText("An example of a simple form dialog");
		FreeFormPage.createSharedFormContent(mform);
		mform.getForm().setBackgroundImage(null);
		mform.getToolkit().decorateFormHeading(mform.getForm().getForm());
		mform.getForm().setImage(
				PlatformUI.getWorkbench().getSharedImages().getImage(
						ISharedImages.IMG_OBJ_ELEMENT));
	}
}
