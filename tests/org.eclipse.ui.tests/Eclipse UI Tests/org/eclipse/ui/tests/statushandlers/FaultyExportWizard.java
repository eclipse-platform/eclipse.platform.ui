/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.tests.statushandlers;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;

/**
 * This wizard with no default constructor. Used during the status handling
 * facility tests.
 */
public class FaultyExportWizard extends Wizard implements IExportWizard {

	/**
	 * Intentional constructor that hides default one
	 *
	 * @param name
	 *            does nothing
	 */
	public FaultyExportWizard(String name) {
		super();
	}

	@Override
	public boolean performFinish() {
		return false;
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		setWindowTitle("window title");
	}

	@Override
	public void addPages() {
		super.addPages();
		addPage(new MyWizardPage("wizard"));
	}

	public class MyWizardPage extends WizardPage {
		/**
		 * Creates some wizard page
		 *
		 * @param pageName
		 */
		protected MyWizardPage(String pageName) {
			super(pageName);
		}

		@Override
		public void createControl(Composite parent) {
			Composite page = new Composite(parent, SWT.NONE);
			GridLayout pageLayout = new GridLayout();
			page.setLayout(pageLayout);
			page.setLayoutData(new GridData(GridData.FILL_BOTH));
			Label nameLabel = new Label(page, SWT.NONE);
			nameLabel.setText("some label");
			setControl(page);
		}
	}
}
