/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.statushandlers.wizards;

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
 * This wizard with no default contructor. Used during the status handling
 * facility tests.
 * 
 * @see org.eclipse.ui.tests.statushandlers.WizardsStatusHandlingTestCase
 * 
 * @since 3.3
 */
public class FaultyExportWizard extends Wizard implements IExportWizard {

	public FaultyExportWizard(String name) {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	public boolean performFinish() {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench,
	 *      org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		setWindowTitle("window_title"); //$NON-NLS-1$
		// setNeedsProgressMonitor(true);
	}

	public void addPages() {
		super.addPages();
		addPage(new MyWizardPage("wizard"));
	}

	public class MyWizardPage extends WizardPage {
		/**
		 * @param pageName
		 */
		protected MyWizardPage(String pageName) {
			super(pageName);
			// TODO Auto-generated constructor stub
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
		 */
		public void createControl(Composite parent) {
			Composite page = new Composite(parent, SWT.NONE);
			GridLayout pageLayout = new GridLayout();
			page.setLayout(pageLayout);
			page.setLayoutData(new GridData(GridData.FILL_BOTH));
			Label nameLabel = new Label(page, SWT.NONE);
			nameLabel.setText("label"); //$NON-NLS-1$
			// setControl(page);
		}
	}
}
