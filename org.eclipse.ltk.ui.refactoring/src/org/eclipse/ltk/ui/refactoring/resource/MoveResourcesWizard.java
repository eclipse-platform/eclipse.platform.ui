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
package org.eclipse.ltk.ui.refactoring.resource;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.IWizardPage;

import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.model.WorkbenchViewerComparator;

import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.MoveRefactoring;
import org.eclipse.ltk.internal.core.refactoring.Messages;
import org.eclipse.ltk.internal.core.refactoring.resource.MoveResourcesProcessor;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIMessages;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;

/**
 * A wizard for the move resources refactoring.
 *
 * @since 3.4
 */
public class MoveResourcesWizard extends RefactoringWizard {

	/**
	 * Creates a {@link MoveResourcesWizard}.
	 *
	 * @param resources
	 *             the resources to move. The resources must exist.
	 */
	public MoveResourcesWizard(IResource[] resources) {
		super(new MoveRefactoring(new MoveResourcesProcessor(resources)), DIALOG_BASED_USER_INTERFACE);
		setDefaultPageTitle(RefactoringUIMessages.MoveResourcesWizard_page_title);
		setWindowTitle(RefactoringUIMessages.MoveResourcesWizard_window_title);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ltk.ui.refactoring.RefactoringWizard#addUserInputPages()
	 */
	protected void addUserInputPages() {
		MoveResourcesProcessor processor= (MoveResourcesProcessor) getRefactoring().getAdapter(MoveResourcesProcessor.class);
		addPage(new MoveResourcesRefactoringConfigurationPage(processor));
	}


	private static class MoveResourcesRefactoringConfigurationPage extends UserInputWizardPage {

		private final MoveResourcesProcessor fRefactoringProcessor;
		private TreeViewer fDestinationField;

		public MoveResourcesRefactoringConfigurationPage(MoveResourcesProcessor processor) {
			super("MoveResourcesRefactoringConfigurationPage"); //$NON-NLS-1$
			fRefactoringProcessor= processor;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
		 */
		public void createControl(Composite parent) {
			initializeDialogUnits(parent);

			Composite composite= new Composite(parent, SWT.NONE);
			composite.setLayout(new GridLayout(2, false));
			composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			composite.setFont(parent.getFont());

			Label label= new Label(composite, SWT.NONE);
			IResource[] resourcesToMove= fRefactoringProcessor.getResourcesToMove();
			if (resourcesToMove.length == 1) {
				label.setText(Messages.format(RefactoringUIMessages.MoveResourcesWizard_description_single, resourcesToMove[0].getName()));
			} else {
				label.setText(Messages.format(RefactoringUIMessages.MoveResourcesWizard_description_multiple, new Integer(resourcesToMove.length)));
			}
			label.setLayoutData(new GridData());

			fDestinationField= new TreeViewer(composite, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
			GridData gd= new GridData(GridData.FILL, GridData.FILL, true, true, 2, 1);
			gd.widthHint= convertWidthInCharsToPixels(40);
			gd.heightHint= convertHeightInCharsToPixels(15);
			fDestinationField.getTree().setLayoutData(gd);
			fDestinationField.setLabelProvider(new WorkbenchLabelProvider());
			fDestinationField.setContentProvider(new BaseWorkbenchContentProvider());
			fDestinationField.setComparator(new WorkbenchViewerComparator());
			fDestinationField.setInput(ResourcesPlugin.getWorkspace());
			fDestinationField.addFilter(new ViewerFilter() {
				public boolean select(Viewer viewer, Object parentElement, Object element) {
					if (element instanceof IProject) {
						IProject project= (IProject) element;
						return project.isAccessible();
					} else if (element instanceof IFolder) {
						return true;
					}
					return false;
				}
			});
			fDestinationField.addSelectionChangedListener(new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
					validatePage();
				}
			});
			if (resourcesToMove.length > 0) {
				fDestinationField.setSelection(new StructuredSelection(resourcesToMove[0].getParent()));
			}
			setPageComplete(false);
			setControl(composite);
		}

		public void setVisible(boolean visible) {
			if (visible) {
				fDestinationField.getTree().setFocus();
				if (getErrorMessage() != null) {
					setErrorMessage(null); // no error messages until user interacts
				}

			}
			super.setVisible(visible);
		}

		private final void validatePage() {
			RefactoringStatus status;

			IStructuredSelection selection= (IStructuredSelection) fDestinationField.getSelection();
			Object firstElement= selection.getFirstElement();
			if (firstElement instanceof IContainer) {
				status= fRefactoringProcessor.validateDestination((IContainer) firstElement);

			} else {
				status= new RefactoringStatus();
				status.addError(RefactoringUIMessages.MoveResourcesWizard_error_no_selection);
			}
			setPageComplete(status);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ltk.ui.refactoring.UserInputWizardPage#performFinish()
		 */
		protected boolean performFinish() {
			initializeRefactoring();
			storeSettings();
			return super.performFinish();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ltk.ui.refactoring.UserInputWizardPage#getNextPage()
		 */
		public IWizardPage getNextPage() {
			initializeRefactoring();
			storeSettings();
			return super.getNextPage();
		}

		private void storeSettings() {
		}

		private void initializeRefactoring() {
			IContainer container= (IContainer) ((IStructuredSelection) fDestinationField.getSelection()).getFirstElement();
			fRefactoringProcessor.setDestination(container);
		}
	}
}