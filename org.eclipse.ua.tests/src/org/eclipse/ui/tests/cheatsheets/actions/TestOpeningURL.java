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
package org.eclipse.ui.tests.cheatsheets.actions;

import java.net.URL;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.cheatsheets.OpenCheatSheetAction;

public class TestOpeningURL implements IWorkbenchWindowActionDelegate {
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		try {
			final String[] result = new String[1];
			Dialog enterURLDialog = new Dialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell()) {
				private Text urlField;
				protected Control createDialogArea(Composite parent) {
					Composite composite = (Composite) super.createDialogArea(parent);
					GridLayout g = new GridLayout(2, false);
					composite.setLayout(g);

					Label l = new Label(composite, SWT.NULL);
					l.setText("URL of cheat sheet:");
					urlField = new Text(composite, SWT.BORDER);
					GridData gd = new GridData(GridData.FILL_HORIZONTAL);
					gd.widthHint = 200;
					urlField.setLayoutData(gd);
					
					return composite;
				}
				
				protected void okPressed() {
					String text = urlField.getText();
					if(text == null || text.length()<=0) {
						result[0] = "";
						super.okPressed();
					} else if(text.substring(0,4).equalsIgnoreCase("http")) {
						result[0] = text;
						super.okPressed();
					} else {
						IStatus status = new Status(IStatus.ERROR, "cheatsheets.test", 0, "Invalid URL, please enter a new URL.", null);
						ErrorDialog.openError(this.getShell(), "Error", null, status);
					}
				}
			};

			enterURLDialog.open();

			if(result[0] != null && result[0].length()>0) {
				URL url = new URL(result[0]);
				OpenCheatSheetAction openCheatSheetAction = new OpenCheatSheetAction("test.opening.url", "Testing Opening URL", url);
				openCheatSheetAction.run();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}
}
