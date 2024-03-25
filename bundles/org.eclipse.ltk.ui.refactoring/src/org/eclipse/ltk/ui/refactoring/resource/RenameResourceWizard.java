/*******************************************************************************
 * Copyright (c) 2007, 2020 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jerome Cambon <jerome.cambon@oracle.com> - [ltk] Rename refactoring should give more control over new file name - https://bugs.eclipse.org/391389
 *******************************************************************************/
package org.eclipse.ltk.ui.refactoring.resource;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import org.eclipse.core.resources.IResource;

import org.eclipse.jface.wizard.IWizardPage;

import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.IRenameResourceProcessor;
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;
import org.eclipse.ltk.internal.core.refactoring.resource.RenameResourceProcessor;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIMessages;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;

/**
 * A wizard for the rename resource refactoring.
 *
 * @since 3.4
 */
public class RenameResourceWizard extends RefactoringWizard {
	/**
	 * Creates a {@link RenameResourceWizard}.
	 *
	 * @param resource
	 *             the resource to rename. The resource must exist.
	 */
	public RenameResourceWizard(IResource resource) {
		super(new RenameRefactoring(new RenameResourceProcessor(resource)), DIALOG_BASED_USER_INTERFACE);
		setDefaultPageTitle(RefactoringUIMessages.RenameResourceWizard_page_title);
		setWindowTitle(RefactoringUIMessages.RenameResourceWizard_window_title);
	}

	/**
	 * Creates a {@link RenameResourceWizard} with the new resource's name set to newName.
	 *
	 * @param resource the resource to rename. The resource must exist.
	 * @param newName The new name to give the resource.
	 * @since 3.10
	 */
	public RenameResourceWizard(IResource resource, String newName) {
		this(resource);
		RenameResourceProcessor fRenameResourceProcessor= (RenameResourceProcessor) ((RenameRefactoring) super.getRefactoring()).getProcessor();
		fRenameResourceProcessor.setNewResourceName(newName);
	}

	@Override
	protected void addUserInputPages() {
		addPage(new RenameResourceRefactoringConfigurationPage(getProcessor()));
	}

	/**
	 * @return the IRenameResourceProcessor used by this wizard
	 * @since 3.11
	 */
	protected IRenameResourceProcessor getProcessor() {
		return getRefactoring().getAdapter(RenameResourceProcessor.class);
	}

	/**
	 * @since 3.11
	 */
	public static class RenameResourceRefactoringConfigurationPage extends UserInputWizardPage {

		private final IRenameResourceProcessor fRefactoringProcessor;
		private Text fNameField;

		public RenameResourceRefactoringConfigurationPage(IRenameResourceProcessor processor) {
			super("RenameResourceRefactoringInputPage"); //$NON-NLS-1$
			fRefactoringProcessor= processor;
		}

		public RenameResourceRefactoringConfigurationPage(String name, IRenameResourceProcessor processor) {
			super(name);
			fRefactoringProcessor= processor;
		}

		/**
		 * Creates the top level Composite for this dialog page
		 * under the given parent composite.
		 * <p>
		 * The created top level Composite will be set as the top level control
		 * for this dialog page and can be accessed using <code>getControl</code>
		 * </p>
		 * <p>
		 * The top level Composite will have a GridLayout of 2 columns of unequal width
		 * </p>
		 */
		@Override
		public void createControl(Composite parent) {
			Composite composite= new Composite(parent, SWT.NONE);
			composite.setLayout(new GridLayout(2, false));
			composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			composite.setFont(parent.getFont());

			Label label= new Label(composite, SWT.NONE);
			label.setText(RefactoringUIMessages.RenameResourceWizard_name_field_label);
			label.setLayoutData(new GridData());

			fNameField= new Text(composite, SWT.BORDER);
			String resourceName= fRefactoringProcessor.getNewResourceName();
			fNameField.setText(resourceName);
			fNameField.setFont(composite.getFont());
			fNameField.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
			fNameField.addModifyListener(e -> validatePage());

			int lastIndexOfDot= resourceName.lastIndexOf('.');
			if ((fRefactoringProcessor.getResource().getType() == IResource.FILE) && (lastIndexOfDot > 0)) {
				fNameField.setSelection(0, lastIndexOfDot);
			} else {
				fNameField.selectAll();
			}
			setPageComplete(false);
			setControl(composite);
		}

		@Override
		public void setVisible(boolean visible) {
			if (visible) {
				fNameField.setFocus();
			}
			super.setVisible(visible);
		}

		protected final void validatePage() {
			String text= fNameField.getText();
			RefactoringStatus status= fRefactoringProcessor.validateNewElementName(text);
			setPageComplete(status);
		}

		@Override
		protected boolean performFinish() {
			initializeRefactoring();
			storeSettings();
			return super.performFinish();
		}

		@Override
		public IWizardPage getNextPage() {
			initializeRefactoring();
			storeSettings();
			return super.getNextPage();
		}

		protected void storeSettings() {
			//do nothing
		}

		protected void initializeRefactoring() {
			fRefactoringProcessor.setNewResourceName(fNameField.getText());
		}

		protected IRenameResourceProcessor getProcessor() {
			return fRefactoringProcessor;
		}
	}
}