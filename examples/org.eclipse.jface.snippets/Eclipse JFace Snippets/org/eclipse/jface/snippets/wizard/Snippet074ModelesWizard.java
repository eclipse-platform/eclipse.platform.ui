/*******************************************************************************
 * Copyright (c) 2019 Remain Software and others,
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wim Jongman - Modeless / Non Modal Wizard
 *******************************************************************************/
package org.eclipse.jface.snippets.wizard;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Test class to show how a non modal wizard can be constructed.
 *
 * @since 3.16
 *
 */
public class Snippet074ModelesWizard {

	private static int fCounter = 0;

	/**
	 * @param args
	 * @since 3.16
	 */
	public static void main(String[] args) {

		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new GridLayout(1, false));

		Text text = new Text(shell, SWT.MULTI | SWT.LEAD | SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		shell.setSize(800, 800);
		shell.open();

		WizardPage p = new WizardPage(WizardPage.class.getSimpleName()) {

			@Override
			public void createControl(Composite parent) {

				Composite composite = new Composite(shell, SWT.NONE);
				composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
				composite.setLayout(new GridLayout(1, false));
				Button button = new Button(parent, SWT.PUSH);
				if (fCounter == 0)
					button.setText("Click Finish to switch to modeless"); //$NON-NLS-1$
				else if (fCounter == 1)
					button.setText("Click Finish to change shell style"); //$NON-NLS-1$
				else
					button.setText("Click Finish to exit"); //$NON-NLS-1$

				fCounter = 0;
				button.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
				setControl(composite);
			}
		};

		Wizard w = new Wizard() {

			@Override
			public boolean performFinish() {
				fCounter++;
				return true;
			}
		};

		w.addPage(p);

		// Modal

		text.setText("Modal: You cannot reach this text"); //$NON-NLS-1$
		new WizardDialog(shell, w).open();

		// Modeless
		text.setText("This text is reachable even though the wizard is on top. \n\n\nsetModal(false);"); //$NON-NLS-1$
		new WizardDialog(shell, w).setModal(false).open();

		// Do not allow min and max
		WizardDialog d = new WizardDialog(shell, w);
		d.setModal(false);
		d.setShellStyle((d.getShellStyle() & ~SWT.MIN & ~SWT.MAX));
		text.setText(
				"The wizard cannot be minimised or maximised.\n\n\nsetModal(false);\nsetShellStyle(getDefaultShellStyle() & ~SWT.MIN & ~SWT.MAX));"); //$NON-NLS-1$
		d.open();

		display.dispose();
	}

}
