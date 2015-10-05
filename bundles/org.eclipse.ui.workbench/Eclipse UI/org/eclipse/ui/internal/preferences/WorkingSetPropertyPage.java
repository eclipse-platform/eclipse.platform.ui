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
package org.eclipse.ui.internal.preferences;

import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.preferences.WizardPropertyPage;

/**
 * Embeds a working set wizard for a given working set into a property page.
 *
 * @since 3.4
 */
public class WorkingSetPropertyPage extends WizardPropertyPage {

	private static final class ReadOnlyWizard extends Wizard {

		public ReadOnlyWizard() {
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean performFinish() {
			return true;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void addPages() {
			addPage(new ReadOnlyPage());
		}
	}

	private static final class ReadOnlyPage extends WizardPage {

		protected ReadOnlyPage() {
			super(WorkbenchMessages.WorkingSetPropertyPage_ReadOnlyWorkingSet_title);
			setDescription(WorkbenchMessages.WorkingSetPropertyPage_ReadOnlyWorkingSet_description);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void createControl(Composite parent) {
			Composite composite= new Composite(parent, SWT.NONE);
			composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			composite.setLayout(new GridLayout(1, false));

			setControl(composite);
		}
	}

	private IWorkingSet fWorkingSet;

	public WorkingSetPropertyPage() {
		noDefaultAndApplyButton();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setElement(IAdaptable element) {
		super.setElement(element);
		fWorkingSet = Adapters.getAdapter(element, IWorkingSet.class, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void applyChanges() {
		//Wizard does all the work
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected IWizard createWizard() {
		if (fWorkingSet.isEditable()) {
			return PlatformUI.getWorkbench().getWorkingSetManager().createWorkingSetEditWizard(fWorkingSet);
		}

		return new ReadOnlyWizard();
	}

}
