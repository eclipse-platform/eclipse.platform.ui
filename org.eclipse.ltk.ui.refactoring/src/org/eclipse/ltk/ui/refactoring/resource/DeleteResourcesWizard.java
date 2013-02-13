/*******************************************************************************
 * Copyright (c) 2007, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.ui.refactoring.resource;

import java.net.URI;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import org.eclipse.core.runtime.IPath;

import org.eclipse.core.resources.IResource;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.LayoutConstants;
import org.eclipse.jface.wizard.IWizardPage;

import org.eclipse.ltk.core.refactoring.participants.DeleteRefactoring;
import org.eclipse.ltk.internal.core.refactoring.Resources;
import org.eclipse.ltk.internal.core.refactoring.resource.DeleteResourcesProcessor;
import org.eclipse.ltk.internal.ui.refactoring.BasicElementLabels;
import org.eclipse.ltk.internal.ui.refactoring.Messages;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIMessages;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;

/**
 * A wizard for the delete resources refactoring.
 *
 * @since 3.4
 */
public class DeleteResourcesWizard extends RefactoringWizard {

	/**
	 * Creates a {@link DeleteResourcesWizard}
	 *
	 * @param resources the resources to delete
	 */
	public DeleteResourcesWizard(IResource[] resources) {
		super(new DeleteRefactoring(new DeleteResourcesProcessor(resources, false)), DIALOG_BASED_USER_INTERFACE);
		setDefaultPageTitle(RefactoringUIMessages.DeleteResourcesWizard_page_title);
		setWindowTitle(RefactoringUIMessages.DeleteResourcesWizard_window_title);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ltk.ui.refactoring.RefactoringWizard#addUserInputPages()
	 */
	protected void addUserInputPages() {
		DeleteResourcesProcessor processor= (DeleteResourcesProcessor) getRefactoring().getAdapter(DeleteResourcesProcessor.class);
		addPage(new DeleteResourcesRefactoringConfigurationPage(processor));
	}

	private static class DeleteResourcesRefactoringConfigurationPage extends UserInputWizardPage {

		private DeleteResourcesProcessor fRefactoringProcessor;
		private Button fDeleteContentsButton;

		public DeleteResourcesRefactoringConfigurationPage(DeleteResourcesProcessor processor) {
			super("DeleteResourcesRefactoringConfigurationPage"); //$NON-NLS-1$
			fRefactoringProcessor= processor;
		}

		public void createControl(Composite parent) {
			initializeDialogUnits(parent);

			Point defaultSpacing= LayoutConstants.getSpacing();

			Composite composite= new Composite(parent, SWT.NONE);
			GridLayout gridLayout= new GridLayout(2, false);
			gridLayout.horizontalSpacing= defaultSpacing.x * 2;
			gridLayout.verticalSpacing= defaultSpacing.y;

			composite.setLayout(gridLayout);
			composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

			Image image= parent.getDisplay().getSystemImage(SWT.ICON_QUESTION);
			Label imageLabel = new Label(composite, SWT.NULL);
			imageLabel.setBackground(image.getBackground());
			imageLabel.setImage(image);
			imageLabel.setLayoutData(new GridData(SWT.CENTER, SWT.BEGINNING, false, false));

			IResource[] resources= fRefactoringProcessor.getResourcesToDelete();
			Label label= new Label(composite, SWT.WRAP);

			boolean onlyProjects= Resources.containsOnlyProjects(resources);
			if (onlyProjects) {
				if (resources.length == 1) {
					label.setText(Messages.format(RefactoringUIMessages.DeleteResourcesWizard_label_single_project, BasicElementLabels.getResourceName(resources[0])));
				} else {
					label.setText(Messages.format(RefactoringUIMessages.DeleteResourcesWizard_label_multi_projects, new Integer(resources.length)));
				}
			} else if (containsLinkedResource(resources)) {
				if (resources.length == 1) {
					label.setText(Messages.format(RefactoringUIMessages.DeleteResourcesWizard_label_single_linked, BasicElementLabels.getResourceName(resources[0])));
				} else {
					label.setText(Messages.format(RefactoringUIMessages.DeleteResourcesWizard_label_multi_linked, new Integer(resources.length)));
				}
			} else {
				if (resources.length == 1) {
					label.setText(Messages.format(RefactoringUIMessages.DeleteResourcesWizard_label_single, BasicElementLabels.getResourceName(resources[0])));
				} else {
					label.setText(Messages.format(RefactoringUIMessages.DeleteResourcesWizard_label_multi, new Integer(resources.length)));
				}
			}
			GridData gridData= new GridData(SWT.FILL, SWT.FILL, true, false);
			gridData.widthHint= convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
			label.setLayoutData(gridData);

			Composite supportArea= new Composite(composite, SWT.NONE);
			supportArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
			gridLayout= new GridLayout(1, false);
			gridLayout.horizontalSpacing= defaultSpacing.x * 2;
			gridLayout.verticalSpacing= defaultSpacing.y;

			supportArea.setLayout(gridLayout);

			if (onlyProjects) {
				fDeleteContentsButton= new Button(supportArea, SWT.CHECK);
				fDeleteContentsButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
				fDeleteContentsButton.setText(RefactoringUIMessages.DeleteResourcesWizard_project_deleteContents);
				fDeleteContentsButton.setFocus();
				
				Label projectLocationsLabel= new Label(supportArea, SWT.NONE);
				GridData labelData= new GridData(SWT.FILL, SWT.FILL, true, false);
				labelData.verticalIndent= 5;
				projectLocationsLabel.setLayoutData(labelData);
				projectLocationsLabel.setText(resources.length == 1
						? RefactoringUIMessages.DeleteResourcesWizard_project_location
						: RefactoringUIMessages.DeleteResourcesWizard_project_locations);
				
				int style= SWT.MULTI | SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL;
				if (resources.length != 1)
					style |= SWT.BORDER;
				StyledText projectLocationsList= new StyledText(supportArea, style);
				projectLocationsList.setAlwaysShowScrollBars(false);
				labelData.horizontalIndent= projectLocationsList.getLeftMargin();
				gridData= new GridData(SWT.FILL, SWT.FILL, true, true);
				projectLocationsList.setLayoutData(gridData);
				projectLocationsList.setBackground(projectLocationsList.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
				
				StringBuffer buf= new StringBuffer();
				for (int i= 0; i < resources.length; i++) {
					String location= getLocation(resources[i]);
					if (location != null) {
						if (buf.length() > 0)
							buf.append('\n');
						buf.append(location);
					}
				}
				projectLocationsList.setText(buf.toString());
				Dialog.applyDialogFont(projectLocationsList);
				gridData.heightHint= Math.min(convertHeightInCharsToPixels(5), projectLocationsList.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
			}
			setControl(composite);
		}

		private static String getLocation(IResource resource) {
			IPath location= resource.getLocation();
			if (location != null)
				return BasicElementLabels.getPathLabel(location, true);
			
			URI uri= resource.getLocationURI();
			if (uri != null)
				return BasicElementLabels.getURLPart(uri.toString());
			
			URI rawLocationURI= resource.getRawLocationURI();
			if (rawLocationURI != null)
				return BasicElementLabels.getURLPart(rawLocationURI.toString());
			
			return BasicElementLabels.getResourceName(resource);
		}

		private boolean containsLinkedResource(IResource[] resources) {
			for (int i = 0; i < resources.length; i++) {
				IResource resource = resources[i];
				if (resource != null && resource.isLinked()) { // paranoia code, can not be null
					return true;
				}
			}
			return false;
		}

		protected boolean performFinish() {
			initializeRefactoring();
			storeSettings();
			return super.performFinish();
		}

		public IWizardPage getNextPage() {
			initializeRefactoring();
			storeSettings();
			return super.getNextPage();
		}

		private void initializeRefactoring() {
			fRefactoringProcessor.setDeleteContents(fDeleteContentsButton == null ? false : fDeleteContentsButton.getSelection());
		}

		private void storeSettings() {
		}
	}
}
